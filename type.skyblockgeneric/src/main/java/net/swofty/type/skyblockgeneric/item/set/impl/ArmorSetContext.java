package net.swofty.type.skyblockgeneric.item.set.impl;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public record ArmorSetContext(
        ArmorSetRegistry registry,
        @Nullable SkyBlockPlayer player,
        Set<ItemType> wornItems,
        int wornPieces
) {
    public ArmorSetContext {
        wornItems = Set.copyOf(wornItems);
    }

    public static ArmorSetContext of(ArmorSetRegistry registry, @Nullable SkyBlockPlayer player) {
        if (player == null) return preview(registry, Set.of());
        Set<ItemType> wornItems = getWornItems(player);
        return new ArmorSetContext(registry, player, wornItems, registry.getWornPieceCount(wornItems));
    }

    public static Set<ItemType> getWornItems(SkyBlockPlayer player) {
        Set<ItemType> wornItems = EnumSet.noneOf(ItemType.class);
        for (SkyBlockItem item : player.getArmor()) {
            if (item == null) continue;
            ItemType itemType = item.getAttributeHandler().getPotentialType();
            if (itemType != null) wornItems.add(itemType);
        }
        return wornItems;
    }

    public static ArmorSetContext preview(ArmorSetRegistry registry, Set<ItemType> wornItems) {
        return new ArmorSetContext(registry, null, wornItems, registry.getWornPieceCount(wornItems));
    }

    public double tierValue(double... values) {
        if (values.length == 0) return 0;
        int index = Math.clamp(wornPieces - 1, 0, values.length - 1);
        return values[index];
    }
}
