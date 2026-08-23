package net.swofty.commons.data;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileIndexesTest {
    @Test
    void addressesBothIndexesOnTheAccountDocument() {
        assertEquals("hypixel:_profiles_index", ProfileIndexes.SKYBLOCK.fullKey());
        assertEquals("ravengard:_profiles_index", ProfileIndexes.RAVENGARD.fullKey());
        assertEquals(List.of(ProfileIndexes.SKYBLOCK, ProfileIndexes.RAVENGARD), ProfileIndexes.ALL);
    }

    @Test
    void decodesTheSelectedProfileAndTheOwnedOnes() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        String stored = second + ";" + first + "," + second;

        assertEquals(second, ProfileIndexes.decodeSelected(stored));
        assertEquals(List.of(first, second), ProfileIndexes.decodeProfiles(stored));
    }

    @Test
    void decodesAnIndexThatHasNoSelectionYet() {
        UUID only = UUID.randomUUID();

        assertNull(ProfileIndexes.decodeSelected(";" + only));
        assertEquals(List.of(only), ProfileIndexes.decodeProfiles(";" + only));
    }

    @Test
    void treatsAMissingOrBrokenIndexAsNoProfiles() {
        assertTrue(ProfileIndexes.decodeProfiles(null).isEmpty());
        assertTrue(ProfileIndexes.decodeProfiles("").isEmpty());
        assertTrue(ProfileIndexes.decodeProfiles(";").isEmpty());
        assertTrue(ProfileIndexes.decodeProfiles("not-a-uuid").isEmpty());
        assertTrue(ProfileIndexes.decodeProfiles(";not-a-uuid").isEmpty());
        assertNull(ProfileIndexes.decodeSelected("not-a-uuid;"));
    }

    @Test
    void listsEachProfileOnlyOnce() {
        UUID profileId = UUID.randomUUID();

        assertEquals(List.of(profileId), ProfileIndexes.decodeProfiles(";" + profileId + "," + profileId));
    }
}
