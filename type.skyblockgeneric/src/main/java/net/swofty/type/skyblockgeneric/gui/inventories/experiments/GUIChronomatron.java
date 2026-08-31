package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentTier;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentType;

public final class GUIChronomatron extends ExperimentTierMenuView {
    @Override
    protected ExperimentType experimentType() {
        return ExperimentType.CHRONOMATRON;
    }

    @Override
    protected View<DefaultState> playView(ExperimentTier tier) {
        return new GUIChronomatronPlay(tier);
    }
}
