package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pearling out of tight geometry, from the corner (mcpearl10 / mmcpearl11) and tunnel captures. MineMen refuses
 * none of these: corners take a 0.400 standoff on BOTH horizontal axes, and a thrower wedged under a low roof
 * rises to the top of their own feet cell instead of walking back.
 */
class Mmc18PearlCornerTest extends HeadlessServerTest {

    /** Walls on +x and +z, 5 tall, floor below: the captured obsidian alcove. */
    private static InstanceContainer alcove(int bx, int by, int bz) {
        InstanceContainer inst = flatInstance(null);
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++) inst.setBlock(bx + dx, by - 1, bz + dz, Block.OBSIDIAN);
        for (int dy = 0; dy <= 4; dy++)
            for (int d = 1; d <= 3; d++) {
                inst.setBlock(bx + d, by + dy, bz, Block.OBSIDIAN);
                inst.setBlock(bx, by + dy, bz + d, Block.OBSIDIAN);
            }
        return inst;
    }

    private static Pos pearlFrom(InstanceContainer inst, Pos stance) {
        LivingEntity shooter = new LivingEntity(EntityType.ZOMBIE);
        shooter.setInstance(inst, stance).join();
        try {
            ProjectileConfig config = Projectiles.config();
            var snap = ProjectileSnapshot.of(shooter, Pearl.INSTANCE).withConfig(config);
            ProjectileEntity pearl = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
            assertNotNull(pearl);
            awaitSpawn(pearl);
            for (int tick = 1; tick <= 40 && !pearl.isRemoved(); tick++) pearl.tick(tick * 50L);
            return shooter.getPosition();
        } finally {
            shooter.remove();
        }
    }

    /** stompearl10's exact refusal: wedged in the corner, throwing point-blank into the +x wall. */
    @Test
    void pointBlankIntoTheCornerStillTeleports() {
        int bx = 4, by = 70, bz = 4;
        Pos stance = new Pos(bx + 0.70, by, bz + 0.70, -80.3619f, 9.8474f);
        Pos after = pearlFrom(alcove(bx, by, bz), stance);
        assertTrue(after.distance(stance) > 1e-6, "MineMen refused 0 of 48 of these; got a refusal echo");
        // free axis backed 0.4 off the +z wall the box was resting against, so the box clears it
        assertTrue(after.z() + 0.3 < bz + 1.0 + 1e-6, "player box still inside the +z wall at " + after);
        assertTrue(after.x() + 0.3 < bx + 1.0 + 1e-6, "player box still inside the +x wall at " + after);
    }

    /** mmcpearl11 walked backwards down the lane; every one of the 19 teleported. */
    @Test
    void everyStandoffDownTheLaneTeleports() {
        int bx = 4, by = 70, bz = 4;
        InstanceContainer inst = alcove(bx, by, bz);
        int refused = 0;
        for (double back = 0.0; back <= 2.5; back += 0.25) {
            Pos stance = new Pos(bx + 0.70 - back, by, bz + 0.70, -90f, 0.2f); // face +x, hugging the +z wall
            if (pearlFrom(inst, stance).distance(stance) < 1e-6) refused++;
        }
        assertEquals(0, refused, "MineMen refused 0 of 19 walking back down the lane");
    }

    /** 2-high corridor: floor, roof 2 above, end wall. {@code slab} raises the stance off the whole block. */
    private static InstanceContainer corridor(int bx, int by, int bz, boolean flooded, boolean slab) {
        InstanceContainer inst = flatInstance(null);
        for (int dx = -1; dx <= 6; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                inst.setBlock(bx + dx, by - 1, bz + dz, Block.OBSIDIAN);
                inst.setBlock(bx + dx, by + 2, bz + dz, Block.OBSIDIAN);
                if (flooded) for (int dy = 0; dy <= 1; dy++) inst.setBlock(bx + dx, by + dy, bz + dz, Block.WATER);
            }
        for (int dy = 0; dy <= 1; dy++)
            for (int dz = -1; dz <= 1; dz++) inst.setBlock(bx + 6, by + dy, bz + dz, Block.OBSIDIAN);
        if (slab) inst.setBlock(bx, by, bz, Block.SMOOTH_STONE_SLAB);
        return inst;
    }

    /** Flat whole-block ground under a 2-high roof: air at the feet, so no lift (mmctunnelpearl, 26/32). */
    @Test
    void flatGroundUnderALowRoofStaysPut() {
        int bx = 4, by = 70, bz = 4;
        Pos stance = new Pos(bx + 0.5, by, bz + 0.5, -90f, 0f);
        Pos after = pearlFrom(corridor(bx, by, bz, false, false), stance);
        assertEquals(by, after.y(), 1e-9, "whole-block stance must not be lifted: " + after);
    }

    /** Same corridor and aim from a part-block stance: standing IN the slab lifts to its top (16/16). */
    @Test
    void partBlockStanceUnderALowRoofLiftsToTheBlockLevel() {
        int bx = 30, by = 70, bz = 4;
        Pos stance = new Pos(bx + 0.5, by + 0.5, bz + 0.5, -90f, 0f);
        Pos after = pearlFrom(corridor(bx, by, bz, false, true), stance);
        assertEquals(by + 1, after.y(), 1e-9, "part-block stance rounds up to the block level: " + after);
        assertEquals(stance.x(), after.x(), 1e-9, "x/z stay the thrower's: " + after);
        assertEquals(stance.z(), after.z(), 1e-9, "x/z stay the thrower's: " + after);
    }

    /** Water is just another block in the feet cell: submerged lifts where the same dry stance stays. */
    @Test
    void submergedWholeBlockStanceIsLiftedWhereTheDryOneStays() {
        Pos dryStance = new Pos(50 + 0.5, 70, 4 + 0.5, -90f, 0f);
        Pos wetStance = new Pos(70 + 0.5, 70, 4 + 0.5, -90f, 0f);
        assertEquals(70, pearlFrom(corridor(50, 70, 4, false, false), dryStance).y(), 1e-9, "dry stays");
        assertEquals(71, pearlFrom(corridor(70, 70, 4, true, false), wetStance).y(), 1e-9, "submerged lifts");
    }

    /** Pearl into the roof: a ceiling hit resolves to plane-2, the thrower's own level, so no lift. */
    @Test
    void ceilingHitUnderALowRoofLeavesTheThrowerPut() {
        int bx = 90, by = 70, bz = 4;
        InstanceContainer inst = flatInstance(null);
        for (int dx = -4; dx <= 4; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                inst.setBlock(bx + dx, by - 1, bz + dz, Block.OBSIDIAN);
                inst.setBlock(bx + dx, by + 2, bz + dz, Block.OBSIDIAN);
            }
        Pos stance = new Pos(bx + 0.5, by, bz + 0.5, 0f, -90f);
        assertEquals(by, pearlFrom(inst, stance).y(), 1e-9, "a ceiling hit stays at the thrower's level");
    }

    /** Thrown away down an open lane the walk-back would drop you at the far floor; wedged, you rise instead. */
    @Test
    void openLaneFromAPartBlockStanceLiftsInstead() {
        InstanceContainer inst = flatInstance(null);
        int bx = 110, by = 70, bz = 4;
        for (int dx = -12; dx <= 2; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                inst.setBlock(bx + dx, by - 1, bz + dz, Block.OBSIDIAN);
                inst.setBlock(bx + dx, by + 2, bz + dz, Block.OBSIDIAN);
            }
        inst.setBlock(bx, by, bz, Block.SMOOTH_STONE_SLAB);
        Pos stance = new Pos(bx + 0.5, by + 0.5, bz + 0.5, 90f, 4f);
        Pos after = pearlFrom(inst, stance);
        assertEquals(by + 1, after.y(), 1e-9, "lifted to the block level, not dropped down the lane: " + after);
        assertEquals(stance.x(), after.x(), 1e-9, "x/z stay the thrower's: " + after);
    }
}
