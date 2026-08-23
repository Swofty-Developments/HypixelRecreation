package net.swofty.type.skywarslobby.perk;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;

/**
 * Rarity levels for SkyWars perks
 */
@Getter
public enum SkywarsPerkRarity {
    COMMON(NamedTextColor.GREEN, "COMMON", Material.LIME_STAINED_GLASS_PANE, 1),
    RARE(NamedTextColor.BLUE, "RARE", Material.BLUE_STAINED_GLASS_PANE, 2),
    LEGENDARY(NamedTextColor.GOLD, "LEGENDARY", Material.ORANGE_STAINED_GLASS_PANE, 3),
    MYTHICAL(NamedTextColor.LIGHT_PURPLE, "MYTHICAL", Material.PINK_STAINED_GLASS_PANE, 4);

    private final TextColor color;
    private final String displayName;
    private final Material glassPane;
    private final int sortOrder;

    SkywarsPerkRarity(TextColor color, String displayName, Material glassPane, int sortOrder) {
        this.color = color;
        this.displayName = displayName;
        this.glassPane = glassPane;
        this.sortOrder = sortOrder;
    }

    /**
     * Get the formatted display name with colour
     */
    public Text getFormattedName() {
        return Text.of("<color:{}>{}", color, displayName);
    }
}
