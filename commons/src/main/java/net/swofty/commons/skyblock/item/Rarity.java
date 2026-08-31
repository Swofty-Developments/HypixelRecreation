package net.swofty.commons.skyblock.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Getter
@AllArgsConstructor
public enum Rarity {
    COMMON(NamedTextColor.WHITE, "hypixel_skyblock:common"),
    UNCOMMON(NamedTextColor.GREEN, "hypixel_skyblock:uncommon"),
    RARE(NamedTextColor.BLUE, "hypixel_skyblock:rare"),
    EPIC(NamedTextColor.DARK_PURPLE, "hypixel_skyblock:epic"),
    LEGENDARY(NamedTextColor.GOLD, "hypixel_skyblock:legendary"),
    MYTHIC(NamedTextColor.LIGHT_PURPLE, "hypixel_skyblock:mythic"),
    DIVINE(NamedTextColor.AQUA, "hypixel_skyblock:divine"),
    SPECIAL(NamedTextColor.RED, "hypixel_skyblock:special", false),
    VERY_SPECIAL(NamedTextColor.RED, "hypixel_skyblock:very_special", false),
    ADMIN(NamedTextColor.DARK_RED, "hypixel_skyblock:admin", false),
    ;

    private final TextColor color;
    private final String tooltipStyle;
    private final boolean canReforge;

    Rarity(TextColor color, String tooltipStyle) {
        this(color, tooltipStyle, true);
    }

    public Rarity upgrade() {
        return values()[Math.min(this.ordinal() + 1, values().length - 1)];
    }

    public Rarity downgrade() {
        if (this.ordinal() - 1 < 0)
            return this;
        return values()[this.ordinal() - 1];
    }

    public boolean isAtLeast(Rarity rarity) {
        return ordinal() >= rarity.ordinal();
    }

    public boolean isAtMost(Rarity rarity) {
        return ordinal() <= rarity.ordinal();
    }

    public Component getDisplay() {
        return Component.text(name().replace("_", " "), color, TextDecoration.BOLD);
    }

    public Component getDisplayCapitalized() {
        return Component.text(name().charAt(0) + name().toLowerCase().replace("_", " ").substring(1), color, TextDecoration.BOLD);
    }

    @Deprecated(forRemoval = true)
    public String getLegacyColor() {
        return LegacyComponentSerializer.legacySection().serialize(Component.text("|", color)).replace("|", "");
    }

    @Deprecated(forRemoval = true)
    public String getLegacyDisplay() {
        return LegacyComponentSerializer.legacySection().serialize(getDisplay());
    }

    public static Rarity getRarity(String string) {
        try {
            return Rarity.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
