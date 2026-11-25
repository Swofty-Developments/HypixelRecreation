package net.swofty.type.skyblockgeneric.item.set.impl;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface ArmorSet {
    String getName();

    ArrayList<String> getDescription();

    default List<ArmorSetEffect> getEffects() {
        ArmorSetRegistry registry = getRegistry();
        int requiredPieces = registry == null ? 4 : registry.getPieceCount();
        return List.of(new ArmorSetEffect() {
            @Override
            public String getName() {
                return ArmorSet.this.getName();
            }

            @Override
            public ArmorSetBonusType getType() {
                return ArmorSetBonusType.FULL_SET;
            }

            @Override
            public List<String> getDescription(ArmorSetContext context) {
                return ArmorSet.this.getDescription().stream()
                        .map(line -> MiniMessage.miniMessage().serialize(
                                LegacyComponentSerializer.legacySection().deserialize(line.replace('&', '§'))))
                        .toList();
            }

            @Override
            public int getRequiredPieces(ArmorSetContext context) {
                return requiredPieces;
            }

            @Override
            public ItemStatistics getStatistics(ArmorSetContext context) {
                return ArmorSet.this.getStatistics();
            }
        });
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

    default Set<ItemType> getWornItemTypes(SkyBlockPlayer player) {
        Set<ItemType> itemTypes = EnumSet.noneOf(ItemType.class);
        for (SkyBlockItem item : player.getArmor()) {
            if (item == null) continue;
            ItemType itemType = item.getAttributeHandler().getPotentialType();
            if (itemType != null) itemTypes.add(itemType);
        }
        return itemTypes;
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
