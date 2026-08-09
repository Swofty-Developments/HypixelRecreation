package net.swofty.type.garden.user;

import lombok.Getter;
import lombok.Setter;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;
import net.swofty.type.garden.plot.GardenPlotService;
import net.swofty.type.garden.world.GardenAssetRegistry;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenCore;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.data.monogdb.GardenDatabase;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.garden.SkyBlockGardenHandle;
import net.swofty.type.skyblockgeneric.garden.WorldBuildLimits;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.bson.types.Binary;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class SkyBlockGarden implements SkyBlockGardenHandle {
    public static final int CURRENT_GARDEN_VERSION = 1;
    private static final WorldBuildLimits BUILD_LIMITS = new WorldBuildLimits(-240, 239, -240, 239);
    private static final Map<UUID, SkyBlockGarden> LOADED_GARDENS = new HashMap<>();

    private final GardenDatabase database;
    private final CoopDatabase.Coop coop;
    private final UUID profileId;
    private final GardenPlotService plotService;

    private boolean created = false;
    private SharedInstance gardenInstance;
    private PolarWorld world;
    @Setter
    private boolean barnSwapInProgress = false;
    @Setter
    private long lastSaved = 0;
    @Setter
    private int gardenVersion = CURRENT_GARDEN_VERSION;

    public SkyBlockGarden(UUID profileId) {
        this.profileId = profileId;
        this.database = new GardenDatabase(profileId.toString());
        this.coop = CoopDatabase.getFromMemberProfile(profileId);
        this.plotService = new GardenPlotService(this);
        LOADED_GARDENS.put(profileId, this);
    }

    public CompletableFuture<SharedInstance> getSharedInstance() {
        InstanceManager manager = MinecraftServer.getInstanceManager();
        CompletableFuture<SharedInstance> future = new CompletableFuture<>();

        new Thread(() -> {
            if (created) {
                future.complete(gardenInstance);
                return;
            }

            RegistryKey<DimensionType> dimensionTypeKey = MinecraftServer.getDimensionTypeRegistry().getKey(
                Key.key("skyblock:island")
            );
            InstanceContainer temporaryInstance = manager.createInstanceContainer(dimensionTypeKey);
            gardenInstance = manager.createSharedInstance(temporaryInstance);

            List<SkyBlockPlayer> onlinePlayers;
            if (coop != null) {
                onlinePlayers = coop.getOnlineMembers().stream()
                    .filter(player -> player.getSkyblockDataHandler() != null)
                    .filter(player -> profileId.equals(player.getSkyblockDataHandler().getCurrentProfileId()))
                    .toList();
            } else {
                onlinePlayers = SkyBlockGenericLoader.getLoadedPlayers().stream()
                    .filter(player -> player.getSkyblockDataHandler() != null)
                    .filter(player -> profileId.equals(player.getSkyblockDataHandler().getCurrentProfileId()))
                    .toList();
            }

            if (!database.exists() || !database.has("polar_blob")) {
                gardenVersion = CURRENT_GARDEN_VERSION;
                try {
                    world = PolarReader.read(Files.readAllBytes(GardenAssetRegistry.getGardenSeed()));
                } catch (IOException e) {
                    Logger.error(e, "Failed to create Garden world");
                    throw new RuntimeException("Failed to create Garden world", e);
                }
                lastSaved = System.currentTimeMillis();
            } else {
                if (database.has("version")) {
                    gardenVersion = (int) database.get("version", Integer.class);
                }
                world = PolarReader.read(((Binary) database.get("polar_blob", Binary.class)).getData());
                lastSaved = (long) database.get("last_saved", Long.class);
                if (gardenVersion < CURRENT_GARDEN_VERSION) {
                    gardenVersion = CURRENT_GARDEN_VERSION;
                }
            }

            temporaryInstance.setChunkLoader(new PolarLoader(world));
            created = true;

            future.complete(gardenInstance);
            onlinePlayers.forEach(SkyBlockPlayer::setReadyForEvents);
        }).start();

        return future;
    }

    public void runVacantCheck() {
        if (gardenInstance == null) {
            return;
        }

        if (gardenInstance.getPlayers().isEmpty()) {
            save();
            created = false;
            gardenInstance.getChunks().forEach(gardenInstance::unloadChunk);
            gardenInstance = null;
            world = null;
        }
    }

    public CompletableFuture<Void> changeBarnSkin(String skinId) {
        return plotService.applyBarnSkin(skinId);
    }

    private void save() {
        if (world == null || gardenInstance == null) {
            return;
        }
        new PolarLoader(world).saveInstance(gardenInstance);
        database.insertOrUpdate("polar_blob", new Binary(PolarWriter.write(world)));
        database.insertOrUpdate("last_saved", System.currentTimeMillis());
        database.insertOrUpdate("version", gardenVersion);
        lastSaved = System.currentTimeMillis();
    }

    @Override
    public WorldBuildLimits getBuildLimits() {
        return BUILD_LIMITS;
    }

    @Override
    public boolean canEdit(net.minestom.server.coordinate.Point point) {
        GardenData.GardenCoreData coreData = resolveCoreData();
        return plotService.canEdit(point, coreData, barnSwapInProgress);
    }

    @Override
    public Optional<String> getDeniedBuildMessage(Point point) {
        if (!isWithinBounds(point)) {
            return SkyBlockGardenHandle.super.getDeniedBuildMessage(point);
        }
        if (plotService.isInBarnSwapRegion(point)) {
            return Optional.empty();
        }
        if (!plotService.isUnlocked(point, resolveCoreData())) {
            return Optional.of("§cYou haven't unlocked this area yet!");
        }
        return Optional.of("§cYou can't edit that part of your Garden yet!");
    }

    private GardenData.GardenCoreData resolveCoreData() {
        SkyBlockPlayer viewer = getOnlineViewer();
        if (viewer == null) {
            return new GardenData.GardenCoreData();
        }
        return viewer.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_CORE, DatapointGardenCore.class)
            .getValue();
    }

    private @Nullable SkyBlockPlayer getOnlineViewer() {
        return SkyBlockGenericLoader.getLoadedPlayers().stream()
            .filter(player -> player.getSkyblockDataHandler() != null)
            .filter(player -> profileId.equals(player.getSkyblockDataHandler().getCurrentProfileId()))
            .findFirst()
            .orElse(null);
    }

    public static @Nullable SkyBlockGarden getGarden(UUID profileId) {
        return LOADED_GARDENS.get(profileId);
    }

    public static void runVacantLoop(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                if (player.isOnGarden() && player.getSkyBlockGarden() instanceof SkyBlockGarden garden) {
                    garden.runVacantCheck();
                }
            });
            return TaskSchedule.tick(4);
        }, ExecutionType.TICK_END);
    }
}
