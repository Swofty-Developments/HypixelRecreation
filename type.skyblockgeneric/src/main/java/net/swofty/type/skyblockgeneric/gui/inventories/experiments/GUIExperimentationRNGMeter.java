package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.item.ItemStack;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentReward;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentationRNGMeter;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterDefinition;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterReward;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GUIExperimentationRNGMeter extends RNGMeterView {
    @Override
    protected RNGMeterDefinition definition() {
        return ExperimentationRNGMeter.INSTANCE;
    }

    @Override
    protected ItemStack.Builder rewardItem(RNGMeterReward reward, SkyBlockPlayer player) {
        return ((ExperimentReward) reward).displayItem(player);
    }
}
