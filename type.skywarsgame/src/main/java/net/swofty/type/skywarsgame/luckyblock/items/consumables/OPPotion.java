package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.Random;

public class OPPotion implements LuckyBlockConsumable {

    private static final Random RANDOM = new Random();
    private static final int DURATION_TICKS = 45 * 20;

    @Override
    public String getId() {
        return "op_potion";
    }

    @Override
    public String getDisplayName() {
        return "OP Potion";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.SPLASH_POTION, """
                <d><l>OP Potion</l>
                <7>An incredibly powerful potion!
                <7>Grants random powerful buffs!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        int effectCount = 3 + RANDOM.nextInt(3);

        PotionEffect[][] effectPools = {
                {PotionEffect.SPEED, PotionEffect.STRENGTH, PotionEffect.JUMP_BOOST},
                {PotionEffect.RESISTANCE, PotionEffect.FIRE_RESISTANCE},
                {PotionEffect.REGENERATION, PotionEffect.ABSORPTION},
                {PotionEffect.HASTE, PotionEffect.NIGHT_VISION}
        };

        for (int i = 0; i < effectCount && i < effectPools.length; i++) {
            PotionEffect[] pool = effectPools[i];
            PotionEffect effect = pool[RANDOM.nextInt(pool.length)];
            int amplifier = 1 + RANDOM.nextInt(2);

            player.addEffect(new Potion(effect, (byte) amplifier, DURATION_TICKS));
        }

        player.sendMessage("<d>OP POTION! You feel incredibly powerful!");
    }
}
