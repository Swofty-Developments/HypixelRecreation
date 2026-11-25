package io.github.term4.polyp.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Golden pins for the unit-vector / yaw / blend math the knockback direction stages reuse. */
class DirectionsCharacterizationTest {

    private static final double EPS = 1e-9;

    private static void assertVec(Vec expected, Vec actual) {
        assertEquals(expected.x(), actual.x(), EPS, "x");
        assertEquals(expected.y(), actual.y(), EPS, "y");
        assertEquals(expected.z(), actual.z(), EPS, "z");
    }

    @Test
    void fromYawCardinals() {
        assertVec(new Vec(0, 0, 1), Directions.fromYaw(0));
        assertVec(new Vec(-1, 0, 0), Directions.fromYaw(90));
        assertVec(new Vec(1, 0, 0), Directions.fromYaw(-90));
        assertVec(new Vec(0, 0, -1), Directions.fromYaw(180));
    }

    @Test
    void yawOfVector() {
        assertEquals(0f, Directions.yaw(new Vec(0, 0, 1)), EPS);
        assertEquals(-90f, Directions.yaw(new Vec(-1, 0, 0)), EPS);
        assertEquals(90f, Directions.yaw(new Vec(1, 0, 0)), EPS);
        assertEquals(180f, Math.abs(Directions.yaw(new Vec(0, 0, -1))), EPS);
    }

    @Test
    void pitchOfVector() {
        assertEquals(90f, Directions.pitch(new Vec(0, 1, 0)), EPS);
        assertEquals(-90f, Directions.pitch(new Vec(0, -1, 0)), EPS);
        assertEquals(0f, Directions.pitch(new Vec(1, 0, 0)), EPS);
        assertEquals(45f, Directions.pitch(new Vec(1, 1, 0)), EPS);
    }

    @Test
    void snapDominantAxis() {
        assertVec(new Vec(1, 0, 0), Directions.snapDominantAxis(new Vec(0.9, 0, 0.1)));
        assertVec(new Vec(0, 0, -1), Directions.snapDominantAxis(new Vec(0.1, 0, -0.9)));
        assertVec(new Vec(-1, 0, 0), Directions.snapDominantAxis(new Vec(-0.5, 0, 0.2)));
        // ties favour x
        assertVec(new Vec(1, 0, 0), Directions.snapDominantAxis(new Vec(0.5, 0, 0.5)));
        assertVec(new Vec(0, 0, 0), Directions.snapDominantAxis(new Vec(0, 0, 0)));
    }

    @Test
    void horizontalBetweenIsUnitDirection() {
        assertVec(new Vec(0.6, 0, 0.8), Directions.horizontalBetween(new Pos(0, 0, 0), new Pos(3, 5, 4)));
        assertVec(new Vec(-1, 0, 0), Directions.horizontalBetween(new Pos(2, 9, 0), new Pos(0, -3, 0)));
    }

    @Test
    void verticalBetweenSignsTheHeightDelta() {
        assertVec(Directions.UP, Directions.verticalBetween(new Pos(0, 5, 0), new Pos(9, 9, 9)));
        assertVec(new Vec(0, -1, 0), Directions.verticalBetween(new Pos(0, 9, 0), new Pos(0, 5, 0)));
        assertVec(Directions.UP, Directions.verticalBetween(new Pos(0, 5, 0), new Pos(7, 5, 7)));   // level -> UP
    }

    @Test
    void horizontalOfStripsY() {
        assertVec(new Vec(0.6, 0, 0.8), Directions.horizontalOf(new Vec(0.36, 100, 0.48)));
    }

    @Test
    void verticalOfSignsY() {
        assertVec(Directions.UP, Directions.verticalOf(new Vec(5, 0.5, 5)));
        assertVec(new Vec(0, -1, 0), Directions.verticalOf(new Vec(5, -0.5, 5)));
        assertVec(Directions.UP, Directions.verticalOf(new Vec(5, 0, 5))); // horizontal -> UP
    }

    @Test
    void blendNormalizesWeightedSum() {
        double inv = 1 / Math.sqrt(2);
        assertVec(new Vec(inv, 0, inv),
                Directions.blend(new Vec(1, 0, 0), new Vec(0, 0, 1), 1, 1, () -> Directions.UP));
        assertVec(new Vec(1, 0, 0),
                Directions.blend(new Vec(1, 0, 0), new Vec(0, 0, 1), 1, 0, () -> Directions.UP));
    }

    @Test
    void blendFallsBackWhenDegenerate() {
        assertVec(Directions.UP,
                Directions.blend(new Vec(1, 0, 0), new Vec(0, 0, 1), 0, 0, () -> Directions.UP));
        // opposing vectors cancel -> fallback
        assertVec(Directions.UP,
                Directions.blend(new Vec(1, 0, 0), new Vec(-1, 0, 0), 1, 1, () -> Directions.UP));
    }

    @Test
    void randomHorizontalIsUnitAndFlat() {
        for (int i = 0; i < 100; i++) {
            Vec v = Directions.randomHorizontal();
            assertEquals(0, v.y(), EPS);
            assertEquals(1.0, v.length(), 1e-9);
        }
    }

    @Test
    void degenerateHorizontalBetweenIsRandomUnit() {
        // same point -> a random horizontal unit vector, so only unit + flat is assertable
        Vec v = Directions.horizontalBetween(new Pos(1, 2, 3), new Pos(1, 9, 3));
        assertEquals(0, v.y(), EPS);
        assertTrue(Math.abs(v.length() - 1.0) < 1e-9);
    }
}
