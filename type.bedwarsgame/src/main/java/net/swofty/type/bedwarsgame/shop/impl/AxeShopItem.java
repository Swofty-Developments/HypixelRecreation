package net.swofty.type.bedwarsgame.shop.impl;

import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.shop.UpgradeableItemTier;
import net.swofty.type.bedwarsgame.shop.UpgradeableShopItem;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;

import java.util.List;

public class AxeShopItem extends UpgradeableShopItem {

    public static final Tag<Integer> AXE_UPGRADE_TAG = Tag.Integer("axe_upgrade");

    public AxeShopItem() {
        super("axe", "Upgradeable Axe", """
                        This is an upgradable item.
                        It will lose 1 tier upon death!

                        You will permanently respawn with at
                        least the lowest tier.""",
                List.of(
                        new UpgradeableItemTier("Wooden Axe (Efficiency I)", _ -> 10, Currency.IRON, Material.WOODEN_AXE),
                        new UpgradeableItemTier("Stone Axe (Efficiency I)", _ ->10, Currency.IRON, Material.STONE_AXE),
                        new UpgradeableItemTier("Golden Axe (Efficiency II)", _ ->3, Currency.GOLD, Material.GOLDEN_AXE),
                        new UpgradeableItemTier("Diamond Axe (Efficiency III)", _ -> 6, Currency.GOLD, Material.DIAMOND_AXE)
                ),
                AXE_UPGRADE_TAG,
                DatapointBedWarsHotbar.HotbarItemType.AXE
        );
    }

    @Override
    public boolean isSameType(Material material) {
        return material == Material.WOODEN_AXE ||
               material == Material.STONE_AXE ||
               material == Material.GOLDEN_AXE ||
               material == Material.DIAMOND_AXE;
    }
}

