package net.swofty.type.skyblockgeneric.user.island;

import lombok.Getter;
import lombok.Setter;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.data.monogdb.IslandDatabase;
import net.swofty.type.skyblockgeneric.furniture.IslandFurnitureManager;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.JerryInformation;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class SkyBlockIsland {
    private static final Map<UUID, SkyBlockIsland> loadedIslands = new ConcurrentHashMap<>();
    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean();

    private final IslandDatabase database;
    private final CoopDatabase.Coop coop;
    private final UUID islandID;
    private final IslandFurnitureManager furnitureManager;
    private Boolean created = false;
    private SharedInstance islandInstance;
    private PolarWorld world;

    @Setter
    private JerryInformation jerryInformation = null;
    @Setter
    private IslandMinionData minionData = null;
    @Setter
    private long lastSaved = 0;
    @Setter
    private Integer islandVersion;

    public SkyBlockIsland(UUID islandID, UUID profileID) {
        this.islandID = islandID;
        this.database = new IslandDatabase(islandID.toString());
        this.coop = CoopDatabase.getFromMemberProfile(profileID);
        this.furnitureManager = new IslandFurnitureManager(this);

        loadedIslands.put(islandID, this);
    }

    public CompletableFuture<SharedInstance> getSharedInstance() {
        CompletableFuture<SharedInstance> future = new CompletableFuture<>();

        Thread.startVirtualThread(() -> {
            try {
                if (created) {
                    future.complete(islandInstance);
                    return;
                }

                Logger.info("[{}] Starting island instance load", islandID);

                InstanceContainer temporaryInstance = createInstanceContainer();
                islandInstance = MinecraftServer.getInstanceManager().createSharedInstance(temporaryInstance);

                IslandWorldStorage.LoadedIslandWorld loadedWorld = IslandWorldStorage.load(database);
                world = loadedWorld.world();
                islandVersion = loadedWorld.version();
                lastSaved = loadedWorld.lastSaved();
                IslandLifecycleContext context = lifecycleContext();

                if (loadedWorld.firstCreated()) {
                    IslandLifecycle.run(IslandLifecyclePhase.CREATE, context);
                }

                migrateIfNeeded();
                temporaryInstance.setChunkLoader(new PolarLoader(world));

                this.created = true;
                IslandLifecycle.run(IslandLifecyclePhase.LOAD, context);
                furnitureManager.restore(islandInstance);

                future.complete(islandInstance);
                context.onlineMembers().forEach(HypixelPlayer::setReadyForEvents);
                Logger.info("[{}] Completed island instance load", islandID);
            } catch (Throwable throwable) {
                Logger.error(throwable, "[{}] Failed island instance load", islandID);
                future.completeExceptionally(throwable);
            }
        });

        return future;
    }

    public synchronized void runVacantCheck() {
        if (islandInstance == null) return;

        if (islandInstance.getPlayers().isEmpty()) {
            IslandLifecycle.run(IslandLifecyclePhase.SAVE, lifecycleContext());

            furnitureManager.clearRuntime();
            save();
            this.created = false;
            islandInstance.getChunks().forEach(chunk -> {
                islandInstance.unloadChunk(chunk);
            });
            this.islandInstance = null;
            this.world = null;
        }
    }

    private synchronized void flush() {
        if (!created || islandInstance == null || world == null) return;

        IslandLifecycle.run(IslandLifecyclePhase.SAVE, lifecycleContext());
        save();
    }

    private void save() {
        new PolarLoader(world).saveInstance(islandInstance);
        IslandWorldStorage.save(database, world, islandVersion);
    }

    private InstanceContainer createInstanceContainer() {
        RegistryKey<DimensionType> dimensionTypeKey = MinecraftServer.getDimensionTypeRegistry().getKey(
                Key.key("skyblock:island")
        );
        return MinecraftServer.getInstanceManager().createInstanceContainer(dimensionTypeKey);
    }

    private IslandLifecycleContext lifecycleContext() {
        List<SkyBlockPlayer> onlineMembers = IslandMembers.onlineMembers(islandID, coop);
        List<UUID> memberProfiles = IslandMembers.memberProfiles(islandID, coop);
        return new IslandLifecycleContext(this, coop != null, onlineMembers, memberProfiles);
    }

    private void migrateIfNeeded() {
        int oldVersion = islandVersion;
        if (islandVersion >= HypixelConst.getCurrentIslandVersion()) return;

        MathUtility.delay(() -> SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(player -> player.getSkyBlockIsland().getIslandID() == islandID)
                .forEach(player -> player.getLogHandler().debug(Text.of("Your island was migrated from version <c>{} <f>to <a>{}<f>!",
                        oldVersion, HypixelConst.getCurrentIslandVersion()))), 20);

        islandVersion = HypixelConst.getCurrentIslandVersion();
    }

    public static boolean hasIsland(UUID islandID) {
        return loadedIslands.containsKey(islandID);
    }

    public static SkyBlockIsland getOrCreate(UUID islandID, UUID profileID) {
        SkyBlockIsland existing = loadedIslands.get(islandID);
        if (existing != null) return existing;

        return new SkyBlockIsland(islandID, profileID);
    }

    public static @Nullable SkyBlockIsland getIsland(UUID islandID) {
        if (!loadedIslands.containsKey(islandID)) return null;
        return loadedIslands.get(islandID);
    }

    public static void runVacantLoop(Scheduler scheduler) {
        registerShutdownHook();

        scheduler.submitTask(() -> {
            loadedIslands.values().forEach(SkyBlockIsland::runVacantCheck);
            return TaskSchedule.tick(4);
        }, ExecutionType.TICK_END);
    }

    private static void registerShutdownHook() {
        if (!shutdownHookRegistered.compareAndSet(false, true)) return;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Saving {} loaded SkyBlock island(s) before shutdown", loadedIslands.size());
            loadedIslands.values().forEach(island -> {
                try {
                    island.flush();
                } catch (Throwable throwable) {
                    Logger.error(throwable, "[{}] Failed to save island during shutdown", island.islandID);
                }
            });
        }, "skyblock-island-shutdown-save"));
    }
}
