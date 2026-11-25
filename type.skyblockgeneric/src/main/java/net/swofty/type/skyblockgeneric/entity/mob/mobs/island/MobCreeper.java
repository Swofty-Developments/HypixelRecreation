package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ai.GoalSelector;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class MobCreeper extends PrivateIslandBestiaryMob {
    private long fuseStarted = -1;

    public MobCreeper() {
        super(PrivateIslandMobDefinitions.CREEPER, new double[]{80}, new double[]{20});
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new VanillaRandomStrollGoal(this, 15));
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (getInstance() == null) {
            return;
        }

        Entity target = getInstance().getNearbyEntities(getPosition(), 3).stream()
                .filter(entity -> entity instanceof SkyBlockPlayer)
                .findFirst()
                .orElse(null);

        if (target == null) {
            fuseStarted = -1;
            return;
        }

        if (fuseStarted < 0) {
            fuseStarted = time;
        }

        if (time - fuseStarted >= 30) {
            getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 3);
            remove();
        }
    }
}
