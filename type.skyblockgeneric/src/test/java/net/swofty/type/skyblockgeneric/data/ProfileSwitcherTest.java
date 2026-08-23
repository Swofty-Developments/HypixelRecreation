package net.swofty.type.skyblockgeneric.data;

import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProfileSwitcherTest {

    @Test
    void refusesProfilesThatAreNotOnTheAccount() {
        UUID selected = UUID.randomUUID();
        SkyBlockPlayerProfiles profiles = profilesWith(selected);

        assertEquals(ProfileSwitcher.Refusal.UNKNOWN_PROFILE,
                ProfileSwitcher.refuse(profiles, selected, UUID.randomUUID()));
    }

    @Test
    void refusesAMissingTargetProfile() {
        UUID selected = UUID.randomUUID();

        assertEquals(ProfileSwitcher.Refusal.UNKNOWN_PROFILE,
                ProfileSwitcher.refuse(profilesWith(selected), selected, null));
    }

    @Test
    void refusesWhenNoProfilesAreLoaded() {
        assertEquals(ProfileSwitcher.Refusal.NO_PROFILES,
                ProfileSwitcher.refuse(null, null, UUID.randomUUID()));
        assertEquals(ProfileSwitcher.Refusal.NO_PROFILES,
                ProfileSwitcher.refuse(new SkyBlockPlayerProfiles(), null, UUID.randomUUID()));
    }

    @Test
    void refusesTheProfileThatIsAlreadyLoaded() {
        UUID selected = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        SkyBlockPlayerProfiles profiles = profilesWith(selected, other);

        assertEquals(ProfileSwitcher.Refusal.ALREADY_SELECTED,
                ProfileSwitcher.refuse(profiles, selected, selected));
    }

    @Test
    void refusesTheSelectedProfileWhenNoHandlerIsLoaded() {
        UUID selected = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        SkyBlockPlayerProfiles profiles = profilesWith(selected, other);

        assertEquals(ProfileSwitcher.Refusal.ALREADY_SELECTED,
                ProfileSwitcher.refuse(profiles, null, selected));
    }

    @Test
    void allowsAnotherProfileOnTheAccount() {
        UUID selected = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        SkyBlockPlayerProfiles profiles = profilesWith(selected, other);

        assertNull(ProfileSwitcher.refuse(profiles, selected, other));
    }

    private static SkyBlockPlayerProfiles profilesWith(UUID selected, UUID... additional) {
        SkyBlockPlayerProfiles profiles = new SkyBlockPlayerProfiles(UUID.randomUUID());
        profiles.addProfile(selected);
        for (UUID profile : additional) profiles.addProfile(profile);
        profiles.setCurrentlySelected(selected);
        return profiles;
    }
}
