package test.presets;

import io.github.term4.polyp.mechanics.explosion.ExplosionExposure;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The fireball->TNT launch against the 2026-07-07_16-13-13 minemen capture (the pedestal jump-shot the A/B
 * was taken on): at each captured geometry the push is 1.3167 * (1-d/4) * seenPercent18FullCube * feet-radial, and
 * the DELIVERED vy carries the victim's settled rest motY +0.013154 additively (vanilla adds blast impulses
 * to stored motion). Two knife-edge shots excluded - sub-wire feet drift flips a 3-ray layer there.
 * Arena translated into chunk 0,0: X=x-363448, Y=y-28, Z=z-363451.
 */
class Mmc18FireballTntLaunchTest extends HeadlessServerTest {

    // fixed point of the grounded tick m -> -0.5*0.98*(m-0.04) (gravity, drag, ground flip): what vanilla
    // physics leaves in a resting TNT's stored motY, delivered additively in every blast kick
    private static final double REST_MOT_Y = 0.5 * 0.98 * 0.04 / (1 + 0.5 * 0.98);

    // feetX, feetZ, centerX, centerY, centerZ, rays/27, measPushX, measDeliveredVy, measPushZ (feetY=65;
    // vy +0.0500 exactly = their grounded tracker floor, asserted as pred+rest <= floor instead)
    private static final double[][] CASES = {
            {3.625,   7.78125, 2.0,     64.84017181396484, 7.65625, 19, 0.5387, 0.0658, 0.0378},
            {3.25,    7.6875,  2.09375, 64.98184967041016, 7.46875, 21, 0.7176, 0.0500, 0.1187},
            {3.75,    7.625,   2.4375,  64.73807525634766, 7.5,     12, 0.3734, 0.0865, 0.0345},
            {3.78125, 7.40625, 2.5625,  64.6273193359375,  7.53125,  3, 0.0919, 0.0500, -0.0087},
            {3.78125, 7.5,     2.125,   64.65416717529297, 7.4375,  15, 0.4053, 0.0971, 0.0150},
            {3.28125, 7.3125,  2.21875, 64.64494323730469, 7.53125, 21, 0.6864, 0.2432, -0.1321},
            {3.65625, 7.25,    2.21875, 64.63603973388672, 7.40625, 15, 0.4368, 0.1221, -0.0420},
            {3.78125, 7.40625, 2.40625, 64.59156799316406, 7.5,      9, 0.2643, 0.0914, -0.0166},
            {3.34375, 7.75,    2.71875, 64.63066864013672, 7.625,   10, 0.3385, 0.2080, 0.0705},
            {3.21875, 7.625,   2.96875, 64.35865783691406, 7.53125,  9, 0.1269, 0.3524, 0.0387},
    };

    @Test
    void reproducesTheCapturedLaunchesRayForRay() {
        instance.setBlock(3, 64, 7, Block.STONE); // the pedestal the TNT rests on
        Entity tnt = new Entity(EntityType.TNT); // registry bb 0.98
        tnt.setInstance(instance, new Pos(3.5, 65, 7.5)).join();
        for (double[] c : CASES) {
            tnt.teleport(new Pos(c[0], 65, c[1])).join();
            Vec center = new Vec(c[2], c[3], c[4]);
            float exp = ExplosionExposure.seenPercent18FullCube(MechanicsWorld.of(instance), center, tnt);
            assertEquals(c[5] / 27, exp, 1e-6, "exact ray count at " + c[2]);

            double dx = c[0] - c[2], dy = 65 - c[3], dz = c[1] - c[4];
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double m = 1.3167 * (1 - d / 4) * exp / d;
            assertEquals(Math.hypot(c[6], c[8]), Math.hypot(dx * m, dz * m), 0.012, "|h| at " + c[2]);
            double vy = dy * m + REST_MOT_Y;
            if (c[7] == 0.05) assertEquals(0.05, Math.max(vy, 0.05), 1e-9, "under their tracker floor at " + c[2]);
            else assertEquals(c[7], vy, 0.008, "delivered vy = push + rest motY at " + c[2]);
        }
        tnt.remove();
        instance.setBlock(3, 64, 7, Block.AIR);
    }
}
