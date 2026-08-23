package net.swofty.type.generic.packet.packets.client;

import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.swofty.type.generic.packet.HypixelPacketClientListener;
import net.swofty.type.generic.user.HypixelPlayer;

public class PacketListenerItemSwitch extends HypixelPacketClientListener {
    @Override
    public Class<? extends ClientPacket> getPacket() {
        return ClientHeldItemChangePacket.class;
    }

    @Override
    public void run(PlayerPacketEvent event, ClientPacket packet, HypixelPlayer player) {
        if (player.getOpenInventory() != null) {
            player.closeInventory();
            player.sendMessage("<c>You can't switch items while in an inventory!");
        }
    }

    @Override
    public boolean overrideMinestomProcessing() {
        return false;
    }
}
