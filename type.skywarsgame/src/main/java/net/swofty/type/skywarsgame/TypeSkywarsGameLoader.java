package net.swofty.type.skywarsgame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.attack.AttackSystem;
import io.github.term4.polyp.mechanics.attribute.AttributeSystem;
import io.github.term4.polyp.mechanics.blocking.BlockingSystem;
import io.github.term4.polyp.mechanics.consumable.ConsumableSystem;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.platform.fixes.Fixes18;
import io.github.term4.polyp.platform.fixes.FixesSystem;
import io.github.term4.polyp.presets.Preset;
import io.github.term4.polyp.vri.Vri;
import io.github.term4.polyp.vri.VriConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.orchestrator.GameHeartbeatProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.skywars.map.SkywarsMapsConfig;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.game.game.GameObject;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.redis.RedisOriginServer;
import net.swofty.type.generic.tab.EmptyTabModule;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.game.SkywarsGameStat;
import net.swofty.type.skywarsgame.item.SimpleInteractableItem;
import net.swofty.type.skywarsgame.item.SimpleInteractableItemHandler;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.swofty.type.generic.HypixelGenericLoader.getLoadedPlayers;

public class TypeSkywarsGameLoader implements HypixelTypeLoader {
    public static final int MAX_GAMES = 8;

    @Getter
    public static final List<SkywarsGame> games = new CopyOnWriteArrayList<>();

    @Getter
    public static final SimpleInteractableItemHandler itemHandler = new SimpleInteractableItemHandler();

    @Getter
    private static SkywarsMapsConfig mapsConfig;
    private static InstanceManager instanceManager;
    private static ExplosionSystem explosions;
    private static RegistryKey<@NotNull DimensionType> fullbrightDimension;
    private Gson gson;

    public static SkywarsGame getGameById(@Nullable String gameId) {
        if (gameId == null) return null;
        return games.stream()
                .filter(game -> game.getGameId().equals(gameId))
                .findFirst()
                .orElse(null);
    }

    public static SkywarsGame getPlayerGame(@NotNull Player player) {
        String gameId = player.getTag(Tag.String("gameId"));
        if (gameId == null) return null;
        return getGameById(gameId);
    }

    @SneakyThrows
    public static synchronized SkywarsGame createGame(@NotNull SkywarsMapsConfig.MapEntry entry,
                                                      @NotNull SkywarsGameType gameType) {
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(gameType, "gameType");
        if (!supportsGameType(entry, gameType)) {
            throw new IllegalArgumentException("Map does not support game type " + gameType);
        }
        if (games.size() >= MAX_GAMES) {
            return null;
        }
        InstanceContainer mapInstance = instanceManager.createInstanceContainer(fullbrightDimension);
        try {
            mapInstance.setChunkLoader(new PolarLoader(new File("./configuration/skywars/" + entry.getId() + ".polar").toPath()));
            mapInstance.setExplosionSupplier(explosions.supplier());
            SkywarsGame game = new SkywarsGame(entry, mapInstance, gameType);
            games.add(game);
            return game;
        } catch (Exception | Error exception) {
            instanceManager.unregisterInstance(mapInstance);
            throw exception;
        }
    }

    private static Component header() {
        return MiniMessage.miniMessage().deserialize("<aqua>You are playing on <bold><yellow>MC.HYPIXEL.NET</yellow></bold>");
    }

    private static Component footer(HypixelPlayer player) {
        Component start = Component.empty();
        SkywarsGame game = getPlayerGame(player);
        if (game != null) {
            SkywarsPlayer swPlayer = (SkywarsPlayer) player;
            start = start.append(MiniMessage.miniMessage().deserialize(
                    "<yellow>Kills: <gold>" + game.getGameStats().get(swPlayer.getUuid(), SkywarsGameStat.KILLS)
            )).appendNewline();
        }
        return start.append(Component.text("§aRanks, Boosters & MORE! §c§lSTORE.HYPIXEL.NET"));
    }

    @Override
    public ServerType getType() {
        return ServerType.SKYWARS_GAME;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        initializePolyp();
        gson = new GsonBuilder().create();
        instanceManager = MinecraftServer.getInstanceManager();
        fullbrightDimension = MinecraftServer.getDimensionTypeRegistry().register(
                "fullbright_skywars",
                DimensionType.builder().ambientLight(1f).setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, Color.WHITE).build()
        );

        Path skywarsDir = Path.of("./configuration/skywars");
        List<SkywarsMapsConfig.MapEntry> mapEntries = new ArrayList<>();

        try {
            File[] configFiles = skywarsDir.toFile().listFiles((dir, name) -> name.endsWith("_config.json"));
            if (configFiles == null || configFiles.length == 0) {
                Logger.warn("No map config files (*_config.json) found in {}, SkyWars games will not be created automatically", skywarsDir.toAbsolutePath());
            } else {
                for (File configFile : configFiles) {
                    try (InputStream in = Files.newInputStream(configFile.toPath())) {
                        byte[] bytes = in.readAllBytes();
                        String json = new String(bytes, StandardCharsets.UTF_8);
                        SkywarsMapsConfig.MapEntry entry = gson.fromJson(json, SkywarsMapsConfig.MapEntry.class);
                        if (entry != null) {
                            mapEntries.add(entry);
                            Logger.info("Loaded map config: {}", entry.getName());
                        }
                    } catch (Exception e) {
                        Logger.error("Failed to load map config: {}", configFile.getName(), e);
                    }
                }

                mapsConfig = new SkywarsMapsConfig();
                mapsConfig.setMaps(mapEntries);

                for (SkywarsMapsConfig.MapEntry e : mapEntries) {
                    for (SkywarsGameType gameType : getSupportedTypes(e)) {
                        try {
                            createGame(e, gameType);
                        } catch (Exception exception) {
                            Logger.error(exception, "Failed to create SkyWars game for map {} and mode {}", e.getName(), gameType);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to scan for map config files", e);
        }

        MinecraftServer.getConnectionManager().setPlayerProvider((playerConnection, gameProfile) -> {
            SkywarsPlayer player = new SkywarsPlayer(playerConnection, gameProfile);

            UUID uuid = gameProfile.uuid();
            String username = gameProfile.name();

            ServerType originServer = RedisOriginServer.consume(uuid);
            if (originServer != null) {
                player.setOriginServer(originServer);
            }

            Logger.info("Received new player: " + username + " (" + uuid + ")");

            return player;
        });
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.skywarsgame.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command " + command.getCommand().getName());
            }
        });

        HypixelGenericLoader.loopThroughPackage("net.swofty.type.skywarsgame.item.impl", SimpleInteractableItem.class).forEach(itemHandler::add);

        SkywarsGameScoreboard.start();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            UUID uuid = HypixelConst.getServerUUID();
            String shortName = HypixelConst.getShortenedServerName();
            int maxPlayers = HypixelConst.getMaxPlayers();
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();

            List<GameObject> commonsGames = new ArrayList<>();
            for (SkywarsGame internalGame : TypeSkywarsGameLoader.getGames()) {
                GameObject commonsGame = new GameObject();
                commonsGame.setGameId(UUID.fromString(internalGame.getGameId()));
                commonsGame.setType(ServerType.SKYWARS_GAME);
                commonsGame.setMap(internalGame.getMapEntry().getName());
                commonsGame.setGameTypeName(internalGame.getGameType().name());
                commonsGame.setAcceptingJoins(internalGame.getState().isWaiting());

                List<UUID> playerUuids = new ArrayList<>();
                for (SkywarsPlayer player : internalGame.getPlayers()) {
                    playerUuids.add(player.getUuid());
                }
                commonsGame.setInvolvedPlayers(playerUuids);
                commonsGame.setDisconnectedPlayers(List.of());

                commonsGames.add(commonsGame);
            }

            List<GameHeartbeatProtocol.MapAdvertisement> mapAdvertisements = new ArrayList<>();
            if (mapsConfig != null && mapsConfig.getMaps() != null) {
                for (SkywarsMapsConfig.MapEntry entry : mapsConfig.getMaps()) {
                    if (entry == null) continue;
                    String mapId = entry.getId();
                    String mapName = entry.getName();
                    if (mapId == null && mapName == null) continue;
                    if (mapId == null) mapId = mapName;
                    if (mapName == null) mapName = mapId;

                    mapAdvertisements.add(new GameHeartbeatProtocol.MapAdvertisement(
                            mapId,
                            mapName,
                            getSupportedTypes(entry).stream().map(SkywarsGameType::name).toList()
                    ));
                }
            }

            int remainingGameSlots = Math.max(0, MAX_GAMES - TypeSkywarsGameLoader.getGames().size());

            var heartbeat = new GameHeartbeatProtocol.HeartbeatMessage(
                    uuid,
                    shortName,
                    getType(),
                    maxPlayers,
                    onlinePlayers,
                    commonsGames,
                    mapAdvertisements,
                    remainingGameSlots
            );
            new ProxyService(ServiceType.ORCHESTRATOR).handleRequest(heartbeat);
        }).delay(TaskSchedule.seconds(5)).repeat(TaskSchedule.seconds(1)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<HypixelPlayer> players = getLoadedPlayers();
            if (players.isEmpty()) return;
            for (HypixelPlayer player : players) {
                player.sendPlayerListHeaderAndFooter(header(), footer(player));
            }
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    public static List<SkywarsGameType> getSupportedTypes(@NotNull SkywarsMapsConfig.MapEntry entry) {
        Objects.requireNonNull(entry, "entry");
        if (entry.getConfiguration() == null || entry.getConfiguration().getTypes() == null) return List.of();

        return entry.getConfiguration().getTypes().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public static boolean supportsGameType(@NotNull SkywarsMapsConfig.MapEntry entry,
                                            @NotNull SkywarsGameType gameType) {
        return getSupportedTypes(entry).contains(gameType);
    }

    private static void initializePolyp() {
        Polyp polyp = Polyp.getInstance();
        polyp.installPlayerProvider = false;
        polyp.metaFix = false;
        polyp.init();
        polyp.profiles().setGlobal(Preset.HYPIXEL.profile().toBuilder()
                .set(MechanicsKeys.COMPAT, Compat18.config())
                .set(MechanicsKeys.FIXES, Fixes18.config())
                .build());
        AttackSystem.install(polyp);
        DamageSystem.install(polyp);
        KnockbackSystem.install(polyp);
        ProjectileSystem.install(polyp);
        AttributeSystem.install(polyp);
        ConsumableSystem.install(polyp);
        BlockingSystem.install(polyp);
        HungerSystem.install(polyp);
        FixesSystem.install(polyp);
        Vri.install(polyp, VriConfig.all());
        explosions = ExplosionSystem.install(polyp);
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.ORCHESTRATOR);
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return List.of(
                        new EmptyTabModule(),
                        new EmptyTabModule(),
                        new EmptyTabModule(),
                        new EmptyTabModule()
                );
            }
        };
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (_) -> new Pos(0, 100, 0, 0, 0),
                false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.skywarsgame.events",
                HypixelEventClass.class
        ).toList();
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.skywarsgame.events.custom",
                HypixelEventClass.class
        ).toList();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return List.of();
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return (List) HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.skywarsgame.redis.service",
                RedisMessageHandler.class
        ).toList();
    }

    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }

    @Override
    public List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of(SkywarsDataHandler.class);
    }
}
