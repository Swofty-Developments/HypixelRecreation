package io.github.term4.polyp.tracking.motion;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Move deltas are anchored to the previous move packet, so a teleport must re-anchor or the first post-teleport
 * packet reads the whole jump as one tick of motion - while leaving the pre-teleport delta in place, since the client
 * keeps its real motion through a teleport. Pearls, /tp and shard hops are all this case.
 */
class TeleportAnchorTest extends HeadlessServerTest {

    private static void move(Player p, Pos to) {
        EventDispatcher.call(new PlayerMoveEvent(p, to, false));
    }

    @Test
    void teleportNeverReadsAsOneTickOfMotion() {
        Instance inst = flatInstance(null);
        Player p = FakePlayer.connect(inst, new Pos(8.5, 80, 8.5), "TeleAnchor").player;

        move(p, new Pos(8.5, 80, 8.5));                    // baseline
        move(p, new Pos(8.5, 80, 9.0));
        assertEquals(new Vec(0, 0, 0.5), MotionTracker.positionDelta(p));

        p.teleport(new Pos(8.5, 40, 8.5)).join();          // 40-block drop: must not become a delta
        move(p, new Pos(8.5, 40, 8.6));                    // first post-teleport packet re-baselines
        assertEquals(new Vec(0, 0, 0.5), MotionTracker.positionDelta(p), "pre-teleport motion carries, no spike");

        move(p, new Pos(8.5, 40, 8.7));
        Vec d = MotionTracker.positionDelta(p);
        assertEquals(0.1, d.z(), 1e-9);
        assertEquals(0.0, d.y(), 1e-9, "the teleported drop never entered the deltas");
    }
}
