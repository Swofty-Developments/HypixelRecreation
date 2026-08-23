package net.swofty.type.ravengardgeneric.item.statistics;

import lombok.Getter;

/**
 * The stats an item can carry, mirroring SkyBlock's ItemStatistic: display name, tooltip order
 * and the pack's stat icon. Values render in the stat colour with the label in white, as every
 * captured tooltip shows.
 */
@Getter
public enum RavengardItemStatistic {
    DAMAGE("Damage", 0xE22F, true),
    RANGED_DAMAGE("Ranged Attack Damage", 0xE230, true),
    ATTACK_SPEED("Attack Speed", 0xE233, false),
    RANGED_ATTACK_SPEED("Ranged Attack Speed", 0xE233, false),
    ABILITY_DAMAGE_BOOST("Ability Damage Boost", 0xE22F, false, true),
    DEFENSE("Defense", 0xE231, true),
    RELOAD("Reload", 0xE232, false),
    COOLDOWN("Cooldown", 0xE22E, false);

    private final String displayName;
    private final int iconGlyph;
    private final boolean boostable;
    private final boolean percent;

    RavengardItemStatistic(String displayName, int iconGlyph, boolean boostable) {
        this(displayName, iconGlyph, boostable, false);
    }

    RavengardItemStatistic(String displayName, int iconGlyph, boolean boostable, boolean percent) {
        this.displayName = displayName;
        this.iconGlyph = iconGlyph;
        this.boostable = boostable;
        this.percent = percent;
    }

    public static RavengardItemStatistic fromKey(String key) {
        for (RavengardItemStatistic value : values()) {
            if (value.name().equalsIgnoreCase(key)) {
                return value;
            }
        }
        return null;
    }
}
