package net.swofty.type.skywarslobby.perk;

import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Data class for SkyWars perks loaded from YAML configuration.
 */
@Getter
public class SkywarsPerk {
    private final String id;
    private final String name;
    private final Material iconMaterial;
    private final SkywarsPerkRarity rarity;
    private final int cost;
    private final int opalCost;
    private final boolean soulWellDrop;
    private final boolean global;
    private final boolean angelsDescentOnly;
    private final boolean tournamentExclusive;
    private final String description;
    private final String effectDescription;
    private final Set<String> modes;
    private final List<ItemStack> startingItems;

    public SkywarsPerk(String id, String name, Material iconMaterial, SkywarsPerkRarity rarity,
                       int cost, int opalCost, boolean soulWellDrop, boolean global,
                       boolean angelsDescentOnly, boolean tournamentExclusive,
                       String description, String effectDescription, Set<String> modes,
                       List<ItemStack> startingItems) {
        this.id = id;
        this.name = name;
        this.iconMaterial = iconMaterial;
        this.rarity = rarity;
        this.cost = cost;
        this.opalCost = opalCost;
        this.soulWellDrop = soulWellDrop;
        this.global = global;
        this.angelsDescentOnly = angelsDescentOnly;
        this.tournamentExclusive = tournamentExclusive;
        this.description = description;
        this.effectDescription = effectDescription;
        this.modes = modes;
        this.startingItems = startingItems != null ? startingItems : new ArrayList<>();
    }

    /**
     * Check if this perk is available for a specific mode
     */
    public boolean isAvailableFor(String mode) {
        return modes.contains(mode);
    }

    /**
     * Check if this perk costs coins
     */
    public boolean costsCoin() {
        return cost > 0;
    }

    /**
     * Check if this perk costs opals
     */
    public boolean costsOpal() {
        return opalCost > 0;
    }

    /**
     * Check if this perk is free
     */
    public boolean isFree() {
        return cost == 0 && opalCost == 0;
    }

    /**
     * Get formatted cost string for display
     */
    public String getFormattedCost() {
        if (opalCost > 0) {
            return Text.of("<9>{} Opal{}", opalCost, opalCost > 1 ? "s" : "").serialize();
        } else if (cost > 0) {
            return Text.of("<6>{:,}", cost).serialize();
        }
        return "<a>FREE";
    }

    /**
     * Get the lore for this perk in GUI
     */
    public List<String> getLore() {
        List<String> lore = new ArrayList<>();
        lore.add(Text.of("<7>{}", description).serialize());
        lore.add("");
        lore.add(Text.of("<7>Effect: {}", effectDescription).serialize());
        lore.add("");
        lore.add(Text.of("<7>Rarity: {}", rarity.getFormattedName()).serialize());
        return lore;
    }

    /**
     * Check if this perk provides starting items
     */
    public boolean hasStartingItems() {
        return !startingItems.isEmpty();
    }

    /**
     * Check if this perk can be purchased with coins
     */
    public boolean isPurchasableWithCoins() {
        return cost > 0 && !global && !tournamentExclusive && !angelsDescentOnly;
    }

    /**
     * Check if this perk is selectable by players (not global/tournament)
     */
    public boolean isSelectable() {
        return !global && !tournamentExclusive;
    }

    /**
     * Get special status text for display
     */
    public String getSpecialStatus() {
        if (tournamentExclusive) {
            return "Tournament Exclusive";
        }
        if (angelsDescentOnly) {
            return "Purchased from Angel's Descent";
        }
        return null;
    }
}
