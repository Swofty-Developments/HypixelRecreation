package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class FrogsPotion implements LuckyBlockItem {

    private static final int DURATION_TICKS = 60 * 20;

    @Override
    public String getId() {
        return "frogs_potion";
    }

    @Override
    public String getDisplayName() {
        return "Frog's Potion";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.POTION, """
                <a><l>Frog's Potion</l>
                <7>Leap like a frog!
                <7>Speed I and Jump Boost II
                <7>for 60 seconds.

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        holder.addEffect(new Potion(PotionEffect.SPEED, (byte) 0, DURATION_TICKS));
        holder.addEffect(new Potion(PotionEffect.JUMP_BOOST, (byte) 1, DURATION_TICKS));
        holder.sendMessage("<a>You feel like a frog!");
        return true;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }
}
