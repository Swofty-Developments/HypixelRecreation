package io.github.term4.polyp.util.geometry;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The shared box math: nearest-point distance + the ray-AABB slab test. */
class AabbTest {

    private static final Aabb UNIT = new Aabb(0, 0, 0, 1, 1, 1);

    @Test
    void nearestDistanceInsideIsZeroOutsideIsEuclidean() {
        assertEquals(0.0, UNIT.nearestDistance(0.5, 0.5, 0.5), "inside -> 0");
        assertEquals(2.0, UNIT.nearestDistance(3.0, 0.5, 0.5), 1e-9, "3 past the +x face at y/z inside -> 2");
        assertEquals(Math.sqrt(3), UNIT.nearestDistance(2, 2, 2), 1e-9, "corner-diagonal");
    }

    @Test
    void ofPlacesTheBoxAtPosAndGrowsByThePad() {
        BoundingBox bb = new BoundingBox(0.6, 1.8, 0.6); // player box: centred on x/z, feet at y
        Aabb grown = Aabb.of(bb, new Pos(5, 64, 5), 0.1);
        assertEquals(5 + bb.minX() - 0.1, grown.minX(), 1e-9);
        assertEquals(64 + bb.minY() - 0.1, grown.minY(), 1e-9);
        assertEquals(64 + bb.maxY() + 0.1, grown.maxY(), 1e-9);
    }

    @Test
    void rayHitsBoxHeadOnReportsEntryDistance() {
        double d = UNIT.rayDistance(-2, 0.5, 0.5, 1, 0, 0, 10);
        assertEquals(2.0, d, 1e-9, "entry distance along the ray");
    }

    @Test
    void rayNormalizesDirectionSoDistanceIsWorldUnits() {
        double d = UNIT.rayDistance(-2, 0.5, 0.5, 5, 0, 0, 10); // non-unit dir, same aim
        assertEquals(2.0, d, 1e-9, "non-unit direction still yields world-unit distance");
    }

    @Test
    void rayMissesWhenAimedAway() {
        assertTrue(UNIT.rayDistance(-2, 0.5, 0.5, -1, 0, 0, 10) < 0, "aimed away -> miss");
        assertTrue(UNIT.rayDistance(-2, 5, 0.5, 1, 0, 0, 10) < 0, "parallel but above the box -> miss");
    }

    @Test
    void rayBeyondMaxDistanceMisses() {
        assertTrue(UNIT.rayDistance(-2, 0.5, 0.5, 1, 0, 0, 1.5) < 0, "entry at 2 but reach 1.5 -> miss");
    }

    @Test
    void originInsideBoxIsZeroDistanceHit() {
        assertEquals(0.0, UNIT.rayDistance(0.5, 0.5, 0.5, 1, 0, 0, 10), 1e-9, "point-blank inside -> 0");
    }
}
