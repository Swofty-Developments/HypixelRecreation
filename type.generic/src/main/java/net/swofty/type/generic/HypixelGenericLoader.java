package net.swofty.type.generic;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.DataAPI;
import net.swofty.api.DataAPIImpl;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.data.SwoftyData;
import net.swofty.redisapi.api.RedisAPI;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextArgRenderers;
import net.swofty.proxyapi.PlayerTransferDataCache;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.achievement.AchievementStatisticsService;
import net.swofty.type.generic.block.BannerBlockHandler;
import net.swofty.type.generic.block.PlayerHeadBlockHandler;
import net.swofty.type.generic.block.SignBlockHandler;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.DataWriteQueue;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.data.GameDataHandlerRegistry;
import net.swofty.type.generic.data.domain.AccountDomain;
import net.swofty.type.generic.data.domain.GameDomain;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.handlers.*;
import net.swofty.type.generic.data.mongodb.AttributeDatabase;
import net.swofty.type.generic.data.mongodb.BedWarsStatsDatabase;
import net.swofty.type.generic.data.mongodb.MongoConnection;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.packet.HypixelPacketClientListener;
import net.swofty.type.generic.packet.HypixelPacketServerListener;
import net.swofty.type.generic.quest.QuestRegistry;
import net.swofty.type.generic.redis.RedisOriginServer;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.world.HypixelWorldLoader;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public record HypixelGenericLoader(HypixelTypeLoader loader) {
    public static final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    private static final int AUTOSAVE_INTERVAL_MINUTES = 5;
    private static final long AUTOSAVE_SPREAD_MILLIS = 30_000L;

    @Getter
    private static MinecraftServer server;

    @SneakyThrows
    public void initialize(MinecraftServer server) {
        HypixelGenericLoader.server = server;
        HypixelConst.setTypeLoader(loader);
        TextArgRenderers.register(HypixelPlayer.class, HypixelPlayer::getRankDisplayName);
        Text.registerTag(Rank.TAG, Rank::resolveTag);
        final boolean isSkyBlockType = loader.getType().isSkyBlock();
        final boolean isRavengardType = loader instanceof RavengardTypeLoader;
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Handle instances
        CustomWorlds mainInstance = loader.getMainInstance();
        if (mainInstance != null) {
            HypixelConst.setInstanceContainer(HypixelWorldLoader.load(loader, instanceManager));
        }
        HypixelConst.setEmptyInstance(instanceManager.createSharedInstance(instanceManager.createInstanceContainer()));
        HypixelConst.getEmptyInstance().setBlock(0, 99, 0, Block.BEDROCK);
        HypixelConst.setEventHandler(MinecraftServer.getGlobalEventHandler());

        // Register commands
        MinecraftServer.getCommandManager().setUnknownCommandCallback((sender, command) -> {
            // Large amount of Clients (such as Lunar) send a `/tip all` when joining
            // due to the scoreboard containing `hypixel.net`
            if (command.startsWith("tip ")) return;
            sender.sendMessage(Text.of("<f>Unknown command. Type \"/help\" for help. ('{}')", command));
        });
        loopThroughPackage("net.swofty.type.generic.command.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command {} in class {}",
                        command.getCommand().getName(), command.getClass().getSimpleName());
            }
        });

        // Register events
        loader.getTraditionalEvents().forEach(HypixelEventHandler::registerEventMethods);
        loader.getCustomEvents().forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("net.swofty.type.generic.event.actions", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("net.swofty.type.generic.gui.v2.event", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        // SkyBlockGenericLoader always runs after the generic loader, so if we are a SkyBlock server,
        // we will let that loader register the events
        if (!isSkyBlockType && !isRavengardType) {
            HypixelEventHandler.register(HypixelConst.getEventHandler());
        }

        /**
         * Register packet events assuming we are not a SkyBlock server, if we are
         * then we will just cache the events and register them in the SkyBlockGenericLoader
         */
        loopThroughPackage("net.swofty.type.generic.packet.packets.client", HypixelPacketClientListener.class)
                .forEach(HypixelPacketClientListener::cacheListener);
        loopThroughPackage("net.swofty.type.generic.packet.packets.server", HypixelPacketServerListener.class)
                .forEach(HypixelPacketServerListener::cacheListener);
        if (!isSkyBlockType && !isRavengardType) {
            HypixelPacketClientListener.register(HypixelConst.getEventHandler());
            HypixelPacketServerListener.register(HypixelConst.getEventHandler());
        }

        /**
         * Start generic tablist
         * SkyBlock has its own format so let SkyBlockGenericLoader handle it
         */
        if (!isSkyBlockType && !(loader.getType() == ServerType.BEDWARS_GAME)) {
            MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event ->
                    LAST_TICK.set(event.getTickMonitor()));
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                Collection<HypixelPlayer> players = getLoadedPlayers();
                if (players.isEmpty())
                    return;


                if (loader.headerFooterPerPlayer()) {
                    return;
                }

                loader.headerFooter().ifPresentOrElse(headerFooter -> {
                    Audiences.players().sendPlayerListHeaderAndFooter(headerFooter.getKey(), headerFooter.getValue());
                }, () -> {
                    final long ramUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() / (long) 1e6; // bytes to MB
                    final TickMonitor tickMonitor = LAST_TICK.get();
                    final double TPS = 1000 / tickMonitor.getTickTime();

                    if (TPS < 20) {
                        HypixelGenericLoader.getLoadedPlayers().forEach(player -> {
                            player.getLogHandler().debug("<c>Server TPS is below 20! TPS: {}", TPS);
                        });
                        Logger.error("Server TPS is below 20! TPS: " + TPS);
                    }

                    final Text newline = Text.literal("\n");
                    final Text header = Text.of("<b>You are playing on <e><l>MC.HYPIXEL.NET")
                            .append(newline)
                            .append(Text.of("<7>RAM USAGE: <8>{} MB", ramUsage))
                            .append(newline)
                            .append(Text.of("<7>TPS: <8>{}", TPS))
                            .append(newline);
                    final Text footer = newline
                            .append(Text.of("<a>Ranks, Boosters & MORE! <c><l>STORE.HYPIXEL.NET"));
                    Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
                });
            }).repeat(10, TimeUnit.SERVER_TICK).schedule();
        }

        /**
         * Start Tablist loop
         */
        loader.getTablistManager().runScheduler(MinecraftServer.getSchedulerManager());

        /**
         * Presence heartbeat to keep friend status fresh
         */
        net.swofty.type.generic.presence.PresenceHeartbeat.start();

        /**
         * Register databases
         */
        ConnectionString cs = new ConnectionString(ConfigProvider.settings().getMongodb());
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
        MongoClient mongoClient = MongoClients.create(settings);

        MongoConnection.connect(mongoClient);
        ProfilesDatabase.connect(mongoClient);
        AttributeDatabase.connect(mongoClient);
        UserDatabase.connect(mongoClient);
        BedWarsStatsDatabase.connect(mongoClient);

        ProxyPlayer.setTransferPreparation((playerUuid, targetServer) -> CompletableFuture.supplyAsync(() -> {
            HypixelPlayer player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUuid) instanceof HypixelPlayer hypixelPlayer
                    ? hypixelPlayer : null;
            if (player == null) return null;

            PlayerDataService.flushForTransfer(loader.getType(), player);
            return new org.json.JSONObject()
                    .put("account_document", PlayerTransferDataCache.encodeDocument(AccountDomain.transferDocument(player)))
                    .toString();
        }));

        ProxyPlayer.setTransferFailure(PlayerDataService::clearHandoff);

        // Initialize leaderboard service (uses Redis for O(log N) leaderboard operations)
        LeaderboardService.connect(ConfigProvider.settings().getRedisUri());

        // Load achievement and quest registries from YAML configuration
        AchievementRegistry.loadFromConfiguration();
        QuestRegistry.loadFromConfiguration();

        // Initialize achievement statistics service for unlock percentages
        AchievementStatisticsService.initialize();

        // Register game data handlers
        GameDataHandlerRegistry.register(new BedWarsDataHandler());
        GameDataHandlerRegistry.register(new PrototypeLobbyDataHandler());
        GameDataHandlerRegistry.register(new MurderMysteryDataHandler());
        GameDataHandlerRegistry.register(new SkywarsDataHandler());
        GameDataHandlerRegistry.register(new ReplayDataHandler());
        GameDataHandlerRegistry.register(new ArcadeDataHandler());

        PlayerDataService.register(new AccountDomain());
        for (Class<? extends GameDataHandler> gameHandlerClass : loader.getAdditionalDataHandlers()) {
            GameDataHandler gameHandler = GameDataHandlerRegistry.get(gameHandlerClass);
            if (gameHandler != null) PlayerDataService.register(new GameDomain(gameHandler));
        }

        startPlayerDataAutosave();
        registerPlayerDataShutdownFlush();

        // Register Block Handlers
        MinecraftServer.getBlockManager().registerHandler(PlayerHeadBlockHandler.KEY, PlayerHeadBlockHandler::new);
        MinecraftServer.getBlockManager().registerHandler(SignBlockHandler.KEY, SignBlockHandler::new);
        MinecraftServer.getBlockManager().registerHandler(BannerBlockHandler.KEY, BannerBlockHandler::new);

        // Register NPCs
        if (mainInstance != null) {
            loader.getNPCs().forEach(HypixelNPC::register);
        }

        // Update NPCs for loaded players
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            for (HypixelPlayer player : getLoadedPlayers()) {
                HypixelNPC.updateForPlayer(player);
            }
        }, TaskSchedule.tick(2), TaskSchedule.tick(2));

        // Register player provider given we aren't a SkyBlock server
        // If we are a SkyBlock server, we will handle the player provider in the SkyBlockGenericLoader
        if (!isSkyBlockType && !isRavengardType) {
            // Handle ConnectionManager
            MinecraftServer.getConnectionManager().setPlayerProvider((playerConnection, gameProfile) -> {
                HypixelPlayer player = new HypixelPlayer(playerConnection, gameProfile);

                UUID uuid = playerConnection.getPlayer().getUuid();
                String username = playerConnection.getPlayer().getUsername();

                ServerType originServer = RedisOriginServer.consume(uuid);

                if (originServer != null) {

                    player.setOriginServer(originServer);
                }

                Logger.info("Received new player: " + username + " (" + uuid + ")");
                return player;
            });
        }

    }

    private void startPlayerDataAutosave() {
        ServerType type = loader.getType();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<HypixelPlayer> players = getLoadedPlayers();
            if (players.isEmpty()) return;

            long spacingMillis = Math.max(1L, AUTOSAVE_SPREAD_MILLIS / players.size());
            Thread.startVirtualThread(() -> {
                for (HypixelPlayer player : players) {
                    if (!player.isOnline()) continue;
                    try {
                        PlayerDataService.saveAll(type, player);
                    } catch (Exception e) {
                        Logger.error(e, "Autosave failed for player {}", player.getUuid());
                    }
                    try {
                        Thread.sleep(spacingMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
        }).delay(AUTOSAVE_INTERVAL_MINUTES, TimeUnit.MINUTE)
                .repeat(AUTOSAVE_INTERVAL_MINUTES, TimeUnit.MINUTE)
                .schedule();
    }

    private void registerPlayerDataShutdownFlush() {
        ServerType type = loader.getType();
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            Logger.info("Flushing player data before shutdown...");
            for (Player online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                if (!(online instanceof HypixelPlayer player)) continue;
                try {
                    PlayerDataService.saveAndUnloadAll(type, player);
                } catch (Exception e) {
                    Logger.error(e, "Failed to flush data for player {} during shutdown", player.getUuid());
                }
            }

            DataWriteQueue.drainAll();

            flushDataApi(SwoftyData.account(), "account");
            flushDataApi(SwoftyData.profile(), "profile");

            try {
                RedisAPI redis = RedisAPI.getInstance();
                if (redis != null) redis.shutdown();
            } catch (Exception e) {
                Logger.error(e, "Failed to shut down the Redis API");
            }
            Logger.info("Player data flush complete");
        });
    }

    private static void flushDataApi(DataAPI api, String name) {
        try {
            if (api instanceof DataAPIImpl impl) impl.shutdown();
        } catch (Exception e) {
            Logger.error(e, "Failed to flush the {} data API during shutdown", name);
        }
    }

    public static List<HypixelPlayer> getLoadedPlayers() {
        List<HypixelPlayer> players = new ArrayList<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers()
                .stream()
                .filter(player -> {
                    try {
                        HypixelDataHandler.getUser(player);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                })
                .filter(player -> player.getInstance() != null)
                .forEach(player -> players.add((HypixelPlayer) player));
        return players;
    }

    public static @Nullable HypixelPlayer getFromUUID(UUID uuid) {
        return getLoadedPlayers().stream().filter(player -> player.getUuid().toString().equalsIgnoreCase(uuid.toString())).findFirst().orElse(null);
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
                        Logger.error(e, "Failed to instantiate {} while scanning {}",
                                subClass.getSimpleName(), packageName);
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }
}
