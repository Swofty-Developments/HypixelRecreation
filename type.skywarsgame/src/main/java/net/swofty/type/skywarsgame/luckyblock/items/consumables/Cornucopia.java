package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class Cornucopia implements LuckyBlockConsumable {

    private static final int DURATION_TICKS = 10 * 60 * 20;

    @Override
    public String getId() {
        return "cornucopia";
    }

    @Override
    public String getDisplayName() {
        return "Cornucopia";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.GOLDEN_CARROT, """
                <6><l>Cornucopia</l>
                <7>The horn of plenty!
                <7>Grants Saturation and
                <7>Regeneration II for 10 minutes!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.SATURATION, (byte) 0, DURATION_TICKS));
        player.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, DURATION_TICKS));

        player.sendMessage("<6>The Cornucopia blesses you with abundance!");
    }
}
