package io.github.term4.polyp.mechanics.knockback;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** mmc18's wire law: |vy| < 0.05 b/t leaves the server as sign*0.05 (0 -> +0.05) - a signed snap, not the old floor. */
class KnockbackWireRuleTest extends HeadlessServerTest {

    private static final double TPS = ServerFlag.SERVER_TICKS_PER_SECOND;

    /** Delivers {@code bt} through the mmc18 wire (values chosen on the 1/8000 grid so quantize is identity). */
    private Vec delivered(Vec bt) {
        LivingEntity victim = zombie(new Pos(0, 64, 750));
        new KnockbackSystem(polyp, io.github.term4.polyp.presets.mmc18.Knockback.melee())
                .deliver(victim, bt.mul(TPS));
        Vec out = victim.getVelocity().div(TPS);
        victim.remove();
        return out;
    }

    @Test
    void smallVerticalSnapsSigned() {
        assertEquals(0.05, delivered(new Vec(0.125, 0.02, 0)).y(), 1e-9, "small positive snaps up");
        assertEquals(-0.05, delivered(new Vec(0.125, -0.02, 0)).y(), 1e-9, "small negative snaps DOWN, not floored up");
        assertEquals(0.05, delivered(new Vec(0.125, 0, 0)).y(), 1e-9, "zero goes out as +0.05");
    }

    @Test
    void everythingElseUntouched() {
        Vec out = delivered(new Vec(0.125, 0.25, -0.5));
        assertEquals(0.25, out.y(), 1e-9);
        assertEquals(0.125, out.x(), 1e-9);
        assertEquals(-0.5, out.z(), 1e-9);
    }
}
