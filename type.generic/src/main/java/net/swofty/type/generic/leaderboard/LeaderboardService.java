package net.swofty.type.generic.leaderboard;

import net.swofty.commons.redis.RedisConnectionPool;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaderboardService {
    private static final String PREFIX = "leaderboard:";
    private static JedisPool jedisPool;
    private static volatile boolean initialized = false;
    private static volatile boolean connecting = false;

    /**
     * @param redisUri Redis connection URI (e.g., "redis://localhost:6379")
     */
    public static void connect(String redisUri) {
        // Connect asynchronously to avoid blocking the main thread
        Thread.startVirtualThread(() -> connectSync(redisUri));
    }

    private static synchronized void connectSync(String redisUri) {
        if (initialized || connecting) return;
        connecting = true;

        try {
            jedisPool = RedisConnectionPool.connect(redisUri, RedisConnectionPool.Settings.standard());
            initialized = true;
            Logger.info("LeaderboardService connected to Redis");
        } catch (Exception e) {
            Logger.warn("LeaderboardService: Redis not available, leaderboards disabled");
            initialized = false;
            jedisPool = null;
        } finally {
            connecting = false;
        }
    }

    /**
     * Check if the service is initialized and ready.
     */
    public static boolean isInitialized() {
        return initialized && jedisPool != null && !jedisPool.isClosed();
    }

    /**
     * Update a player's score on a leaderboard.
     * @param leaderboardKey The leaderboard key (e.g., "bedwars:experience")
     * @param playerUuid The player's UUID
     * @param score The player's score
     */
    public static void updateScore(String leaderboardKey, UUID playerUuid, double score) {
        if (!isInitialized()) return;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.zadd(PREFIX + leaderboardKey, score, playerUuid.toString());
        } catch (Exception e) {
            Logger.warn(e, "Failed to update leaderboard score for {} on {}", playerUuid, leaderboardKey);
        }
    }

    /**
     * Update a player's score asynchronously.
     */
    public static void updateScoreAsync(String leaderboardKey, UUID playerUuid, double score) {
        Thread.startVirtualThread(() -> updateScore(leaderboardKey, playerUuid, score));
    }

    /**
     * Get the top N players on a leaderboard.
     * @param leaderboardKey The leaderboard key
     * @param n Number of entries to retrieve
     * @return List of leaderboard entries (1-indexed ranks)
     */
    public static List<LeaderboardEntry> getTopN(String leaderboardKey, int n) {
        if (!isInitialized()) return List.of();

        try (Jedis jedis = jedisPool.getResource()) {
            List<Tuple> results = jedis.zrevrangeWithScores(PREFIX + leaderboardKey, 0, n - 1);
            List<LeaderboardEntry> entries = new ArrayList<>();
            int rank = 1;
            for (Tuple tuple : results) {
                try {
                    entries.add(new LeaderboardEntry(
                        UUID.fromString(tuple.getElement()),
                        rank++,
                        tuple.getScore()
                    ));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
            return entries;
        } catch (Exception e) {
            Logger.warn(e, "Failed to get top {} for leaderboard {}", n, leaderboardKey);
            return List.of();
        }
    }

    /**
     * Get a player's rank on a leaderboard (1-indexed).
     * @param leaderboardKey The leaderboard key
     * @param playerUuid The player's UUID
     * @return The player's rank (1-indexed), or -1 if not found
     */
    public static int getPlayerRank(String leaderboardKey, UUID playerUuid) {
        if (!isInitialized()) return -1;

        try (Jedis jedis = jedisPool.getResource()) {
            Long rank = jedis.zrevrank(PREFIX + leaderboardKey, playerUuid.toString());
            return rank == null ? -1 : rank.intValue() + 1; // Convert 0-indexed to 1-indexed
        } catch (Exception e) {
            Logger.warn(e, "Failed to get rank for {} on {}", playerUuid, leaderboardKey);
            return -1;
        }
    }

    /**
     * Get a player's score on a leaderboard.
     * @param leaderboardKey The leaderboard key
     * @param playerUuid The player's UUID
     * @return The player's score, or null if not found
     */
    public static Double getPlayerScore(String leaderboardKey, UUID playerUuid) {
        if (!isInitialized()) return null;

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zscore(PREFIX + leaderboardKey, playerUuid.toString());
        } catch (Exception e) {
            Logger.warn(e, "Failed to get score for {} on {}", playerUuid, leaderboardKey);
            return null;
        }
    }

    /**
     * Get a player's rank and score as a LeaderboardEntry.
     * @param leaderboardKey The leaderboard key
     * @param playerUuid The player's UUID
     * @return The player's entry, or null if not found
     */
    public static LeaderboardEntry getPlayerRankEntry(String leaderboardKey, UUID playerUuid) {
        if (!isInitialized()) return null;

        try (Jedis jedis = jedisPool.getResource()) {
            Long rank = jedis.zrevrank(PREFIX + leaderboardKey, playerUuid.toString());
            Double score = jedis.zscore(PREFIX + leaderboardKey, playerUuid.toString());
            if (rank == null || score == null) return null;
            return new LeaderboardEntry(playerUuid, rank.intValue() + 1, score);
        } catch (Exception e) {
            Logger.warn(e, "Failed to get rank entry for {} on {}", playerUuid, leaderboardKey);
            return null;
        }
    }

    /**
     * Get players around a specific player on the leaderboard.
     * @param leaderboardKey The leaderboard key
     * @param playerUuid The player's UUID
     * @param range Number of players above and below to include
     * @return List of leaderboard entries around the player
     */
    public static List<LeaderboardEntry> getAroundPlayer(String leaderboardKey, UUID playerUuid, int range) {
        if (!isInitialized()) return List.of();

        try (Jedis jedis = jedisPool.getResource()) {
            Long rank = jedis.zrevrank(PREFIX + leaderboardKey, playerUuid.toString());
            if (rank == null) return List.of();

            long start = Math.max(0, rank - range);
            long end = rank + range;

            List<Tuple> results = jedis.zrevrangeWithScores(PREFIX + leaderboardKey, start, end);
            List<LeaderboardEntry> entries = new ArrayList<>();
            int currentRank = (int) start + 1;
            for (Tuple tuple : results) {
                try {
                    entries.add(new LeaderboardEntry(
                        UUID.fromString(tuple.getElement()),
                        currentRank++,
                        tuple.getScore()
                    ));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                    currentRank++;
                }
            }
            return entries;
        } catch (Exception e) {
            Logger.warn(e, "Failed to get players around {} on {}", playerUuid, leaderboardKey);
            return List.of();
        }
    }

    /**
     * Get the total number of players on a leaderboard.
     * @param leaderboardKey The leaderboard key
     * @return The total number of players
     */
    public static long getTotalPlayers(String leaderboardKey) {
        if (!isInitialized()) return 0;

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcard(PREFIX + leaderboardKey);
        } catch (Exception e) {
            Logger.warn(e, "Failed to get total players for {}", leaderboardKey);
            return 0;
        }
    }

    /**
     * Remove a player from a leaderboard.
     * @param leaderboardKey The leaderboard key
     * @param playerUuid The player's UUID
     */
    public static void removePlayer(String leaderboardKey, UUID playerUuid) {
        if (!isInitialized()) return;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.zrem(PREFIX + leaderboardKey, playerUuid.toString());
        } catch (Exception e) {
            Logger.warn(e, "Failed to remove {} from {}", playerUuid, leaderboardKey);
        }
    }

    public static int removeFromAllLeaderboards(UUID playerUuid) {
        if (!isInitialized()) return 0;

        int removed = 0;
        try (Jedis jedis = jedisPool.getResource()) {
            ScanParams params = new ScanParams().match(PREFIX + "*").count(500);
            String cursor = ScanParams.SCAN_POINTER_START;
            do {
                ScanResult<String> scan = jedis.scan(cursor, params);
                for (String key : scan.getResult()) {
                    if (!"zset".equals(jedis.type(key))) continue;
                    if (jedis.zrem(key, playerUuid.toString()) > 0) removed++;
                }
                cursor = scan.getCursor();
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
        } catch (Exception e) {
            Logger.warn(e, "Failed to remove {} from every leaderboard", playerUuid);
        }
        return removed;
    }

    /**
     * Get players within a score range.
     * @param leaderboardKey The leaderboard key
     * @param minScore Minimum score (inclusive)
     * @param maxScore Maximum score (inclusive)
     * @return List of leaderboard entries within the range
     */
    public static List<LeaderboardEntry> getByScoreRange(String leaderboardKey, double minScore, double maxScore) {
        if (!isInitialized()) return List.of();

        try (Jedis jedis = jedisPool.getResource()) {
            List<Tuple> results = jedis.zrevrangeByScoreWithScores(PREFIX + leaderboardKey, maxScore, minScore);
            List<LeaderboardEntry> entries = new ArrayList<>();
            for (Tuple tuple : results) {
                try {
                    // Note: ranks in range queries aren't sequential; use getPlayerRank for accurate rank
                    int rank = getPlayerRank(leaderboardKey, UUID.fromString(tuple.getElement()));
                    entries.add(new LeaderboardEntry(
                        UUID.fromString(tuple.getElement()),
                        rank,
                        tuple.getScore()
                    ));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
            return entries;
        } catch (Exception e) {
            Logger.warn(e, "Failed to get score range for {}", leaderboardKey);
            return List.of();
        }
    }

    /**
     * Shutdown the leaderboard service.
     */
    public static void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            initialized = false;
            Logger.info("LeaderboardService shutdown");
        }
    }

    /**
     * Represents an entry on a leaderboard.
     */
    public record LeaderboardEntry(UUID playerUuid, int rank, double score) {
        /**
         * Get the score as a long (for integer-based scores).
         */
        public long scoreAsLong() {
            return (long) score;
        }

        /**
         * Get the score as an int (for smaller integer-based scores).
         */
        public int scoreAsInt() {
            return (int) score;
        }
    }
}
