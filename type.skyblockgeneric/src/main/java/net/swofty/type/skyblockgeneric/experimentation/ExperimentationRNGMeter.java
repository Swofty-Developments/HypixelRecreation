package net.swofty.type.skyblockgeneric.experimentation;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterDefinition;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterReward;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterType;

import java.util.Arrays;
import java.util.List;

public final class ExperimentationRNGMeter implements RNGMeterDefinition {
    public static final ExperimentationRNGMeter INSTANCE = new ExperimentationRNGMeter();

    private final List<ExperimentReward> rewards = Arrays.stream(ExperimentReward.values())
            .filter(reward -> reward.requiredXp() > 0)
            .toList();

    private ExperimentationRNGMeter() {
    }

    @Override
    public RNGMeterType type() {
        return RNGMeterType.EXPERIMENTATION;
    }

    @Override
    public Text displayName() {
        return Text.literal("Experimentation Table");
    }

    @Override
    public Text activityName() {
        return Text.literal("Superpairs");
    }

    @Override
    public Text progressName() {
        return Text.literal("Enchanting XP");
    }

    @Override
    public Text rewardProgressName() {
        return Text.literal("Experimental XP");
    }

    @Override
    public String iconTexture() {
        return "81b843451184a8ccd8e6e49d0edf3451d3dea50fde5b6a2f98ab7cf1138bcece";
    }

    @Override
    public List<? extends RNGMeterReward> rewards() {
        return rewards;
    }

    @Override
    public RNGMeterReward defaultReward() {
        return ExperimentReward.TITANIC_EXPERIENCE_BOTTLE;
    }
}
