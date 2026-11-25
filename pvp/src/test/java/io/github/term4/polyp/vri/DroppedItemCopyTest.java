package io.github.term4.polyp.vri;

import io.github.term4.polyp.entity.DroppedItemEntity;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** The {@link MechanicsWorld#ENTITY_COPY} stamp: dropped items describe their own copy for world fork/respawn cloners. */
class DroppedItemCopyTest extends HeadlessServerTest {

    @Test
    void dropsCarryACopySupplier() {
        DroppedItemEntity item = new DroppedItemEntity(ItemStack.of(Material.DIAMOND, 3), DroppedItemEntity.Model.LEGACY);
        Supplier<Entity> copySupplier = item.getTag(MechanicsWorld.ENTITY_COPY);
        assertNotNull(copySupplier, "drops are copy-ready out of the box");
        DroppedItemEntity copy = assertInstanceOf(DroppedItemEntity.class, copySupplier.get());
        assertEquals(item.getItemStack(), copy.getItemStack(), "the copy carries the stack");
    }
}
