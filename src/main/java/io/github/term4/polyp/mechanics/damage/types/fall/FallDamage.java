package io.github.term4.polyp.mechanics.damage.types.fall;

import io.github.term4.polyp.api.event.damage.types.FallDistanceResetEvent;
import io.github.term4.polyp.util.tick.TickContext;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.DamageProducers;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionCategory;
import io.github.term4.polyp.util.BlockContact;
import io.github.term4.polyp.util.tick.TickPhase;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerGameModeChangeEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Fall damage ({@code minecraft:fall}). Vanilla 1.8: distance accumulates while descending, water/climbing zero it,
 * lava halves it, and landing applies damage from {@link FallDamageConfig}.
 *
 * <p>Self-driven: players are tracked off their own move packets (with a per-tick poll for status-only onGround
 * packets); other living entities per tick. Creative/spectator/flying are exempt; (re)spawn resets; a gamemode change
 * resets unless {@link FallDistanceResetEvent} is cancelled. A plain teleport keeps the distance (vanilla 1.8/26 both;
 * the pearl zeroes it explicitly) but re-anchors the y-baseline, so the jump itself never accrues; callers reset via
 * {@link #resetFallDistance}.
 */
public final class FallDamage extends DamageType {

    public static final Key KEY = Key.key("minecraft:fall");
    public static final FallDamage INSTANCE = new FallDamage();

    /** Accumulated fall distance in blocks (absent = 0). */
    private static final Tag<Float> FALL_DISTANCE = Tag.Transient("polyp:fall-distance");
    /** Previous observation (move packet for players, tick for others): y + onGround for delta/landing detection. */
    private static final Tag<PrevMove> PREV = Tag.Transient("polyp:fall-prev");

    private record PrevMove(double y, boolean onGround) {}

    private @Nullable EventNode<@NotNull Event> node;
    private @Nullable DamageSystem system;
    private @Nullable TickSystem.Registration pollHook;

    private FallDamage() {
        super(KEY, "Fall", VanillaTypes.FALL, FallDamageConfig.builder().build());
    }

    /** Fall is the Feather Falling category (plus general Protection); it bypasses armor but not EPF. */
    @Override public Set<ProtectionCategory> protectionCategories() { return Set.of(ProtectionCategory.FALL); }

    @Override
    public void enable(DamageSystem system, Polyp polyp) {
        if (pollHook != null) { pollHook.cancel(); pollHook = null; } // re-enable (fresh registry over the singleton) must not stack the poll
        this.system = system;
        EventNode<@NotNull Event> n = EventNode.all("polyp:fall-damage");
        n.addListener(PlayerMoveEvent.class, this::onMove);
        n.addListener(EntityTickEvent.class, this::onTick);
        n.addListener(PlayerSpawnEvent.class, e -> resetFallDistance(e.getPlayer()));
        // vanilla 1.8 zeroes fallDistance on a gamemode change (26.1 dropped it) - cancel the event to keep it
        n.addListener(PlayerGameModeChangeEvent.class, e -> {
            if (e.isCancelled()) return; // listeners still run after a cancel
            Player p = e.getPlayer();
            EventDispatcher.callCancellable(
                    new FallDistanceResetEvent(p, e.getNewGameMode(), fallDistance(p)),
                    () -> resetFallDistance(p));
        });
        // Minestom reuses the Player across respawn (vanilla remakes it) - a death-fall would carry into the respawn
        n.addListener(EntityDeathEvent.class, e -> { if (e.getEntity() instanceof LivingEntity le) resetFallDistance(le); });
        // keeps the distance, kills the delta: vanilla re-anchors its move reference on teleport (1.8 checkMovement,
        // 26.1 awaitingPositionFromClient) - dropping the baseline is that re-anchor
        n.addListener(EntityTeleportEvent.class, e -> e.getEntity().removeTag(PREV));
        system.node().addChild(n);
        node = n;
        // fallback poll a tick behind onMove: catches status-only onGround landings (no PlayerMoveEvent)
        pollHook = TickSystem.register(TickPhase.DEFAULT, this::pollLandings);
    }

    @Override
    public void disable() {
        if (system != null && node != null) system.node().removeChild(node);
        node = null;
        if (pollHook != null) { pollHook.cancel(); pollHook = null; }
    }

    /** Clears an entity's accumulated fall distance ((re)spawn + explicit resets like the ender pearl; vanilla does NOT reset on a plain teleport). */
    public static void resetFallDistance(Entity entity) {
        entity.removeTag(FALL_DISTANCE);
        entity.removeTag(PREV);
    }

    /** The entity's currently accumulated fall distance in blocks. */
    public static float fallDistance(Entity entity) {
        Float v = entity.getTag(FALL_DISTANCE);
        return v != null ? v : 0f;
    }

    /** Players: client-authoritative deltas off their own move packets (ping-invariant landings). */
    private void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Pos newPos = e.getNewPosition();

        // full reset, not just distance: a stale baseline would turn the first post-exempt move into a huge dy
        if (DamageProducers.exempt(p)) {
            resetFallDistance(p);
            return;
        }
        // client onGround, not MotionTracker.simCollided: the server sim trips a tick early on fast falls.
        // newPos, not getPosition(): not committed until after this event - the slime check would look blocks up.
        step(p, newPos, newPos.y(), e.isOnGround());
    }

    /** Non-player living entities: server-side per-tick deltas. */
    private void onTick(EntityTickEvent e) {
        if (e.getEntity() instanceof Player) return; // players ride their own move packets
        if (!(e.getEntity() instanceof LivingEntity living) || living.isDead()) return;
        if (living.getInstance() == null) return;
        step(living, living.getPosition(), living.getPosition().y(), living.isOnGround());
    }

    private void step(LivingEntity living, Point pos, double y, boolean onGround) {
        PrevMove prev = living.getTag(PREV);
        living.setTag(PREV, new PrevMove(y, onGround));
        if (prev != null) accumulate(living, pos, y - prev.y(), onGround);
    }

    /** Fallback landing poll for players (status-only onGround packets fire no move event); one instance per tick. */
    private void pollLandings(TickContext ctx) {
        for (Player p : ctx.world().players()) {
            if (!ctx.owns(p) || !p.isOnGround()) continue;
            float dist = fallDistance(p);
            if (dist <= 0) continue;
            if (!DamageProducers.exempt(p)) land(p, p.getPosition(), dist);
            p.removeTag(FALL_DISTANCE);
        }
    }

    /** One observation step; {@code pos} is the landing position (the move destination for players). */
    private void accumulate(LivingEntity living, Point pos, double dy, boolean onGround) {
        float dist = fallDistance(living);

        if (dist > 0 || dy < 0) {
            boolean[] contact = new boolean[2]; // water, lava
            BlockContact.scan(living, block -> {
                if (block.compare(Block.WATER)) contact[0] = true;
                else if (block.compare(Block.LAVA)) contact[1] = true;
                return contact[0] && contact[1];
            });
            if (contact[0] || BlockContact.climbing(living)) {
                living.removeTag(FALL_DISTANCE);
                dist = 0f;
            } else if (contact[1] && dist > 0) {
                dist *= 0.5f;
                living.setTag(FALL_DISTANCE, dist);
            }
        }

        if (onGround) {
            if (dist > 0) land(living, pos, dist);
            living.removeTag(FALL_DISTANCE);
        } else if (dy < 0) {
            living.setTag(FALL_DISTANCE, dist + (float) -dy);
        }
    }


    private static boolean flyAbilityExempt(DamageContext ctx) {
        Boolean knob = ctx.typeConfig() instanceof FallDamageConfig f ? f.flyAbilityExempt(ctx) : null;
        return knob == null || knob;
    }

    /** Slime bounce negates fall damage unless the entity is sneaking - the damage half of the 1.8 {@code BlockSlime} bounce
     *  ({@link io.github.term4.polyp.tracking.motion.MotionTracker} does the velocity half); the block is under the landing feet. */
    private static boolean bounceNegatesFall(LivingEntity living, Point pos) {
        if (living instanceof Player p && p.isSneaking()) return false;
        if (living.getInstance() == null) return false;
        Block below = MechanicsWorld.viewed(living).getBlock(pos.sub(0, 0.5000001, 0), Block.Getter.Condition.TYPE);
        return below != null && below.compare(Block.SLIME_BLOCK);
    }


    /** Emits the landing's damage snapshot with the fall distance as the {@code detail} payload. */
    private void land(LivingEntity living, Point pos, float distance) {
        DamageSystem sys = this.system;
        if (sys == null) return;
        if (bounceNegatesFall(living, pos)) return;
        DamageSnapshot snap = DamageSnapshot.of(living, this).withDetail(FallDetail.of(distance));
        DamageContext ctx = sys.contextFor(snap);
        // the fly ABILITY blocks fall damage even when not flying (1.8 EntityHuman.e !canFly, 26.1 mayfly);
        // gated at landing, so revoking it mid-fall still lands the accrued damage (vanilla)
        if (flyAbilityExempt(ctx) && living instanceof Player p && p.isAllowFlying()) return;
        if (!ctx.typeConfig().enabled(ctx)) return;
        // skip below-threshold landings before any event fires
        if (ctx.baseAmount() <= 0) return;
        sys.apply(snap);
    }
}
