package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class AbsorptionHearts implements LuckyBlockConsumable {

    public static final String ID = "absorption_hearts";
    private static final int DURATION_TICKS = 60 * 20;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Absorption Hearts";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.GOLDEN_APPLE, """
                <6>Absorption Hearts

                <7>Grants <e>10 Absorption Hearts
                <7>for 1 minute!

                <e>Right-click to use!

                <6><l>LUCKY BLOCK ITEM""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 4, DURATION_TICKS));
        player.sendMessage("<6>You gained 10 Absorption Hearts!");
    }
}
