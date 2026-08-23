package net.swofty.commons.skyblock;

import net.swofty.commons.data.SwoftyData;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class IslandStorage {
    public static final String ISLAND_PREFIX = "hsb:island:";

    private IslandStorage() {}

    public static byte[] key(String islandId) {
        return (ISLAND_PREFIX + islandId).getBytes(StandardCharsets.UTF_8);
    }

    public static boolean delete(String islandId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            return jedis.del(key(islandId)) > 0;
        }
    }

    public static boolean delete(UUID islandId) {
        return delete(islandId.toString());
    }

    public static boolean exists(String islandId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            return jedis.exists(key(islandId));
        }
    }
}
