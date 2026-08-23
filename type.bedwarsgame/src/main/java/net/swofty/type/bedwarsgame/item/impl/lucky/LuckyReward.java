package net.swofty.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.bedwarsgame.util.BedWarsInventoryManipulator;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;
import net.swofty.type.generic.gui.inventory.ItemStacks;

public abstract class LuckyReward {
    private final String name;

    protected LuckyReward(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public abstract void apply(BedWarsPlayer player, Pos openedAt);

    protected void give(BedWarsPlayer player, ItemStack item) {
        BedWarsInventoryManipulator.addItemWithHotbarPriority(player, item, DatapointBedWarsHotbar.HotbarItemType.UTILITY);
    }

    protected static ItemStack named(Material material, String markup) {
        return ItemStacks.named(material, markup).build();
    }
}
