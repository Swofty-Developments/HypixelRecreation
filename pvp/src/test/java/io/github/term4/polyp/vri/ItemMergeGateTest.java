package io.github.term4.polyp.vri;

import io.github.term4.polyp.entity.DroppedItemEntity;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Minestom's item-merge scan is instance-wide; the vri node keeps merging within one world. */
class ItemMergeGateTest extends HeadlessServerTest {

    @BeforeAll
    static void install() {
        Vri.install(polyp, VriConfig.builder().build()); // all toggles off - the merge gate is unconditional
    }

    @Test
    void freshDropsMergeInsidePickupDelay() {
        Pos pos = new Pos(14.5, 66, 14.5);
        var a = new DroppedItemEntity(ItemStack.of(Material.STONE, 4), DroppedItemEntity.Model.LEGACY);
        var b = new DroppedItemEntity(ItemStack.of(Material.STONE, 4), DroppedItemEntity.Model.LEGACY);
        a.setPickupDelay(40, net.minestom.server.utils.time.TimeUnit.SERVER_TICK);
        b.setPickupDelay(40, net.minestom.server.utils.time.TimeUnit.SERVER_TICK);
        a.setInstance(instance, pos).join();
        b.setInstance(instance, pos).join();
        try {
            assertFalse(a.isPickable(), "inside the 40t pickup delay");
            a.update(System.currentTimeMillis());
            assertTrue(b.isRemoved(), "vanilla: the merge scan is not gated on the pickup delay");
            assertEquals(8, a.getItemStack().amount());
        } finally {
            for (ItemEntity e : new ItemEntity[]{a, b}) if (!e.isRemoved()) e.remove();
        }
    }

    @Test
    void mergingStaysWithinAWorld() {
        Pos pos = new Pos(10.5, 66, 10.5);
        ItemEntity bound = new ItemEntity(ItemStack.of(Material.STONE, 4));
        bound.setTag(MechanicsWorld.ENTITY_TAG, MechanicsWorld.of(instance));
        ItemEntity unbound = new ItemEntity(ItemStack.of(Material.STONE, 4));
        ItemEntity second = new ItemEntity(ItemStack.of(Material.STONE, 4));
        bound.setInstance(instance, pos).join();
        unbound.setInstance(instance, pos).join();
        try {
            long now = System.currentTimeMillis();
            bound.update(now);   // ItemEntity.update = the merge pass
            unbound.update(now);
            assertFalse(bound.isRemoved(), "differently-bound pairs never merge");
            assertFalse(unbound.isRemoved());
            assertEquals(4, bound.getItemStack().amount());

            second.setInstance(instance, pos).join();
            second.update(System.currentTimeMillis());
            assertTrue(unbound.isRemoved(), "same-world merging stays vanilla");
            assertEquals(8, second.getItemStack().amount());
            assertEquals(4, bound.getItemStack().amount());
        } finally {
            for (ItemEntity e : new ItemEntity[]{bound, unbound, second}) if (!e.isRemoved()) e.remove();
        }
    }
}
