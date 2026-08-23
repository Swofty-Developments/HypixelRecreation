package net.swofty.commons.skyblock;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoopStorageTest {
    @Test
    void keepsTheKeysTheCoopRosterIsStoredUnder() {
        assertEquals("hsb:coop:", CoopStorage.COOP_PREFIX);
        assertEquals("hsb:coop:bymember", CoopStorage.BY_MEMBER);
        assertEquals("hsb:coop:byprofile", CoopStorage.BY_PROFILE);
        assertEquals("hsb:island:", IslandStorage.ISLAND_PREFIX);
    }

    @Test
    void abandonsACoopOnlyOnceItsLastMemberIsGone() {
        UUID leaving = UUID.randomUUID();
        UUID leavingProfile = UUID.randomUUID();
        UUID staying = UUID.randomUUID();
        UUID stayingProfile = UUID.randomUUID();

        Document shared = roster(List.of(leaving, staying), List.of(), List.of(leavingProfile, stayingProfile));
        CoopStorage.withoutMember(shared, leaving, leavingProfile);

        assertEquals(List.of(staying.toString()), CoopStorage.ids(shared, CoopStorage.MEMBERS));
        assertEquals(List.of(stayingProfile.toString()), CoopStorage.ids(shared, CoopStorage.MEMBER_PROFILES));
        assertFalse(CoopStorage.isAbandoned(shared));

        CoopStorage.withoutMember(shared, staying, stayingProfile);
        assertTrue(CoopStorage.isAbandoned(shared));
    }

    @Test
    void dropsAPendingInviteAlongsideTheMembership() {
        UUID leaving = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Document shared = roster(List.of(leaving), List.of(leaving), List.of(profileId));
        CoopStorage.withoutMember(shared, leaving, profileId);

        assertTrue(CoopStorage.ids(shared, CoopStorage.MEMBERS).isEmpty());
        assertTrue(CoopStorage.ids(shared, CoopStorage.MEMBER_INVITES).isEmpty());
        assertTrue(CoopStorage.isAbandoned(shared));
    }

    @Test
    void readsARosterThatIsMissingItsListsAsAnEmptyOne() {
        Document empty = new Document("_id", UUID.randomUUID().toString());

        assertTrue(CoopStorage.isAbandoned(empty));
        assertEquals(List.of(), CoopStorage.ids(empty, CoopStorage.MEMBERS));
    }

    @Test
    void leavesACoopWithOnlyInvitesLeftStandingForItsRemainingProfile() {
        UUID invited = UUID.randomUUID();
        UUID stayingProfile = UUID.randomUUID();

        Document shared = roster(List.of(), List.of(invited), List.of(stayingProfile));
        assertFalse(CoopStorage.isAbandoned(shared));
    }

    private static Document roster(List<UUID> members, List<UUID> invites, List<UUID> profiles) {
        Document document = new Document("_id", UUID.randomUUID().toString());
        document.put(CoopStorage.MEMBERS, new ArrayList<>(members.stream().map(UUID::toString).toList()));
        document.put(CoopStorage.MEMBER_INVITES, new ArrayList<>(invites.stream().map(UUID::toString).toList()));
        document.put(CoopStorage.MEMBER_PROFILES, new ArrayList<>(profiles.stream().map(UUID::toString).toList()));
        return document;
    }
}
