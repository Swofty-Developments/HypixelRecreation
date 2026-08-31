package io.github.term4.polyp.presets.vanilla;

import io.github.term4.polyp.mechanics.consumable.ComponentFood;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfig;
import io.github.term4.polyp.mechanics.consumable.ConsumableTypeConfig;
import io.github.term4.polyp.mechanics.consumable.catalog.VanillaConsumables;
import net.kyori.adventure.key.Key;
import net.minestom.server.potion.PotionEffect;

/**
 * Modern (26) consumables: 26-source golden apple and pufferfish effects, plus the modern {@code canEat} gate (creative
 * always eats, no item loss) on every food incl. the component floor.
 */
public final class Consumables {

    private Consumables() {}

    public static ConsumableConfig config() {
        return ConsumableConfig.builder()
                .typeConfigs(
                        ConsumableTypeConfig.builder(VanillaConsumables.GOLDEN_APPLE.key())
                                .canConsume(ctx -> VanillaConsumables.modernCanEat(ctx, true)) // always-edible
                                .behavior(VanillaConsumables.effectFood(4, 9.6f,
                                        VanillaConsumables.eff(PotionEffect.REGENERATION, 2, 100),
                                        VanillaConsumables.eff(PotionEffect.ABSORPTION, 1, 2400)))
                                .build(),
                        ConsumableTypeConfig.builder(VanillaConsumables.ENCHANTED_GOLDEN_APPLE.key())
                                .canConsume(ctx -> VanillaConsumables.modernCanEat(ctx, true))
                                .behavior(VanillaConsumables.effectFood(4, 9.6f,
                                        VanillaConsumables.eff(PotionEffect.REGENERATION, 2, 400),
                                        VanillaConsumables.eff(PotionEffect.RESISTANCE, 1, 6000),
                                        VanillaConsumables.eff(PotionEffect.FIRE_RESISTANCE, 1, 6000),
                                        VanillaConsumables.eff(PotionEffect.ABSORPTION, 4, 2400)))
                                .build(),
                        food(VanillaConsumables.RAW_CHICKEN.key()),
                        food(VanillaConsumables.ROTTEN_FLESH.key()),
                        food(VanillaConsumables.SPIDER_EYE.key()),
                        food(VanillaConsumables.POISONOUS_POTATO.key()),
                        ConsumableTypeConfig.builder(VanillaConsumables.PUFFERFISH.key())
                                .canConsume(ctx -> VanillaConsumables.modernCanEat(ctx, false))
                                .behavior(VanillaConsumables.effectFood(1, 0.2f,
                                        VanillaConsumables.eff(PotionEffect.POISON, 2, 1200),
                                        VanillaConsumables.eff(PotionEffect.HUNGER, 3, 300),
                                        VanillaConsumables.eff(PotionEffect.NAUSEA, 1, 300)))
                                .build(),
                        ConsumableTypeConfig.builder(ComponentFood.KEY)
                                .canConsume(ctx -> VanillaConsumables.modernCanEat(ctx, ComponentFood.alwaysEdible(ctx.item())))
                                .build())
                .types(VanillaConsumables.types())
                .build();
    }

    private static ConsumableTypeConfig food(Key key) {
        return ConsumableTypeConfig.builder(key).canConsume(ctx -> VanillaConsumables.modernCanEat(ctx, false)).build();
    }
}
