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

public class MysteryMeat implements LuckyBlockConsumable {

    private static final Random RANDOM = new Random();
    private static final int BASE_DURATION_TICKS = 20 * 20;

    private static final PotionEffect[] GOOD_EFFECTS = {
            PotionEffect.SPEED,
            PotionEffect.STRENGTH,
            PotionEffect.JUMP_BOOST,
            PotionEffect.REGENERATION,
            PotionEffect.RESISTANCE,
            PotionEffect.FIRE_RESISTANCE,
            PotionEffect.ABSORPTION,
            PotionEffect.HASTE,
            PotionEffect.NIGHT_VISION,
            PotionEffect.INVISIBILITY
    };

    private static final PotionEffect[] BAD_EFFECTS = {
            PotionEffect.SLOWNESS,
            PotionEffect.MINING_FATIGUE,
            PotionEffect.NAUSEA,
            PotionEffect.BLINDNESS,
            PotionEffect.HUNGER,
            PotionEffect.WEAKNESS,
            PotionEffect.POISON,
            PotionEffect.WITHER,
            PotionEffect.LEVITATION
    };

    @Override
    public String getId() {
        return "mystery_meat";
    }

    @Override
    public String getDisplayName() {
        return "Mystery Meat";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.ROTTEN_FLESH, """
                <5><l>Mystery Meat</l>
                <7>Apply 3 random effects...
                <7>Could be good or bad!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        StringBuilder effectsMessage = new StringBuilder("Effects: ");

        for (int i = 0; i < 3; i++) {
            PotionEffect effect;
            boolean isGood = RANDOM.nextBoolean();

            if (isGood) {
                effect = GOOD_EFFECTS[RANDOM.nextInt(GOOD_EFFECTS.length)];
            } else {
                effect = BAD_EFFECTS[RANDOM.nextInt(BAD_EFFECTS.length)];
            }

            int amplifier = RANDOM.nextInt(3);
            int duration = BASE_DURATION_TICKS + RANDOM.nextInt(10 * 20);

            player.addEffect(new Potion(effect, (byte) amplifier, duration));

            if (i > 0) effectsMessage.append(", ");
            effectsMessage.append(formatEffectName(effect.name()));
        }

        player.sendMessage("<5>Mystery Meat consumed!");
        player.sendMessage("<7>{}", effectsMessage.toString());
    }

    private String formatEffectName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
