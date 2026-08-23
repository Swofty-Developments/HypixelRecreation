package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class PlusHealthItem implements LuckyBlockConsumable {

    @Override
    public String getId() {
        return "plus_four_hearts";
    }

    @Override
    public String getDisplayName() {
        return "+4 Hearts";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.RED_DYE, """
                <c><l>+4 Hearts</l>
                <7>Gives you 4 extra hearts
                <7>for the rest of the game!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 3, Integer.MAX_VALUE));

        player.setHealth(20.0f);

        player.sendMessage("<6>You gained 4 extra golden hearts!");
    }
}
