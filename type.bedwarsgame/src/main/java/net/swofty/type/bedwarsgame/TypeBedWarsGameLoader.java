package net.swofty.type.bedwarsgame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
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
import io.github.term4.polyp.presets.vanilla18.Explosion;
import io.github.term4.polyp.world.MechanicsWorld;
import lombok.Getter;
import lombok.SneakyThrows;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
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
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.protocol.objects.orchestrator.GameHeartbeatProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItemHandler;
import net.swofty.type.bedwarsgame.replay.BedWarsMechanicsWorld;
import net.swofty.type.bedwarsgame.replay.BedWarsReplayAdapter;
import net.swofty.type.bedwarsgame.shop.ShopManager;
import net.swofty.type.bedwarsgame.shop.TeamShopManager;
import net.swofty.type.bedwarsgame.shop.TrapManager;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameObject;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.replay.api.ReplayAdapterRegistry;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleCatalog;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.redis.RedisOriginServer;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.user.HypixelPlayer;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static net.swofty.type.generic.HypixelGenericLoader.getLoadedPlayers;

public class TypeBedWarsGameLoader implements HypixelTypeLoader {

    public static final int MAX_GAMES = 12;

    @Getter
    public static final List<BedWarsGame> games = new ArrayList<>();

    @Getter
    public static final ShopManager shopManager = new ShopManager();
    @Getter
    public static final TeamShopManager teamShopManager = new TeamShopManager();
    @Getter
    public static final TrapManager trapManager = new TrapManager();
    @Getter
    public static final SimpleInteractableItemHandler itemHandler = new SimpleInteractableItemHandler();
    @Getter
    private static final ReplayAdapterRegistry<Function<BedWarsGame, BedWarsReplayAdapter>> replayAdapters = new ReplayAdapterRegistry<>();

    public static final Tag<@NotNull Boolean> PLAYER_PLACED_TAG = Tag.Boolean("player_placed");
    public static final Tag<@NotNull Integer> ARMOR_LEVEL_TAG = Tag.Integer("armor_level");

    @Deprecated // please remember to remove this ARI
    public static void printHierarchy(Class<?> clazz) {
        if (clazz == null) return;

        System.out.println(clazz.getName());

        for (Class<?> iface : clazz.getInterfaces()) {
            System.out.println("  Implements: " + iface.getName());
        }

        printHierarchy(clazz.getSuperclass());
    }

    private static ExplosionSystem explosions;

    public static void explodeBedWars(Instance instance, Point center, float power, Entity source) {
        explosions.explode(instance, center, power, source);
    }

    @Getter
    private static BedWarsMapsConfig mapsConfig;
    private static InstanceManager instanceManager;
    private static RegistryKey<@NotNull DimensionType> fullbrightDimension;
    private Gson gson;

    @Nullable
    public static BedWarsGame getGameById(@NotNull String gameId) {
        return games.stream()
            .filter(game -> game.getGameId().equals(gameId))
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public static BedWarsGame getGameByInstance(@NotNull Instance instance) {
        return games.stream()
            .filter(game -> game.getInstance().equals(instance))
            .findFirst()
            .orElse(null);
    }

    @SneakyThrows
    public static synchronized BedWarsGame createGame(@NotNull BedWarsMapsConfig.MapEntry entry, @NotNull BedWarsGameType type) {
        if (games.size() >= MAX_GAMES) {
            return null;
        }
        InstanceContainer mapInstance = instanceManager.createInstanceContainer(fullbrightDimension);
        mapInstance.setChunkLoader(new PolarLoader(new File("./configuration/bedwars/" + entry.getId() + ".polar").toPath()));
        mapInstance.setExplosionSupplier(explosions.supplier());

        BedWarsGame game = new BedWarsGame(entry, mapInstance, type);
        mapInstance.setTag(MechanicsWorld.TAG, new BedWarsMechanicsWorld(mapInstance, game));
        games.add(game);
        return game;
    }

    private static List<BedWarsGameType> getSupportedTypes(BedWarsMapsConfig.MapEntry mapEntry) {
        BedWarsMapsConfig.MapEntry.MapConfiguration config = mapEntry.getConfiguration();
        if (config == null || config.getTypes() == null) {
            return List.of();
        }
        return config.getTypes().stream()
            .filter(Objects::nonNull)
            .flatMap(type -> Stream.concat(Stream.of(type), BedWarsGameType.getDreamTypes().stream()
                .filter(dreamType -> dreamType.getMapCompatibilityType() == type)))
            .distinct()
            .toList();
    }

    private static Text header() {
        return Text.of("<b>You are playing on <l><e>MC.HYPIXEL.NET</e></l>");
    }

    private static Text footer(BedWarsPlayer player) {
        Text start = Text.empty();
        if (player.getGame() != null) {
            start = start.append("<b>Kills: <e>0 <b>Final Kills: <e>0 <b>Beds Broken: <e>0\n");
        }
        return start.append("<a>Ranks, Boosters & MORE! <c><l>STORE.HYPIXEL.NET");
    }

    @Override
    public ServerType getType() {
        return ServerType.BEDWARS_GAME;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        replayAdapters.register(BedWarsReplayAdapter.GAME_TYPE, BedWarsReplayAdapter::new);
        initializePolyp();
        BedWarsCollectibleCatalog.initialize();
        gson = new GsonBuilder()
            .registerTypeAdapter(BedWarsGameType.class, (JsonDeserializer<BedWarsGameType>) (json, _, _) -> {
                String value = json.getAsString();
                BedWarsGameType gameType = BedWarsGameType.from(value);
                if (gameType == null) {
                    throw new JsonParseException("Unknown BedWars game type: " + value);
                }
                return gameType;
            })
            .create();
        instanceManager = MinecraftServer.getInstanceManager();
        fullbrightDimension = MinecraftServer.getDimensionTypeRegistry().register("fullbright", DimensionType.builder().ambientLight(1f).setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, Color.WHITE).build());

        Path mapsPath = Path.of("./configuration/bedwars/maps.json");
        if (!Files.exists(mapsPath)) {
            Logger.error("maps.json not found at {}", mapsPath.toAbsolutePath());
            return;
        }
        try (InputStream in = Files.newInputStream(mapsPath)) {
            byte[] bytes = in.readAllBytes();
            String json = new String(bytes, StandardCharsets.UTF_8);
            mapsConfig = gson.fromJson(json, BedWarsMapsConfig.class);
            if (mapsConfig == null || mapsConfig.getMaps() == null) {
                Logger.warn("maps.json was loaded but contained no BedWars maps");
            }
        } catch (Exception e) {
            Logger.error("Failed to load maps.json");
            e.printStackTrace();
            return;
        }

        MinecraftServer.getConnectionManager().setPlayerProvider((gameProfile, playerConnection) -> {
            BedWarsPlayer player = new BedWarsPlayer(gameProfile, playerConnection);

            UUID uuid = gameProfile.getPlayer().getUuid();
            String username = gameProfile.getPlayer().getUsername();

            if (RedisOriginServer.origin.containsKey(uuid)) {
                player.setOriginServer(RedisOriginServer.origin.get(uuid));
                RedisOriginServer.origin.remove(uuid);
            }

            Logger.info("Received new player: " + username + " (" + uuid + ")");

            return player;
        });
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.bedwarsgame.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command " + command.getCommand().getName() + " in class " + command.getClass().getSimpleName());
            }
        });
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.bedwarsgame.item.impl", SimpleInteractableItem.class).forEach(itemHandler::add);
        itemHandler.getShopBackedItems().forEach(shopManager::addInteractableItem);

        // heartbeat to orchestrator with supported maps and current load
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            UUID uuid = HypixelConst.getServerUUID();
            String shortName = HypixelConst.getShortenedServerName();
            int maxPlayers = HypixelConst.getMaxPlayers();
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();

            // Convert BedWarsGame objects to commons Game objects
            List<GameObject> commonsGames = new ArrayList<>();
            for (BedWarsGame game : TypeBedWarsGameLoader.getGames()) {
                GameObject commonsGame = new GameObject();
                commonsGame.setGameId(UUID.fromString(game.getGameId()));
                commonsGame.setType(ServerType.BEDWARS_GAME);
                commonsGame.setMap(game.getMapEntry().getName());
                commonsGame.setGameTypeName(game.getGameType().name());
                commonsGame.setAcceptingJoins(game.getState() == GameState.WAITING || game.getState() == GameState.COUNTDOWN);

                // Get involved players from the game
                List<UUID> playerUuids = new ArrayList<>();
                for (BedWarsPlayer player : game.getPlayers()) {
                    playerUuids.add(player.getUuid());
                }
                commonsGame.setInvolvedPlayers(playerUuids);

                // Add disconnected players for rejoin system
                commonsGame.setDisconnectedPlayers(game.getDisconnectedPlayerUuids());

                commonsGames.add(commonsGame);
            }

            List<GameHeartbeatProtocol.MapAdvertisement> mapAdvertisements = new ArrayList<>();
            if (mapsConfig != null && mapsConfig.getMaps() != null) {
                for (BedWarsMapsConfig.MapEntry entry : mapsConfig.getMaps()) {
                    String mapId = entry.getId();
                    String mapName = entry.getName();
                    if (mapId == null && mapName == null) {
                        continue;
                    }
                    if (mapId == null) {
                        mapId = mapName;
                    }
                    if (mapName == null) {
                        mapName = mapId;
                    }

                    List<String> supportedModes = getSupportedTypes(entry).stream()
                        .map(BedWarsGameType::name)
                        .toList();
                    mapAdvertisements.add(new GameHeartbeatProtocol.MapAdvertisement(mapId, mapName, supportedModes));
                }
            }

            int remainingGameSlots = Math.max(0, MAX_GAMES - TypeBedWarsGameLoader.getGames().size());

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
            if (players.isEmpty())
                return;
            for (HypixelPlayer player : players) {
                player.sendPlayerListHeaderAndFooter(header(), footer((BedWarsPlayer) player));
            }
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    private static void initializePolyp() {
        Polyp polyp = Polyp.getInstance();
        polyp.installPlayerProvider = false;
        polyp.metaFix = false;
        polyp.init();
        polyp.profiles().setGlobal(Preset.HYPIXEL_BEDWARS.profile().toBuilder()
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
        var explosionConfig = polyp.profiles().resolve(null, MechanicsKeys.EXPLOSION).toBuilder()
                .blockBreaking(Explosion.blockBreaking().toBuilder()
                        .breakRule((block, position, ignored) -> Boolean.TRUE.equals(block.getTag(PLAYER_PLACED_TAG)))
                        .build())
                .build();
        polyp.profiles().setGlobal(MechanicsKeys.EXPLOSION, explosionConfig);
        explosions = ExplosionSystem.install(polyp, explosionConfig);
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
                    new BedWarsGameTabListModule()
                );
            }
        };
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
            (_) -> new Pos(-39.5, 72, 0, -90, 0), // Spawn position
            false // Announce death messages
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return Stream.concat(
            HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.bedwarsgame.events",
                HypixelEventClass.class
            ),
            HypixelGenericLoader.loopThroughPackage(
                    "net.swofty.type.bedwarsgame.game.listener",
                HypixelEventClass.class
            )
        ).toList();
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return Stream.concat(HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.bedwarsgame.events.custom",
                HypixelEventClass.class
            ),
            HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.game.game.event",
                HypixelEventClass.class
            )).toList();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return HypixelGenericLoader.loopThroughPackage(
            "net.swofty.type.bedwarsgame.npcs",
            HypixelNPC.class
        ).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return (List) HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.bedwarsgame.redis.service",
                RedisMessageHandler.class
        ).toList();
    }

	@Override
	@SuppressWarnings("unchecked")
	public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
		return (List<RedisMessageHandler<?, ?>>) (List<?>) HypixelGenericLoader.loopThroughPackage(
				"net.swofty.type.bedwarsgame.redis",
				RedisMessageHandler.class
		).toList();
	}

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }

    @Override
    public List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of(BedWarsDataHandler.class);
    }
}
