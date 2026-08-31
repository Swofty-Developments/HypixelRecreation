package net.swofty.type.murdermysterygame;

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
import io.github.term4.polyp.presets.hypixel.Tnt;
import io.github.term4.polyp.vri.Vri;
import io.github.term4.polyp.vri.VriConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import net.hollowcube.polar.PolarLoader;
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
import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.commons.murdermystery.map.MurderMysteryMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.commons.protocol.objects.orchestrator.GameHeartbeatProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.game.game.GameObject;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.data.handlers.MurderMysteryDataHandler;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.redis.RedisOriginServer;
import net.swofty.type.generic.tab.EmptyTabModule;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.item.SimpleInteractableItem;
import net.swofty.type.murdermysterygame.item.SimpleInteractableItemHandler;
import net.swofty.type.murdermysterygame.maphandler.MapHandler;
import net.swofty.type.murdermysterygame.maphandler.MapHandlerRegistry;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
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
import java.util.UUID;

import static net.swofty.type.generic.HypixelGenericLoader.getLoadedPlayers;

public class TypeMurderMysteryGameLoader implements HypixelTypeLoader {

    public static final int MAX_GAMES = 8;

    @Getter
    public static final List<Game> games = new ArrayList<>();

    @Getter
    public static final SimpleInteractableItemHandler itemHandler = new SimpleInteractableItemHandler();

    @Getter
    private static MurderMysteryMapsConfig mapsConfig;
    private static InstanceManager instanceManager;
    private static ExplosionSystem explosions;
    private static RegistryKey<@NotNull DimensionType> fullbrightDimension;
    private Gson gson;

    public static Game getGameById(@NotNull String gameId) {
        return games.stream()
                .filter(game -> game.getGameId().equals(gameId))
                .findFirst()
                .orElse(null);
    }

    public static Game getPlayerGame(@NotNull Player player) {
        String gameId = player.getTag(Tag.String("gameId"));
        if (gameId == null) return null;
        return getGameById(gameId);
    }

    @SneakyThrows
    public static Game createGame(MurderMysteryMapsConfig.MapEntry entry, MurderMysteryGameType gameType) {
        if (games.size() >= MAX_GAMES) {
            return null;
        }
        InstanceContainer mapInstance = instanceManager.createInstanceContainer(fullbrightDimension);
        mapInstance.setChunkLoader(new PolarLoader(new File("./configuration/murdermystery/" + entry.getId() + ".polar").toPath()));
        mapInstance.setExplosionSupplier(explosions.supplier());
        Game game = new Game(entry, mapInstance, gameType);
        games.add(game);
        return game;
    }

    private static Text header() {
        return Text.of("<b>You are playing on <l><e>MC.HYPIXEL.NET");
    }

    private static Text footer(HypixelPlayer player) {
        Text store = Text.of("<a>Ranks, Boosters & MORE! <c><l>STORE.HYPIXEL.NET");
        if (TypeMurderMysteryGameLoader.getPlayerGame(player) != null) {
            return Text.of("<c>Role: <e>??? <c>Kills: <e>0\n").append(store);
        }
        return store;
    }

    @Override
    public ServerType getType() {
        return ServerType.MURDER_MYSTERY_GAME;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        initializePolyp();
        gson = new GsonBuilder().create();
        instanceManager = MinecraftServer.getInstanceManager();
        fullbrightDimension = MinecraftServer.getDimensionTypeRegistry().register("fullbright", DimensionType.builder().ambientLight(1f).setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, Color.WHITE).build());

        Path mapsPath = Path.of("./configuration/murdermystery/maps.json");
        if (!Files.exists(mapsPath)) {
            Logger.warn("maps.json not found at {}, Murder Mystery games will not be created automatically", mapsPath.toAbsolutePath());
        } else {
            try (InputStream in = Files.newInputStream(mapsPath)) {
                byte[] bytes = in.readAllBytes();
                String json = new String(bytes, StandardCharsets.UTF_8);
                mapsConfig = gson.fromJson(json, MurderMysteryMapsConfig.class);
                if (mapsConfig != null && mapsConfig.getMaps() != null) {
                    for (MurderMysteryMapsConfig.MapEntry e : mapsConfig.getMaps()) {
                        createGame(e, MurderMysteryGameType.CLASSIC);
                    }
                }
            } catch (Exception e) {
                Logger.error("Failed to load maps.json", e);
            }
        }

        MinecraftServer.getConnectionManager().setPlayerProvider((playerConnection, gameProfile) -> {
            MurderMysteryPlayer player = new MurderMysteryPlayer(playerConnection, gameProfile);

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
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.murdermysterygame.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command " + command.getCommand().getName() + " in class " + command.getClass().getSimpleName());
            }
        });
        MurderMysteryGameScoreboard.start();
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.murdermysterygame.item.impl", SimpleInteractableItem.class).forEach(itemHandler::add);

        // Register map handlers
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.murdermysterygame.maphandler.impl", MapHandler.class).forEach(MapHandlerRegistry::register);

        // heartbeat to orchestrator with supported maps and current load
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            UUID uuid = HypixelConst.getServerUUID();
            String shortName = HypixelConst.getShortenedServerName();
            int maxPlayers = HypixelConst.getMaxPlayers();
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();

            List<GameObject> commonsGames = new ArrayList<>();
            for (Game internalGame : TypeMurderMysteryGameLoader.getGames()) {
                GameObject commonsGame = new GameObject();
                commonsGame.setGameId(UUID.fromString(internalGame.getGameId()));
                commonsGame.setType(ServerType.MURDER_MYSTERY_GAME);
                commonsGame.setMap(internalGame.getMapEntry().getName());
                commonsGame.setGameTypeName(internalGame.getGameType().name());

                List<UUID> playerUuids = new ArrayList<>();
                for (MurderMysteryPlayer player : internalGame.getPlayers()) {
                    playerUuids.add(player.getUuid());
                }
                commonsGame.setInvolvedPlayers(playerUuids);
                commonsGame.setDisconnectedPlayers(internalGame.getDisconnectedPlayerUuids());

                commonsGames.add(commonsGame);
            }

            var heartbeat = new GameHeartbeatProtocol.HeartbeatMessage(
                    uuid,
                    shortName,
                    getType(),
                    maxPlayers,
                    onlinePlayers,
                    commonsGames
            );
            new ProxyService(ServiceType.ORCHESTRATOR).handleRequest(heartbeat);
        }).delay(TaskSchedule.seconds(5)).repeat(TaskSchedule.seconds(1)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<HypixelPlayer> players = getLoadedPlayers();
            if (players.isEmpty())
                return;
            for (HypixelPlayer player : players) {
                player.sendPlayerListHeaderAndFooter(header(), footer(player));
            }
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    private static void initializePolyp() {
        Polyp polyp = Polyp.getInstance();
        polyp.installPlayerProvider = false;
        polyp.metaFix = false;
        polyp.init();
        polyp.profiles().setGlobal(Preset.HYPIXEL.profile().toBuilder()
                .set(MechanicsKeys.COMPAT, Compat18.config())
                .set(MechanicsKeys.FIXES, Fixes18.config())
                .set(MechanicsKeys.TNT, Tnt.config().toBuilder().igniteOnPlace(false).build())
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
                (_) -> new Pos(0, 72, 0, 0, 0),
                false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.murdermysterygame.events",
                HypixelEventClass.class
        ).toList();
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.murdermysterygame.events.custom",
                HypixelEventClass.class
        ).toList();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.murdermysterygame.npcs",
                HypixelNPC.class
        ).toList();
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return (List) HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.murdermysterygame.redis.service",
                RedisMessageHandler.class
        ).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return (List<RedisMessageHandler<?, ?>>) (List<?>) HypixelGenericLoader.loopThroughPackage(
                "net.swofty.type.murdermysterygame.redis",
                RedisMessageHandler.class
        ).toList();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }

    @Override
    public List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of(MurderMysteryDataHandler.class);
    }
}
