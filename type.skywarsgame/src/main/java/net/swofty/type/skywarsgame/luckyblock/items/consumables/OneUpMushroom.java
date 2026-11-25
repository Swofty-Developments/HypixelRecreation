package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class OneUpMushroom implements LuckyBlockConsumable {

    private static final float HEAL_AMOUNT = 10.0f;

    @Override
    public String getId() {
        return "one_up_mushroom";
    }

    @Override
    public String getDisplayName() {
        return "1-up Mushroom";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.RED_MUSHROOM, """
                <c><l>1-up Mushroom</l>
                <7>Instantly restores 5 hearts!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        float newHealth = Math.min(player.getHealth() + HEAL_AMOUNT, 20.0f);
        player.setHealth(newHealth);

        player.sendMessage("<a>1-UP! Health restored!");
    }
}
