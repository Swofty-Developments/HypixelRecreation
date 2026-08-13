package net.swofty.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.impl.ProfiledBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.StandardMobDefinitions;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobGravelSkeleton extends ProfiledBestiaryMob {
    public MobGravelSkeleton() {
        super(StandardMobDefinitions.GRAVEL_SKELETON);
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new RangedAttackGoal(this, 40, 16, 8, true, 1, 0.1, TimeUnit.SERVER_TICK),
                new RandomRegionStrollGoal(this, 15, RegionType.SPIDERS_DEN));
    }
}
