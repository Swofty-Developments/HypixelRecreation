package net.swofty.type.skyblockgeneric.experimentation;

import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;

public enum SuperPairItem {
    EXPERIENCE(ExperimentReward.EXPERIENCE),
    EXPERIENCE_BOTTLE(ExperimentReward.EXPERIENCE_BOTTLE),
    GRAND_EXPERIENCE_BOTTLE(ExperimentReward.GRAND_EXPERIENCE_BOTTLE),
    TITANIC_EXPERIENCE_BOTTLE(ExperimentReward.TITANIC_EXPERIENCE_BOTTLE),
    SCAVENGER_V(ExperimentReward.SCAVENGER_V),
    SHARPNESS_VI(ExperimentReward.SHARPNESS_VI),
    LIFE_STEAL_IV(ExperimentReward.LIFE_STEAL_IV),
    POWER_VI(ExperimentReward.POWER_VI),
    ENDER_SLAYER_VI(ExperimentReward.ENDER_SLAYER_VI),
    THUNDERBOLT_VI(ExperimentReward.THUNDERBOLT_VI),
    GROWTH_VI(ExperimentReward.GROWTH_VI),
    CHANCE_IV(ExperimentReward.CHANCE_IV),
    BLAST_PROTECTION_VI(ExperimentReward.BLAST_PROTECTION_VI),
    RESPITE_III(ExperimentReward.RESPITE_III),
    VENOMOUS_VI(ExperimentReward.VENOMOUS_VI),
    PROJECTILE_PROTECTION_VI(ExperimentReward.PROJECTILE_PROTECTION_VI),
    FIRE_PROTECTION_VI(ExperimentReward.FIRE_PROTECTION_VI),
    WOODSPLITTER_VI(ExperimentReward.WOODSPLITTER_VI),
    DRAIN_IV(ExperimentReward.DRAIN_IV),
    PROTECTION_VI(ExperimentReward.PROTECTION_VI),
    TITAN_KILLER_VI(ExperimentReward.TITAN_KILLER_VI),
    GIANT_KILLER_VI(ExperimentReward.GIANT_KILLER_VI),
    METAPHYSICAL_SERUM(ExperimentReward.METAPHYSICAL_SERUM),
    EXPERIMENT_THE_FISH(ExperimentReward.EXPERIMENT_THE_FISH),
    GUARDIAN_PET(ExperimentReward.GUARDIAN_PET),
    A_BEGINNERS_GUIDE_TO_PESTHUNTING(ExperimentReward.A_BEGINNERS_GUIDE_TO_PESTHUNTING),
    SEVERED_PINCER(ExperimentReward.SEVERED_PINCER),
    CHANCE_V(ExperimentReward.CHANCE_V),
    THUNDERLORD_VII(ExperimentReward.THUNDERLORD_VII),
    ENSNARED_SNAIL(ExperimentReward.ENSNARED_SNAIL),
    GIANT_KILLER_VII(ExperimentReward.GIANT_KILLER_VII),
    GRAVITY_VI(ExperimentReward.GRAVITY_VI),
    GOLDEN_BOUNTY(ExperimentReward.GOLDEN_BOUNTY),
    SEVERED_HAND(ExperimentReward.SEVERED_HAND),
    CRITICAL_VII(ExperimentReward.CRITICAL_VII),
    VIBRANT_CORAL(ExperimentReward.VIBRANT_CORAL),
    SNIPE_IV(ExperimentReward.SNIPE_IV),
    LIFE_STEAL_V(ExperimentReward.LIFE_STEAL_V),
    GOLD_BOTTLE_CAP(ExperimentReward.GOLD_BOTTLE_CAP),
    LOOTING_V(ExperimentReward.LOOTING_V),
    FIRST_STRIKE_V(ExperimentReward.FIRST_STRIKE_V),
    FIRE_PROTECTION_VII(ExperimentReward.FIRE_PROTECTION_VII),
    THUNDERBOLT_VII(ExperimentReward.THUNDERBOLT_VII),
    CUBISM_VI(ExperimentReward.CUBISM_VI),
    TRIPLE_STRIKE_V(ExperimentReward.TRIPLE_STRIKE_V),
    CHAIN_OF_THE_END_TIMES(ExperimentReward.CHAIN_OF_THE_END_TIMES),
    DRAIN_V(ExperimentReward.DRAIN_V),
    FATEFUL_STINGER(ExperimentReward.FATEFUL_STINGER),
    BLAST_PROTECTION_VII(ExperimentReward.BLAST_PROTECTION_VII),
    CLEAVE_VI(ExperimentReward.CLEAVE_VI),
    OCTOPUS_TENDRIL(ExperimentReward.OCTOPUS_TENDRIL),
    TITAN_KILLER_VII(ExperimentReward.TITAN_KILLER_VII),
    LUCK_VII(ExperimentReward.LUCK_VII),
    END_STONE_IDOL(ExperimentReward.END_STONE_IDOL),
    EXECUTE_VI(ExperimentReward.EXECUTE_VI),
    POWER_VII(ExperimentReward.POWER_VII),
    TROUBLED_BUBBLE(ExperimentReward.TROUBLED_BUBBLE),
    PROJECTILE_PROTECTION_VII(ExperimentReward.PROJECTILE_PROTECTION_VII),
    GROWTH_VII(ExperimentReward.GROWTH_VII),
    SHARPNESS_VII(ExperimentReward.SHARPNESS_VII),
    PROTECTION_VII(ExperimentReward.PROTECTION_VII),
    PROSECUTE_VI(ExperimentReward.PROSECUTE_VI),
    NADESHIKO_DYE(ExperimentReward.NADESHIKO_DYE),
    EXTRA_CLICK(null, Material.IRON_NUGGET, PowerUp.EXTRA_CLICK),
    EXPERIENCE_POWERUP(null, Material.EXPERIENCE_BOTTLE, PowerUp.EXPERIENCE),
    EXTRA_CLICKS(null, Material.GOLD_NUGGET, PowerUp.EXTRA_CLICKS),
    INSTANT_FIND(null, Material.NETHER_STAR, PowerUp.INSTANT_FIND),
    NEXT_CLICK_FREE(null, Material.CLOCK, PowerUp.NEXT_CLICK_FREE);

    private final ExperimentReward reward;
    private final Material powerUpMaterial;
    private final PowerUp powerUp;

    SuperPairItem(ExperimentReward reward) {
        this.reward = reward;
        this.powerUpMaterial = null;
        this.powerUp = null;
    }

    SuperPairItem(ExperimentReward reward, Material powerUpMaterial, PowerUp powerUp) {
        this.reward = reward;
        this.powerUpMaterial = powerUpMaterial;
        this.powerUp = powerUp;
    }

    public Material material() {
        return reward == null ? powerUpMaterial : reward.material();
    }

    public Text displayName() { return displayNameOrPowerUp(); }
    public ExperimentReward reward() { return reward; }

    public boolean isPowerUp() {
        return powerUp != null;
    }

    public PowerUp powerUp() {
        return powerUp;
    }

    public Text displayNameOrPowerUp() {
        return reward == null ? switch (powerUp) {
            case EXTRA_CLICK -> Text.of("<e>+1 Click");
            case EXPERIENCE -> Text.of("<3>Experience Power-up");
            case EXTRA_CLICKS -> Text.of("<e>+3 Clicks");
            case INSTANT_FIND -> Text.of("<a>Instant Find");
            case NEXT_CLICK_FREE -> Text.of("<e>Next Click Free");
        } : reward.displayName();
    }

    public enum PowerUp {
        EXTRA_CLICK,
        EXPERIENCE,
        EXTRA_CLICKS,
        INSTANT_FIND,
        NEXT_CLICK_FREE
    }
}
