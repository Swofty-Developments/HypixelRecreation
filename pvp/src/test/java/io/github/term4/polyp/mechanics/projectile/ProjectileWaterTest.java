package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 1.8 projectile water physics ({@code EntityArrow}/{@code EntityProjectile}: drag {@code 0.6}/{@code 0.8}
 * replaces the air {@code 0.99} while the {@code Entity.W()} inset box touches water; the current shoves
 * {@code 0.014} along the flow). Detection uses the VANILLA entity box, quirks included - the test flight
 * heights sit inside the band the inset detects at.
 */
class ProjectileWaterTest extends HeadlessServerTest {

    @BeforeAll
    static void pool() {
        // an isolated far-away zone: the shared instance carries other tests' arenas
        for (int x = 50; x <= 53; x++)
            for (int z = 0; z <= 1; z++)
                instance.loadChunk(x, z).join();
        // a deep still pool: source water, zero slope
        for (int x = 808; x <= 840; x++)
            for (int y = 60; y <= 70; y++)
                for (int z = 8; z <= 24; z++) instance.setBlock(x, y, z, Block.WATER);
    }

    private static ProjectileEntity launch(io.github.term4.polyp.mechanics.projectile.ProjectileConfig config,
                                           Object type, Pos from) {
        LivingEntity shooter = looseZombie();
        shooter.setInstance(instance, from).join();
        var snap = ProjectileSnapshot.of(shooter,
                        (io.github.term4.polyp.mechanics.projectile.types.ProjectileType) type)
                .withConfig(config);
        ProjectileEntity entity = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(entity);
        awaitSpawn(entity);
        shooter.remove();
        return entity;
    }

    /** Per-tick horizontal drag ratio (x-axis: gravity only touches y). The 1.8 inset water box detects a
     *  short entity only over part of each block's height (the vanilla flicker) - {@code atY} pins the
     *  sampled ticks inside the band, and the short window keeps gravity from sinking it out. */
    private static double horizontalRatio(ProjectileEntity e, double atY, int samples) {
        e.teleport(e.getPosition().withY(atY)).join();
        e.tick(50L);
        double ratio = 0;
        double prev = Math.abs(e.velocityBt().x());
        for (int t = 0; t < samples; t++) {
            e.tick((2 + t) * 50L);
            double cur = Math.abs(e.velocityBt().x());
            ratio += cur / prev;
            prev = cur;
        }
        return ratio / samples;
    }

    @Test
    void throwableDragsAtVanilla08InWater() {
        ProjectileEntity ball = launch(Vanilla18.projectiles(), Snowball.INSTANCE,
                new Pos(810.5, 62.4, 16.5, -90, 0)); // throws +x, submerged
        // snowball (0.25 tall) inset band: frac(y) in [0.151, 0.599)
        double ratio = horizontalRatio(ball, 62.45, 2);
        assertEquals(0.8, ratio, 0.02, "submerged throwable drags at the water 0.8");
    }

    @Test
    void arrowDragsAtVanilla06InWater() {
        ProjectileEntity arrow = launch(Vanilla18.projectiles(), Arrow.INSTANCE,
                new Pos(810.5, 62.4, 20.5, -90, 0));
        // arrow (0.5 tall) inset band: frac(y) in [0, 0.599)
        double ratio = horizontalRatio(arrow, 62.45, 2);
        assertEquals(0.6, ratio, 0.02, "submerged arrow drags at the water 0.6");
    }

    @Test
    void dryFlightKeepsAirDrag() {
        ProjectileEntity ball = launch(Vanilla18.projectiles(), Snowball.INSTANCE,
                new Pos(810.5, 100, 16.5, -90, 0)); // far above the pool
        double ratio = horizontalRatio(ball, 100.45, 2);
        assertEquals(0.99, ratio, 0.005, "dry flight keeps the 0.99 air drag");
    }

    @Test
    void waterKnobsOffKeepAirDragSubmerged() {
        var config = io.github.term4.polyp.mechanics.projectile.ProjectileConfig
                .builder(Vanilla18.projectiles())
                .defaults(ProjectileTypeConfig.builder(io.github.term4.polyp.presets.vanilla18.Projectiles.defaults())
                        .waterDrag(1.0).waterPush(0.0).build())
                .build();
        ProjectileEntity ball = launch(config, Snowball.INSTANCE, new Pos(810.5, 62.4, 16.5, -90, 0));
        double ratio = horizontalRatio(ball, 62.45, 2);
        assertEquals(0.99, ratio, 0.005, "waterDrag(1)/waterPush(0) = air behavior under water");
    }

    @Test
    void currentPushesAlongTheFlow() {
        // a floored 3-wide channel with the gradient along x (source -> 2 -> 4 -> 6): z-slopes cancel,
        // no falling-water down-term diluting the unit flow
        int y = 62, z = 30;
        for (int x = 807; x <= 812; x++) {
            for (int dz = -1; dz <= 1; dz++) {
                instance.setBlock(x, y - 1, z + dz, Block.STONE);
                for (int yy = y + 1; yy <= y + 6; yy++) instance.setBlock(x, yy, z + dz, Block.AIR); // the generator's ground sits here
            }
        }
        for (int x = 808; x <= 811; x++) {
            int level = (x - 808) * 2;
            for (int dz = -1; dz <= 1; dz++) {
                instance.setBlock(x, y, z + dz, level == 0 ? Block.WATER
                        : Block.WATER.withProperty("level", String.valueOf(level)));
            }
        }
        // zero spread: the random launch jitter would decay under drag and read as a phantom shove
        var pushCfg = io.github.term4.polyp.mechanics.projectile.ProjectileConfig
                .builder(Vanilla18.projectiles())
                .defaults(ProjectileTypeConfig.builder(io.github.term4.polyp.presets.vanilla18.Projectiles.defaults())
                        .spread(0.0).build())
                .build();
        double pushed = currentDvx(pushCfg, y, z);
        var noPush = io.github.term4.polyp.mechanics.projectile.ProjectileConfig
                .builder(Vanilla18.projectiles())
                .defaults(ProjectileTypeConfig.builder(io.github.term4.polyp.presets.vanilla18.Projectiles.defaults())
                        .spread(0.0).waterPush(0.0).build())
                .build();
        double still = currentDvx(noPush, y, z);
        assertTrue(Math.abs(pushed) > 0.0015, "the current shoved the projectile (dvx " + pushed + ")");
        assertTrue(Math.abs(still) < 3.0e-4, "waterPush(0) turns the shove off (dvx " + still + ")");
    }

    private static double currentDvx(io.github.term4.polyp.mechanics.projectile.ProjectileConfig config,
                                     int y, int z) {
        // STRAIGHT up: the fluid pass samples the pre-move position (the launch tick is wet), and the
        // flight never leaves the rig - an angled throw died on terrain in chunks other test classes load
        ProjectileEntity ball = launch(config, Snowball.INSTANCE, new Pos(809.5, 65, z + 0.5, 0, -90));
        ball.teleport(new Pos(809.5, y + 0.45, z + 0.5)).join();
        double vx0 = ball.velocityBt().x();
        for (int t = 1; t <= 2; t++) ball.tick(t * 50L);
        ball.remove();
        return ball.velocityBt().x() - vx0;
    }
    /** 26.1 senses water by fluid HEIGHT (no inset): at a height where the 1.8 box inverts and reads dry
     *  (snowball dead band frac(y) < 0.151), the modern model still drags. Same values, different sensing. */
    @Test
    void modernModelHasNoDetectionFlicker() {
        // legacy at the flicker height: dry -> air drag
        double legacy = horizontalRatio(launch(Vanilla18.projectiles(), Snowball.INSTANCE,
                new Pos(810.5, 62.4, 16.5, -90, 0)), 62.05, 2);
        assertEquals(0.99, legacy, 0.005, "1.8 sensing flickers dry at this height (vanilla-exact)");

        var modern = io.github.term4.polyp.mechanics.projectile.ProjectileConfig
                .builder(Vanilla18.projectiles())
                .defaults(ProjectileTypeConfig.builder(io.github.term4.polyp.presets.vanilla18.Projectiles.defaults())
                        .waterModel(ProjectileTypeConfig.WaterModel.MODERN).build())
                .build();
        double sensed = horizontalRatio(launch(modern, Snowball.INSTANCE,
                new Pos(810.5, 62.4, 16.5, -90, 0)), 62.05, 2);
        assertEquals(0.8, sensed, 0.02, "26.1 sensing stays wet at every height");
    }
}
