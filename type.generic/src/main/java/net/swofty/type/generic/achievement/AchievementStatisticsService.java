package net.swofty.type.generic.achievement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.data.SwoftyData;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementStatisticsService {

    private static final String PLAYERS_KEY = "hsb:stats:players";
    private static final String UNLOCKS_KEY = "hsb:stats:achievement_unlocks";

    private static final Map<String, Long> UNLOCK_CACHE = new ConcurrentHashMap<>();
    private static long totalPlayerCount = 0;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        Thread.startVirtualThread(AchievementStatisticsService::runAggregation);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            try {
                runAggregation();
            } catch (Exception e) {
                Logger.error("Error running achievement statistics aggregation: " + e.getMessage());
            }
        }).delay(TaskSchedule.minutes(15))
          .repeat(TaskSchedule.minutes(15))
          .schedule();

        Logger.info("Achievement Statistics Service initialized");
    }

    public static double getUnlockPercentage(String achievementId, int tier) {
        if (totalPlayerCount == 0) return -1;

        Long unlockCount = UNLOCK_CACHE.get(buildCacheKey(achievementId, tier));
        if (unlockCount == null) return 0.0;

        return (unlockCount * 100.0) / totalPlayerCount;
    }

    public static double getUnlockPercentage(String achievementId) {
        return getUnlockPercentage(achievementId, 0);
    }

    public static String getFormattedPercentage(String achievementId, int tier) {
        double percentage = getUnlockPercentage(achievementId, tier);
        if (percentage < 0) return "?.??";
        return String.format("%.2f", percentage);
    }

    public static String getFormattedPercentage(String achievementId) {
        return getFormattedPercentage(achievementId, 0);
    }

    public static void recordPlayer(UUID uuid) {
        if (SwoftyData.jedisPool() == null) return;
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            if (jedis.sadd(PLAYERS_KEY, uuid.toString()) == 1) totalPlayerCount++;
        }
    }

    public static void recordUnlock(AchievementDefinition def, AchievementData.AchievementProgress progress) {
        if (SwoftyData.jedisPool() == null) return;

        String field = def.getType() == AchievementType.TIERED
                ? def.getId() + ":" + progress.getCurrentTier()
                : def.getId();

        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            jedis.hincrBy(UNLOCKS_KEY, field, 1);
        }
        UNLOCK_CACHE.merge(field, 1L, Long::sum);
    }

    public static void runAggregation() {
        if (SwoftyData.jedisPool() == null) return;

        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            totalPlayerCount = jedis.scard(PLAYERS_KEY);

            Map<String, String> raw = jedis.hgetAll(UNLOCKS_KEY);
            Map<String, Long> newCounts = new ConcurrentHashMap<>();
            raw.forEach((key, value) -> {
                try {
                    newCounts.put(key, Long.parseLong(value));
                } catch (NumberFormatException ignored) {
                }
            });

            UNLOCK_CACHE.clear();
            UNLOCK_CACHE.putAll(newCounts);
        } catch (Exception e) {
            Logger.error(e, "Failed to run achievement statistics aggregation");
        }
    }

    private static String buildCacheKey(String achievementId, int tier) {
        if (tier > 0) {
            return achievementId + ":" + tier;
        }
        return achievementId;
    }

    public static void forceRefresh() {
        Thread.startVirtualThread(AchievementStatisticsService::runAggregation);
    }

    public static long getTotalPlayerCount() {
        return totalPlayerCount;
    }
}
