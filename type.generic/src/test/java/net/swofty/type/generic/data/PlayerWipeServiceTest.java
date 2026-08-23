package net.swofty.type.generic.data;

import net.swofty.PlayerField;
import net.swofty.codec.Codecs;
import net.swofty.commons.data.ProfileIndexes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerWipeServiceTest {
    private static final PlayerField<String> GAME_WINS =
            PlayerField.create("game", "bedwars_wins", Codecs.STRING, null);

    @Test
    void keepsOnlyTheIdentityFieldsWhenTheAccountIsDeleted() {
        RecordingAccountStore store = new RecordingAccountStore();
        store.stored.put(HypixelDataHandler.Data.RANK.accountField(), "\"ADMIN\"");
        store.stored.put(HypixelDataHandler.Data.IGN.accountField(), "\"Swofty\"");
        store.stored.put(HypixelDataHandler.Data.IGN_LOWER.accountField(), "\"swofty\"");
        store.stored.put(HypixelDataHandler.Data.LOCALE.accountField(), "\"EN_US\"");
        store.stored.put(HypixelDataHandler.Data.HYPIXEL_EXPERIENCE.accountField(), "123456");
        store.stored.put(HypixelDataHandler.Data.ACHIEVEMENT_DATA.accountField(), "{}");
        store.stored.put(ProfileIndexes.SKYBLOCK, "a;a,b");
        store.stored.put(GAME_WINS, "42");

        PlayerWipeService.wipeAccount(store);

        assertEquals(List.of("\"ADMIN\"", "\"Swofty\"", "\"swofty\"", "\"EN_US\""),
                PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.stream()
                        .map(data -> store.stored.get(data.accountField()))
                        .toList());
        assertFalse(store.stored.containsKey(HypixelDataHandler.Data.HYPIXEL_EXPERIENCE.accountField()));
        assertFalse(store.stored.containsKey(HypixelDataHandler.Data.ACHIEVEMENT_DATA.accountField()));
        assertFalse(store.stored.containsKey(ProfileIndexes.SKYBLOCK));
        assertFalse(store.stored.containsKey(GAME_WINS));
    }

    @Test
    void readsEveryPreservedFieldBeforeTheDocumentIsDeleted() {
        RecordingAccountStore store = new RecordingAccountStore();
        PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.forEach(data ->
                store.stored.put(data.accountField(), data.getKey()));

        PlayerWipeService.wipeAccount(store);

        int deletedAt = store.calls.indexOf("delete");
        assertTrue(deletedAt >= 0);
        for (int i = 0; i < deletedAt; i++) assertTrue(store.calls.get(i).startsWith("read "));
        for (int i = deletedAt + 1; i < store.calls.size(); i++) assertTrue(store.calls.get(i).startsWith("write "));
        assertEquals(PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.size(), store.calls.size() - deletedAt - 1);
    }

    @Test
    void restoresNothingForAFieldThatWasNeverSet() {
        RecordingAccountStore store = new RecordingAccountStore();
        store.stored.put(HypixelDataHandler.Data.RANK.accountField(), "\"STAFF\"");

        PlayerWipeService.wipeAccount(store);

        assertEquals(Map.of(HypixelDataHandler.Data.RANK.accountField(), "\"STAFF\""), store.stored);
    }

    @Test
    void preservesTheRankSoAWipeCannotDemoteStaff() {
        assertTrue(PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.contains(HypixelDataHandler.Data.RANK));
        assertTrue(PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.contains(HypixelDataHandler.Data.IGN));
        assertTrue(PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.contains(HypixelDataHandler.Data.LOCALE));

        PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.forEach(data -> {
            assertNotNull(data.accountField());
            assertEquals("hypixel", data.accountField().namespace());
        });

        List<HypixelDataHandler.Data> dropped = Arrays.stream(HypixelDataHandler.Data.values())
                .filter(data -> !PlayerWipeService.PRESERVED_ACCOUNT_FIELDS.contains(data))
                .toList();
        assertTrue(dropped.contains(HypixelDataHandler.Data.QUEST_DATA));
        assertTrue(dropped.contains(HypixelDataHandler.Data.ACHIEVEMENT_DATA));
        assertTrue(dropped.contains(HypixelDataHandler.Data.HYPIXEL_EXPERIENCE));
    }

    @Test
    void findsEveryProfileThroughBothAccountIndexes() {
        UUID skyblockOne = UUID.randomUUID();
        UUID skyblockTwo = UUID.randomUUID();
        UUID ravengard = UUID.randomUUID();

        Map<PlayerField<String>, String> indexes = Map.of(
                ProfileIndexes.SKYBLOCK, skyblockOne + ";" + skyblockOne + "," + skyblockTwo,
                ProfileIndexes.RAVENGARD, ravengard + ";" + ravengard);

        assertEquals(List.of(skyblockOne, skyblockTwo, ravengard),
                PlayerWipeService.profileIdsFrom(indexes::get));
    }

    @Test
    void findsNoProfilesForAPlayerNeitherGameHasSeen() {
        assertEquals(List.of(), PlayerWipeService.profileIdsFrom(index -> null));
    }

    @Test
    void findsASelectedProfileThatTheIndexForgot() {
        UUID listed = UUID.randomUUID();
        UUID selectedOnly = UUID.randomUUID();
        Map<PlayerField<String>, String> indexes = Map.of(
                ProfileIndexes.SKYBLOCK, selectedOnly + ";" + listed);

        assertEquals(List.of(listed, selectedOnly), PlayerWipeService.profileIdsFrom(indexes::get));
    }

    @Test
    void listsAProfileHeldByBothIndexesOnlyOnce() {
        UUID shared = UUID.randomUUID();
        Map<PlayerField<String>, String> indexes = Map.of(
                ProfileIndexes.SKYBLOCK, ";" + shared,
                ProfileIndexes.RAVENGARD, ";" + shared);

        assertEquals(List.of(shared), PlayerWipeService.profileIdsFrom(indexes::get));
    }

    @Test
    void purgesTheMongoCollectionsKeyedByTheWipedPlayer() {
        List<String> names = PlayerWipeService.OWNED_COLLECTIONS.stream()
                .map(PlayerWipeService.OwnedCollection::name)
                .toList();

        assertTrue(names.contains("ravengard_tracked_items"));
        assertTrue(names.contains("ravengard_profiles"));
        assertTrue(names.contains("bedwars_stat_events"));
        assertFalse(names.contains("ravengard_regions"));
    }

    @Test
    void matchesRowsByOwnerAndByEveryProfileTheIndexesListed() {
        UUID playerUuid = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        PlayerWipeService.OwnedCollection owned = named("ravengard_tracked_items");

        String withProfiles = PlayerWipeService.ownedFilter(owned, playerUuid, List.of(profileId.toString()))
                .toBsonDocument().toJson();
        assertTrue(withProfiles.contains(playerUuid.toString()));
        assertTrue(withProfiles.contains(profileId.toString()));
        assertTrue(withProfiles.contains(owned.ownerField()));
        assertTrue(withProfiles.contains(owned.profileField()));

        String withoutProfiles = PlayerWipeService.ownedFilter(owned, playerUuid, List.of())
                .toBsonDocument().toJson();
        assertTrue(withoutProfiles.contains(playerUuid.toString()));
        assertFalse(withoutProfiles.contains(owned.profileField()));
    }

    @Test
    void matchesRowsByOwnerAloneWhenTheCollectionHasNoProfileColumn() {
        UUID playerUuid = UUID.randomUUID();
        PlayerWipeService.OwnedCollection owned = named("bedwars_stat_events");

        assertNull(owned.profileField());
        String filter = PlayerWipeService.ownedFilter(owned, playerUuid, List.of(UUID.randomUUID().toString()))
                .toBsonDocument().toJson();
        assertTrue(filter.contains(playerUuid.toString()));
        assertFalse(filter.contains("$or"));
    }

    private static PlayerWipeService.OwnedCollection named(String name) {
        return PlayerWipeService.OWNED_COLLECTIONS.stream()
                .filter(owned -> owned.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void refusesASecondWipeWhileOneIsRunning() {
        UUID playerUuid = UUID.randomUUID();

        assertTrue(PlayerWipeService.begin(playerUuid));
        assertTrue(PlayerWipeService.isInProgress(playerUuid));
        assertFalse(PlayerWipeService.begin(playerUuid));

        PlayerWipeService.finish(playerUuid);
        assertFalse(PlayerWipeService.isInProgress(playerUuid));
        assertTrue(PlayerWipeService.begin(playerUuid));
        PlayerWipeService.finish(playerUuid);
    }

    private static class RecordingAccountStore implements PlayerWipeService.AccountStore {
        private final Map<PlayerField<String>, String> stored = new LinkedHashMap<>();
        private final List<String> calls = new ArrayList<>();

        @Override
        public String read(PlayerField<String> field) {
            calls.add("read " + field.fullKey());
            return stored.get(field);
        }

        @Override
        public void write(PlayerField<String> field, String value) {
            calls.add("write " + field.fullKey());
            stored.put(field, value);
        }

        @Override
        public void deleteDocument() {
            calls.add("delete");
            stored.clear();
        }
    }
}
