package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentTier;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentType;

public final class GUISuperPairs extends ExperimentTierMenuView {
    @Override
    protected ExperimentType experimentType() {
        return ExperimentType.SUPERPAIRS;
    }

    @Override
    protected View<DefaultState> playView(ExperimentTier tier) {
        return new GUISuperPairsPlay(tier);
    }
}
