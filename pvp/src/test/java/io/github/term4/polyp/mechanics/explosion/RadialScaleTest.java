package io.github.term4.polyp.mechanics.explosion;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The per-direction base scaling + its fallback chain (specific → horizontal → base). */
class RadialScaleTest {

    private static void assertVec(Vec expected, Vec actual) {
        assertEquals(expected.x(), actual.x(), 1e-9, "x");
        assertEquals(expected.y(), actual.y(), 1e-9, "y");
        assertEquals(expected.z(), actual.z(), 1e-9, "z");
    }

    @Test
    void isotropicScalesEveryAxisByMagnitude() {
        Vec u = new Vec(0.6, 0.8, 0.0);
        assertVec(new Vec(1.2, 1.6, 0.0), RadialScale.ISOTROPIC.apply(2.0, u));
    }

    @Test
    void hypixelShapeUpIsBaseDownIsScaledHorizontalIsScaled() {
        RadialScale s = RadialScale.builder().down(0.4).horizontal(0.8).build();
        assertVec(new Vec(0, 1.0, 0), s.apply(1.0, new Vec(0, 1, 0)), "up = base 1.0");
        assertVec(new Vec(0, -0.4, 0), s.apply(1.0, new Vec(0, -1, 0)), "down 0.4");
        assertVec(new Vec(0.8, 0, 0), s.apply(1.0, new Vec(1, 0, 0)), "+x -> horizontal");
        assertVec(new Vec(0, 0, -0.8), s.apply(1.0, new Vec(0, 0, -1)), "-z -> horizontal");
    }

    @Test
    void axisOverridesHorizontalOverridesBase() {
        // +x explicit, -x falls to horizontal, +z/-z fall to horizontal, y falls to base
        RadialScale s = RadialScale.builder().base(2.0).horizontal(0.5).plusX(0.3).build();
        assertEquals(0.3, s.apply(1.0, new Vec(1, 0, 0)).x(), 1e-9, "+x explicit");
        assertEquals(-0.5, s.apply(1.0, new Vec(-1, 0, 0)).x(), 1e-9, "-x -> horizontal");
        assertEquals(0.5, s.apply(1.0, new Vec(0, 0, 1)).z(), 1e-9, "+z -> horizontal");
        assertEquals(2.0, s.apply(1.0, new Vec(0, 1, 0)).y(), 1e-9, "up -> base");
    }

    @Test
    void unsetHorizontalFallsThroughToBase() {
        RadialScale s = RadialScale.builder().base(0.7).build();
        assertVec(new Vec(0.7, 0.7, 0.7), s.apply(1.0, new Vec(1, 1, 1)));
    }

    private static void assertVec(Vec expected, Vec actual, String msg) {
        assertEquals(expected.x(), actual.x(), 1e-9, msg + " x");
        assertEquals(expected.y(), actual.y(), 1e-9, msg + " y");
        assertEquals(expected.z(), actual.z(), 1e-9, msg + " z");
    }
}
