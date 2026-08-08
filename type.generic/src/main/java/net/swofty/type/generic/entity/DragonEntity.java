package net.swofty.type.generic.entity;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.event.custom.DragonHitEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DragonEntity extends LivingEntity {

    private Pos targetPosition = null;
    private double moveSpeed = 0.8;
    private UUID followingPlayer = null;

    private boolean idleMode = false;
    private Pos idleCenter = null;
    private double idleDistance = 30;
    private Pos currentIdleTarget = null;
    private double idleAngle = 0;
    private double idleHeight = 0;

    public DragonEntity() {
        super(EntityType.ENDER_DRAGON);
        setNoGravity(true);
        hasPhysics = false;
    }

    public void setTarget(Pos target, double speed) {
        this.targetPosition = target;
        this.moveSpeed = speed;
        this.followingPlayer = null;
        this.idleMode = false;
    }

    public void followPlayer(Player player, double speed) {
        this.followingPlayer = player.getUuid();
        this.moveSpeed = speed;
        this.idleMode = false;
    }

    public void setIdle(Pos center, double distance, double speed) {
        this.idleCenter = center;
        this.idleDistance = distance;
        this.moveSpeed = speed;
        this.idleMode = true;
        this.followingPlayer = null;
        this.targetPosition = null;
        this.idleAngle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        this.idleHeight = ThreadLocalRandom.current().nextDouble(-5, 10);
        pickNewIdleTarget();
    }

    public void clearTarget() {
        this.targetPosition = null;
        this.followingPlayer = null;
        this.idleMode = false;
        this.idleCenter = null;
    }

    public boolean isFollowingPlayer() {
        return followingPlayer != null;
    }

    public boolean isIdle() {
        return idleMode;
    }

    public UUID getFollowingPlayer() {
        return followingPlayer;
    }

    @Override
    public boolean damage(Damage damage) {
        if (damage.getSource() instanceof Player player) {
            HypixelPlayer hypixelPlayer = (HypixelPlayer) player;
            DragonHitEvent event = new DragonHitEvent(hypixelPlayer, this, damage.getAmount());
            HypixelEventHandler.callCustomEvent(event);

            if (event.isCancelled()) {
                return false;
            }
        }
        return super.damage(damage);
    }

    private void pickNewIdleTarget() {
        if (idleCenter == null) return;

        idleAngle += ThreadLocalRandom.current().nextDouble(0.3, 0.8);
        if (idleAngle > Math.PI * 2) idleAngle -= Math.PI * 2;

        idleHeight += ThreadLocalRandom.current().nextDouble(-3, 3);
        idleHeight = Math.max(-5, Math.min(15, idleHeight));

        double radius = idleDistance * (0.5 + ThreadLocalRandom.current().nextDouble(0.5));
        double x = idleCenter.x() + Math.cos(idleAngle) * radius;
        double z = idleCenter.z() + Math.sin(idleAngle) * radius;
        double y = idleCenter.y() + idleHeight;

        currentIdleTarget = new Pos(x, y, z);
    }

    @Override
    protected void movementTick() {
        Pos effectiveTarget = targetPosition;

        if (followingPlayer != null) {
            Player player = instance.getPlayers().stream()
                    .filter(p -> p.getUuid().equals(followingPlayer))
                    .findFirst()
                    .orElse(null);

            if (player == null) {
                clearTarget();
                setVelocity(Vec.ZERO);
                return;
            }

            effectiveTarget = player.getPosition().add(0, 5, 0);
        } else if (idleMode && idleCenter != null) {
            if (currentIdleTarget == null) {
                pickNewIdleTarget();
            }

            double distToTarget = getPosition().distance(currentIdleTarget);
            if (distToTarget < 5.0) {
                pickNewIdleTarget();
            }

            effectiveTarget = currentIdleTarget;
        }

        if (effectiveTarget == null) {
            setVelocity(Vec.ZERO);
            return;
        }

        Pos current = getPosition();
        double dx = effectiveTarget.x() - current.x();
        double dy = effectiveTarget.y() - current.y();
        double dz = effectiveTarget.z() - current.z();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < 0.001) {
            setVelocity(Vec.ZERO);
            return;
        }

        if (dist < 3.0 && !idleMode) {
            setVelocity(Vec.ZERO);
            return;
        }

        double velocityMagnitude = moveSpeed * ServerFlag.SERVER_TICKS_PER_SECOND;

        double vx = (dx / dist) * velocityMagnitude;
        double vy = (dy / dist) * velocityMagnitude;
        double vz = (dz / dist) * velocityMagnitude;

        setVelocity(new Vec(vx, vy, vz));
        lookAt(getPosition().add(-vx, vy, -vz));

        super.movementTick();
    }
}
