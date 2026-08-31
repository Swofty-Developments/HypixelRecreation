package io.github.term4.polyp.world;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.entity.DroppedItemEntity;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tick ownership gate: a covered entity ticked by a thread that is not its owner's must no-op entirely
 * (dispatcher eviction is queued, so a stalled server can drive a just-adopted entity late).
 */
class OwnershipGateTest extends HeadlessServerTest {

    @Test
    void staleDriversNeverRunACoveredEntitysTick() {
        DroppedItemEntity drop = new DroppedItemEntity(ItemStack.of(Material.STONE), DroppedItemEntity.Model.LEGACY);
        drop.setInstance(instance, new Pos(0.5, 70, 0.5)).join();
        try {
            MechanicsWorld.resolver(new MechanicsWorld.Resolver() {
                @Override public MechanicsWorld resolve(Entity e) { return e.getTag(MechanicsWorld.ENTITY_TAG); }
                @Override public boolean ownsCurrentTick(Entity e) { return e != drop; }
            });
            long before = drop.getAliveTicks();
            drop.tick(System.currentTimeMillis());
            assertEquals(before, drop.getAliveTicks(), "a stale driver's tick must not run");

            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
            drop.tick(System.currentTimeMillis());
            assertEquals(before + 1, drop.getAliveTicks(), "the owner's tick runs");
        } finally {
            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
            drop.remove();
        }
    }

    /** PrimedTnt was the missing case: without a tick guard a foreign clock advances its fuse and detonates early. */
    @Test
    void staleDriverNeverAdvancesTntFuse() {
        var tnt = io.github.term4.polyp.presets.mmc18.Tnt.spawn(
                polyp.module(io.github.term4.polyp.mechanics.explosion.ExplosionSystem.class),
                instance, new net.minestom.server.coordinate.BlockVec(4, 70, 4));
        try {
            MechanicsWorld.resolver(new MechanicsWorld.Resolver() {
                @Override public MechanicsWorld resolve(Entity e) { return e.getTag(MechanicsWorld.ENTITY_TAG); }
                @Override public boolean ownsCurrentTick(Entity e) { return e != tnt; }
            });
            long before = tnt.getAliveTicks();
            for (int i = 0; i < 5; i++) tnt.tick(System.currentTimeMillis());
            assertEquals(before, tnt.getAliveTicks(), "a stale driver's tick must not advance the fuse");

            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
            tnt.tick(System.currentTimeMillis());
            assertEquals(before + 1, tnt.getAliveTicks(), "the owner's tick runs");
        } finally {
            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
            tnt.remove();
        }
    }

    /** Every self-driven polyp entity carries the marker, so one bridge rule covers them all. */
    @Test
    void selfDrivenEntitiesAreExternallyTickable() {
        assertTrue(new DroppedItemEntity(ItemStack.of(Material.STONE), DroppedItemEntity.Model.LEGACY) instanceof ExternallyTickable);
        var tnt = io.github.term4.polyp.presets.mmc18.Tnt.spawn(
                polyp.module(io.github.term4.polyp.mechanics.explosion.ExplosionSystem.class),
                instance, new net.minestom.server.coordinate.BlockVec(6, 70, 6));
        try {
            assertTrue(tnt instanceof ExternallyTickable, "PrimedTnt is now covered");
        } finally {
            tnt.remove();
        }
    }
}
