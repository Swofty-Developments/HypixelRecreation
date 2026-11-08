package net.swofty.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientPunchPacket;
import net.swofty.anticheat.event.packet.AnimationPacket;
import net.swofty.anticheat.event.packet.SwoftyPacket;
import net.swofty.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

public class MinestomHandlerAnimationPacket
        extends LoaderPacketHandler<ClientPunchPacket> {

    @Override
    public SwoftyPacket buildSwoftyPacket(UUID uuid, ClientPunchPacket packet) {
        AnimationPacket.Hand hand = AnimationPacket.Hand.MAIN_HAND;

        return new AnimationPacket(uuid, hand);
    }

    @Override
    public ClientPunchPacket buildLoaderPacket(UUID uuid, SwoftyPacket packet) {
        return null;
    }

    @Override
    public Class<ClientPunchPacket> getHandledPacketClass() {
        return ClientPunchPacket.class;
    }
}
