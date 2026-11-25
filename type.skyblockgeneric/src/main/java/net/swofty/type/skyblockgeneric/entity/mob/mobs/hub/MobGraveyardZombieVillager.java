package net.swofty.type.skyblockgeneric.entity.mob.mobs.hub;

import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobGraveyardZombieVillager extends MobZombieVillager implements RegionPopulator {
    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.GRAVEYARD, 5));
    }
}
