package io.github.term4.polyp.tracking.motion;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Golden pins for the 1.8 wire-grid quantization of outgoing knockback velocity. Pure math over
 * {@code ServerFlag.SERVER_TICKS_PER_SECOND}, pinned at the default 20 TPS. See docs/attributes-design.md.
 */
class LegacyVelocityCharacterizationTest {

    private static final double EPS = 1e-9;

    private static void assertVec(Vec expected, Vec actual) {
        assertEquals(expected.x(), actual.x(), EPS, "x");
        assertEquals(expected.y(), actual.y(), EPS, "y");
        assertEquals(expected.z(), actual.z(), EPS, "z");
    }

    /** Every pin below assumes 20 TPS (b/s = 20 * b/t). */
    @Test
    void tpsPrecondition() {
        assertEquals(20, (int) ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    @Test
    void zeroStaysZero() {
        assertVec(Vec.ZERO, LegacyVelocity.snap(Vec.ZERO));
    }

    @Test
    void snapsToVanillaShortBucket() {
        // 8 b/s = 0.4 b/t -> (int)(0.4*8000)=3200 shorts -> exact on-grid 3200*20/8000 = 8.0 b/s
        assertVec(new Vec(8.0, 0, 0), LegacyVelocity.snap(new Vec(8, 0, 0)));
        // negative truncates toward zero
        assertVec(new Vec(-8.0, 0, 0), LegacyVelocity.snap(new Vec(-8, 0, 0)));
    }

    @Test
    void allAxesSnapIndependently() {
        // x=8->0.4 (3200), y=4->0.2 (1600), z=-8->-0.4 (-3200), each re-emitted exact on-grid
        assertVec(new Vec(8.0, 4.0, -8.0), LegacyVelocity.snap(new Vec(8, 4, -8)));
    }

    @Test
    void clampsToVanillaWireLimit() {
        // 100 b/s = 5 b/t -> clamped to 3.9 b/t -> (int)(3.9*8000)=31200 -> exact on-grid 31200*20/8000 = 78.0 (decodes 3.9, not 3.900125)
        assertVec(new Vec(78.0, 0, 0), LegacyVelocity.snap(new Vec(100, 0, 0)));
        assertVec(new Vec(0, -78.0, 0), LegacyVelocity.snap(new Vec(0, -100, 0)));
    }

    @Test
    void clampsToConfiguredCap() {
        // a tighter cap overrides the 3.9 default: 100 b/s = 5 b/t -> clamped to 2.0 b/t -> (int)(2.0*8000)=16000 -> 40.0 b/s
        assertVec(new Vec(40.0, 0, 0), LegacyVelocity.snap(new Vec(100, 0, 0), 2.0));
        // below the cap the bucket is unchanged
        assertVec(new Vec(8.0, 0, 0), LegacyVelocity.snap(new Vec(8, 0, 0), 2.0));
    }

    @Test
    void subShortVelocityTruncatesToZero() {
        // 0.001 b/s = 0.00005 b/t -> 0.00005*8000=0.4 -> (int)=0 -> 0
        assertVec(Vec.ZERO, LegacyVelocity.snap(new Vec(0.001, 0.001, -0.001)));
    }
}
