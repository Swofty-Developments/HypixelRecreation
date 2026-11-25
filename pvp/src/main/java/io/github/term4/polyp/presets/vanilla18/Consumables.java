package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.consumable.ComponentFood;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfig;
import io.github.term4.polyp.mechanics.consumable.ConsumableTypeConfig;
import io.github.term4.polyp.mechanics.consumable.catalog.VanillaConsumables;
import net.kyori.adventure.key.Key;
import net.minestom.server.potion.PotionEffect;

/**
 * Vanilla 1.8 consumables: 1.8-source golden apple and pufferfish effects, plus the 1.8 {@code canEat} gate (creative
 * never eats) on every food incl. the component floor.
 */
public final class Consumables {

    private Consumables() {}

    public static ConsumableConfig config() {
        return ConsumableConfig.builder()
                .typeConfigs(
                        ConsumableTypeConfig.builder(VanillaConsumables.GOLDEN_APPLE.key())
                                .canConsume(ctx -> VanillaConsumables.legacyCanEat(ctx, true)) // always-edible, but 1.8 still blocks creative
                                .behavior(VanillaConsumables.effectFood(4, 9.6f,
                                        VanillaConsumables.eff(PotionEffect.REGENERATION, 2, 100),
                                        VanillaConsumables.eff(PotionEffect.ABSORPTION, 1, 2400)))
                                .build(),
                        ConsumableTypeConfig.builder(VanillaConsumables.ENCHANTED_GOLDEN_APPLE.key())
                                .canConsume(ctx -> VanillaConsumables.legacyCanEat(ctx, true))
                                .behavior(VanillaConsumables.effectFood(4, 9.6f,
                                        VanillaConsumables.eff(PotionEffect.REGENERATION, 5, 600),
                                        VanillaConsumables.eff(PotionEffect.RESISTANCE, 1, 6000),
                                        VanillaConsumables.eff(PotionEffect.FIRE_RESISTANCE, 1, 6000),
                                        VanillaConsumables.eff(PotionEffect.ABSORPTION, 1, 2400)))
                                .build(),
                        food(VanillaConsumables.RAW_CHICKEN.key()),
                        food(VanillaConsumables.ROTTEN_FLESH.key()),
                        food(VanillaConsumables.SPIDER_EYE.key()),
                        food(VanillaConsumables.POISONOUS_POTATO.key()),
                        ConsumableTypeConfig.builder(VanillaConsumables.PUFFERFISH.key())
                                .canConsume(ctx -> VanillaConsumables.legacyCanEat(ctx, false))
                                .behavior(VanillaConsumables.effectFood(1, 0.2f,
                                        VanillaConsumables.eff(PotionEffect.POISON, 4, 1200),
                                        VanillaConsumables.eff(PotionEffect.HUNGER, 3, 300),
                                        VanillaConsumables.eff(PotionEffect.NAUSEA, 2, 300)))
                                .build(),
                        ConsumableTypeConfig.builder(ComponentFood.KEY)
                                .canConsume(ctx -> VanillaConsumables.legacyCanEat(ctx, ComponentFood.alwaysEdible(ctx.item())))
                                .build())
                .types(VanillaConsumables.types())
                .build();
    }

    private static ConsumableTypeConfig food(Key key) {
        return ConsumableTypeConfig.builder(key).canConsume(ctx -> VanillaConsumables.legacyCanEat(ctx, false)).build();
    }
}
