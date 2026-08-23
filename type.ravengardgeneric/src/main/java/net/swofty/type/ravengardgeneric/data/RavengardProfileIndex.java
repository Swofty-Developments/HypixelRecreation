package net.swofty.type.ravengardgeneric.data;

import net.swofty.commons.data.SwoftyData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record RavengardProfileIndex(UUID selected, List<UUID> profiles) {
    private static final RavengardProfileIndex EMPTY = new RavengardProfileIndex(null, List.of());
    private static final String SELECTED_SEPARATOR = ";";
    private static final String PROFILE_SEPARATOR = ",";

    public RavengardProfileIndex {
        profiles = List.copyOf(profiles);
    }

    public static RavengardProfileIndex empty() {
        return EMPTY;
    }

    public static RavengardProfileIndex read(UUID player) {
        String stored = SwoftyData.account().get(player, RavengardProfileFields.PROFILES_INDEX);
        if (stored != null) return decode(stored);

        RavengardProfileIndex migrated = RavengardProfileMigration.migrate(player);
        if (migrated.profiles().isEmpty()) return EMPTY;

        write(player, migrated);
        return migrated;
    }

    public static void write(UUID player, RavengardProfileIndex index) {
        SwoftyData.account().set(player, RavengardProfileFields.PROFILES_INDEX, index.encode());
    }

    public static boolean isStored(UUID player) {
        return SwoftyData.account().get(player, RavengardProfileFields.PROFILES_INDEX) != null;
    }

    public String encode() {
        return (selected == null ? "" : selected.toString())
                + SELECTED_SEPARATOR
                + profiles.stream().map(UUID::toString).collect(Collectors.joining(PROFILE_SEPARATOR));
    }

    public static RavengardProfileIndex decode(String stored) {
        if (stored == null || stored.isEmpty()) return EMPTY;

        String[] parts = stored.split(SELECTED_SEPARATOR, 2);
        List<UUID> profiles = new ArrayList<>();
        if (parts.length > 1 && !parts[1].isEmpty()) {
            for (String profile : parts[1].split(PROFILE_SEPARATOR)) {
                UUID parsed = parse(profile);
                if (parsed != null && !profiles.contains(parsed)) profiles.add(parsed);
            }
        }

        UUID selected = parse(parts[0]);
        if (selected != null && !profiles.contains(selected)) selected = null;
        return new RavengardProfileIndex(selected, profiles);
    }

    public boolean contains(UUID profileId) {
        return profileId != null && profiles.contains(profileId);
    }

    public RavengardProfileIndex with(UUID profileId) {
        if (profileId == null || profiles.contains(profileId)) return this;
        List<UUID> updated = new ArrayList<>(profiles);
        updated.add(profileId);
        return new RavengardProfileIndex(selected, updated);
    }

    public RavengardProfileIndex withSelected(UUID profileId) {
        if (profileId == null) return new RavengardProfileIndex(null, profiles);
        RavengardProfileIndex holder = with(profileId);
        return new RavengardProfileIndex(profileId, holder.profiles());
    }

    public RavengardProfileIndex without(UUID profileId) {
        if (profileId == null || !profiles.contains(profileId)) return this;
        List<UUID> updated = new ArrayList<>(profiles);
        updated.remove(profileId);

        UUID next = profileId.equals(selected)
                ? (updated.isEmpty() ? null : updated.getFirst())
                : selected;
        return new RavengardProfileIndex(next, updated);
    }

    private static UUID parse(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
