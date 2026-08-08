package io.github.term4.polyp.mechanics.damage.types.fall;

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
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.entity.EntityTickEvent;
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
 * packets); other living entities per tick. Creative/spectator/flying are exempt; (re)spawn resets. A plain teleport does
 * NOT reset (vanilla 1.8/26 leave fall distance on teleport - the pearl zeroes it explicitly) but does re-anchor the
 * y-baseline, so the jump itself never accrues; callers reset via {@link #resetFallDistance}.
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
        // reset on death too: Minestom reuses the Player across respawn (vanilla makes a fresh entity), so a fall in progress
        // at death would otherwise carry its distance to the respawn and land as phantom fall damage.
        n.addListener(EntityDeathEvent.class, e -> { if (e.getEntity() instanceof LivingEntity le) resetFallDistance(le); });
        // teleport keeps the DISTANCE but never yields a fall delta: vanilla also accrues players from per-packet
        // position deltas (1.8 PlayerConnection:459 player.a(locY - d10), 26.1 doCheckFallDamage(clientDeltaMovement))
        // and excludes teleports by re-anchoring its reference position while moves await the client's confirm
        // (1.8 checkMovement, 26.1 awaitingPositionFromClient/lastGood). Dropping the baseline IS that re-anchor.
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

        // clearing prev too is what stops quick-respawn phantom damage: a dead player's death-fall position would
        // otherwise be the baseline for the first live move after respawn, giving a huge dy
        if (DamageProducers.exempt(p)) {
            p.removeTag(FALL_DISTANCE);
            p.removeTag(PREV);
            return;
        }
        PrevMove prev = p.getTag(PREV);
        p.setTag(PREV, new PrevMove(newPos.y(), e.isOnGround()));
        if (prev == null) return;
        // land on the CLIENT's onGround flag (vanilla Entity.checkFallDamage), NOT MotionTracker.simCollided - the server
        // sim trips a tick early during fast falls, firing fall damage above the real ground.
        // newPos, not getPosition(): the position isn't committed until after this event, so at high fall speed the
        // landing-block (slime) check would look many blocks up.
        accumulate(p, newPos, newPos.y() - prev.y(), e.isOnGround());
    }

    /** Non-player living entities: server-side per-tick deltas. */
    private void onTick(EntityTickEvent e) {
        if (e.getEntity() instanceof Player) return; // players ride their own move packets
        if (!(e.getEntity() instanceof LivingEntity living) || living.isDead()) return;
        if (living.getInstance() == null) return;
        double y = living.getPosition().y();
        boolean onGround = living.isOnGround();
        PrevMove prev = living.getTag(PREV);
        living.setTag(PREV, new PrevMove(y, onGround));
        if (prev == null) return;
        accumulate(living, living.getPosition(), y - prev.y(), onGround);
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
        if (!ctx.typeConfig().enabled(ctx)) return;
        // skip below-threshold landings before any event fires
        if (ctx.baseAmount() <= 0) return;
        sys.apply(snap);
    }
}
