package net.swofty.type.bedwarsgame.shop.impl;

import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.shop.UpgradeableItemTier;
import net.swofty.type.bedwarsgame.shop.UpgradeableShopItem;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PickaxeShopItem extends UpgradeableShopItem {

    public static final Tag<@NotNull Integer> PICKAXE_UPGRADE_TAG = Tag.Integer("pickaxe_upgrade");

    public PickaxeShopItem() {
        super("pickaxe", "Upgradeable Pickaxe", """
                        This is an upgradable item.
                        It will lose 1 tier upon death!

                        You will permanently respawn with at
                        least the lowest tier.""",
                List.of(
                        new UpgradeableItemTier("Wooden Pickaxe (Efficiency I)", _ -> 10, Currency.IRON, Material.WOODEN_PICKAXE),
                        new UpgradeableItemTier("Iron Pickaxe (Efficiency II) ", _ -> 10, Currency.IRON, Material.IRON_PICKAXE),
                        new UpgradeableItemTier("Golden Pickaxe (Efficiency III, Sharpness II)", _ -> 3, Currency.GOLD, Material.GOLDEN_PICKAXE),
                        new UpgradeableItemTier("Diamond Pickaxe (Efficiency III)", _ -> 6, Currency.GOLD, Material.DIAMOND_PICKAXE)
                ),
                PICKAXE_UPGRADE_TAG,
                DatapointBedWarsHotbar.HotbarItemType.PICKAXE
        );
    }

    @Override
    public boolean isSameType(Material material) {
        for (UpgradeableItemTier t : getTiers()) {
            if (t.material() == material) return true;
        }
        return false;
    }

}

