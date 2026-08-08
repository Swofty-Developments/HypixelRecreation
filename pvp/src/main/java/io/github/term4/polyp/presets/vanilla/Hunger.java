package io.github.term4.polyp.presets.vanilla;

import io.github.term4.polyp.mechanics.hunger.ExhaustionCost;
import io.github.term4.polyp.mechanics.hunger.HungerConfig;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;

/**
 * Modern hunger ({@code FoodData.tick} + the {@code ServerPlayer} action costs; full catalog with source refs in
 * docs/exhaustion-sources.md): saturation fast regen at food 20 (heal {@code min(sat,6)/6} every 10 ticks, exhaustion =
 * the spent saturation), else 1 per 80 ticks at food 18+. Walking is free (removed in 1.11) - the key stays unpriced.
 */
public final class Hunger {

    private Hunger() {}

    public static HungerConfig config() {
        return HungerConfig.builder()
                .naturalRegen(true)
                .regenFoodThreshold(18)
                .regenInterval(80)
                .saturationRegen(true)
                .exhaustionCost(HungerSystem.REGEN_COST, ExhaustionCost.flat(6.0f))
                .exhaustionCost(HungerSystem.SATURATION_REGEN_COST, ExhaustionCost.dynamic())
                .exhaustionCost(HungerSystem.DAMAGE_TAKEN_COST, ExhaustionCost.dynamic())
                .exhaustionCost(HungerSystem.ATTACK_COST, ExhaustionCost.flat(0.1f))
                .exhaustionCost(HungerSystem.JUMP_COST, ExhaustionCost.flat(0.05f))
                .exhaustionCost(HungerSystem.SPRINT_JUMP_COST, ExhaustionCost.flat(0.2f))
                .exhaustionCost(HungerSystem.DIVE_COST, ExhaustionCost.scaled(0.01f))
                .exhaustionCost(HungerSystem.SWIM_COST, ExhaustionCost.scaled(0.01f))
                .exhaustionCost(HungerSystem.SPRINT_COST, ExhaustionCost.scaled(0.1f))
                .exhaustionCost(HungerSystem.BLOCK_BREAK_COST, ExhaustionCost.flat(0.005f))
                .exhaustionCost(HungerSystem.HUNGER_EFFECT_COST, ExhaustionCost.scaled(0.005f))
                .build();
    }
}
