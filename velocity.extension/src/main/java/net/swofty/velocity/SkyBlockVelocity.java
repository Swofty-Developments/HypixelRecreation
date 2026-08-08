package net.swofty.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.AwaitingEventExecutor;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.viaversion.viabackwards.ViaBackwardsPlatformImpl;
import com.viaversion.viarewind.ViaRewindPlatformImpl;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import io.netty.channel.Channel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.config.Settings;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.*;
import net.swofty.commons.protocol.objects.punishment.GetActivePunishmentProtocol;
import net.swofty.commons.punishment.*;
import net.swofty.commons.redis.ProxyHeartbeat;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.redis.RedisEndpoint;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.proxyapi.ProxyService;
import net.swofty.redisapi.api.RedisAPI;
import net.swofty.velocity.command.*;
import net.swofty.velocity.data.AuthenticationDatabase;
import net.swofty.velocity.data.CoopDatabase;
import net.swofty.velocity.data.ProfilesDatabase;
import net.swofty.velocity.data.UserDatabase;
import net.swofty.velocity.gamemanager.BalanceConfiguration;
import net.swofty.velocity.gamemanager.BalanceConfigurations;
import net.swofty.velocity.gamemanager.GameManager;
import net.swofty.velocity.gamemanager.TransferHandler;
import net.swofty.velocity.packet.listener.PlayerMovementListener;
import net.swofty.velocity.presence.PresencePublisher;
import net.swofty.velocity.redis.RedisHandlerRegistry;
import net.swofty.velocity.redis.listeners.ListenerStaffChat;
import net.swofty.velocity.redis.listeners.ListenerStorePurchaseFulfillment;
import net.swofty.velocity.testflow.TestFlowManager;
import net.swofty.velocity.viaversion.SkyBlockPlatform;
import net.swofty.velocity.viaversion.SkyBlockPlatformLoader;
import net.swofty.velocity.viaversion.injector.SkyBlockViaInjector;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(
    id = "skyblock",
    name = "SkyBlock",
    version = "1.0",
    description = "SkyBlock plugin for Velocity",
    authors = {"Swofty"}
)
public class SkyBlockVelocity {
    @Getter
    private static ProxyServer server = null;
    @Getter
    private static SkyBlockVelocity plugin;
    @Getter
    private final PluginContainer pluginContainer;
    @Getter
    private final org.slf4j.Logger logger;
    @Getter
    private final Path dataDirectory;
    @Getter
    private static RegisteredServer limboServer;
    @Getter
    private static boolean supportCrossVersion = false;
    @Inject
    private ProxyServer proxy;

    @Getter
    private static final Set<UUID> unauthenticated = ConcurrentHashMap.newKeySet();

    @Inject
    public SkyBlockVelocity(ProxyServer tempServer, org.slf4j.Logger logger, PluginContainer pluginContainer, @DataDirectory Path dataDirectory) {
        plugin = this;
        server = tempServer;
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;

        Settings.LimboSettings limbo = ConfigProvider.settings().getLimbo();
        limboServer = server.registerServer(new ServerInfo("limbo", new InetSocketAddress(limbo.getHostName(), limbo.getPort())));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server = proxy;
        supportCrossVersion = ConfigProvider.settings().getIntegrations().isViaVersion();

        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, this.pluginContainer, this.logger, this.dataDirectory));
        PacketEvents.getAPI().getSettings().checkForUpdates(false);
        PacketEvents.getAPI().load();

        EventManager events = PacketEvents.getAPI().getEventManager();
        events.registerListener(new PlayerMovementListener(), PacketListenerPriority.NORMAL);

        PacketEvents.getAPI().init();

        if (supportCrossVersion) {
            ViaManagerImpl.initAndLoad(
                new SkyBlockPlatform(dataDirectory.toFile()),
                new SkyBlockViaInjector(),
                new ViaCommandHandler(false),
                new SkyBlockPlatformLoader(),
                ViaBackwardsPlatformImpl::new,
                ViaRewindPlatformImpl::new
            );
        }

        // Register packets
        server.getEventManager().register(this, PostLoginEvent.class,
            (AwaitingEventExecutor<PostLoginEvent>) postLoginEvent -> EventTask.withContinuation(continuation -> {
                // injectPlayer(postLoginEvent.getPlayer());
                TestFlowManager.handlePlayerJoin(postLoginEvent.getPlayer().getUsername());
                PresencePublisher.publish(postLoginEvent.getPlayer(), true, (String) null, null);

                // Broadcast staff join notification (servers will check if player is staff)
                ListenerStaffChat.broadcastStaffJoin(postLoginEvent.getPlayer().getUniqueId());

                continuation.resume();
            }));
        server.getEventManager().register(this, PermissionsSetupEvent.class,
            (AwaitingEventExecutor<PermissionsSetupEvent>) permissionsEvent -> EventTask.withContinuation(continuation -> {
                permissionsEvent.setProvider(_ -> PermissionFunction.ALWAYS_FALSE);
                continuation.resume();
            }));
        server.getEventManager().register(this, DisconnectEvent.class, PostOrder.LAST,
            (AwaitingEventExecutor<DisconnectEvent>) disconnectEvent ->
                disconnectEvent.getLoginStatus() == DisconnectEvent.LoginStatus.CONFLICTING_LOGIN
                    ? null
                    : EventTask.async(() -> {
                    // Broadcast staff leave notification (servers will check if player is staff)
                    ListenerStaffChat.broadcastStaffLeave(disconnectEvent.getPlayer().getUniqueId());

                    // Handle test flow player leave
                    TestFlowManager.handlePlayerLeave(disconnectEvent.getPlayer().getUsername());
                    PresencePublisher.publish(disconnectEvent.getPlayer(), false, (String) null, null);
                    removePlayer(disconnectEvent.getPlayer());
                })
        );

        server.getEventManager().register(this, ServerConnectedEvent.class,
            (AwaitingEventExecutor<ServerConnectedEvent>) serverConnectedEvent ->
                EventTask.async(() -> {
                    RegisteredServer newServer = serverConnectedEvent.getServer();
                    var type = GameManager.getTypeFromRegisteredServer(newServer);
                    PresencePublisher.publish(serverConnectedEvent.getPlayer(), true, newServer, type != null ? type.name() : null);
                }));

        server.getScheduler().buildTask(SkyBlockVelocity.getPlugin(), () -> {
            server.getAllPlayers().forEach(player -> {
                var current = player.getCurrentServer();
                var type = current.map(conn -> GameManager.getTypeFromRegisteredServer(conn.getServer())).orElse(null);
                PresencePublisher.publish(player, true, current.map(ServerConnection::getServer).orElse(null),
                    type != null ? type.name() : null);
            });
        }).repeat(Duration.ofSeconds(10)).schedule();

        // Register commands
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta statusCommandMeta = commandManager.metaBuilder("serverstatus")
            .aliases("status")
            .plugin(this)
            .build();

        CommandMeta protocolVersionMeta = commandManager.metaBuilder("protocolversion")
            .aliases("protocol")
            .plugin(this)
            .build();

        CommandMeta limboCommandMeta = commandManager.metaBuilder("limbo")
            .plugin(this)
            .build();

        CommandMeta loginCommandMeta = commandManager.metaBuilder("login")
            .plugin(this)
            .build();

        CommandMeta registerCommandMeta = commandManager.metaBuilder("register")
            .plugin(this)
            .build();

        CommandMeta lobbyCommandMeta = commandManager.metaBuilder("lobby")
            .plugin(this)
            .build();

        commandManager.register(statusCommandMeta, new ServerStatusCommand());
        commandManager.register(protocolVersionMeta, new ProtocolVersionCommand());
        commandManager.register(limboCommandMeta, new LimboCommand());
        commandManager.register(loginCommandMeta, new LoginCommand());
        commandManager.register(registerCommandMeta, new RegisterCommand());
        commandManager.register(lobbyCommandMeta, new LobbyCommand());

        // Handle database
        new ProfilesDatabase("_placeHolder").connect(ConfigProvider.settings().getMongodb());
        new AuthenticationDatabase(new UUID(0, 0)).connect(ConfigProvider.settings().getMongodb());
        UserDatabase.connect(ConfigProvider.settings().getMongodb());
        CoopDatabase.connect(ConfigProvider.settings().getMongodb());
        ListenerStorePurchaseFulfillment.startRankExpirationReconciler();

        // Setup Redis
        RedisAPI.generateInstance(ConfigProvider.settings().getRedisUri());
        RedisAPI.getInstance().setFilterId("proxy");
        RedisClient.identify(RedisEndpoint.proxy());

        ProxyHeartbeat.init(ConfigProvider.settings().getRedisUri());
        server.getScheduler().buildTask(SkyBlockVelocity.getPlugin(), ProxyHeartbeat::beat)
            .repeat(Duration.ofSeconds(2)).schedule();
        loopThroughPackage("net.swofty.velocity.redis.listeners", RedisMessageHandler.class)
            .forEach(RedisHandlerRegistry::register);
        RedisProtocol<?, ?>[] fromProxyProtocols = {
            new TeleportProtocol(), new PlayerSwitchedProtocol(),
            new DoesServerHaveIslandProtocol(), new RefreshCoopDataProtocol(),
            new RunEventProtocol(), new PingServerProtocol(),
            new GivePlayersOriginTypeProtocol(), new BroadcastStaffChatProtocol()
        };
        for (RedisProtocol<?, ?> protocol : fromProxyProtocols) {
            RedisClient.registerResponseProtocol(protocol);
        }
        loopThroughPackage("net.swofty.commons.protocol.objects", RedisProtocol.class)
            .filter(obj -> !obj.getClass().getPackageName().startsWith("net.swofty.commons.protocol.objects.proxy"))
            .forEach(RedisClient::registerResponseProtocol);
        RedisAPI.getInstance().startListeners();

        // Setup GameManager
        GameManager.loopServers(server);
    }

    private boolean checkPunished(Player player) {
        try {
            ProxyService service = new ProxyService(ServiceType.PUNISHMENT);

            CompletableFuture<?> banFuture = service.handleRequest(
                new GetActivePunishmentProtocol.GetActivePunishmentMessage(player.getUniqueId(), PunishmentType.BAN.name()));
            CompletableFuture<?> muteFuture = service.handleRequest(
                new GetActivePunishmentProtocol.GetActivePunishmentMessage(player.getUniqueId(), PunishmentType.MUTE.name()));

            CompletableFuture.allOf(banFuture, muteFuture).orTimeout(3, TimeUnit.SECONDS).join();

            Object banResult = banFuture.join();
            if (banResult instanceof GetActivePunishmentProtocol.GetActivePunishmentResponse(
                boolean found1, String type1, String id, PunishmentReason reason1,
                long at, List<PunishmentTag> tags1, boolean success1, String error1
            ) && found1) {
                ActivePunishment punishment = new ActivePunishment(
                    type1, id, reason1, at, tags1);
                player.disconnect(PunishmentMessages.banMessage(punishment));
                return true;
            }

            Object muteResult = muteFuture.join();
            if (muteResult instanceof GetActivePunishmentProtocol.GetActivePunishmentResponse(
                boolean found, String type, String banId, PunishmentReason reason,
                long expiresAt, List<PunishmentTag> tags, boolean success, String error
            ) && found) {
                ActivePunishment punishment = new ActivePunishment(
                    type, banId, reason, expiresAt, tags);
                player.sendMessage(PunishmentMessages.muteMessage(punishment));
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Subscribe
    public EventTask onPlayerJoin(PlayerChooseInitialServerEvent event) {
        return EventTask.async(() -> {
            final Player player = event.getPlayer();

            if (checkPunished(player)) {
                return;
            }

            if (ConfigProvider.settings().isRequireAuth()) {
                unauthenticated.add(player.getUniqueId());
                event.setInitialServer(limboServer);

                player.sendPlainMessage("§aHey! You need to authenticate your account to play on this server.");
                player.sendPlainMessage("§aAll passwords are encrypted using modern standards, so you're safe!");
                player.sendPlainMessage(" ");

                AuthenticationDatabase.AuthenticationData data = new AuthenticationDatabase(player.getUniqueId()).getAuthenticationData();
                if (data == null) {
                    player.sendPlainMessage("§eYou must first register to play this server!");
                    player.sendPlainMessage("§eIn the Minecraft chat, type §6/register <password> <password>§e.");
                } else {
                    player.sendPlainMessage("§eIn the Minecraft chat, type §6/login <password>§e.");
                }

                server.getScheduler().buildTask(SkyBlockVelocity.getPlugin(), () -> {
                    if (player.isActive() && unauthenticated.contains(player.getUniqueId())) {
                        player.disconnect(Component.text("§cYou have been kicked for not authenticating your account."));
                    }
                }).delay(Duration.ofSeconds(30)).schedule();
                return;
            }

            if (!GameManager.hasType(ServerType.PROTOTYPE_LOBBY) || !GameManager.isAnyEmpty(ServerType.PROTOTYPE_LOBBY)) {
                player.disconnect(
                    Component.text("§cThere are no Prototype Lobby servers available at the moment.")
                );
                return;
            }

            List<GameManager.GameServer> gameServers = GameManager.getFromType(ServerType.PROTOTYPE_LOBBY);
            if (TestFlowManager.isPlayerInTestFlow(player.getUsername())) {
                TestFlowManager.ProxyTestFlowInstance instance = TestFlowManager.getTestFlowForPlayer(player.getUsername());
                player.sendPlainMessage("§7You are currently in test flow " + instance.getName() + ".");
                player.sendPlainMessage("§7Servers involved include " + instance.getGameServers().stream().map(GameManager.GameServer::displayName).collect(Collectors.joining(", ")));
                player.sendPlainMessage("§7We are expecting " + instance.getTotalExpectedServers() + " servers to instantiate.");
                player.sendPlainMessage("§7Test flow has been running for " + instance.getUptime() / 1000 + " seconds.");

                gameServers.removeIf(server -> {
                    TestFlowManager.ProxyTestFlowInstance testFlowInstance = TestFlowManager.getFromServerUUID(
                        server.internalID()
                    );

                    return testFlowInstance == null || !instance.hasServer(server.internalID());
                });
            } else {
                gameServers.removeIf(server -> {
                    TestFlowManager.ProxyTestFlowInstance testFlowInstance = TestFlowManager.getFromServerUUID(
                        server.internalID()
                    );

                    return testFlowInstance != null;
                });
            }

            if (gameServers.isEmpty()) {
                player.disconnect(
                    Component.text("§cThere are no servers (type=PROTOTYPE_LOBBY) servers available at the moment.")
                );
                return;
            }

            List<BalanceConfiguration> configurations = BalanceConfigurations.CONFIGURATIONS.get(ServerType.BEDWARS_LOBBY);
            GameManager.GameServer toSendTo = gameServers.getFirst();

            for (BalanceConfiguration configuration : configurations) {
                GameManager.GameServer server = configuration.getServer(player, gameServers);
                if (server != null) {
                    toSendTo = server;
                    break;
                }
            }

            // TODO: Force Resource Pack
            event.setInitialServer(toSendTo.registeredServer());
        });
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        unauthenticated.remove(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onServerCrash(KickedFromServerEvent event) {
        if (checkPunished(event.getPlayer())) {
            return;
        }

        // Send the player to the limbo
        RegisteredServer originalServer = event.getServer();
        Component reason = event.getServerKickReason().orElse(Component.text(
            "§cYour connection to the server was lost. Please try again later."
        ));
        ServerType serverType = GameManager.getTypeFromRegisteredServer(originalServer);

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(
            limboServer,
            null
        ));

        TransferHandler transferHandler = new TransferHandler(event.getPlayer());

        CompletableFuture.delayedExecutor(GameManager.SLEEP_TIME + 300, TimeUnit.MILLISECONDS)
            .execute(() -> {
                // Determine if the registeredServer disconnect was due to a crash —
                // if it was, send the player to another server of that type;
                // otherwise disconnect them with the original reason.
                try {
                    ServerType serverTypeToTry = serverType;
                    if (!GameManager.hasType(serverTypeToTry) || !GameManager.isAnyEmpty(serverTypeToTry)) {
                        serverTypeToTry = ServerType.PROTOTYPE_LOBBY;
                    }

                    GameManager.GameServer server = BalanceConfigurations.getServerFor(event.getPlayer(), serverTypeToTry);
                    if (server != null && server.registeredServer().equals(originalServer)) {
                        serverTypeToTry = ServerType.PROTOTYPE_LOBBY;
                        server = BalanceConfigurations.getServerFor(event.getPlayer(), serverTypeToTry);
                        if (server != null && server.registeredServer().equals(originalServer)) {
                            server = null;
                        }
                    }
                    if (server == null) {
                        transferHandler.forceRemoveFromLimbo();
                        event.getPlayer().disconnect(reason);
                        return;
                    }
                    transferHandler.transferTo(server.registeredServer());

                    if (!serverTypeToTry.isSkyBlock()) {
                        event.getPlayer().sendPlainMessage("§cAn exception occurred in your connection, so you were put into the Prototype Lobby.");
                    } else {
                        event.getPlayer().sendPlainMessage("§cAn exception occurred in your connection, so you were put into another SkyBlock server.");
                    }
                    event.getPlayer().sendPlainMessage("§7Sending to server " + server.displayName() + "...");
                } catch (Exception e) {
                    Logger.getAnonymousLogger().log(Level.SEVERE, "An exception occurred while trying to transfer " + event.getPlayer().getUsername() + " to " + serverType, e);
                    transferHandler.forceRemoveFromLimbo();
                    event.getPlayer().disconnect(reason);
                }
            });
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        event.setPing(new ServerPing(
            event.getPing().getVersion(),
            null,
            Component.text("               §aHypixel Recreation §c[26.x]"),
            event.getPing().getFavicon().orElse(null)
        ));
    }

    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.MAXIMUM_VERSION.getProtocol()) && ConfigProvider.settings().getIntegrations().isViaVersion()) {
            String message = "\n" +
                "§6§l----------- §cServer Notice §6§l-----------\n" +
                "§cAlthough we do support versions prior to §6" + ProtocolVersion.MAXIMUM_VERSION.getVersionIntroducedIn() + "§c, the experience may be degraded.\n" +
                "§cIf you experience any issues, please test if it also occurs on §6" + ProtocolVersion.MAXIMUM_VERSION.getVersionIntroducedIn() + "§c before reporting it.\n" +
                "§6§l---------------------------------\n" +
                "\n";

            player.sendMessage(Component.text(message));
        }

        player.getCurrentServer().ifPresent(connection -> {
            if (connection.getServer() == limboServer) {
                if (unauthenticated.contains(player.getUniqueId())) return;

                player.sendMessage(Component.text("§cYou were spawned in Limbo."));
                player.sendMessage(Component.text("§b/limbo for more information."));
            }
        });
    }

    public static <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);

        return subTypes.stream()
            .map(subClass -> {
                try {
                    return clazz.cast(subClass.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                         InvocationTargetException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull);
    }

    private void injectPlayer(final Player player) {
        // final ConnectedPlayer connectedPlayer = (ConnectedPlayer) player;
        // Channel channel = connectedPlayer.getConnection().getChannel();
        // ChannelPipeline pipeline = channel.pipeline();
        // pipeline.addBefore(Connections.HANDLER, "PACKET", new PlayerChannelHandler(player));
    }

    private void removePlayer(final Player player) {
        final ConnectedPlayer connectedPlayer = (ConnectedPlayer) player;
        final Channel channel = connectedPlayer.getConnection().getChannel();

        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("PACKET");
        });
    }
}
