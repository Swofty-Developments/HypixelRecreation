package io.github.term4.polyp.platform.fixes.client;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import io.github.term4.polyp.platform.PacketShapes;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;

import java.util.EnumMap;
import java.util.Map;

/**
 * Strips empty (AIR) slots from outgoing {@code EntityEquipmentPacket}s (vanilla parity - {@code ServerEntity} skips
 * them). Minestom sends every slot incl. {@code BODY=AIR}; ViaBackwards remaps BODY onto the chestplate slot and
 * {@code Map.copyOf} order decides which entry wins - the real chestplate intermittently renders invisible on legacy
 * clients. Applied to ALL clients: a legacy gate leaks through the join window (equipment is sent before the viewer's
 * protocol resolves). Temporary: upstream branch {@code fix/skip-empty-equipment-slots}.
 */
public final class EquipmentSlotsFix {

    private static volatile boolean enabled;

    private EquipmentSlotsFix() {}

    public static void install() {
        enabled = true;
    }

    /** Strips empty equipment slots from an {@code EntityEquipmentPacket}; otherwise returns {@code packet} unchanged. */
    public static SendablePacket rewrite(SendablePacket packet) {
        if (!enabled) return packet;
        final ServerPacket serverPacket = PacketShapes.unwrapStateless(packet);
        if (!(serverPacket instanceof EntityEquipmentPacket equipment)) return packet;

        final Map<EquipmentSlot, ItemStack> equipments = equipment.equipments();
        final Map<EquipmentSlot, ItemStack> kept = new EnumMap<>(EquipmentSlot.class);
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipments.entrySet()) {
            if (!entry.getValue().isAir()) kept.put(entry.getKey(), entry.getValue());
        }
        // all-empty passes through: a single-slot AIR is a legit slot-clear
        if (kept.size() == equipments.size() || kept.isEmpty()) return packet;
        return new EntityEquipmentPacket(equipment.entityId(), kept);
    }
}
