package test.presets;

import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfig;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.FireballEntity;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.presets.mmc18.Explosion;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MineMen block-break, fitted on the 2026-07-25/26 flat-pad + distance-sweep captures (12x12 one-layer pad over
 * obsidian). Measured broke counts - TNT: end stone 21-27, planks 49-56, wool 78-81; fireball: planks 14-19,
 * wool 26-29. Vanilla rays give ~2/~12/~65 for TNT, so the ranges also prove the MineMen model is live.
 */
class Mmc18BlockBreakTest extends HeadlessServerTest {

    private static final int PAD_X = 700, PAD_Y = 64, PAD_Z = 700;

    private static Instance pad(Block top) { return padAt(PAD_X, PAD_Z, top); }

    private static Instance padAt(int x0, int z0, Block top) {
        Instance inst = flatInstance(MechanicsProfile.builder().build());
        for (int dx = -6; dx <= 5; dx++) {
            for (int dz = -6; dz <= 5; dz++) {
                inst.setBlock(x0 + dx, PAD_Y - 2, z0 + dz, Block.OBSIDIAN);
                inst.setBlock(x0 + dx, PAD_Y - 1, z0 + dz, Block.OBSIDIAN);
                inst.setBlock(x0 + dx, PAD_Y, z0 + dz, top);
            }
        }
        return inst;
    }

    /** 5x5 end-stone wall in the z={@code wz} plane; {@code center} replaces the middle block. */
    private static Instance wall(int wx, int wy, int wz, Block center) {
        Instance inst = flatInstance(MechanicsProfile.builder().build());
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++)
                inst.setBlock(wx + dx, wy + dy, wz, dx == 0 && dy == 0 ? center : Block.END_STONE);
        return inst;
    }

    private static ExplosionSystem system() {
        return new ExplosionSystem(polyp, Explosion.config());
    }

    private static int broke(Block top, float power, @Nullable Entity source, long seedShift) {
        Instance inst = pad(top);
        // captured centers sit at the exact pad surface with a fractional x/z; seedShift varies it like the TNT hop
        Pos center = new Pos(PAD_X + 0.3 + (seedShift % 5) * 0.1, PAD_Y + 1, PAD_Z + 0.3 + (seedShift % 3) * 0.15);
        system().explode(inst, center, power, source);
        int gone = 0;
        for (int dx = -6; dx <= 5; dx++)
            for (int dz = -6; dz <= 5; dz++)
                if (inst.getBlock(PAD_X + dx, PAD_Y, PAD_Z + dz).isAir()) gone++;
        return gone;
    }

    /** Mean of a few shots vs the capture range (wide margins: the ray intensity is random per shot). */
    private static void assertRange(Block top, float power, @Nullable Entity source, int measuredLo, int measuredHi) {
        int total = 0, shots = 4;
        for (int i = 0; i < shots; i++) total += broke(top, power, source, i);
        double mean = total / (double) shots;
        assertTrue(mean >= measuredLo * 0.7 && mean <= measuredHi * 1.3,
                top.name() + " mean broke " + mean + " outside capture range [" + measuredLo + "," + measuredHi + "]");
    }

    private static FireballEntity fireball() {
        return new FireballEntity(null, EntityType.FIREBALL,
                new ProjectileSnapshot(null, Fireball.INSTANCE, null, 1.0, null, null, null, null),
                ProjectileTypeConfig.builder(Fireball.KEY).build());
    }

    @Test
    void endStonePadBreaksLikeMinemen() { assertRange(Block.END_STONE, 4.0f, null, 21, 27); }

    @Test
    void planksPadBreaksLikeMinemen() { assertRange(Block.OAK_PLANKS, 4.0f, null, 49, 56); }

    @Test
    void woolPadBreaksLikeMinemen() { assertRange(Block.RED_WOOL, 4.0f, null, 78, 81); }

    @Test
    void fireballPadCountsMatchCaptures() {
        assertRange(Block.OAK_PLANKS, 2.0f, fireball(), 14, 19);
        assertRange(Block.RED_WOOL, 2.0f, fireball(), 26, 29);
    }

    /** The frozen-table skeleton under the noise: the reference double-precision walk at phase
     *  (.6875, +1.9532, .3125) on a wool pad, asserted with the noise knob zeroed. */
    private static final int[][] FROZEN_WOOL_PATTERN = {
            {-2, 1}, {-1, -2}, {-1, -1}, {-1, 0}, {-1, 1}, {-1, 2}, {0, -3}, {0, -2}, {0, -1}, {0, 0}, {0, 1},
            {0, 2}, {1, -3}, {1, -2}, {1, -1}, {1, 0}, {1, 1}, {1, 2}, {2, -2}, {2, -1}, {2, 0}, {2, 1}};

    private static Set<String> brokenPattern(int x0, int z0) {
        Instance inst = padAt(x0, z0, Block.RED_WOOL);
        var pure = Explosion.fireballFrozenTable().toBuilder().intensityNoise(0).build();
        var sys = new ExplosionSystem(polyp, ExplosionConfig.builder(Explosion.config()).blockBreaking(ctx -> pure).build());
        sys.explode(inst, new Pos(x0 + 0.6875, PAD_Y + 1.9532, z0 + 0.3125), 2.0f, fireball());
        Set<String> broken = new HashSet<>();
        for (int dx = -6; dx <= 5; dx++)
            for (int dz = -6; dz <= 5; dz++)
                if (inst.getBlock(x0 + dx, PAD_Y, z0 + dz).isAir()) broken.add(dx + "," + dz);
        return broken;
    }

    @Test
    void fireballPatternIsFrozen() {
        Set<String> expected = new HashSet<>();
        for (int[] c : FROZEN_WOOL_PATTERN) expected.add(c[0] + "," + c[1]);
        assertEquals(expected, brokenPattern(PAD_X, PAD_Z));
    }

    /** Same phase at a different position must break the identical relative cells (the capture-proven signature
     *  that killed position-hashed seeding). */
    @Test
    void fireballPatternTranslates() {
        assertEquals(brokenPattern(PAD_X, PAD_Z), brokenPattern(PAD_X + 37, PAD_Z - 23));
    }

    /** Fireballs never destroy end stone; the same blast from TNT (sourceless here) does. */
    @Test
    void fireballNeverBreaksEndStone() {
        assertEquals(0, broke(Block.END_STONE, 2.0f, fireball(), 0), "fireball spared the end stone");
        assertTrue(broke(Block.END_STONE, 2.0f, null, 0) > 0, "non-fireball blast still breaks it");
    }

    /** A wall hit NEXT to wood protruding from it breaks the wood: the pre-move blast stands off the wall in air,
     *  and its rays reach the wood while dying inside the blast-proof end stone. */
    @Test
    void nearMissWallHitBreaksProtrudingWood() {
        int wx = 760, wy = 66, wz = 760;
        Instance inst = wall(wx, wy, wz, Block.END_STONE);
        inst.setBlock(wx, wy, wz - 1, Block.OAK_PLANKS); // protruding from the wall face
        ExplosionSystem.install(polyp, Explosion.config()); // the flight path detonates via the installed module
        FireballEntity fb = fireball();
        fb.setExplosionPower(2.0f);
        fb.setAerodynamics(new Aerodynamics(0.0, 0.95, 0.95));
        fb.setInstance(inst, new Pos(wx + 1.5, wy + 0.5, wz - 3.5)).join(); // aimed at the wall a block beside the wood
        fb.setVelocityBt(new Vec(0, 0, 1.0));
        for (int i = 0; i < 10 && !fb.isRemoved(); i++) fb.tick(i);
        assertTrue(fb.isRemoved(), "fireball hit the wall");
        assertTrue(inst.getBlock(wx, wy, wz - 1).isAir(), "protruding wood breaks off the near-miss");
        assertTrue(inst.getBlock(wx + 1, wy, wz).compare(Block.END_STONE), "the wall itself holds");
    }

    /** A 1-thick end-stone wall shields wood mounted on its far side: the fireball ball reaches it, the line of
     *  sight crosses the wall and doesn't. TNT rays punch through. */
    @Test
    void endStoneWallShieldsWoodBehindIt() {
        int wx = 740, wy = 66, wz = 740;
        Instance inst = wall(wx, wy, wz, Block.END_STONE);
        inst.setBlock(wx, wy, wz - 1, Block.OAK_PLANKS); // mounted on the wall's far side (from the blast)
        ExplosionSystem sys = system();
        Pos farSide = new Pos(wx + 0.5, wy + 0.5, wz + 1.6); // in air, within the wood's ball radius
        sys.explode(inst, farSide, 2.0f, fireball());
        assertTrue(inst.getBlock(wx, wy, wz - 1).compare(Block.OAK_PLANKS), "wood behind the wall survives a fireball");
        sys.explode(inst, farSide, 4.0f, null);
        assertTrue(inst.getBlock(wx, wy, wz - 1).isAir(), "TNT rays punch through");
    }
}
