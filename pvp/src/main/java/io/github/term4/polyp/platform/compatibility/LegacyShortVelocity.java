package io.github.term4.polyp.platform.compatibility;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.server.BufferedPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Sends a 1.8-wire {@code SET_ENTITY_MOTION} (entity id + three shorts) to an Animatium client that decodes it natively
 * ({@code ServerFeature.SHORTS_VELOCITY}), byte-exact where the modern LpVec3 wire isn't.
 *
 * <p>No public seam emits custom bytes at a vanilla packet id ({@code SendablePacket} is sealed, {@link BufferedPacket} its
 * only raw-bytes member, registry class-keyed), so this frames the shorts via {@link PacketWriting} at the resolved id and
 * sends a {@link BufferedPacket} - which also dodges {@code PlayerPacketOutEvent}, so the send can't re-trip the rewrite.
 */
final class LegacyShortVelocity {

    private record Payload(int entityId, short vx, short vy, short vz) {
        static final NetworkBuffer.Type<Payload> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.VAR_INT, Payload::entityId,
                NetworkBuffer.SHORT, Payload::vx,
                NetworkBuffer.SHORT, Payload::vy,
                NetworkBuffer.SHORT, Payload::vz,
                Payload::new);
    }

    private static volatile int packetId = -1;

    private LegacyShortVelocity() {}

    static void send(@NotNull Player player, int entityId, short vx, short vy, short vz) {
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer(32, MinecraftServer.process());
        PacketWriting.writeFramedPacket(buffer, Payload.SERIALIZER, velocityPacketId(),
                new Payload(entityId, vx, vy, vz), MinecraftServer.getCompressionThreshold());
        player.sendPacket(new BufferedPacket(buffer, 0, buffer.writeIndex()));
    }

    /** {@code SET_ENTITY_MOTION}'s id: frame a reference packet uncompressed, then read the id back ({@code [3-byte length][id varint][payload]}). */
    private static int velocityPacketId() {
        int id = packetId;
        if (id >= 0) return id;
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer(32, MinecraftServer.process());
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, new EntityVelocityPacket(0, Vec.ZERO), 0);
        buffer.readIndex(3);
        id = buffer.read(NetworkBuffer.VAR_INT);
        packetId = id;
        return id;
    }
}
