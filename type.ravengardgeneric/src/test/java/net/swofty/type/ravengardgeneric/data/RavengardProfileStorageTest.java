package net.swofty.type.ravengardgeneric.data;

import net.swofty.LinkedField;
import net.swofty.PlayerField;
import net.swofty.transaction.Transaction;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.data.datapoints.DatapointRavengardBoolean;
import net.swofty.type.ravengardgeneric.data.datapoints.DatapointRavengardInteger;
import net.swofty.type.ravengardgeneric.data.datapoints.DatapointRavengardString;
import net.swofty.type.ravengardgeneric.profile.RavengardProfile;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RavengardProfileStorageTest {
    private static final UUID PROFILE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OWNER = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void writesEveryProfileFieldInTheDatapointEncoding() {
        RavengardProfile profile = new RavengardProfile(PROFILE, OWNER);
        profile.setProfileClass(RavengardClass.SORCERER);
        profile.setLevel(7);
        profile.setExperience(42);
        profile.setCrowns(1234);
        profile.setAbilityPoints(3);
        profile.setTutorial(false);
        profile.setPlaytimeSeconds(9876L);
        profile.setCreated(1700000000000L);
        profile.getIntros().add("diago");
        profile.getDiscoveredRegions().add("NEVERMORE");
        profile.getInventory().put(8, "{id:\"minecraft:stone\"}");

        RecordingTransaction transaction = new RecordingTransaction();
        RavengardProfileStorage.writeAll(transaction, profile);

        assertEquals("\"" + OWNER + "\"", transaction.written.get(RavengardProfileFields.OWNER));
        assertEquals("1700000000000", transaction.written.get(RavengardProfileFields.CREATED));
        assertEquals("\"SORCERER\"", transaction.written.get(RavengardProfileFields.CLASS));
        assertEquals("7", transaction.written.get(RavengardProfileFields.LEVEL));
        assertEquals("42", transaction.written.get(RavengardProfileFields.EXPERIENCE));
        assertEquals("1234", transaction.written.get(RavengardProfileFields.CROWNS));
        assertEquals("3", transaction.written.get(RavengardProfileFields.ABILITY_POINTS));
        assertEquals("false", transaction.written.get(RavengardProfileFields.TUTORIAL));
        assertEquals("9876", transaction.written.get(RavengardProfileFields.PLAYTIME_SECONDS));
        assertEquals("[\"diago\"]", transaction.written.get(RavengardProfileFields.INTROS));
        assertEquals("[\"NEVERMORE\"]", transaction.written.get(RavengardProfileFields.DISCOVERED_REGIONS));
        assertEquals(RavengardProfileFields.PROFILE_FIELDS.size(), transaction.written.size());
    }

    @Test
    void writesAnAbsentClassAsTheDatapointDefault() {
        RavengardProfile profile = new RavengardProfile(PROFILE, OWNER);

        RecordingTransaction transaction = new RecordingTransaction();
        RavengardProfileStorage.writeAll(transaction, profile);

        assertEquals("\"\"", transaction.written.get(RavengardProfileFields.CLASS));
        assertEquals("1", transaction.written.get(RavengardProfileFields.LEVEL));
        assertEquals("true", transaction.written.get(RavengardProfileFields.TUTORIAL));
        assertEquals("0", transaction.written.get(RavengardProfileFields.CROWNS));
    }

    @Test
    void storesTheSameBytesTheOverlappingDatapointsWrite() {
        DatapointRavengardString profileClass = new DatapointRavengardString("ravengard_class", "SORCERER");
        DatapointRavengardInteger level = new DatapointRavengardInteger("ravengard_level", 7);
        DatapointRavengardInteger crowns = new DatapointRavengardInteger("ravengard_crowns", 1234);
        DatapointRavengardBoolean tutorial = new DatapointRavengardBoolean("ravengard_is_tutorial", false);

        assertEquals(profileClass.getSerializedValue(), RavengardProfileFields.STRING.serialize("SORCERER"));
        assertEquals(level.getSerializedValue(), RavengardProfileFields.INTEGER.serialize(7));
        assertEquals(crowns.getSerializedValue(), RavengardProfileFields.INTEGER.serialize(1234));
        assertEquals(tutorial.getSerializedValue(), RavengardProfileFields.BOOLEAN.serialize(false));
    }

    @Test
    void roundTripsAnInventoryThroughItsStoredForm() {
        Map<Integer, String> inventory = new LinkedHashMap<>();
        inventory.put(0, "{id:\"minecraft:diamond_sword\",count:1}");
        inventory.put(36, "{id:\"minecraft:bread\",count:16}");

        assertEquals(inventory, RavengardProfileStorage.decodeInventory(
                RavengardProfileStorage.encodeInventory(inventory)));
    }

    @Test
    void readsAnEmptyOrUnreadableInventoryAsNothing() {
        assertTrue(RavengardProfileStorage.decodeInventory(null).isEmpty());
        assertTrue(RavengardProfileStorage.decodeInventory("").isEmpty());
        assertTrue(RavengardProfileStorage.decodeInventory("not json").isEmpty());
        assertTrue(RavengardProfileStorage.decodeInventory(
                RavengardProfileStorage.encodeInventory(new HashMap<>())).isEmpty());
    }

    @Test
    void roundTripsTheStoredNameLists() {
        Set<String> names = new LinkedHashSet<>(Set.of("diago"));
        names.add("quartermaster");

        assertEquals(java.util.List.of("diago", "quartermaster"),
                RavengardProfileStorage.decodeNames(RavengardProfileStorage.encodeNames(names)));
        assertTrue(RavengardProfileStorage.decodeNames(null).isEmpty());
        assertTrue(RavengardProfileStorage.decodeNames("nonsense").isEmpty());
    }

    @Test
    void fallsBackWhenAStoredScalarIsMissingOrBroken() {
        assertEquals(5, RavengardProfileStorage.decodeInt(null, 5));
        assertEquals(5, RavengardProfileStorage.decodeInt("oops", 5));
        assertEquals(9, RavengardProfileStorage.decodeInt("9", 5));
        assertEquals(3L, RavengardProfileStorage.decodeLong("3", 0L));
        assertTrue(RavengardProfileStorage.decodeBoolean(null, true));
        assertEquals("KNIGHT", RavengardProfileStorage.decodeString("\"KNIGHT\"", ""));
        assertNull(RavengardProfileStorage.decodeString(null, null));
    }

    @Test
    void keepsEveryProfileFieldInTheRavengardNamespace() {
        for (PlayerField<String> field : RavengardProfileFields.PROFILE_FIELDS) {
            assertEquals(RavengardProfileFields.NAMESPACE, field.namespace());
            assertEquals(RavengardProfileFields.NAMESPACE + ":" + field.key(), field.fullKey());
        }
        assertEquals(RavengardProfileFields.NAMESPACE, RavengardProfileFields.PROFILES_INDEX.namespace());
        assertEquals("ravengard:_profiles_index", RavengardProfileFields.PROFILES_INDEX.fullKey());
    }

    @Test
    void backsTheOverlappingDatapointsWithProfileFieldsAndLeavesTheRestOnTheAccount() {
        for (RavengardDataHandler.Data data : RavengardDataHandler.Data.values()) {
            if (data == RavengardDataHandler.Data.DATA_VERSION) {
                assertNull(data.profileField());
                assertNotNull(data.accountField());
                assertEquals("game:ravengard_data_version", data.accountField().fullKey());
                continue;
            }
            assertNotNull(data.profileField());
            assertNull(data.accountField());
            assertTrue(RavengardProfileFields.PROFILE_FIELDS.contains(data.profileField()));
        }

        assertEquals(RavengardProfileFields.CLASS, RavengardDataHandler.Data.CLASS.profileField());
        assertEquals(RavengardProfileFields.LEVEL, RavengardDataHandler.Data.LEVEL.profileField());
        assertEquals(RavengardProfileFields.CROWNS, RavengardDataHandler.Data.CROWNS.profileField());
        assertEquals(RavengardProfileFields.TUTORIAL, RavengardDataHandler.Data.IS_TUTORIAL.profileField());
        assertNull(RavengardDataHandler.Data.fromKey("ravengard_selected_profile"));
    }

    private static final class RecordingTransaction implements Transaction {
        private final Map<PlayerField<?>, Object> written = new LinkedHashMap<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(PlayerField<T> field) {
            return (T) written.get(field);
        }

        @Override
        public <T> void set(PlayerField<T> field, T value) {
            written.put(field, value);
        }

        @Override
        public <T> void update(PlayerField<T> field, UnaryOperator<T> updater) {
            set(field, updater.apply(get(field)));
        }

        @Override
        public <K, T> T get(LinkedField<K, T> field) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <K, T> void set(LinkedField<K, T> field, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <K, T> void update(LinkedField<K, T> field, UnaryOperator<T> updater) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void abort() {
            throw new UnsupportedOperationException();
        }
    }
}
