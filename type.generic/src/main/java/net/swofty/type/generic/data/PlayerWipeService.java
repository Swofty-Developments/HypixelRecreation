package net.swofty.type.generic.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import net.swofty.PlayerField;
import net.swofty.commons.data.ProfileIndexes;
import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.skyblock.CoopLinks;
import net.swofty.commons.skyblock.CoopStorage;
import net.swofty.commons.skyblock.IslandStorage;
import net.swofty.commons.skyblock.SkyBlockProfileFields;
import net.swofty.type.generic.data.mongodb.MongoConnection;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlayerWipeService {
    public static final List<HypixelDataHandler.Data> PRESERVED_ACCOUNT_FIELDS = List.of(
            HypixelDataHandler.Data.RANK,
            HypixelDataHandler.Data.IGN,
            HypixelDataHandler.Data.IGN_LOWER,
            HypixelDataHandler.Data.LOCALE);

    public static final List<OwnedCollection> OWNED_COLLECTIONS = List.of(
            new OwnedCollection("ravengard_tracked_items", "owner", "profile"),
            new OwnedCollection("ravengard_profiles", "_owner", "_id"),
            new OwnedCollection("bedwars_stat_events", "playerUuid", null));

    private static final JacksonSerializer<UUID> UUID_SERIALIZER = new JacksonSerializer<>(UUID.class);

    private static final Set<UUID> inProgress = ConcurrentHashMap.newKeySet();
    private static final List<Consumer<UUID>> islandDropHooks = new CopyOnWriteArrayList<>();

    private PlayerWipeService() {}

    public record OwnedCollection(String name, String ownerField, String profileField) {}

    public record Result(List<UUID> profiles, int islands, int leaderboards, long ownedRows) {}

    public interface AccountStore {
        String read(PlayerField<String> field);

        void write(PlayerField<String> field, String value);

        void deleteDocument();
    }

    public static boolean begin(UUID playerUuid) {
        return inProgress.add(playerUuid);
    }

    public static void finish(UUID playerUuid) {
        inProgress.remove(playerUuid);
    }

    public static boolean isInProgress(UUID playerUuid) {
        return inProgress.contains(playerUuid);
    }

    public static void onIslandDropped(Consumer<UUID> hook) {
        islandDropHooks.add(hook);
    }

    public static List<UUID> profileIdsOf(UUID playerUuid) {
        return profileIdsFrom(index -> readIndex(playerUuid, index));
    }

    static List<UUID> profileIdsFrom(Function<PlayerField<String>, String> reader) {
        List<UUID> profiles = new ArrayList<>();
        for (PlayerField<String> index : ProfileIndexes.ALL) {
            String stored = reader.apply(index);
            for (UUID profileId : ProfileIndexes.decodeProfiles(stored)) {
                if (!profiles.contains(profileId)) profiles.add(profileId);
            }

            UUID selected = ProfileIndexes.decodeSelected(stored);
            if (selected != null && !profiles.contains(selected)) profiles.add(selected);
        }
        return profiles;
    }

    static void wipeAccount(AccountStore store) {
        Map<PlayerField<String>, String> preserved = new LinkedHashMap<>();
        for (HypixelDataHandler.Data data : PRESERVED_ACCOUNT_FIELDS) {
            String value = store.read(data.accountField());
            if (value != null) preserved.put(data.accountField(), value);
        }

        store.deleteDocument();
        preserved.forEach(store::write);
    }

    public static Result wipe(UUID playerUuid) {
        List<UUID> profiles = profileIdsOf(playerUuid);

        int islands = 0;
        for (UUID profileId : profiles) {
            try {
                islands += wipeProfile(playerUuid, profileId);
            } catch (Exception e) {
                Logger.error(e, "Failed to wipe profile {} of user {}", profileId, playerUuid);
            }
        }

        wipeAccount(new LiveAccountStore(playerUuid));

        int leaderboards = LeaderboardService.removeFromAllLeaderboards(playerUuid);
        long ownedRows = wipeOwnedCollections(playerUuid, profiles);

        return new Result(profiles, islands, leaderboards, ownedRows);
    }

    private static String readIndex(UUID playerUuid, PlayerField<String> index) {
        try {
            return SwoftyData.account().get(playerUuid, index);
        } catch (Exception e) {
            Logger.error(e, "Failed to read the profile index {} of user {}", index.fullKey(), playerUuid);
            return null;
        }
    }

    private static int wipeProfile(UUID playerUuid, UUID profileId) {
        UUID sharedCoopId = CoopStorage.coopIdOfProfile(profileId);
        UUID coopId = sharedCoopId == null ? profileId : sharedCoopId;
        boolean coopWasLoaded = SwoftyData.profile().isLinkLoaded(CoopLinks.COOP, coopId);

        Set<UUID> islandIds = islandIdsOf(profileId, coopId);
        boolean abandoned = sharedCoopId == null
                || CoopStorage.removeMember(sharedCoopId, playerUuid, profileId);

        SwoftyData.profile().unlink(profileId, CoopLinks.COOP);
        SwoftyData.profile().deletePlayer(profileId);

        if (!abandoned) {
            if (!coopWasLoaded) releaseCoop(coopId);
            return 0;
        }

        SwoftyData.profile().deleteLink(CoopLinks.COOP, coopId);

        int dropped = 0;
        for (UUID islandId : islandIds) {
            if (dropIsland(islandId)) dropped++;
        }
        return dropped;
    }

    private static Set<UUID> islandIdsOf(UUID profileId, UUID coopId) {
        Set<UUID> islandIds = new LinkedHashSet<>();
        islandIds.add(profileId);

        try {
            addIslandId(islandIds, SwoftyData.profile().getDirect(coopId, SkyBlockProfileFields.ISLAND_UUID));
        } catch (Exception e) {
            Logger.error(e, "Failed to read the island linked to coop {}", coopId);
        }

        try {
            String document = SwoftyData.profile().get(profileId, SkyBlockProfileFields.DOCUMENT);
            if (document != null) {
                addIslandId(islandIds, Document.parse(document).getString(SkyBlockProfileFields.ISLAND_UUID_KEY));
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to read the stored document of profile {}", profileId);
        }
        return islandIds;
    }

    private static void addIslandId(Set<UUID> islandIds, String serialized) {
        if (serialized == null || serialized.isEmpty()) return;
        try {
            UUID islandId = UUID_SERIALIZER.deserialize(serialized);
            if (islandId != null) islandIds.add(islandId);
        } catch (Exception e) {
            Logger.error(e, "Failed to deserialize the island id {}", serialized);
        }
    }

    private static boolean dropIsland(UUID islandId) {
        for (Consumer<UUID> hook : islandDropHooks) {
            try {
                hook.accept(islandId);
            } catch (Exception e) {
                Logger.error(e, "Failed to drop the loaded island {}", islandId);
            }
        }
        try {
            return IslandStorage.delete(islandId);
        } catch (Exception e) {
            Logger.error(e, "Failed to delete the stored island {}", islandId);
            return false;
        }
    }

    private static void releaseCoop(UUID coopId) {
        try {
            SwoftyData.profile().unloadLink(CoopLinks.COOP, coopId);
        } catch (Exception e) {
            Logger.error(e, "Failed to release the coop container {}", coopId);
        }
    }

    private static long wipeOwnedCollections(UUID playerUuid, List<UUID> profiles) {
        List<String> profileIds = profiles.stream().map(UUID::toString).toList();

        long deleted = 0;
        for (OwnedCollection owned : OWNED_COLLECTIONS) {
            MongoCollection<Document> collection = MongoConnection.collection(owned.name());
            if (collection == null) continue;
            try {
                DeleteResult result = collection.deleteMany(ownedFilter(owned, playerUuid, profileIds));
                deleted += result.getDeletedCount();
            } catch (Exception e) {
                Logger.error(e, "Failed to delete the rows of user {} in {}", playerUuid, owned.name());
            }
        }
        return deleted;
    }

    static Bson ownedFilter(OwnedCollection owned, UUID playerUuid, List<String> profileIds) {
        Bson byOwner = Filters.eq(owned.ownerField(), playerUuid.toString());
        if (owned.profileField() == null || profileIds.isEmpty()) return byOwner;
        return Filters.or(byOwner, Filters.in(owned.profileField(), profileIds));
    }

    private record LiveAccountStore(UUID playerUuid) implements AccountStore {
        @Override
        public String read(PlayerField<String> field) {
            try {
                return SwoftyData.account().get(playerUuid, field);
            } catch (Exception e) {
                Logger.error(e, "Failed to read the preserved field {} of user {}", field.fullKey(), playerUuid);
                return null;
            }
        }

        @Override
        public void write(PlayerField<String> field, String value) {
            try {
                SwoftyData.account().set(playerUuid, field, value);
            } catch (Exception e) {
                Logger.error(e, "Failed to restore the preserved field {} of user {}", field.fullKey(), playerUuid);
            }
        }

        @Override
        public void deleteDocument() {
            SwoftyData.account().deletePlayer(playerUuid);
        }
    }
}
