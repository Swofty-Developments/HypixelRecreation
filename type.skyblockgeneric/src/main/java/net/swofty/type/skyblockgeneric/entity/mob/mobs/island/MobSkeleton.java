package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;

import java.util.List;

public class MobSkeleton extends PrivateIslandBestiaryMob {
    private static final double[] HEALTH = {100, 115, 132, 152, 174, 201, 231, 266, 305, 351, 404, 465, 535, 615, 707};
    private static final double[] DAMAGE = {15, 17, 19, 22, 24, 28, 31, 35, 40, 45, 51, 58, 65, 73, 83};

    public MobSkeleton() {
        super(PrivateIslandMobDefinitions.SKELETON, HEALTH, DAMAGE,
                new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 8},
                new int[]{1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6});
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new RangedAttackGoal(this, 40, 16, 8, true, 1, 0.1, TimeUnit.SERVER_TICK),
                new VanillaRandomStrollGoal(this, 15));
    }
}
