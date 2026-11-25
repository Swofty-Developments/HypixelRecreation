package net.swofty.type.skyblockgeneric.entity.mob.impl;

import lombok.Builder;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.List;

public interface RegionPopulator {
    List<Populator> getPopulators();

    default boolean canPopulate(Instance instance) {
        return true;
    }

    static void populateRegion(MobRegistry registry, Populator populator) {
        if (HypixelConst.getInstanceContainer() == null) return;
        SkyBlockRegion region = SkyBlockRegion.getRandomRegionOfType(populator.regionType());

        if (region == null) return;

        Pos randomPosition = region.getRandomPositionForEntity(HypixelConst.getInstanceContainer());

        if (randomPosition == null) return;

        if (!HypixelConst.getInstanceContainer().isChunkLoaded(randomPosition))
            HypixelConst.getInstanceContainer().loadChunk(randomPosition).join();

        SkyBlockMob mob = registry.asMob();
        mob.setInstance(HypixelConst.getInstanceContainer(), randomPosition);
    }

    @Builder
    record Populator(RegionType regionType, int minimumAmountToPopulate) {}
}
