package net.swofty.type.garden.pest;

import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class GardenPestRuntime {
    private static final long OFFLINE_SPAWN_INTERVAL_MS = 3_600_000L;

    private GardenPestRuntime() {
    }

    public static void start(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            tickAllGardens();
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_END);
    }

    private static void tickAllGardens() {
        Map<UUID, List<SkyBlockPlayer>> playersByProfile = SkyBlockGenericLoader.getLoadedPlayers().stream()
            .filter(SkyBlockPlayer::isOnGarden)
            .filter(player -> player.getSkyblockDataHandler() != null)
            .collect(Collectors.groupingBy(player -> player.getSkyblockDataHandler().getCurrentProfileId()));

        playersByProfile.values().forEach(GardenPestRuntime::tickProfile);
    }

    private static void tickProfile(List<SkyBlockPlayer> viewers) {
        if (viewers.isEmpty()) {
            return;
        }

        SkyBlockPlayer primary = viewers.getFirst();
        GardenData.GardenCoreData core = GardenGuiSupport.core(primary);
        GardenData.GardenPestsData pests = GardenGuiSupport.pests(primary);
        long now = System.currentTimeMillis();

        if (pests.getLastSpawnCheckAt() <= 0L) {
            pests.setLastSpawnCheckAt(now);
            return;
        }

        long offlineReference = Math.max(pests.getLastSpawnCheckAt(), core.getLastActiveAt());
        long offlineElapsed = Math.max(0L, now - offlineReference);
        if (offlineElapsed > 0L) {
            pests.setOfflineAccumulatorMs(pests.getOfflineAccumulatorMs() + offlineElapsed);
        }

        int spawned = 0;
        while (pests.getOfflineAccumulatorMs() >= OFFLINE_SPAWN_INTERVAL_MS && core.getLevel() >= GardenConfigRegistry.getInt(
            GardenConfigRegistry.getConfig("pests.yml"),
            "start_garden_level",
            5
        )) {
            if (!spawnOfflinePest(primary, pests, core, now)) {
                break;
            }
            pests.setOfflineAccumulatorMs(pests.getOfflineAccumulatorMs() - OFFLINE_SPAWN_INTERVAL_MS);
            spawned++;
        }

        if (spawned > 0) {
            int totalSpawned = spawned;
            viewers.forEach(player -> player.sendMessage("§cWhile you were away, §6" + totalSpawned + " §cpest" + (totalSpawned == 1 ? "" : "s") + " infested your Garden."));
        }
        pests.setLastSpawnCheckAt(now);
    }

    private static boolean spawnOfflinePest(
        SkyBlockPlayer primary,
        GardenData.GardenPestsData pests,
        GardenData.GardenCoreData core,
        long now
    ) {
        List<Map<String, Object>> eligible = GardenServices.pests().getPests().stream()
            .filter(entry -> !GardenConfigRegistry.getBoolean(entry, "trap_only", false))
            .filter(entry -> core.getLevel() >= GardenConfigRegistry.getInt(entry, "garden_level", 5))
            .toList();
        if (eligible.isEmpty()) {
            return false;
        }

        Map<String, Object> chosen = eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
        GardenData.GardenPestState pest = new GardenData.GardenPestState();
        pest.setPestId(GardenConfigRegistry.getString(chosen, "id", ""));
        pest.setPlotId("central");
        pest.setSpawnedAt(now);
        pest.setOfflineSpawned(true);
        pests.getActivePests().add(pest);
        primary.sendMessage("§cOffline pest spawned: §6" + GardenConfigRegistry.getString(chosen, "display_name", "Pest"));
        return true;
    }
}
