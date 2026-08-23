package net.swofty.commons.skyblock;

import net.swofty.api.DataAPIImpl;
import net.swofty.commons.data.SwoftyData;
import net.swofty.lock.DistributedLock;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public final class CoopStorage {
    public static final String COOP_PREFIX = "hsb:coop:";
    public static final String BY_MEMBER = "hsb:coop:bymember";
    public static final String BY_PROFILE = "hsb:coop:byprofile";

    public static final String MEMBERS = "members";
    public static final String MEMBER_INVITES = "memberInvites";
    public static final String MEMBER_PROFILES = "memberProfiles";

    private static final String ROSTER_LOCK = "coop-roster:";
    private static final Duration ROSTER_LOCK_TIMEOUT = Duration.ofSeconds(5);

    private CoopStorage() {}

    public static DistributedLock.Handle rosterLock(UUID coopId) {
        return ((DataAPIImpl) SwoftyData.profile()).lock(ROSTER_LOCK + coopId, ROSTER_LOCK_TIMEOUT);
    }

    public static Document read(UUID coopId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            return read(jedis, coopId);
        }
    }

    public static UUID coopIdOfMember(UUID member) {
        return lookup(BY_MEMBER, member);
    }

    public static UUID coopIdOfProfile(UUID profileId) {
        return lookup(BY_PROFILE, profileId);
    }

    public static Document readByMember(UUID member) {
        return readIndexed(BY_MEMBER, member);
    }

    public static Document readByProfile(UUID profileId) {
        return readIndexed(BY_PROFILE, profileId);
    }

    public static void write(Document roster) {
        UUID coopId = UUID.fromString(roster.getString("_id"));
        try (DistributedLock.Handle ignored = rosterLock(coopId);
             Jedis jedis = SwoftyData.jedisPool().getResource()) {
            write(jedis, roster);
        }
    }

    public static Document update(UUID coopId, UnaryOperator<Document> mutation) {
        try (DistributedLock.Handle ignored = rosterLock(coopId);
             Jedis jedis = SwoftyData.jedisPool().getResource()) {
            Document stored = read(jedis, coopId);
            if (stored == null) return null;

            Document mutated = mutation.apply(stored);
            write(jedis, mutated);
            return mutated;
        }
    }

    public static boolean removeMember(UUID coopId, UUID player, UUID profileId) {
        Document remaining = update(coopId, roster -> withoutMember(roster, player, profileId));
        return remaining == null || isAbandoned(remaining);
    }

    public static Document withoutMember(Document roster, UUID player, UUID profileId) {
        ids(roster, MEMBERS).remove(player.toString());
        ids(roster, MEMBER_INVITES).remove(player.toString());
        if (profileId != null) ids(roster, MEMBER_PROFILES).remove(profileId.toString());
        return roster;
    }

    public static boolean isAbandoned(Document roster) {
        return ids(roster, MEMBERS).isEmpty() && ids(roster, MEMBER_PROFILES).isEmpty();
    }

    public static List<String> ids(Document roster, String field) {
        Object stored = roster.get(field);
        if (stored instanceof List<?> list) {
            List<String> ids = new ArrayList<>();
            for (Object id : list) ids.add(String.valueOf(id));
            roster.put(field, ids);
            return ids;
        }
        List<String> ids = new ArrayList<>();
        roster.put(field, ids);
        return ids;
    }

    private static UUID lookup(String index, UUID key) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            String coopId = jedis.hget(index, key.toString());
            return coopId == null ? null : UUID.fromString(coopId);
        }
    }

    private static Document readIndexed(String index, UUID key) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            String coopId = jedis.hget(index, key.toString());
            return coopId == null ? null : read(jedis, UUID.fromString(coopId));
        }
    }

    private static Document read(Jedis jedis, UUID coopId) {
        String json = jedis.get(COOP_PREFIX + coopId);
        return json == null ? null : Document.parse(json);
    }

    private static void write(Jedis jedis, Document roster) {
        String coopId = roster.getString("_id");

        Document existing = read(jedis, UUID.fromString(coopId));
        if (existing != null) {
            ids(existing, MEMBERS).forEach(id -> jedis.hdel(BY_MEMBER, id));
            ids(existing, MEMBER_INVITES).forEach(id -> jedis.hdel(BY_MEMBER, id));
            ids(existing, MEMBER_PROFILES).forEach(id -> jedis.hdel(BY_PROFILE, id));
        }

        List<String> members = ids(roster, MEMBERS);
        List<String> invites = ids(roster, MEMBER_INVITES);
        List<String> profiles = ids(roster, MEMBER_PROFILES);

        if (members.isEmpty() && invites.isEmpty()) {
            jedis.del(COOP_PREFIX + coopId);
            return;
        }

        jedis.set(COOP_PREFIX + coopId, roster.toJson());
        members.forEach(id -> jedis.hset(BY_MEMBER, id, coopId));
        invites.forEach(id -> jedis.hset(BY_MEMBER, id, coopId));
        profiles.forEach(id -> jedis.hset(BY_PROFILE, id, coopId));
    }
}
