package net.swofty.type.skyblockgeneric.skilltree;

import java.util.List;

public record TreeTierDefinition(
        int tier,
        long cumulativeExperience,
        int tokenReward,
        int forgeSlotReward,
        int skyBlockExperienceReward,
        List<String> rewards
) {
    public TreeTierDefinition {
        rewards = List.copyOf(rewards);
    }
}
