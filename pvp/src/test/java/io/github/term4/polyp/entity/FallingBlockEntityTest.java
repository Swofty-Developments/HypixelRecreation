package io.github.term4.polyp.entity;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 1.8 falling block: the first tick clears the origin, the fall steps 0.04/0.98, landing places into the
 * cell it stops in - or drops the item when that cell is taken (sand on a torch).
 */
class FallingBlockEntityTest extends HeadlessServerTest {

    private static void settle(FallingBlockEntity e) {
        for (int i = 0; i < 80 && !e.isRemoved(); i++) e.tick(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()));
        assertTrue(e.isRemoved(), "fall never settled");
    }

    @Test
    void fallsAndPlacesOnTheFloor() {
        MechanicsWorld world = MechanicsWorld.of(instance);
        Vec origin = new Vec(8, 70, 40);
        instance.setBlock(origin, Block.SAND);

        FallingBlockEntity e = FallingBlockEntity.spawn(world, origin, Block.SAND);
        awaitSpawn(e);
        settle(e);

        assertTrue(instance.getBlock(origin).air(), "origin cleared on the first tick");
        assertEquals(Block.SAND, instance.getBlock(new Vec(8, 64, 40)), "landed on the floor");
    }

    @Test
    void dropsWhenTheLandingCellIsTaken() {
        MechanicsWorld world = MechanicsWorld.of(instance);
        Vec origin = new Vec(10, 70, 40);
        instance.setBlock(new Vec(10, 64, 40), Block.TORCH);
        instance.setBlock(origin, Block.SAND);

        FallingBlockEntity e = FallingBlockEntity.spawn(world, origin, Block.SAND);
        awaitSpawn(e);
        settle(e);

        assertEquals(Block.TORCH, instance.getBlock(new Vec(10, 64, 40)), "torch survives");
        var drops = instance.getEntities().stream().filter(en -> en instanceof DroppedItemEntity).toList();
        assertTrue(!drops.isEmpty(), "the sand dropped as an item");
        drops.forEach(Entity::remove); // shared instance: later tests scan its entities
    }

    @Test
    void copyAndSaveCarryTheFallInProgress() {
        MechanicsWorld world = MechanicsWorld.of(instance);
        Vec origin = new Vec(12, 70, 40);
        instance.setBlock(origin, Block.SAND);
        FallingBlockEntity e = FallingBlockEntity.spawn(world, origin, Block.SAND);
        awaitSpawn(e);
        for (int i = 0; i < 3; i++) e.tick(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()));

        FallingBlockEntity copy = (FallingBlockEntity) e.getTag(MechanicsWorld.ENTITY_COPY).get();
        assertTrue(copy != e && !copy.isRemoved(), "fresh unspawned twin");

        var saved = e.getTag(MechanicsWorld.ENTITY_SAVE).get();
        assertEquals("polyp:falling_block", saved.getString("id"));
        assertTrue(saved.getInt("fallTime") > 0, "a mid-fall save keeps its clock, so the twin never re-clears the origin");
        FallingBlockEntity revived = FallingBlockEntity.fromSave(saved);
        assertTrue(!revived.isRemoved(), "revivable");

        e.remove();
        instance.setBlock(origin, Block.AIR);
    }
}
