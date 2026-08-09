package net.swofty.type.skyblockgeneric.item.set.impl;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public interface ArmorSet {
    String getName();

    ArrayList<String> getDescription();

    default ArrayList<String> getLore() {
        ArmorSetRegistry registry = getRegistry();
        if (registry == null) return getDescription();

        LinkedHashSet<String> lore = new LinkedHashSet<>();
        for (ItemType itemType : registry.getItemTypes()) {
            ConfigurableSkyBlockItem item = ConfigurableSkyBlockItem.getFromID(itemType.name());
            if (item != null && item.getLore() != null) lore.addAll(item.getLore());
        }
        return lore.isEmpty() ? getDescription() : new ArrayList<>(lore);
    }

    default ArmorSetRegistry getRegistry() {
        return ArmorSetRegistry.getArmorSet(this.getClass());
    }

    default ItemStatistics getStatistics() {
        return ItemStatistics.empty();
    }

    default ItemStatistics getStatistics(SkyBlockPlayer player) {
        return isWearingSet(player) ? getStatistics() : ItemStatistics.empty();
    }

    default boolean isWearingSet(SkyBlockPlayer player) {
        return player.getArmorSet() != null && player.getArmorSet().equals(getRegistry());
    }

    default int getWornPieceCount(SkyBlockPlayer player) {
        ArmorSetRegistry registry = getRegistry();
        if (registry == null) return 0;

        ItemType helmet = new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType();
        ItemType chestplate = new SkyBlockItem(player.getChestplate()).getAttributeHandler().getPotentialType();
        ItemType leggings = new SkyBlockItem(player.getLeggings()).getAttributeHandler().getPotentialType();
        ItemType boots = new SkyBlockItem(player.getBoots()).getAttributeHandler().getPotentialType();

        return registry.getWornPieceCount(boots, leggings, chestplate, helmet);
    }

    default boolean hasAtLeastPieces(SkyBlockPlayer player, int minPieces) {
        return getWornPieceCount(player) >= minPieces;
    }

    default List<SkyBlockPlayer> getWearingSet() {
        ArrayList<SkyBlockPlayer> toReturn = new ArrayList<>();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getArmorSet() != null && player.getArmorSet().equals(getRegistry())) {
                toReturn.add(player);
            }
        }
        return toReturn;
    }
}
