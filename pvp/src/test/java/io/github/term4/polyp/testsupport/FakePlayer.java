package io.github.term4.polyp.testsupport;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A fully joined player over a packet-capturing connection, for tests inside the shared {@link HeadlessServerTest}
 * process - Minestom's {@code TestConnection} spins its own {@code Env} server, which clashes with the one-per-JVM
 * harness. Join flow mirrors {@code TestConnectionImpl}, going through the ConnectionManager so the installed
 * OptimizedPlayer provider applies.
 */
public final class FakePlayer {

    public final Player player;
    public final List<SendablePacket> sent = new CopyOnWriteArrayList<>();

    private FakePlayer(Player player) { this.player = player; }

    public static FakePlayer connect(@NotNull Instance instance, @NotNull Pos pos, @NotNull String name) {
        var holder = new FakePlayer[1];
        var connection = new PlayerConnection() {
            @Override public void sendPacket(@NotNull SendablePacket packet) { holder[0].sent.add(packet); }
            @Override public SocketAddress getRemoteAddress() { return new InetSocketAddress("localhost", 25565); }
            @Override public void disconnect() {}
        };
        var manager = MinecraftServer.getConnectionManager();
        CompletableFuture<Void> joined = new CompletableFuture<>();
        // createPlayer asserts it runs on a virtual thread (the real login path), so the whole join runs on one
        Thread.startVirtualThread(() -> {
            Player player = manager.createPlayer(connection, new GameProfile(UUID.randomUUID(), name));
            holder[0] = new FakePlayer(player);
            player.eventNode().addListener(AsyncPlayerConfigurationEvent.class, e -> {
                e.setSpawningInstance(instance);
                e.getPlayer().setRespawnPoint(pos);
            });
            manager.doConfiguration(player, false);
            manager.transitionConfigToPlay(player);
            joined.complete(null);
        });
        joined.join();
        connection.setClientState(ConnectionState.PLAY);
        connection.setServerState(ConnectionState.PLAY);
        manager.updateWaitingPlayers();
        return holder[0];
    }

    /** In send order, with broadcast CachedPacket wrappers unwrapped. */
    public <T extends SendablePacket> List<T> sent(Class<T> type) {
        return sent.stream()
                .map(p -> (SendablePacket) SendablePacket.extractServerPacket(ConnectionState.PLAY, p))
                .filter(type::isInstance).map(type::cast).toList();
    }

    /** Spawn/metadata/velocity/teleport packets for {@code entityId}, in send order, CachedPacket unwrapped. */
    public List<ServerPacket> packetsFor(int entityId) {
        return sent.stream()
                .map(p -> SendablePacket.extractServerPacket(ConnectionState.PLAY, p))
                .filter(p -> p instanceof SpawnEntityPacket s && s.entityId() == entityId
                        || p instanceof EntityMetaDataPacket m && m.entityId() == entityId
                        || p instanceof EntityVelocityPacket v && v.entityId() == entityId
                        || p instanceof EntityTeleportPacket t && t.entityId() == entityId).toList();
    }
}
