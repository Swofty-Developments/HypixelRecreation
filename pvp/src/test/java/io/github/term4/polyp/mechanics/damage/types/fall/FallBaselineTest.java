package io.github.term4.polyp.mechanics.damage.types.fall;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.entity.EntityTickEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Fall distance accrues from y-deltas against a PREV baseline, so a teleport must re-anchor the baseline (or the jump
 * itself reads as a fall) while KEEPING the accumulated distance (vanilla does not reset fall distance on teleport).
 */
class FallBaselineTest extends HeadlessServerTest {

    private static void tick(LivingEntity e) {
        MinecraftServer.getGlobalEventHandler().call(new EntityTickEvent(e));
    }

    @Test
    void downwardTeleportDoesNotAccrueFallDistance() {
        LivingEntity z = zombie(new Pos(0, 100, 210));
        tick(z);                                   // baseline @100
        z.refreshPosition(new Pos(0, 99, 210));
        tick(z);                                   // real fall: +1
        assertEquals(1f, FallDamage.fallDistance(z), 1e-6);

        z.teleport(new Pos(0, 50, 210)).join();    // the 49-block jump must not count
        tick(z);                                   // re-anchors @50
        z.refreshPosition(new Pos(0, 49.5, 210));
        tick(z);                                   // real fall resumes: +0.5
        assertEquals(1.5f, FallDamage.fallDistance(z), 1e-6);
    }
}
