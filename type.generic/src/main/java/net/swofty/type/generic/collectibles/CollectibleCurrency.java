package net.swofty.type.generic.collectibles;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Locale;

@Getter
public enum CollectibleCurrency {
    BEDWARS_TOKENS("Tokens", NamedTextColor.DARK_GREEN),
    SKYWARS_COINS("Tokens", NamedTextColor.DARK_GREEN),
    MURDER_MYSTERY_COINS("Tokens", NamedTextColor.DARK_GREEN);

    private final String displayName;
    private final TextColor color;

    CollectibleCurrency(String displayName, TextColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public static CollectibleCurrency fromString(String value, CollectibleCurrency fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return CollectibleCurrency.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
