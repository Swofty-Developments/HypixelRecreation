package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The empty-slot strip on the GROUPED viewer-send path: it must happen before Minestom wraps the packet in a
 * {@code CachedPacket} the per-viewer transform can't unwrap.
 */
class EquipmentSlotsFixTest extends HeadlessServerTest {

    private static FakePlayer wearer;
    private static FakePlayer viewer;

    @BeforeAll
    static void setUp() {
        EquipmentSlotsFix.install();
        wearer = FakePlayer.connect(instance, new Pos(0.5, 64, 0.5), "Wearer");
        viewer = FakePlayer.connect(instance, new Pos(1.5, 64, 1.5), "Viewer");
        wearer.player.addViewer(viewer.player);
    }

    @Test
    void groupedBulkEquipmentReachesViewerWithoutEmptySlots() {
        // Minestom's getEquipmentsPacket includes BODY=AIR for a player
        Map<EquipmentSlot, ItemStack> bulk = new LinkedHashMap<>();
        bulk.put(EquipmentSlot.HELMET, ItemStack.of(Material.DIAMOND_HELMET));
        bulk.put(EquipmentSlot.CHESTPLATE, ItemStack.of(Material.DIAMOND_CHESTPLATE));
        bulk.put(EquipmentSlot.BODY, ItemStack.AIR);

        viewer.sent.clear();
        wearer.player.sendPacketToViewers(new EntityEquipmentPacket(wearer.player.getEntityId(), bulk));

        EntityEquipmentPacket received = viewer.sent.stream()
                .map(EquipmentSlotsFixTest::asEquipment)
                .filter(p -> p != null && p.entityId() == wearer.player.getEntityId())
                .reduce((a, b) -> b).orElseThrow();

        assertFalse(received.equipments().containsKey(EquipmentSlot.BODY), "stray BODY=AIR must be stripped before grouping");
        assertFalse(received.equipments().values().stream().anyMatch(ItemStack::isAir), "no empty slots survive");
        assertTrue(received.equipments().containsKey(EquipmentSlot.HELMET));
        assertEquals(Material.DIAMOND_CHESTPLATE, received.equipments().get(EquipmentSlot.CHESTPLATE).material());
    }

    private static EntityEquipmentPacket asEquipment(SendablePacket packet) {
        SendablePacket resolved = packet instanceof CachedPacket cached ? cached.packet(ConnectionState.PLAY) : packet;
        return resolved instanceof EntityEquipmentPacket equipment ? equipment : null;
    }
}
