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
 * {@link ExplosionExposure#seenPercent18FullCube} against minemen's measured pillar-capture values (14-14-57): at
 * their exact pillar-frame geometries the port reproduces the k/27 readings ray-for-ray (5 exact, others
 * within one ray; two cliff-edge cases excluded - sub-wire feet drift flips layers there).
 */
class Mmc18FireballExposureTest extends HeadlessServerTest {

    @Test
    void reproducesTheMinemenLadderRayForRay() {
        instance.setBlock(5, 64, 5, Block.STONE); // pillar occupies [5,6]x[64,65]x[5,6]; polyp pillar (352649,92,352646)
        Entity tnt = new Entity(EntityType.TNT); // registry bb 0.98
        tnt.setInstance(instance, new Pos(5.5, 65, 5.5)).join();
        double[][] cases = { // feetX-649, feetZ-646, centerX-649, centerY-92, centerZ-646, minemen measured
                {0.78125, 0.40625, 0.5625, 0.6763, -0.78125, 0.704},   // 501700
                {0.65625, 0.25,    0.5,    0.5774, -0.71875, 0.695},   // 501995
                {0.3125,  0.28125, 0.46875,0.6612, -0.53125, 0.693},   // 502952
                {0.21875, 0.46875, 0.4375, 0.4194, -0.15625, 0.128},   // 503275
                {0.75,    0.34375, 0.5,    0.4776, -0.1875,  0.273},   // 503423
                {0.75,    0.34375, 0.5,    0.7185, -0.90625, 0.717},   // 503137
                {0.21875, 0.40625, 0.4375, 0.1100, -0.625,   0.362},   // 504855
                {0.75,    0.375,   0.53125,0.2062, -0.5625,  0.400},   // 505050
                {0.71875, 0.71875, 0.53125,0.4876, -0.75,    0.343},   // 502730
        };
        for (double[] c : cases) {
            tnt.teleport(new Pos(5 + c[0], 65, 5 + c[1])).join();
            Vec center = new Vec(5 + c[2], 64 + c[3], 5 + c[4]);
            float e = ExplosionExposure.seenPercent18FullCube(MechanicsWorld.of(instance), center, tnt);
            assertEquals(c[5], e, 1.05 / 27.0, "k/27 within one ray of the minemen reading");
        }
        tnt.remove();
        instance.setBlock(5, 64, 5, Block.AIR);
    }
}
