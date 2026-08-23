package net.swofty.type.skyblockgeneric.fishing.catches;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Optional;

/**
 * Enumerated trophy fish tier. Replaces the previous stringly-typed
 * deduction that scanned item IDs for "_DIAMOND"/"_GOLD" substrings.
 */
public enum TrophyTier {
    BRONZE(NamedTextColor.RED, "Bronze"),
    SILVER(NamedTextColor.GRAY, "Silver"),
    GOLD(NamedTextColor.GOLD, "Gold"),
    DIAMOND(NamedTextColor.AQUA, "Diamond");

    private final TextColor colour;
    private final String displayName;

    TrophyTier(TextColor colour, String displayName) {
        this.colour = colour;
        this.displayName = displayName;
    }

    public TextColor colour() {
        return colour;
    }

    public String displayName() {
        return displayName;
    }

    public String suffix() {
        return "_" + name();
    }

    public static Optional<TrophyTier> fromItemId(String itemId) {
        if (itemId == null) return Optional.empty();
        for (TrophyTier tier : values()) {
            if (itemId.contains(tier.suffix())) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    public static List<TrophyTier> orderedDescending() {
        return List.of(DIAMOND, GOLD, SILVER, BRONZE);
    }
}
