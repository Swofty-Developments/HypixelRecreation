package net.swofty.type.theend.service;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EndRaceService {
    private static final Map<UUID, Race> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> BEST_TIMES = new ConcurrentHashMap<>();
    private static volatile boolean initialized;

    private EndRaceService() {
    }

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        MinecraftServer.getSchedulerManager().buildTask(EndRaceService::tick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    public static boolean start(SkyBlockPlayer player) {
        if (ACTIVE.containsKey(player.getUuid())) return false;
        ACTIVE.put(player.getUuid(), new Race(player.getPosition(), System.currentTimeMillis()));
        player.sendMessage("§d§lTHE END RACE §fRace started! Reach the far side and come back!");
        return true;
    }

    public static boolean isRacing(SkyBlockPlayer player) {
        return ACTIVE.containsKey(player.getUuid());
    }

    public static boolean hasCompleted(SkyBlockPlayer player) {
        return BEST_TIMES.containsKey(player.getUuid());
    }

    public static long bestTime(SkyBlockPlayer player) {
        return BEST_TIMES.getOrDefault(player.getUuid(), 0L);
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        ACTIVE.forEach((uuid, race) -> {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(uuid);
            if (player == null || player.getInstance() == null || now - race.startedAt > 180_000) {
                ACTIVE.remove(uuid);
                return;
            }

            if (!race.reachedFarSide && player.getPosition().distanceSquared(race.start) >= 80 * 80) {
                race.reachedFarSide = true;
                player.sendMessage("§d§lTHE END RACE §fYou reached the far side! Turn around and return!");
            }

            if (race.reachedFarSide && player.getPosition().distanceSquared(race.start) <= 12 * 12) {
                complete(player, now - race.startedAt);
                ACTIVE.remove(uuid);
            }
        });
    }

    private static void complete(SkyBlockPlayer player, long elapsed) {
        BEST_TIMES.merge(player.getUuid(), elapsed, Math::min);
        player.sendMessage("§d§lTHE END RACE §fFinished in §a" + elapsed / 1000.0 + "s§f!");

        if (elapsed < 180_000) player.addAndUpdateItem(ItemType.SILENT_PEARL, 8);
        if (elapsed < 120_000) player.addAndUpdateItem(ItemType.ENDERMAN_MASK);
        if (elapsed < 80_000) player.addAndUpdateItem(ItemType.NOPE_THE_FISH);
        if (elapsed < 48_000) player.addAndUpdateItem(ItemType.PIGS_FOOT);
    }

    private static final class Race {
        private final Pos start;
        private final long startedAt;
        private boolean reachedFarSide;

        private Race(Pos start, long startedAt) {
            this.start = start;
            this.startedAt = startedAt;
        }
    }
}
