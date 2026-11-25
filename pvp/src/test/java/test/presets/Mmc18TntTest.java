package test.presets;

import io.github.term4.polyp.mechanics.explosion.ExplosionExposure;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.github.term4.polyp.presets.mmc18.Explosion;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.presets.mmc18.Tnt;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import net.minestom.server.instance.Instance;
import io.github.term4.polyp.util.tick.TickScalingConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The mmc18 MineMen TNT against the capture-measured spec (mmc18-explosion-model memory). */
class Mmc18TntTest extends HeadlessServerTest {

    private static ExplosionSystem explosions;

    @BeforeAll
    static void setUp() {
        // these measure PUSH geometry on fixed terrain; the preset's block breaking would crater the
        // pits and pillars later cases rely on, so it is off here rather than a hidden variable
        explosions = new ExplosionSystem(polyp, Explosion.config().toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build());
    }

    @Test
    void fallingTntBouncesOnLanding() {
        PrimedTnt tnt = Tnt.spawn(explosions, instance, new BlockVec(4, 70, 14)); // spawns 70.5, surface at 64: a HARD landing
        boolean bounced = false;
        for (int i = 0; i < 45 && !bounced; i++) {
            tnt.tick(0);
            bounced = tnt.isOnGround() && tnt.getVelocity().y() > 1.0; // b/s; the impact flipped x-0.5
        }
        assertTrue(bounced, "landing must flip motY (the live vanilla ground lines)");
        tnt.remove();
    }

    @Test
    void groundedRestMotYConvergesToTheVanillaFixedPoint() {
        // blast kicks ADD to stored motion, so the rest value rides every grounded launch: the 16-13-13
        // capture reads +0.0132 in each delivered vy (the old -0.0392 probe flattened every arc by 0.052)
        PrimedTnt tnt = Tnt.spawn(explosions, instance, new BlockVec(2, 64, 8));
        for (int i = 0; i < 30; i++) tnt.tick(0);
        assertTrue(tnt.isOnGround(), "settled on the ground");
        assertEquals(0.5 * 0.98 * 0.04 / (1 + 0.5 * 0.98), tnt.getVelocity().div(20).y(), 5e-4,
                "stored motY = the fixed point of the grounded tick m -> -0.5*0.98*(m-0.04)");
        tnt.remove();
    }

    @Test
    void tntBlastPushesGroundedTntAtMeasuredScale() {
        PrimedTnt victim = Tnt.spawn(explosions, instance, new BlockVec(2, 64, 8));
        PrimedTnt source = Tnt.spawn(explosions, instance, new BlockVec(5, 64, 8));
        for (int i = 0; i < 51; i++) { victim.tick(0); source.tick(0); } // both settle; fuse 52
        Pos vp = victim.getPosition(), sp = source.getPosition();
        assertTrue(Math.hypot(victim.getVelocity().x(), victim.getVelocity().z()) < 0.2, "settled before the blast");
        source.tick(0); // 52nd tick: detonates at its feet
        double d = vp.distance(sp);
        double expected = 1.1 * (1 - d / 8); // TNT-on-TNT scale, level victims -> pure horizontal (b/t)
        Vec v = victim.getVelocity().div(20);
        assertEquals(expected, Math.hypot(v.x(), v.z()), 0.02, "grounded TNT push = 1.1*(1-d/8) at d=" + d);
        assertTrue(v.x() < 0, "pushed away from the source");
        victim.remove();
    }

    @Test
    void spawnHopNeverClearsAdjacentBlock() {
        // vanilla hop apex is +0.386 (gravity precedes each move); the raw-motion move order reached +0.577
        // and let the kick carry TNT onto neighbouring blocks
        instance.setBlock(3, 64, 8, Block.STONE); // 1-high wall east of the spawn
        PrimedTnt tnt = Tnt.spawn(explosions, instance, new BlockVec(2, 64, 8));
        tnt.setVelocity(new Vec(0.02, 0.2, 0).mul(20)); // deterministic kick straight at the wall
        double maxY = tnt.getPosition().y();
        for (int i = 0; i < 25; i++) {
            tnt.tick(0);
            maxY = Math.max(maxY, tnt.getPosition().y());
        }
        assertEquals(0.386, maxY - 64.5, 0.01, "hop apex must match vanilla");
        assertEquals(64.0, tnt.getPosition().y(), 0.01, "wall halts the kick - no climb");
        tnt.remove();
        instance.setBlock(3, 64, 8, Block.AIR);
    }

    @Test
    void pillarUnderVictimReducesPushByExposure() {
        instance.setBlock(11, 64, 3, Block.STONE); // 1-block pillar
        PrimedTnt victim = Tnt.spawn(explosions, instance, new BlockVec(11, 65, 3)); // rests on the pillar at 65
        victim.setVelocity(new Vec(0, 0.2, 0).mul(20)); // driftless - deterministic geometry
        PrimedTnt source = Tnt.spawn(explosions, instance, new BlockVec(13, 64, 3));
        source.setVelocity(new Vec(0, 0.2, 0).mul(20));
        for (int i = 0; i < 51; i++) { victim.tick(0); source.tick(0); }
        assertEquals(65.0, victim.getPosition().y(), 0.01, "victim must rest on the pillar");
        double d = victim.getPosition().distance(source.getPosition());
        float exposure = ExplosionExposure.seenPercent18FullCube(MechanicsWorld.of(instance), source.getPosition(), victim);
        source.tick(0);
        assertEquals(1.1 * (1 - d / 8) * exposure, victim.getVelocity().div(20).length(), 0.03,
                "push = 1.1 * F * shared 1.8 exposure (the pillar shadow reduces it)");
        victim.remove();
        instance.setBlock(11, 64, 3, Block.AIR);
    }

    @Test
    void elevatedVictimTakesTheScaledVanillaPush() {
        // the collapse: no special 1.44 "seam release" - just 1.1 x the shared vanilla push (real 1.8 exposure).
        // stays inside chunk (0,0): the swept exposure rays treat unloaded chunks as solid
        instance.setBlock(3, 64, 9, Block.STONE);
        PrimedTnt victim = Tnt.spawn(explosions, instance, new BlockVec(3, 65, 9));
        victim.setVelocity(new Vec(0, 0.2, 0).mul(20)); // driftless kick - stays centered on the pillar
        PrimedTnt source = Tnt.spawn(explosions, instance, new BlockVec(3, 64, 4));
        for (int i = 0; i < 51; i++) { victim.tick(0); source.tick(0); }
        assertEquals(65.0, victim.getPosition().y(), 0.01, "victim must rest on the pillar");
        double d = victim.getPosition().distance(source.getPosition());
        float exposure = ExplosionExposure.seenPercent18FullCube(MechanicsWorld.of(instance), source.getPosition(), victim);
        source.tick(0);
        assertEquals(1.1 * (1 - d / 8) * exposure, victim.getVelocity().div(20).length(), 0.03,
                "elevated push = 1.1 * F * shared 1.8 exposure");
        victim.remove();
        instance.setBlock(3, 64, 9, Block.AIR);
    }

    @Test
    void surfaceBlastPushesNothingBelowItsLevel() {
        instance.setBlock(11, 63, 3, Block.AIR); // 2-deep pit (1-deep lets the 0.98 bb clamber out)
        instance.setBlock(11, 62, 3, Block.AIR);
        PrimedTnt victim = Tnt.spawn(explosions, instance, new BlockVec(11, 62, 3)); // rests at 62, below the surface
        PrimedTnt source = Tnt.spawn(explosions, instance, new BlockVec(13, 64, 3)); // rests on the surface at 64
        for (int i = 0; i < 51; i++) { victim.tick(0); source.tick(0); }
        assertEquals(62.0, victim.getPosition().y(), 0.01, "victim must be in the pit");
        source.tick(0);
        Vec v = victim.getVelocity().div(20);
        assertTrue(Math.hypot(v.x(), v.z()) < 0.01, "feet-level center self-occludes downward: " + v);
        victim.remove();
        instance.setBlock(11, 63, 3, Block.STONE);
        instance.setBlock(11, 62, 3, Block.STONE);
    }

    /**
     * The spawn scatter (0.2 up) must re-rate with the sim rate. Gravity is already ×s²; if the initial velocity
     * stays native, apex = v²/2g = 0.5/s² and slo-mo TNT rockets skyward (8 blocks at s=0.25 vs ~0.4 native).
     */
    @Test
    void spawnScatterApexIsRateInvariant() {
        double nativeApex = spawnApex(null);
        double sloMoApex = spawnApex(TickScalingConfig.simulated(5)); // s = 0.25
        assertEquals(nativeApex, sloMoApex, 0.1, "slo-mo apex tracks native (~0.4), not 1/s² = ~6 blocks");
    }

    // its own instance so the resolver reads THIS scope (setGlobal loses to the profile resolver on the shared one)
    private double spawnApex(TickScalingConfig cfg) {
        var profile = MechanicsProfile.builder();
        if (cfg != null) profile.set(MechanicsKeys.TICK_SCALING, cfg);
        Instance inst = flatInstance(profile.build());
        PrimedTnt tnt = Tnt.spawn(explosions, inst, new BlockVec(8, 90, 8));
        double y0 = tnt.getPosition().y(), maxRise = 0;
        for (int i = 0; i < 40 && !tnt.isRemoved(); i++) {
            tnt.tick(0);
            if (!tnt.isRemoved()) maxRise = Math.max(maxRise, tnt.getPosition().y() - y0);
        }
        tnt.remove();
        return maxRise;
    }
}
