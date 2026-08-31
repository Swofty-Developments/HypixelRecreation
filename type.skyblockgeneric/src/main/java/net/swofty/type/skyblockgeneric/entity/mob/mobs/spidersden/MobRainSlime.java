package net.swofty.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.entity.mob.impl.ProfiledBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.StandardMobDefinitions;

public class MobRainSlime extends ProfiledBestiaryMob {
    public MobRainSlime() {
        super(StandardMobDefinitions.RAIN_SLIME);
    }

    @Override
    public boolean canPopulate(Instance instance) {
        return instance != null && instance.getWeather().isRaining();
    }
}
