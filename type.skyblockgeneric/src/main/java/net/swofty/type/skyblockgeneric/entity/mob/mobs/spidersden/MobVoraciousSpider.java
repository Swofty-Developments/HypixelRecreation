package net.swofty.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.entity.mob.impl.ProfiledBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.StandardMobDefinitions;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class MobVoraciousSpider extends ProfiledBestiaryMob {
    private long nextLeapAt;

    public MobVoraciousSpider() {
        super(StandardMobDefinitions.VORACIOUS_SPIDER);
    }

    @Override
    public List<RegionPopulator.Populator> getPopulators() {
        return List.of(new RegionPopulator.Populator(RegionType.SPIDERS_DEN, 3));
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (System.currentTimeMillis() < nextLeapAt) return;
        Entity target = getTarget();
        double distance = target == null ? Double.POSITIVE_INFINITY : target.getPosition().distance(getPosition());
        if (!(target instanceof SkyBlockPlayer) || target.getInstance() != getInstance()
                || distance > 12 || distance < 0.01) return;
        Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
        setVelocity(direction.mul(0.8).withY(0.45));
        nextLeapAt = System.currentTimeMillis() + 900;
    }
}
