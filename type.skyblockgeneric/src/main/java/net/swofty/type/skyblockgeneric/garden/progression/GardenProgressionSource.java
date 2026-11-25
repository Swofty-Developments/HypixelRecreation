package net.swofty.type.skyblockgeneric.garden.progression;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public interface GardenProgressionSource {
    default List<GardenProgressionReward> gardenProgressionRewards(SkyBlockPlayer player) {
        return List.of();
    }
}
