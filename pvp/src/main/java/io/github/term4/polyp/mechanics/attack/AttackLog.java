package io.github.term4.polyp.mechanics.attack;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.attack.PreAttackEvent;
import io.github.term4.polyp.platform.compatibility.ClientEye;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.tracking.ClientInfoTracker;
import io.github.term4.polyp.tracking.ClientVersion;
import io.github.term4.polyp.util.geometry.Aabb;
import io.github.term4.polyp.util.geometry.Sightline;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerCancelDiggingEvent;
import net.minestom.server.event.player.PlayerFinishDiggingEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Attack-geometry hub: the reach guard plus queries a consumer can GET about a melee attack.
 *
 * <p><b>Reach guard.</b> Cancels a {@link PreAttackEvent} whose {@link #optimalReach} exceeds {@code maxReach}, mirroring
 * vanilla's server check ({@code Player.isWithinEntityInteractionRange} = {@code box.distanceToSqr(eyePosition)} vs
 * {@code entityInteractionRange 3.0 + buffer 3.0 = 6.0}): nearest-point, rotation-INDEPENDENT, never a ray along the
 * attacker's aim - the server's view of yaw/pitch lags the client at high flick speeds, so an aim ray would falsely
 * reject legitimate hits. Two leniencies over vanilla: the target box grows by the attacker's real client hitbox margin
 * (1.8's 0.1 / a modern client's {@code attack_range}) rather than a flat buffer, and the eye takes the min over
 * client-perceived candidates. Keep {@code maxReach} generous (only kill clearly-impossible hits).
 *
 * <p><b>Obstruction.</b> {@link #obstructed} is a query only - nothing enforces it. Vanilla melee does NOT line-of-sight
 * server-side (the client's entity-pick clips against blocks); this mirrors that clip. The precise lag-compensated reach
 * analysis lives on the {@code anticheat} branch.
 */
public final class AttackLog {

    private static final Tag<Boolean> DIGGING = Tag.Transient("polyp:digging");
    private static final AtomicBoolean DIG_TRACKING = new AtomicBoolean();
    private static final AtomicBoolean REACH_GUARD = new AtomicBoolean();

    private AttackLog() {}

    /** Installs the reach guard: cancels a hit whose lower-bound reach exceeds {@code maxReach}. Once only. */
    public static void install(Polyp polyp, double maxReach) {
        if (!REACH_GUARD.compareAndSet(false, true)) throw new IllegalStateException("reach guard is already installed");
        ClientInfoTracker clientInfo = polyp.clientInfo();
        EventNode<@NotNull Event> node = EventNode.all("polyp:reach-guard");
        node.addListener(PreAttackEvent.class, e -> {
            if (e.target() != null && e.attacker() instanceof Player attacker
                    && optimalReach(attacker, e.target(), clientInfo) > maxReach) {
                e.setCancelled(true);
            }
        });
        polyp.install(node);
    }

    /**
     * The attacker's optimal (nearest-point) reach to {@code target}: a rotation-independent lower bound on true reach,
     * over the client-perceived eye-height candidates and the target's current + previous position. Grows the target box
     * by the attacker's hit pad (1.8's native 0.1, or a modern client's stamped {@code attack_range} margin).
     */
    public static double optimalReach(Player attacker, Entity target, ClientInfoTracker clientInfo) {
        double pad = hitPad(attacker, clientInfo);
        BoundingBox bb = target.getBoundingBox();
        Aabb now = Aabb.of(bb, target.getPosition(), pad);
        Aabb prev = Aabb.of(bb, target.getPreviousPosition(), pad);
        Pos base = attacker.getPosition();
        double best = Double.POSITIVE_INFINITY;
        for (double eye : ClientEye.candidates(attacker, clientInfo.getProtocol(attacker))) {
            double ey = base.y() + eye;
            best = Math.min(best, Math.min(
                    now.nearestDistance(base.x(), ey, base.z()), prev.nearestDistance(base.x(), ey, base.z())));
        }
        return best;
    }

    /**
     * The distance at which the attacker's look ray enters {@code target}'s padded box within {@code maxReach}, or
     * {@code -1} if the ray misses. The directional counterpart of {@link #optimalReach} - hit DETECTION uses it (is the
     * crosshair on the target), unlike the reach guard which stays aim-independent. Uses the attacker's live eye + look.
     */
    public static double rayReach(Player attacker, Entity target, ClientInfoTracker clientInfo, double maxReach) {
        return rayReach(attacker, target, clientInfo, maxReach, attacker.getPosition());
    }

    /**
     * {@link #rayReach} from an explicit eye source {@code from} (its position + look) rather than the attacker's live
     * one - e.g. a {@code PlayerMoveEvent}'s reported {@code getNewPosition()}, so the ray follows the incoming look while
     * the attacker's own state (what knockback reads) is left untouched.
     */
    public static double rayReach(Player attacker, Entity target, ClientInfoTracker clientInfo, double maxReach, Pos from) {
        Aabb box = Aabb.of(target.getBoundingBox(), target.getPosition(), hitPad(attacker, clientInfo));
        Vec look = from.direction();
        return box.rayDistance(from.x(), from.y() + attacker.getEyeHeight(), from.z(), look.x(), look.y(), look.z(), maxReach);
    }

    /** Whether solid terrain blocks the attacker's eye -&gt; target-centre line. A query; the reach guard does not enforce it. */
    public static boolean obstructed(Player attacker, Entity target) {
        Pos eye = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        BoundingBox bb = target.getBoundingBox();
        Point centre = target.getPosition().add(0, (bb.minY() + bb.maxY()) / 2.0, 0);
        return Sightline.blocked(MechanicsWorld.of(attacker), eye, centre);
    }

    /** Installs block-break state tracking so {@link #digging} works; idempotent, separate from the reach guard. */
    public static void installDigTracking(Polyp polyp) {
        if (!DIG_TRACKING.compareAndSet(false, true)) return;
        EventNode<Event> node = EventNode.all("polyp:dig-state");
        node.addListener(PlayerStartDiggingEvent.class, e -> e.getPlayer().setTag(DIGGING, true));
        node.addListener(PlayerCancelDiggingEvent.class, e -> e.getPlayer().setTag(DIGGING, false));
        node.addListener(PlayerFinishDiggingEvent.class, e -> e.getPlayer().setTag(DIGGING, false));
        polyp.install(node);
    }

    /**
     * Whether {@code player} is mid block-break - an action flag, not geometry; the consumer decides what it means.
     * Requires {@link #installDigTracking}; {@code false} if untracked.
     */
    public static boolean digging(Player player) {
        return Boolean.TRUE.equals(player.getTag(DIGGING));
    }

    /** 1.8 grows the attack box 0.1 natively; a modern client grows by its compat-stamped {@code attack_range} margin (same CompatState as the attack-box feature, so the two can't diverge). */
    private static double hitPad(Player attacker, ClientInfoTracker clientInfo) {
        double nativePad = ClientVersion.isLegacy(clientInfo.getProtocol(attacker)) ? 0.1 : 0.0;
        Float margin = attacker instanceof OptimizedPlayer op ? op.compat().attackHitboxMargin() : null;
        return Math.max(nativePad, margin != null ? margin : 0.0);
    }
}
