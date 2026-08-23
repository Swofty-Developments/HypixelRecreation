package net.swofty.type.ravengardgeneric.data;

import net.swofty.PlayerField;
import net.swofty.commons.data.SwoftyData;
import net.swofty.type.ravengardgeneric.data.monogdb.RavengardProfileDatabase;
import net.swofty.type.ravengardgeneric.profile.RavengardProfile;
import org.tinylog.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class RavengardProfileMigration {
    private RavengardProfileMigration() {
    }

    public static boolean shouldReadLegacy(String storedIndex, String legacySelected) {
        return storedIndex == null && legacySelectedProfile(legacySelected) != null;
    }

    public static UUID legacySelectedProfile(String stored) {
        String value = RavengardProfileStorage.decodeString(stored, stored);
        if (value == null || value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static RavengardProfileIndex indexOf(List<RavengardProfile> legacy, UUID legacySelected) {
        List<UUID> ordered = legacy.stream()
                .sorted(Comparator.comparingLong(RavengardProfile::getCreated))
                .map(RavengardProfile::getId)
                .distinct()
                .toList();
        if (ordered.isEmpty()) return RavengardProfileIndex.empty();

        UUID selected = ordered.contains(legacySelected) ? legacySelected : ordered.getFirst();
        return new RavengardProfileIndex(selected, ordered);
    }

    public static RavengardProfileIndex migrate(UUID player) {
        String legacySelected = SwoftyData.account().get(player, RavengardProfileFields.LEGACY_SELECTED_PROFILE);
        if (!shouldReadLegacy(null, legacySelected)) return RavengardProfileIndex.empty();

        List<RavengardProfile> legacy;
        try {
            legacy = RavengardProfileDatabase.byOwner(player);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to read the legacy Ravengard profiles of {}", player);
            return RavengardProfileIndex.empty();
        }
        if (legacy.isEmpty()) return RavengardProfileIndex.empty();

        for (RavengardProfile profile : legacy) {
            try {
                if (RavengardProfileStorage.exists(profile.getId())) continue;
                RavengardProfileStorage.writeAll(profile);
            } catch (Exception exception) {
                Logger.error(exception, "Failed to migrate the Ravengard profile {} of {}",
                        profile.getId(), player);
            }
        }

        RavengardProfileIndex index = indexOf(legacy, legacySelectedProfile(legacySelected));
        Logger.info("Migrated {} legacy Ravengard profile(s) onto the data api for {}",
                index.profiles().size(), player);
        return index;
    }

    public static void seedFromLegacyAccount(UUID player, UUID profileId) {
        copy(player, profileId, RavengardProfileFields.LEGACY_CLASS, RavengardProfileFields.CLASS);
        copy(player, profileId, RavengardProfileFields.LEGACY_LEVEL, RavengardProfileFields.LEVEL);
        copy(player, profileId, RavengardProfileFields.LEGACY_TUTORIAL, RavengardProfileFields.TUTORIAL);
        copy(player, profileId, RavengardProfileFields.LEGACY_CROWNS, RavengardProfileFields.CROWNS);
    }

    private static void copy(UUID player, UUID profileId, PlayerField<String> from, PlayerField<String> to) {
        String stored = SwoftyData.account().get(player, from);
        if (stored == null || stored.isEmpty()) return;
        SwoftyData.profile().set(profileId, to, stored);
    }
}
