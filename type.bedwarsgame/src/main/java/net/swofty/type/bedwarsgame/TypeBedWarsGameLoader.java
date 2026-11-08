package net.swofty.type.bedwarsgame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.SneakyThrows;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.SwingAnimationPacket;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
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
import net.swofty.proxyapi.ProxyService;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.text.Text;
import net.swofty.pvp.MinestomPvP;
import net.swofty.pvp.feature.CombatFeatureSet;
import net.swofty.pvp.feature.CombatFeatures;
import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.utils.CombatVersion;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItemHandler;
import net.swofty.type.bedwarsgame.shop.ShopManager;
import net.swofty.type.bedwarsgame.shop.TeamShopManager;
import net.swofty.type.bedwarsgame.shop.TrapManager;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameObject;
import net.swofty.type.game.game.GameState;
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
import java.util.stream.Stream;

import static net.swofty.pvp.feature.CombatFeatures.*;
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

    static CombatFeatureSet combatFeatures = CombatFeatures.empty().version(CombatVersion.LEGACY).addAll(List.of(
        VANILLA_ARMOR, VANILLA_ATTACK, VANILLA_CRITICAL, //VANILLA_SWEEPING,
        VANILLA_EQUIPMENT, VANILLA_BLOCK, VANILLA_ATTACK_COOLDOWN, VANILLA_ITEM_COOLDOWN,
        VANILLA_DAMAGE, VANILLA_EFFECT, VANILLA_ENCHANTMENT, VANILLA_EXPLOSION,
        VANILLA_EXPLOSIVE, VANILLA_FALL, VANILLA_FOOD, LEGACY_VANILLA_BLOCK,
        VANILLA_REGENERATION, VANILLA_KNOCKBACK, VANILLA_POTION, VANILLA_BOW,
        VANILLA_CROSSBOW, VANILLA_FISHING_ROD, VANILLA_MISC_PROJECTILE,
        VANILLA_PROJECTILE_ITEM, VANILLA_TRIDENT, VANILLA_SPECTATE,
        VANILLA_PLAYER_STATE, VANILLA_TOTEM//, VANILLA_DEATH_MESSAGE
    )).soundProvider((audience, original, x, y, z) -> {
        printHierarchy(audience.getClass());
        audience.playSound(original, x, y, z);
        // Try to get the game from the audience context
        BedWarsGame game = null;
        if (audience instanceof BedWarsPlayer player) {
            game = player.getGame();
        } else if (audience instanceof net.minestom.server.entity.Entity entity) {
            game = TypeBedWarsGameLoader.getGameByInstance(entity.getInstance());
        }
        if (game != null && game.getReplayManager().isRecording()) {
            game.getReplayManager().recordSound(original, x, y, z);
        }
    }).packetProvider((viewable, packet) -> {
        printHierarchy(viewable.getClass());
        viewable.sendPacketToViewersAndSelf(packet);
        BedWarsGame game = null;
        if (viewable instanceof BedWarsPlayer bwPlayer) {
            game = bwPlayer.getGame();
        } else if (viewable instanceof Entity entity) {
            game = TypeBedWarsGameLoader.getGameByInstance(entity.getInstance());
        }
        if (game == null || !game.getReplayManager().isRecording()) {
            return;
        }

        if (packet instanceof ParticlePacket particlePacket) {
            game.getReplayManager().recordParticle(particlePacket);
        } else if (packet instanceof EntityAnimationPacket animationPacket) {
            game.getReplayManager().recordEntityAnimation(animationPacket);
        } else if (packet instanceof SwingAnimationPacket swingPacket) {
            game.getReplayManager().recordSwingAnimation(swingPacket);
        } else if (packet instanceof DamageEventPacket damagePacket) {
            game.getReplayManager().recordDamageAnimation(damagePacket);
        }
    }).build();

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
        mapInstance.setExplosionSupplier(combatFeatures.get(FeatureType.EXPLOSION).getExplosionSupplier());

        BedWarsGame game = new BedWarsGame(entry, mapInstance, type);
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
        Text text = Text.empty();
        if (player.getGame() != null) {
            text = text.append("<b>Kills: <e>0 <b>Final Kills: <e>0 <b>Beds Broken: <e>0\n");
        }
        return text.append("<a>Ranks, Boosters & MORE! <c><l>STORE.HYPIXEL.NET");
    }

    @Override
    public ServerType getType() {
        return ServerType.BEDWARS_GAME;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
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
        MinecraftServer.getGlobalEventHandler().addChild(combatFeatures.createNode());

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
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.bedwarsgame.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command " + command.getCommand().getName() + " in class " + command.getClass().getSimpleName());
            }
        });
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.bedwarsgame.item.impl", SimpleInteractableItem.class).forEach(itemHandler::add);
        itemHandler.getShopBackedItems().forEach(shopManager::addInteractableItem);
        MinestomPvP.init();

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
                "net.swofty.type.bedwarsgame.game.v2.listener",
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
