package io.github.term4.polyp.tracking.motion;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.Test;

/**
 * Field repro (far_explosion capture): after a fireball-jump arc, a client that keeps streaming grounded move
 * packets every tick (1.8 float jitter at large coords) must not leave a stale falling motY in the tracker.
 */
class GroundedStaleMotYTest extends HeadlessServerTest {

    private static void tick(Instance inst) { EventDispatcher.call(new InstanceTickEvent(inst, 0, 0)); }

    private static void move(Player p, double y, boolean onGround) {
        Pos pos = new Pos(8.5, y, 8.5);
        p.refreshPosition(pos, true, false);
        p.refreshOnGround(onGround);
        EventDispatcher.call(new PlayerMoveEvent(p, pos, onGround));
    }

    private double runArc(Instance inst, Player p, boolean streamAfterLanding) {
        move(p, 64.0, true);
        tick(inst);
        // the fireball jump delivery
        MotionTracker.foldDelivered(p, new Vec(-0.3728, 1.6655, -0.3728));
        double y = 64.0, v = 1.6655;
        for (int t = 0; t < 60 && y > 63.9999; t++) {
            y += v;
            if (y <= 64.0) y = 64.0;
            move(p, y, y == 64.0);
            tick(inst);
            v = (v - 0.08) * 0.98;
        }
        // post-landing: silence (close) vs every-tick grounded stream (far)
        for (int t = 0; t < 15; t++) {
            if (streamAfterLanding) move(p, 64.0, true);
            tick(inst);
        }
        return MotionTracker.serverMotY(p, 0, true);
    }

    @Test
    void groundedStreamDoesNotRetainFallVelocity() {
        var inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.VELOCITY, VelocityRule.simulated(
                        VelocityConfig.builder().motYOnMovePacket(true).build()))
                .build());
        Player far = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), "FarStream").player;
        double farY = runArc(inst, far, true);
        far.remove();
        Player close = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), "CloseSilent").player;
        double closeY = runArc(inst, close, false);
        close.remove();
        // grounded motY is the vanilla 0/-0.0784 cycle, never a retained fall speed
        org.junit.jupiter.api.Assertions.assertTrue(farY >= -0.0785, "streaming client kept a stale fall motY: " + farY);
        org.junit.jupiter.api.Assertions.assertTrue(closeY >= -0.0785, "silent client kept a stale fall motY: " + closeY);
    }
}
