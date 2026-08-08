package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.tracking.ClientInfoTracker;
import io.github.term4.polyp.tracking.SprintTracker;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.util.BlockContact;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code PlayerMoveEvent} home for the compat movement restrictions. Speed restrictions are modern-clients-only and
 * always skip the knockback i-frame window so a hit still launches; {@code restrictMovement} rejects only a move that
 * NEWLY puts the hitbox into a solid block (a stuck player can still slide out), set back without an absolute-view snap
 * (camera kept). Installed once; inert unless a player's config enables a restriction.
 *
 * <p>Plain server API throughout, so foreign-provider players get these too (tag-carried {@link CompatState},
 * policy re-resolved per move); the packet-level compat elsewhere still needs {@code OptimizedPlayer}.
 */
public final class CompatMovement {

    private static final double MIN_MOVE_SQ = 1.0e-8;

    /** Foreign-provider players' state; Animatium natives stay empty (undetectable without the provider). */
    private static final Tag<CompatState> FALLBACK_COMPAT = Tag.Transient("polyp:compat-movement-fallback");

    private CompatMovement() {}

    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:compat-movement", EventFilter.PLAYER);
        // resolve the trackers lazily - install runs before they're created in init()
        node.addListener(PlayerMoveEvent.class, e -> onMove(polyp, e));
        polyp.install(node);
    }

    private static @Nullable CompatState compatOf(Polyp polyp, Player player) {
        if (player instanceof OptimizedPlayer op) return op.compat();
        // PlayerConfigApplier is provider-gated, so nothing pushes policy changes here - re-resolve each move
        CompatConfig cfg = polyp.profiles().resolve(player, MechanicsKeys.COMPAT);
        CompatState c = player.getTag(FALLBACK_COMPAT);
        if (c == null) {
            if (cfg == null) return null; // no policy anywhere: keep the move path allocation-free
            c = new CompatState();
            player.setTag(FALLBACK_COMPAT, c);
        }
        c.apply(cfg);
        return c;
    }

    private static void onMove(Polyp polyp, PlayerMoveEvent event) {
        ClientInfoTracker clientInfo = polyp.clientInfo();
        SprintTracker sprintTracker = polyp.sprintTracker();
        Player player = event.getPlayer();
        CompatState c = compatOf(polyp, player);
        if (c == null) return;
        // DISABLE_CRAWL_POSE clients block the squeeze-to-fit crawl natively; the revert would then only false-positive
        // on their fast 1.8 fluid descent (box briefly overshoots the floor) and bounce them off water
        boolean collision = c.restrictMovement() && !c.handlesNatively(AnimatiumFeature.DISABLE_CRAWL_POSE);
        boolean speed = c.anySpeedRestriction();
        if (!collision && !speed) return;
        // spectators noclip; a passenger's collision is the vehicle's - leave both alone
        if (player.getGameMode() == GameMode.SPECTATOR || player.getVehicle() != null) return;
        Pos from = player.getPosition();
        Pos to = event.getNewPosition();
        if (to.samePoint(from)) return; // look-only change: the hitbox didn't move
        if (player.getInstance() == null) return;
        MechanicsWorld world = MechanicsWorld.viewed(player); // the blocks the CLIENT renders, not the base world
        // a view-only observer (block view != binding) can legitimately disagree mid-resync - never bounce them
        if (collision && world != MechanicsWorld.of(player)) collision = false;

        if (collision && entersNewCollision(world, player.getBoundingBox(), from, to)) {
            boolean rotated = to.yaw() != from.yaw() || to.pitch() != from.pitch();
            // a non-rotating revert-to-from has no view delta to trip the early-out, so nudge the yaw one ULP (invisible)
            float yaw = rotated ? to.yaw() : Math.nextUp(from.yaw());
            Pos pos = from.withView(yaw, to.pitch());
            player.refreshPosition(pos, false, false);
            player.sendPacket(new PlayerPositionAndLookPacket(-1, pos, Vec.ZERO, 0f, 0f, RelativeFlags.VIEW));
            return;
        }

        // skip the knockback i-frame window: a hit's velocity would be clamped away
        if (!speed || clientInfo.isLegacy(player) || DamageSystem.isInvulnerableToDamage(player)) return;
        // OLD_FLUID_PHYSICS clients already move at 1.8 speed natively; the dampen would fight the 1.8 current
        if (c.restrictSwimSpeed() && player.isSprinting() && ClientEye.feetInWater(player, world)
                && !c.handlesNatively(AnimatiumFeature.OLD_FLUID_PHYSICS)) {
            dampenSwim(player, c, from, to);
            return;
        }
        restrictSprint(player, c, sprintTracker);
    }

    /**
     * Sneak/use 1.8 parity via the sprint state itself: while the CLIENT believes it's sprinting, {@code setSprinting(false)}
     * drops the server's {@code +0.3} sprint speed modifier + the sprint flag when sneaking/using, and restores it otherwise.
     * Reads tracked sprint, not {@code isSprinting()} (which we force off); {@code setSprinting} fires no sprint events, so
     * the tracker stays clean.
     */
    private static void restrictSprint(Player player, CompatState c, @Nullable SprintTracker sprintTracker) {
        if (!SprintTracker.isClientSprinting(sprintTracker, player)) {
            c.setSprintStripped(false); // the sprint we stripped is over; a stale flag would restore the NEXT one
            return;
        }
        // skip what Animatium fixes natively (forces sprint off client-side, no rubber-band)
        boolean strip = (c.restrictSprintSneak() && player.isSneaking() && !c.handlesNatively(AnimatiumFeature.FIX_SPRINT_SNEAKING))
                || (c.restrictSprintUse() && player.isUsingItem() && !c.handlesNatively(AnimatiumFeature.FIX_SPRINT_ITEM_USE));
        if (strip) {
            if (player.isSprinting()) { player.setSprinting(false); c.setSprintStripped(true); }
        } else if (c.sprintStripped()) {
            // restore ONLY the sprint WE stripped - a combat sprint-reset didn't set the flag, keeping the 1.8 w-tap requirement
            if (!player.isSprinting()) player.setSprinting(true);
            c.setSprintStripped(false);
        }
    }

    /** Hypixel-style swim cap: spam a reduced velocity each move instead of a position setback; vertical damps harder since holding space re-adds swim-up. */
    private static void dampenSwim(Player player, CompatState c, Pos from, Pos to) {
        double dx = to.x() - from.x(), dy = to.y() - from.y(), dz = to.z() - from.z();
        if (dx * dx + dy * dy + dz * dz <= MIN_MOVE_SQ) return;
        Vec velocity = new Vec(dx / c.swimFactor(), dy / c.swimVerticalFactor(), dz / c.swimFactor());
        player.sendPacket(new EntityVelocityPacket(player.getEntityId(), velocity));
    }


    /**
     * Whether the move newly puts {@code box} into a solid block it wasn't already overlapping at {@code from} - a block it
     * already overlaps is ignored, so a player stuck in a block can still slide out.
     */
    private static boolean entersNewCollision(MechanicsWorld world, BoundingBox box, Pos from, Pos to) {
        var blocks = box.getBlocks(to);
        while (blocks.hasNext()) {
            var bp = blocks.next();
            Block block;
            try {
                block = world.getBlock(bp.blockX(), bp.blockY(), bp.blockZ(), Block.Getter.Condition.TYPE);
            } catch (Exception ignored) {
                continue; // unloaded chunk -> no collision
            }
            // passable blocks are meant to be occupied (vanilla ladders carry a wall-side slab, which was setting climb
            // moves back); scaffolding's dynamic shape Minestom doesn't model
            if (block == null || BlockContact.isPassable(block) || block.id() == Block.SCAFFOLDING.id()) continue;
            var shape = block.registry().collisionShape();
            if (!shape.intersectBox(to.sub(bp.blockX(), bp.blockY(), bp.blockZ()), box)) continue;
            if (shape.intersectBox(from.sub(bp.blockX(), bp.blockY(), bp.blockZ()), box)) continue; // already inside -> allow sliding out
            return true;
        }
        return false;
    }
}
