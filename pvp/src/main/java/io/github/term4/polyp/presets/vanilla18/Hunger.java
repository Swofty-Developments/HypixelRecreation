package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.hunger.ExhaustionCost;
import io.github.term4.polyp.mechanics.hunger.HungerConfig;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;

/**
 * 1.8 hunger ({@code FoodMetaData.a} + the {@code EntityHuman} action costs; full catalog with source refs in
 * docs/exhaustion-sources.md): no saturation fast regen (1.9+), and 1.8 charges plain walking.
 */
public final class Hunger {

    private Hunger() {}

    public static HungerConfig config() {
        return HungerConfig.builder()
                .naturalRegen(true)
                .regenFoodThreshold(18)
                .regenInterval(80)
                .saturationRegen(false)
                .exhaustionCost(HungerSystem.REGEN_COST, ExhaustionCost.flat(3.0f))
                .exhaustionCost(HungerSystem.DAMAGE_TAKEN_COST, ExhaustionCost.dynamic())
                .exhaustionCost(HungerSystem.ATTACK_COST, ExhaustionCost.flat(0.3f))
                .exhaustionCost(HungerSystem.JUMP_COST, ExhaustionCost.flat(0.2f))
                .exhaustionCost(HungerSystem.SPRINT_JUMP_COST, ExhaustionCost.flat(0.8f))
                .exhaustionCost(HungerSystem.DIVE_COST, ExhaustionCost.scaled(0.015f))
                .exhaustionCost(HungerSystem.SWIM_COST, ExhaustionCost.scaled(0.015f))
                .exhaustionCost(HungerSystem.SPRINT_COST, ExhaustionCost.scaled(0.099999994f))
                .exhaustionCost(HungerSystem.WALK_COST, ExhaustionCost.scaled(0.01f))
                .exhaustionCost(HungerSystem.BLOCK_BREAK_COST, ExhaustionCost.flat(0.025f))
                .exhaustionCost(HungerSystem.HUNGER_EFFECT_COST, ExhaustionCost.scaled(0.025f))
                .build();
    }
}
