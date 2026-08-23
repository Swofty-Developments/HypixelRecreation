package net.swofty.type.ravengardgeneric.data;

import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.profile.RavengardProfile;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RavengardProfileMigrationTest {
    private static final UUID OWNER = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OLDEST = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID MIDDLE = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
    private static final UUID NEWEST = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003");

    @Test
    void readsTheLegacyPointerInTheDatapointEncoding() {
        assertEquals(OLDEST, RavengardProfileMigration.legacySelectedProfile("\"" + OLDEST + "\""));
        assertEquals(OLDEST, RavengardProfileMigration.legacySelectedProfile(OLDEST.toString()));
    }

    @Test
    void treatsAnEmptyOrUnreadablePointerAsNoLegacyProfile() {
        assertNull(RavengardProfileMigration.legacySelectedProfile(null));
        assertNull(RavengardProfileMigration.legacySelectedProfile(""));
        assertNull(RavengardProfileMigration.legacySelectedProfile("\"\""));
        assertNull(RavengardProfileMigration.legacySelectedProfile("\"not-a-uuid\""));
    }

    @Test
    void onlyReadsMongoForAPlayerWhoHasNoIndexAndDidPlayRavengard() {
        assertTrue(RavengardProfileMigration.shouldReadLegacy(null, "\"" + OLDEST + "\""));
        assertFalse(RavengardProfileMigration.shouldReadLegacy(null, "\"\""));
        assertFalse(RavengardProfileMigration.shouldReadLegacy(null, null));
        assertFalse(RavengardProfileMigration.shouldReadLegacy(";", "\"" + OLDEST + "\""));
        assertFalse(RavengardProfileMigration.shouldReadLegacy(OLDEST + ";" + OLDEST, "\"" + OLDEST + "\""));
    }

    @Test
    void ordersMigratedProfilesByCreationAndKeepsTheLegacySelection() {
        List<RavengardProfile> legacy = List.of(
                legacyProfile(NEWEST, 300L),
                legacyProfile(OLDEST, 100L),
                legacyProfile(MIDDLE, 200L));

        RavengardProfileIndex index = RavengardProfileMigration.indexOf(legacy, MIDDLE);

        assertEquals(List.of(OLDEST, MIDDLE, NEWEST), index.profiles());
        assertEquals(MIDDLE, index.selected());
    }

    @Test
    void selectsTheOldestProfileWhenTheLegacyPointerIsGone() {
        List<RavengardProfile> legacy = List.of(legacyProfile(NEWEST, 300L), legacyProfile(OLDEST, 100L));

        assertEquals(OLDEST, RavengardProfileMigration.indexOf(legacy, null).selected());
        assertEquals(OLDEST, RavengardProfileMigration.indexOf(legacy, MIDDLE).selected());
    }

    @Test
    void migratesNothingForAPlayerWithoutLegacyProfiles() {
        RavengardProfileIndex index = RavengardProfileMigration.indexOf(List.of(), OLDEST);

        assertEquals(RavengardProfileIndex.empty(), index);
        assertNull(index.selected());
        assertTrue(index.profiles().isEmpty());
    }

    @Test
    void carriesTheWholeLegacyDocumentOntoTheProfileModel() {
        Document document = new Document("_id", OLDEST.toString())
                .append("_owner", OWNER.toString())
                .append("class", "HUNTER")
                .append("level", 4)
                .append("experience", 17)
                .append("crowns", 250)
                .append("ability_points", 2)
                .append("tutorial", false)
                .append("playtime_seconds", 4321L)
                .append("created", 1600000000000L)
                .append("intros", List.of("diago"))
                .append("inventory", new Document("3", "{id:\"minecraft:bread\"}"));

        RavengardProfile profile = RavengardProfile.fromDocument(document);

        assertEquals(OLDEST, profile.getId());
        assertEquals(OWNER, profile.getOwner());
        assertEquals(RavengardClass.HUNTER, profile.getProfileClass());
        assertEquals(4, profile.getLevel());
        assertEquals(17, profile.getExperience());
        assertEquals(250, profile.getCrowns());
        assertEquals(2, profile.getAbilityPoints());
        assertFalse(profile.isTutorial());
        assertEquals(4321L, profile.getPlaytimeSeconds());
        assertEquals(1600000000000L, profile.getCreated());
        assertEquals(java.util.Set.of("diago"), profile.getIntros());
        assertEquals("{id:\"minecraft:bread\"}", profile.getInventory().get(3));
    }

    private static RavengardProfile legacyProfile(UUID id, long created) {
        RavengardProfile profile = new RavengardProfile(id, OWNER);
        profile.setCreated(created);
        return profile;
    }
}
