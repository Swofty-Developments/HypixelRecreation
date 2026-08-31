package io.github.term4.polyp.tracking.motion;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A void reset teleports a falling victim flush onto the pad: the swept probe misses flush contact, a still
 * client sends no moves, and under {@link VelocityConfig#motYOnMovePacket} the held sim froze the carried
 * fall into every later fold - the first hit after a score reset delivered weak vertical knockback.
 *
 * <p>Per-era law, both verified against source: 1.8 carries mot frozen through the teleport-confirm gap
 * ({@code checkMovement} gates {@code player.l()}) and collide-zeroes it on the confirming packet - here
 * the held sim lands by the client's onGround flag. 26 zeroes an absolute teleport's delta outright
 * ({@code teleportSetPosition}) - the per-tick law clears at the teleport.
 */
class TeleportCarriedFallTest extends HeadlessServerTest {

    private static void tick(Instance inst) { EventDispatcher.call(new InstanceTickEvent(inst, 0, 0)); }

    private static void move(Player p, double y, boolean onGround) {
        Pos pos = new Pos(8.5, y, 8.5);
        p.refreshPosition(pos, true, false);
        p.refreshOnGround(onGround);
        EventDispatcher.call(new PlayerMoveEvent(p, pos, onGround));
    }

    @Test
    void aTeleportedFallLandsByTheFlagWithoutMovePackets() {
        var inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.VELOCITY, VelocityRule.simulated(
                        VelocityConfig.builder().motYOnMovePacket(true).build()))
                .build());
        Player p = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), "TeleFall").player;

        move(p, 64.0, true);
        tick(inst);
        move(p, 64.42, false);                    // rising: seeds the arc
        tick(inst);
        double y = 95.0;                          // ride a long fall well clear of the floor
        for (int t = 0; t < 30; t++) {
            move(p, y, false);
            tick(inst);
            y -= 0.3;
        }
        Double fall = MotionTracker.serverMotY(p, 0, true);
        assertNotNull(fall);
        assertTrue(fall < -1.0, "the fall never accumulated: " + fall);

        p.teleport(new Pos(8.5, 64, 8.5)).join(); // the score/void reset onto the pad, flush on the floor
        tick(inst);                               // confirm gap: no move packets arrive
        tick(inst);
        assertEquals(fall, MotionTracker.serverMotY(p, 0, true), 0.0,
                "1.8 freezes through the confirm gap - carried, not integrated");

        p.refreshOnGround(true);                  // the client's flying packet lands the flag, still no move
        tick(inst);
        assertEquals(-0.08 * 0.98, MotionTracker.serverMotY(p, 0, true), 1e-12,
                "the confirm step lands the fall at the grounded fixed point");
        p.remove();
    }

    @Test
    void theModernLawZeroesTheDeltaAtTheTeleport() {
        var inst = flatInstance(null);            // default rule: per-tick advancement, the 26 law
        Player p = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), "TeleZero").player;

        move(p, 64.0, true);
        tick(inst);
        move(p, 64.42, false);
        tick(inst);
        double y = 95.0;
        for (int t = 0; t < 30; t++) {
            move(p, y, false);
            tick(inst);
            y -= 0.3;
        }
        Double fall = MotionTracker.serverMotY(p, 0, true);
        assertNotNull(fall);
        assertTrue(fall < -1.0, "the fall never accumulated: " + fall);

        p.teleport(new Pos(8.5, 64, 8.5)).join();
        assertEquals(0.0, MotionTracker.serverMotY(p, 0, true), 0.0, "26 teleports zero the carried delta");
        p.remove();
    }
}
