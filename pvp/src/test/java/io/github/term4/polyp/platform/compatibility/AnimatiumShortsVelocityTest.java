package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.CapturingConnection;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.tracking.motion.LegacyVelocity;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.BufferedPacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.player.GameProfile;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The server-side Animatium SHORTS_VELOCITY chain, handshake to velocity rewrite. {@code PlayerPacketOutEvent} is
 * dispatched directly - it only fires on the socket write path, which a headless connection doesn't have.
 */
class AnimatiumShortsVelocityTest extends HeadlessServerTest {

    private static final Vec VELOCITY_BT = new Vec(0.55, 0.4, -0.1);

    private static OptimizedPlayer animatiumPlayer(CapturingConnection conn, boolean shortsKnob, boolean advertisesCapabilities) {
        OptimizedPlayer p = new OptimizedPlayer(conn, new GameProfile(UUID.randomUUID(), "AnimTest"));
        polyp.profiles().setPlayer(p, MechanicsProfile.builder()
                .set(MechanicsKeys.COMPAT, Compat18.config().toBuilder().nativeShortVelocity(shortsKnob).build())
                .build());
        EventDispatcher.call(new PlayerPluginMessageEvent(p, CompatAnimatium.INFO_CHANNEL, infoPayload(advertisesCapabilities)));
        return p;
    }

    /** Byte layout of the fork's {@code InfoPayloadPacket.write}; no capabilities field = upstream/old build. */
    private static byte[] infoPayload(boolean withCapabilities) {
        NetworkBuffer buf = NetworkBuffer.resizableBuffer(64, MinecraftServer.process());
        buf.write(NetworkBuffer.DOUBLE, 3.3);
        buf.write(NetworkBuffer.BOOLEAN, false);
        if (withCapabilities) {
            BitSet bits = new BitSet();
            for (AnimatiumFeature f : AnimatiumFeature.values()) bits.set(f.bit);
            buf.write(NetworkBuffer.BYTE_ARRAY, bits.toByteArray());
        }
        byte[] out = new byte[(int) buf.writeIndex()];
        buf.copyTo(0, out, 0, out.length);
        return out;
    }

    private static PlayerPacketOutEvent dispatchVelocity(OptimizedPlayer p) {
        var event = new PlayerPacketOutEvent(p, new EntityVelocityPacket(p.getEntityId(), VELOCITY_BT));
        EventDispatcher.call(event);
        return event;
    }

    @Test
    void rewritesVelocityToExactShorts() {
        var conn = new CapturingConnection();
        OptimizedPlayer p = animatiumPlayer(conn, true, true);

        assertTrue(p.compat().supports(AnimatiumFeature.SHORTS_VELOCITY));
        assertTrue(p.compat().handlesNatively(AnimatiumFeature.SHORTS_VELOCITY));
        assertTrue(featurePush(conn).get(AnimatiumFeature.SHORTS_VELOCITY.bit), "handshake must push the shorts bit");

        conn.sent.clear();
        assertTrue(dispatchVelocity(p).isCancelled(), "LpVec3 velocity must be cancelled for the shorts rewrite");

        BufferedPacket shorts = (BufferedPacket) conn.sent.stream()
                .filter(BufferedPacket.class::isInstance).findFirst().orElseThrow();
        NetworkBuffer buf = shorts.buffer();
        buf.readIndex(3); // 3-byte length, then with compression a data length (0 = uncompressed, always at 14 bytes)
        if (MinecraftServer.getCompressionThreshold() > 0) assertEquals(0, buf.read(NetworkBuffer.VAR_INT));
        buf.read(NetworkBuffer.VAR_INT); // packet id
        assertEquals(p.getEntityId(), buf.read(NetworkBuffer.VAR_INT));
        short[] sent = { buf.read(NetworkBuffer.SHORT), buf.read(NetworkBuffer.SHORT), buf.read(NetworkBuffer.SHORT) };
        assertArrayEquals(LegacyVelocity.wireShorts(VELOCITY_BT.mul(ServerFlag.SERVER_TICKS_PER_SECOND)), sent);
    }

    @Test
    void clientWithoutCapabilityFieldIsNeverSentShorts() {
        var conn = new CapturingConnection();
        OptimizedPlayer p = animatiumPlayer(conn, true, false);

        assertTrue(p.compat().isAnimatiumClient());
        assertFalse(p.compat().supports(AnimatiumFeature.SHORTS_VELOCITY));
        assertFalse(featurePush(conn).get(AnimatiumFeature.SHORTS_VELOCITY.bit), "wire feature must be gated off without advertised support");
        assertFalse(p.compat().handlesNatively(AnimatiumFeature.SHORTS_VELOCITY));

        conn.sent.clear();
        assertFalse(dispatchVelocity(p).isCancelled());
        assertTrue(conn.sent.isEmpty());
    }

    @Test
    void shortsRequireTheNativeShortVelocityKnob() {
        var conn = new CapturingConnection();
        OptimizedPlayer p = animatiumPlayer(conn, false, true);

        assertTrue(p.compat().supports(AnimatiumFeature.SHORTS_VELOCITY));
        assertFalse(p.compat().handlesNatively(AnimatiumFeature.SHORTS_VELOCITY));

        conn.sent.clear();
        assertFalse(dispatchVelocity(p).isCancelled());
        assertTrue(conn.sent.isEmpty());
    }

    private static BitSet featurePush(CapturingConnection conn) {
        PluginMessagePacket push = conn.sent.stream()
                .filter(PluginMessagePacket.class::isInstance).map(PluginMessagePacket.class::cast)
                .filter(m -> CompatAnimatium.SET_FEATURES_CHANNEL.equals(m.channel()))
                .reduce((a, b) -> b).orElseThrow();
        return BitSet.valueOf(push.data());
    }
}
