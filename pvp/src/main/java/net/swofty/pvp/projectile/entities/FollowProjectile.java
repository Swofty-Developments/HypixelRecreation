package net.swofty.pvp.projectile.entities;

import net.swofty.pvp.projectile.AbstractProjectile;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FollowProjectile extends AbstractProjectile {
    private final Entity target;
    private final float attractionAcceleration;
    private final float attractionVelocity;

    private long ticks = 0;

    public FollowProjectile(EntityType type, Entity shooter, Entity target, float attractionVelocity, float attractionAcceleration) {
        super(type, shooter);
        setNoGravity(true);
        this.target = target;
        this.attractionVelocity = attractionVelocity;
        this.attractionAcceleration = attractionAcceleration;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        ticks++;
    }

    @Override
    public long getAliveTicks() {
        return ticks;
    }

    @Override
    public void shoot(@NotNull Point from, @NotNull Point to, double power, double spread) {
        var instance = shooter.getInstance();
        if (instance == null) return;

        float yaw = -shooter.getPosition().yaw();
        float originalPitch = -shooter.getPosition().pitch();

        double pitchDiff = originalPitch - 45f;
        if (pitchDiff == 0) pitchDiff = 0.0001;
        double pitchAdjust = pitchDiff * 0.002145329238474369D;

        double dx = to.x() - from.x();
        double dy = to.y() - from.y() + pitchAdjust;
        double dz = to.z() - from.z();

        if (!hasNoGravity()) {
            final double xzLength = Math.sqrt(dx * dx + dz * dz);
            dy += xzLength * 0.20000000298023224D;
        }

        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        Random random = ThreadLocalRandom.current();
        spread *= 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;

        final EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, from, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return;
        }

        final double mul = ServerProperties.SERVER_TICKS_PER_SECOND.get() * power;
        Vec v = new Vec(dx * mul, dy * mul * 0.9, dz * mul);

        this.setInstance(instance, new Pos(from.x(), from.y() - this.boundingBox.height()/2, from.z(), yaw, originalPitch)).whenComplete((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                synchronizePosition(); // initial synchronization, required to be 100% precise
                setVelocity(v);
            }
        });
    }

    protected @NotNull Vec updateVelocity(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Block.@NotNull Getter blockGetter, @NotNull Aerodynamics aerodynamics, boolean positionChanged, boolean entityFlying, boolean entityOnGround, boolean entityNoGravity) {
        Vec directionVector = entityPosition.sub(target.getPosition().add(0, target.getEyeHeight(), 0)).asVec().normalize().mul(attractionVelocity + attractionAcceleration * this.getAliveTicks());

        double x = currentVelocity.x() - directionVector.x();
        double y = currentVelocity.y() - directionVector.y();
        double z = currentVelocity.z() - directionVector.z();

        return new Vec(Math.abs(x) < 1.0E-6 ? 0.0 : x, Math.abs(y) < 1.0E-6 ? 0.0 : y, Math.abs(z) < 1.0E-6 ? 0.0 : z);
    }
}