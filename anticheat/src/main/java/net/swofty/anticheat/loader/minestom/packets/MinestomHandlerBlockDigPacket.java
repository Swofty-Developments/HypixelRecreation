package net.swofty.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.swofty.anticheat.event.packet.BlockDigPacket;
import net.swofty.anticheat.event.packet.SwoftyPacket;
import net.swofty.anticheat.loader.LoaderPacketHandler;
import net.swofty.anticheat.math.Pos;

import java.util.UUID;

public class MinestomHandlerBlockDigPacket
        extends LoaderPacketHandler<ClientPlayerActionPacket> {

    @Override
    public SwoftyPacket buildSwoftyPacket(UUID uuid, ClientPlayerActionPacket packet) {
        BlockDigPacket.Status status = mapStatus(packet.status());
        BlockDigPacket.Direction direction = mapDirection(packet.blockFace());
        Pos position = new Pos(
                packet.blockPosition().blockX(),
                packet.blockPosition().blockY(),
                packet.blockPosition().blockZ()
        );

        return new BlockDigPacket(uuid, status, position, direction, packet.sequence());
    }

    @Override
    public ClientPlayerActionPacket buildLoaderPacket(UUID uuid, SwoftyPacket packet) {
        return null;
    }

    @Override
    public Class<ClientPlayerActionPacket> getHandledPacketClass() {
        return ClientPlayerActionPacket.class;
    }

    private BlockDigPacket.Status mapStatus(ClientPlayerActionPacket.Status status) {
        return switch (status) {
            case CHANGED_DIGGING_DIRECTION -> BlockDigPacket.Status.CHANGED_DIGGING_DIRECTION;
            case STARTED_DIGGING -> BlockDigPacket.Status.STARTED_DIGGING;
            case CANCELLED_DIGGING -> BlockDigPacket.Status.CANCELLED_DIGGING;
            case FINISHED_DIGGING -> BlockDigPacket.Status.FINISHED_DIGGING;
            case DROP_ITEM_STACK -> BlockDigPacket.Status.DROP_ITEM_STACK;
            case DROP_ITEM -> BlockDigPacket.Status.DROP_ITEM;
            case UPDATE_ITEM_STATE -> BlockDigPacket.Status.UPDATE_HELD_ITEM;
            case SWAP_ITEM_HAND -> BlockDigPacket.Status.SWAP_ITEM_IN_HAND;
            case STAB -> BlockDigPacket.Status.STAB;
        };
    }

    private BlockDigPacket.Direction mapDirection(net.minestom.server.instance.block.BlockFace face) {
        return switch (face) {
            case BOTTOM -> BlockDigPacket.Direction.DOWN;
            case TOP -> BlockDigPacket.Direction.UP;
            case NORTH -> BlockDigPacket.Direction.NORTH;
            case SOUTH -> BlockDigPacket.Direction.SOUTH;
            case WEST -> BlockDigPacket.Direction.WEST;
            case EAST -> BlockDigPacket.Direction.EAST;
        };
    }
}
