package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class FistUpgrade implements LuckyBlockConsumable {

    public static final String ID = "fist_upgrade";
    public static final String PERK_KEY = "fist_upgrade";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Fist Upgrade";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.IRON_NUGGET, """
                <e>Fist Upgrade

                <7>Permanently upgrades your
                <7>fist damage for the rest
                <7>of the game!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        player.getActivePerks().add(PERK_KEY);
        player.sendMessage("<e>Your fists have been upgraded!");
    }
}
