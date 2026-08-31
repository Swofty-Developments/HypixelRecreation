package net.swofty.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.swofty.type.skyblockgeneric.entity.mob.impl.ProfiledBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.StandardMobDefinitions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class MobDasherSpider extends ProfiledBestiaryMob {
    private Entity dashedTarget;
    private int dashesRemaining;
    private long nextDashAt;

    public MobDasherSpider() {
        super(StandardMobDefinitions.DASHER_SPIDER);
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        Entity target = getTarget();
        double distance = target == null ? Double.POSITIVE_INFINITY : target.getPosition().distance(getPosition());
        if (!(target instanceof SkyBlockPlayer) || target.getInstance() != getInstance()
                || distance > 16 || distance < 0.01) {
            dashedTarget = null;
            dashesRemaining = 0;
            return;
        }
        if (target != dashedTarget) {
            dashedTarget = target;
            dashesRemaining = 3;
            nextDashAt = 0;
        }
        long now = System.currentTimeMillis();
        if (dashesRemaining <= 0) {
            if (now < nextDashAt) return;
            dashesRemaining = 3;
        }
        if (now < nextDashAt) return;
        Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
        setVelocity(direction.mul(1.25).withY(0.65));
        dashesRemaining--;
        nextDashAt = now + (dashesRemaining == 0 ? 2_000 : 650);
    }
}
