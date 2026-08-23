package net.swofty.type.ravengardgeneric.data;

import net.swofty.commons.data.ProfileIndexes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RavengardProfileIndexTest {
    private static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID THIRD = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void encodesTheSelectedProfileAheadOfTheOwnedOnes() {
        RavengardProfileIndex index = new RavengardProfileIndex(SECOND, List.of(FIRST, SECOND));

        assertEquals(SECOND + ";" + FIRST + "," + SECOND, index.encode());
    }

    @Test
    void encodesAnEmptyIndexWithoutLosingTheSeparator() {
        assertEquals(";", RavengardProfileIndex.empty().encode());
        assertEquals(RavengardProfileIndex.empty(), RavengardProfileIndex.decode(";"));
    }

    @Test
    void roundTripsThroughTheStoredForm() {
        RavengardProfileIndex index = new RavengardProfileIndex(THIRD, List.of(FIRST, SECOND, THIRD));
        RavengardProfileIndex decoded = RavengardProfileIndex.decode(index.encode());

        assertEquals(THIRD, decoded.selected());
        assertEquals(List.of(FIRST, SECOND, THIRD), decoded.profiles());
    }

    @Test
    void decodesAnIndexThatOnlyListsProfiles() {
        RavengardProfileIndex decoded = RavengardProfileIndex.decode(";" + FIRST + "," + SECOND);

        assertNull(decoded.selected());
        assertEquals(List.of(FIRST, SECOND), decoded.profiles());
    }

    @Test
    void dropsASelectionThatIsNoLongerOwned() {
        RavengardProfileIndex decoded = RavengardProfileIndex.decode(THIRD + ";" + FIRST);

        assertNull(decoded.selected());
        assertEquals(List.of(FIRST), decoded.profiles());
    }

    @Test
    void ignoresUnreadableAndRepeatedProfileIds() {
        RavengardProfileIndex decoded = RavengardProfileIndex.decode(";" + FIRST + ",not-a-uuid," + FIRST);

        assertEquals(List.of(FIRST), decoded.profiles());
    }

    @Test
    void decodesNothingAsAnEmptyIndex() {
        assertEquals(RavengardProfileIndex.empty(), RavengardProfileIndex.decode(null));
        assertEquals(RavengardProfileIndex.empty(), RavengardProfileIndex.decode(""));
    }

    @Test
    void selectingAProfileAlsoAddsIt() {
        RavengardProfileIndex index = RavengardProfileIndex.empty().withSelected(FIRST);

        assertEquals(FIRST, index.selected());
        assertEquals(List.of(FIRST), index.profiles());
        assertTrue(index.contains(FIRST));
    }

    @Test
    void addingAProfileLeavesTheSelectionAlone() {
        RavengardProfileIndex index = RavengardProfileIndex.empty().withSelected(FIRST).with(SECOND);

        assertEquals(FIRST, index.selected());
        assertEquals(List.of(FIRST, SECOND), index.profiles());
        assertEquals(index, index.with(SECOND));
    }

    @Test
    void removingTheSelectedProfileFallsBackToTheFirstRemainingOne() {
        RavengardProfileIndex index = new RavengardProfileIndex(FIRST, List.of(FIRST, SECOND)).without(FIRST);

        assertEquals(SECOND, index.selected());
        assertEquals(List.of(SECOND), index.profiles());
    }

    @Test
    void removingTheLastProfileLeavesNothingSelected() {
        RavengardProfileIndex index = new RavengardProfileIndex(FIRST, List.of(FIRST)).without(FIRST);

        assertNull(index.selected());
        assertTrue(index.profiles().isEmpty());
        assertFalse(index.contains(FIRST));
    }

    @Test
    void removingAnUnselectedProfileKeepsTheSelection() {
        RavengardProfileIndex index = new RavengardProfileIndex(FIRST, List.of(FIRST, SECOND)).without(SECOND);

        assertEquals(FIRST, index.selected());
        assertEquals(List.of(FIRST), index.profiles());
    }

    @Test
    void livesUnderTheAccountKeyEveryServerTypeCanFindIt() {
        assertSame(ProfileIndexes.RAVENGARD, RavengardProfileFields.PROFILES_INDEX);
        assertTrue(ProfileIndexes.ALL.contains(RavengardProfileFields.PROFILES_INDEX));

        RavengardProfileIndex index = new RavengardProfileIndex(SECOND, List.of(FIRST, SECOND));
        assertEquals(index.profiles(), ProfileIndexes.decodeProfiles(index.encode()));
        assertEquals(index.selected(), ProfileIndexes.decodeSelected(index.encode()));
    }
}
