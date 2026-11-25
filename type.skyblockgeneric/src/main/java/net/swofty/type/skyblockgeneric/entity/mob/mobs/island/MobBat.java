package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;

import java.util.List;

public class MobBat extends PrivateIslandBestiaryMob {
    public MobBat() {
        super(PrivateIslandMobDefinitions.BAT, new double[]{100}, new double[]{0});
        setNoGravity(true);
    }

    @Override
    public List<RegionPopulator.Populator> getPopulators() {
        return List.of();
    }
}
