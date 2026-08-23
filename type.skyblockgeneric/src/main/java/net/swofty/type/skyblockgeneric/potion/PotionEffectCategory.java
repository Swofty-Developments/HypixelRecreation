package net.swofty.type.skyblockgeneric.potion;

import lombok.Getter;

@Getter
public enum PotionEffectCategory {
    BUFF("Buff", "a"),       // Positive stat effects
    DEBUFF("Debuff", "c"),   // Negative effects (e.g., Weakness)
    INSTANT("Instant", "d"), // Immediate effect (Healing, Harming)
    UTILITY("Utility", "b"), // Non-stat effects (Night Vision, Fire Resistance)
    BASE("Base", "7");       // Base potions (Awkward, Thick)

    private final String displayName;
    private final String colorToken;

    PotionEffectCategory(String displayName, String colorToken) {
        this.displayName = displayName;
        this.colorToken = colorToken;
    }

    public String getColor() {
        return "<" + colorToken + ">";
    }
}
