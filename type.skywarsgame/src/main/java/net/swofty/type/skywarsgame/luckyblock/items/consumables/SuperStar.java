package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class SuperStar implements LuckyBlockConsumable {

    private static final int DURATION_TICKS = 15 * 20;

    @Override
    public String getId() {
        return "super_star";
    }

    @Override
    public String getDisplayName() {
        return "Super Star";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.NETHER_STAR, """
                <6><l>Super Star</l>
                <7>Become nearly invincible
                <7>for 15 seconds!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.RESISTANCE, (byte) 4, DURATION_TICKS));
        player.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 3, DURATION_TICKS));
        player.addEffect(new Potion(PotionEffect.GLOWING, (byte) 0, DURATION_TICKS));

        player.sendMessage("<6>You are INVINCIBLE for 15 seconds!");
    }
}
