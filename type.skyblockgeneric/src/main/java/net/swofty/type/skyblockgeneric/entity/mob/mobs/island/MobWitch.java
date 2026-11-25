package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;

import java.util.List;

public class MobWitch extends PrivateIslandBestiaryMob {
    private static final double[] HEALTH = {150, 173, 198, 228, 262, 301, 346, 399, 458, 527, 606, 697, 802, 922, 1061};
    private static final double[] DAMAGE = {20, 23, 26, 29, 33, 37, 42, 47, 53, 60, 68, 77, 87, 98, 111};

    public MobWitch() {
        super(PrivateIslandMobDefinitions.WITCH, HEALTH, DAMAGE);
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new RangedAttackGoal(this, 40, 16, 8, true, 1, 0.1, TimeUnit.SERVER_TICK),
                new VanillaRandomStrollGoal(this, 15));
    }
}
