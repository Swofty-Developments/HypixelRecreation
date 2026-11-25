package net.swofty.type.skyblockgeneric.rngmeter;

import net.swofty.commons.text.Text;

import java.util.List;

public interface RNGMeterDefinition {
    RNGMeterType type();

    /**
     * Stable persistence key. Override for individual Slayer types or Catacombs floors.
     */
    default String id() {
        return type().name();
    }

    Text displayName();

    List<? extends RNGMeterReward> rewards();

    RNGMeterReward defaultReward();

    default Text activityName() {
        return displayName();
    }

    default Text progressName() {
        return Text.literal("Experience");
    }

    default Text rewardProgressName() {
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
