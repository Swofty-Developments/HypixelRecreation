package io.github.term4.polyp.mechanics.death;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Death sends no inventory clear, so the 1.8 "held item disappears on the death screen" residual (after the ViaRewind
 * status-3 fix) is a Via/client artifact.
 */
class DeathHeldItemProbeTest extends HeadlessServerTest {

    @Test
    void deathSendsNoInventoryClearToTheDyingPlayer() {
        FakePlayer p = FakePlayer.connect(instance, new Pos(300.5, 64, 300.5), "DeathProbe");
        try {
            p.player.setHeldItemSlot((byte) 0);
            p.player.getInventory().setItemStack(0, ItemStack.of(Material.DIAMOND_SWORD));
            p.sent.clear();

            p.player.kill();

            List<String> clears = new ArrayList<>();
            for (SendablePacket sp : p.sent) {
                var pk = SendablePacket.extractServerPacket(ConnectionState.PLAY, sp);
                if (pk instanceof SetSlotPacket s && s.itemStack().isAir()) clears.add("SetSlot win=" + s.windowId() + " slot=" + s.slot());
                else if (pk instanceof SetPlayerInventorySlotPacket s && s.itemStack().isAir()) clears.add("SetPlayerInvSlot slot=" + s.slot());
                else if (pk instanceof WindowItemsPacket s) clears.add("WindowItems win=" + s.windowId());
            }
            assertTrue(clears.isEmpty(), "death must not send an inventory clear (server side is clean; the held-item bug is Via): " + clears);
        } finally {
            p.player.remove();
        }
    }
}
