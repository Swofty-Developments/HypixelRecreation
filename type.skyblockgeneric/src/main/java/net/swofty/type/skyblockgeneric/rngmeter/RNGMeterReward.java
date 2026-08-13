package net.swofty.type.skyblockgeneric.rngmeter;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public interface RNGMeterReward {
    String id();

    String displayName();

    double requiredXp();

    void give(SkyBlockPlayer player);

    RNGMeterLoot loot();
}
