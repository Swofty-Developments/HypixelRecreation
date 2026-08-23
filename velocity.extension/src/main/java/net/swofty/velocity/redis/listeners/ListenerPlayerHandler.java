package net.swofty.velocity.redis.listeners;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.RunEventProtocol;
import net.swofty.commons.protocol.objects.proxy.from.TeleportProtocol;
import net.swofty.commons.protocol.objects.proxy.to.PlayerHandlerProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.text.Text;
import net.swofty.redisapi.api.requests.DataRequest;
import net.swofty.velocity.SkyBlockVelocity;
import net.swofty.velocity.gamemanager.GameManager;
import net.swofty.velocity.gamemanager.TransferHandler;
import net.swofty.velocity.presence.PresencePublisher;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ListenerPlayerHandler implements RedisMessageHandler<
        PlayerHandlerProtocol.Request,
        PlayerHandlerProtocol.Response> {

    private static final PlayerHandlerProtocol.Response EMPTY = new PlayerHandlerProtocol.Response(Map.of(), true, null);

    @Override
    public RedisProtocol<PlayerHandlerProtocol.Request, PlayerHandlerProtocol.Response> protocol() {
        return new PlayerHandlerProtocol();
    }

    @Override
    public PlayerHandlerProtocol.Response handle(PlayerHandlerProtocol.Request message, RedisMessageContext context) {
        UUID uuid = UUID.fromString(message.uuid());
        PlayerHandlerProtocol.Action action = message.action();
        Map<String, Object> data = message.data() != null ? message.data() : Map.of();

        Optional<Player> potentialPlayer = SkyBlockVelocity.getServer().getPlayer(uuid);
        if (potentialPlayer.isEmpty()) {
            if (action == PlayerHandlerProtocol.Action.IS_ONLINE) {
                return new PlayerHandlerProtocol.Response(Map.of("isOnline", false), true, null);
            }
            return EMPTY;
        }
        if (action == PlayerHandlerProtocol.Action.IS_ONLINE) {
            Player player = potentialPlayer.get();
            publishPresence(player, true);
            return new PlayerHandlerProtocol.Response(Map.of("isOnline", true), true, null);
        }
        Player player = potentialPlayer.get();
        Optional<ServerConnection> potentialServer = player.getCurrentServer();

        switch (action) {
            case RESOLVE_TRANSFER -> {
                ServerType type = ServerType.valueOf((String) data.get("type"));
                if (!GameManager.hasType(type) || !GameManager.isAnyEmpty(type)) {
                    return new PlayerHandlerProtocol.Response(Map.of(), false,
                            "There are no " + StringUtility.toNormalCase(type.name()) + " servers available");
                }
                GameManager.GameServer server = net.swofty.velocity.gamemanager.BalanceConfigurations.getServerFor(player, type);
                if (server == null || !server.hasEmptySlots()) {
                    return new PlayerHandlerProtocol.Response(Map.of(), false, "No destination server available");
                }
                return new PlayerHandlerProtocol.Response(
                        Map.of("server_uuid", server.internalID().toString()), true, null);
            }
            case GET_SERVER -> {
                UUID playersServer = UUID.fromString(potentialServer.get().getServer().getServerInfo().getName());
                GameManager.GameServer serverInfo = GameManager.getFromUUID(playersServer);
                if (serverInfo == null) {
                    return EMPTY;
                }

                publishPresence(player, true);
                UnderstandableProxyServer ups = new UnderstandableProxyServer(
                    serverInfo.displayName(),
                    serverInfo.internalID(),
                    GameManager.getTypeFromRegisteredServer(serverInfo.registeredServer()),
                    serverInfo.registeredServer().getServerInfo().getAddress().getPort(),
                    serverInfo.registeredServer().getPlayersConnected().stream().map(Player::getUniqueId).toList(),
                    serverInfo.maxPlayers(),
                    serverInfo.shortDisplayName()
                );
                return new PlayerHandlerProtocol.Response(Map.of("server", ups.toJSON().toMap()), true, null);
            }
            case TRANSFER_WITH_UUID -> {
                UUID server = UUID.fromString((String) data.get("server_uuid"));
                GameManager.GameServer serverInfo = GameManager.getFromUUID(server);
                TransferHandler transferHandler = new TransferHandler(player);

                if (serverInfo == null) {
                    player.sendMessage(Text.of(
                        "<c>We encountered an issue while attempting to locate the server on the network. Please try again later."
                    ));
                    return new PlayerHandlerProtocol.Response(Map.of(), false,
                            "The destination server could not be located on the network");
                }

                player.sendMessage(Text.of("<7>Sending to server {}...", serverInfo.displayName()));

                if (!serverInfo.hasEmptySlots()) {
                    player.sendMessage(Text.of(
                        "<c>Attempted to connect to {}, but there are no empty slots available. Please try again later.",
                        serverInfo.displayName()
                    ));
                    return new PlayerHandlerProtocol.Response(Map.of(), false, "The destination server is full");
                }

                if (data.get("document") instanceof String document) {
                    JSONObject snapshot = new JSONObject(document);
                    JSONObject payload = new JSONObject()
                            .put("uuid", uuid.toString())
                            .put("account_document", snapshot.getString("account_document"));
                    if (snapshot.has("profile_document")) {
                        payload.put("profile_document", snapshot.getString("profile_document"));
                    }
                    var cacheResponse = new DataRequest(server.toString(), "player-transfer-data", payload).await().join();
                    if (cacheResponse.data() == null || !cacheResponse.data().optBoolean("cached")) {
                        return new PlayerHandlerProtocol.Response(Map.of(), false,
                                "The destination server did not acknowledge your data");
                    }
                }

                transferHandler.addToDisregard();
                transferHandler.transferTo(serverInfo.registeredServer())
                    .thenRun(transferHandler::removeFromDisregard);
                return new PlayerHandlerProtocol.Response(Map.of(), true, null);
            }
            case TRANSFER -> {
                ServerType type = ServerType.valueOf((String) data.get("type"));
                if (!GameManager.hasType(type)
                    || new TransferHandler(player).isInLimbo()
                    || !GameManager.isAnyEmpty(type)) {
                    player.sendMessage(Text.of(
                        "<c>Attempted to transfer to a {} server, but there are no empty slots available. Please try again later.",
                        StringUtility.toNormalCase(type.name())
                    ));
                    return EMPTY;
                }
                new TransferHandler(player).transferTo(type);
            }
            case LIMBO -> {
                TransferHandler transferHandler = new TransferHandler(player);
                String reason = data.containsKey("reason") ? (String) data.get("reason") : "";

                if ("AFK".equalsIgnoreCase(reason)) {
                    ServerType originType = resolveAfkOriginType(data, potentialServer);
                    transferHandler.sendToLimboFromAfk(originType).join();
                } else {
                    transferHandler.sendToLimbo().join();
                }
            }
            case TELEPORT -> {
                if (potentialServer.isEmpty()) {
                    return EMPTY;
                }
                UUID server = UUID.fromString(potentialServer.get().getServer().getServerInfo().getName());
                Number x = (Number) data.get("x");
                Number y = (Number) data.get("y");
                Number z = (Number) data.get("z");
                Number yaw = (Number) data.get("yaw");
                Number pitch = (Number) data.get("pitch");
                RedisClient.requestServer(server,
                    new TeleportProtocol(),
                    new TeleportProtocol.Request(uuid.toString(),
                        x.doubleValue(), y.doubleValue(), z.doubleValue(),
                        yaw.floatValue(), pitch.floatValue())).join();
            }
            case EVENT -> {
                if (potentialServer.isEmpty()) {
                    return EMPTY;
                }
                UUID server = UUID.fromString(potentialServer.get().getServer().getServerInfo().getName());
                RedisClient.requestServer(server,
                    new RunEventProtocol(),
                    new RunEventProtocol.Request(uuid.toString(),
                        (String) data.get("event"),
                        (String) data.get("data"))).join();
            }
            case MESSAGE -> {
                String messageToSend = (String) data.get("message");
                player.sendMessage(JSONComponentSerializer.json().deserialize(messageToSend));
            }
        }
        return EMPTY;
    }

    private ServerType resolveAfkOriginType(Map<String, Object> data, Optional<ServerConnection> potentialServer) {
        if (data.containsKey("origin-type")) {
            String rawOriginType = (String) data.get("origin-type");
            if (ServerType.isServerType(rawOriginType)) {
                ServerType parsedType = ServerType.valueOf(rawOriginType.toUpperCase());
                if (parsedType.name().endsWith("_LOBBY")) {
                    return parsedType;
                }
            }
        }

        ServerType currentType = potentialServer
            .map(connection -> GameManager.getTypeFromRegisteredServer(connection.getServer()))
            .orElse(null);
        if (currentType != null && currentType.name().endsWith("_LOBBY")) {
            return currentType;
        }

        return ServerType.PROTOTYPE_LOBBY;
    }

    private void publishPresence(Player player, boolean online) {
        try {
            Optional<ServerConnection> serverConn = player.getCurrentServer();
            var type = serverConn.map(conn -> GameManager.getTypeFromRegisteredServer(conn.getServer())).orElse(null);
            PresencePublisher.publish(player, online, serverConn.map(ServerConnection::getServer).orElse(null),
                type != null ? type.name() : null);
        } catch (Exception ignored) {
        }
    }
}
