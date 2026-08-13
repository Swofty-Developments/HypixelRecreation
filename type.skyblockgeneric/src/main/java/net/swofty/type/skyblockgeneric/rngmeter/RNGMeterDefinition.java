package net.swofty.type.skyblockgeneric.rngmeter;

import java.util.List;

public interface RNGMeterDefinition {
    RNGMeterType type();

    /**
     * Stable persistence key. Override for individual Slayer types or Catacombs floors.
     */
    default String id() {
        return type().name();
    }

    String displayName();

    List<? extends RNGMeterReward> rewards();

    RNGMeterReward defaultReward();

    default String activityName() {
        return displayName();
    }

    default String progressName() {
        return "Experience";
    }

    default String rewardProgressName() {
        return progressName();
    }

    default String iconTexture() {
        return null;
    }

    default RNGMeterReward reward(String id) {
        return rewards().stream()
                .filter(reward -> reward.id().equalsIgnoreCase(id))
                .map(reward -> (RNGMeterReward) reward)
                .findFirst()
                .orElse(defaultReward());
    }
}
