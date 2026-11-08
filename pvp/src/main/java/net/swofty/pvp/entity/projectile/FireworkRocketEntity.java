package net.swofty.pvp.entity.projectile;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.projectile.FireworkRocketMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.swofty.pvp.feature.block.BlockFeature;
import net.swofty.pvp.utils.ProjectileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FireworkRocketEntity extends CustomEntityProjectile {

    private static final @NotNull BoundingBox POINT_BOX = new BoundingBox(0, 0, 0);
    private static final double GRACE_PERIOD_METERS = 1.7;

    private int lifeTicks;
    private final int lifeTimeTicks;
    private final boolean shotAtAngle;
    private PhysicsResult previousPhysicsResult;
    private Point spawnPoint;

    private final BlockFeature blockFeature;

    public FireworkRocketEntity(Entity shooter, ItemStack itemStack, boolean shotAtAngle, BlockFeature blockFeature) {
        super(shooter, EntityType.FIREWORK_ROCKET);
        this.setNoGravity(true);
        this.lifeTicks = 0;
        this.shotAtAngle = shotAtAngle;
        this.blockFeature = blockFeature;

        var meta = getFireworkRocketMeta(itemStack, shotAtAngle);

        // Firework duration calculation
        ThreadLocalRandom random = ThreadLocalRandom.current();
        FireworkList fireworkComponent = meta.getFireworkInfo().get(DataComponents.FIREWORKS);
        int flightDuration = 1;

        flightDuration += fireworkComponent != null ? fireworkComponent.flightDuration() : 0;
        this.lifeTimeTicks = 10 * flightDuration + random.nextInt(6) + random.nextInt(7);

        if (!shotAtAngle) {
            this.setVelocity(new Vec(
                nextTriangular(0.0, 0.002297, random),
                0.05,
                nextTriangular(0.0, 0.002297, random)
            ));
        }
    }

    private @NotNull FireworkRocketMeta getFireworkRocketMeta(ItemStack itemStack, boolean shotAtAngle) {
        var meta = (FireworkRocketMeta) this.getEntityMeta();

        /*
          The shooter in FireworkRocketMeta makes the firework particles follow the player and renders the rocket
          invisible, which is used for elytra rocketing.
          The shooter in FireworkRocketMeta does NOT mean the player who shot the firework from the crossbow,
          that's the shooter inherited from CustomEntityProjectile
        */
        // TODO add firework elytra functionality (need elytra first)
        meta.setShooter(null);
        meta.setShotAtAngle(shotAtAngle);
        meta.setFireworkInfo(itemStack);
        return meta;
    }

    public double nextTriangular(double mean, double deviation, ThreadLocalRandom random) {
        return ((random.nextDouble() - random.nextDouble()) * deviation) + mean;
    }

    @Override
    public void spawn() {
        Pos pos = this.getPosition();
        this.spawnPoint = pos;
        getViewersAsAudience().playSound(Sound.sound(
            SoundEvent.ENTITY_FIREWORK_ROCKET_LAUNCH,
            Sound.Source.AMBIENT,
            3.0f,
            1.0f
        ), pos.x(), pos.y(), pos.z());
    }

    @Override
    public boolean onHit(Entity entity) {
        if (entity instanceof Player && entity.getDistanceSquared(spawnPoint) < GRACE_PERIOD_METERS * GRACE_PERIOD_METERS)
            return false;
        this.explode();
        return true;
    }

    @Override
    public boolean onStuck() {
        if (this.canSlide()) return false;
        this.explode();
        return true;
    }

    // Sliding across the surface when the firework rocket has no explosions
    @SuppressWarnings("UnstableApiUsage") // Marked unstable because either internal or experimental
    @Override
    protected void movementTick() {
        this.gravityTickCount = isStuck() ? 0 : gravityTickCount + 1;
        if (vehicle != null) return;

        if (!shotAtAngle) {
            double horizontal = horizontalCollision() ? 1.0 : 1.15;
            this.setVelocity(this.getVelocity().mul(horizontal, 1.0, horizontal)
                .add(0.0, 0.04 * ServerProperties.SERVER_TICKS_PER_SECOND.get(), 0.0));
        }

        if (!isStuck()) {
            Vec diff = velocity.div(ServerProperties.SERVER_TICKS_PER_SECOND.get());

            if (instance.isInVoid(position)) {
                scheduler().scheduleNextProcess(this::remove);
                return;
            }

            ChunkCache blockGetter = new ChunkCache(instance, currentChunk, Block.AIR);
            PhysicsResult physicsResult = ProjectileUtil.simulateMovement(position, diff, POINT_BOX,
                instance.getWorldBorder(), blockGetter, hasPhysics, previousPhysicsResult, true);
            this.previousPhysicsResult = physicsResult;

            Pos newPosition = physicsResult.newPosition();

            if (!noClip) {
                boolean noCollideShooter = getAliveTicks() < 6;
                Collection<EntityCollisionResult> entityResult = CollisionUtils.checkEntityCollisions(instance, boundingBox.expand(0.1, 0.3, 0.1),
                    position.add(0, -0.3, 0), diff, 3, e -> {
                        if (noCollideShooter && e == getShooter()) return false;
                        return e != this && canHit(e);
                    }, physicsResult);

                if (!entityResult.isEmpty()) {
                    Vec prevVelocity = velocity;
                    EntityCollisionResult collided = entityResult.stream().findFirst().orElse(null);

                    var event = new ProjectileCollideWithEntityEvent(this, (collided.collisionPoint()).asPos(), collided.entity());
                    EventDispatcher.call(event);
                    if (!event.isCancelled()) {
                        if (onHit(collided.entity())) {
                            scheduler().scheduleNextProcess(this::remove);
                            return;
                        } else {
                            if (velocity != prevVelocity) newPosition = position;
                        }
                    }
                }
            }

            Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, physicsResult.newPosition());
            if (!ChunkUtils.isLoaded(finalChunk)) return;

            if (physicsResult.hasCollision()) {
                double signumX = physicsResult.collisionX() ? Math.signum(velocity.x()) : 0;
                double signumY = physicsResult.collisionY() ? Math.signum(velocity.y()) : 0;
                double signumZ = physicsResult.collisionZ() ? Math.signum(velocity.z()) : 0;
                Vec blockCollisionDirection = new Vec(signumX, signumY, signumZ);

                Point collidedPosition = blockCollisionDirection.add(physicsResult.newPosition()).apply(Vec.Operator.FLOOR);
                Block block = instance.getBlock(collidedPosition);

                var event = new ProjectileCollideWithBlockEvent(this, physicsResult.newPosition().withCoord(collidedPosition), block);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    if (canSlide()) {
                        double newX = physicsResult.collisionX() ? 0 : velocity.x();
                        double newY = physicsResult.collisionY() ? 0 : velocity.y();
                        double newZ = physicsResult.collisionZ() ? 0 : velocity.z();
                        newX *= this.getAerodynamics().horizontalAirResistance();
                        newY *= this.getAerodynamics().verticalAirResistance();
                        newZ *= this.getAerodynamics().horizontalAirResistance();

                        velocity = new Vec(newX, newY, newZ);
                    } else {
                        setNoGravity(true);
                        setVelocity(Vec.ZERO);
                        this.collisionDirection = blockCollisionDirection;
                        if (onStuck()) {
                            scheduler().scheduleNextProcess(this::remove);
                        }
                    }
                }
            }

            Aerodynamics aerodynamics = getAerodynamics();
            velocity = velocity.mul(
                aerodynamics.horizontalAirResistance(),
                aerodynamics.verticalAirResistance(),
                aerodynamics.horizontalAirResistance()
            ).sub(0, hasNoGravity() ? 0 : getAerodynamics().gravity() * ServerProperties.SERVER_TICKS_PER_SECOND.get(), 0);
            onGround = physicsResult.isOnGround();

            float yaw = position.yaw();
            float pitch = position.pitch();

            if (!noClip) {
                yaw = (float) Math.toDegrees(Math.atan2(diff.x(), diff.z()));
                pitch = (float) Math.toDegrees(
                    Math.atan2(diff.y(), Math.sqrt(diff.x() * diff.x() + diff.z() * diff.z())));

                yaw = lerp(prevYaw, yaw);
                pitch = lerp(prevPitch, pitch);
            }

            this.prevYaw = yaw;
            this.prevPitch = pitch;

            refreshPosition(newPosition.withView(yaw, pitch), noClip, isStuck());
        }
    }

    public boolean horizontalCollision() {
        if (previousPhysicsResult != null)
            return previousPhysicsResult.collisionX() || previousPhysicsResult.collisionZ();
        return false;
    }

    @Override
    public int getUpdateInterval() {
        return 1;
    }

    public boolean canSlide() {
        FireworkList fireworkComponent = ((FireworkRocketMeta) this.getEntityMeta()).getFireworkInfo().get(DataComponents.FIREWORKS);

        if (fireworkComponent != null)
            return fireworkComponent.explosions().isEmpty();
        return true;
    }

    @Override
    public void update(long time) {
        this.lifeTicks += 1;
        if (this.lifeTicks >= this.lifeTimeTicks) {
            this.explode();
            this.remove();
            return;
        }
        super.update(time);
    }

    public void explode() {
        PacketSendingUtils.sendGroupedPacket(getViewers(), new EntityStatusPacket(this.getEntityId(), (byte) 17));

        FireworkList fireworkComponent = ((FireworkRocketMeta) this.getEntityMeta()).getFireworkInfo().get(DataComponents.FIREWORKS);
        List<FireworkExplosion> explosions = List.of();
        if (fireworkComponent != null)
            explosions = fireworkComponent.explosions();

        if (explosions.isEmpty()) return;

        FireworkRocketMeta meta = (FireworkRocketMeta) this.getEntityMeta();
        Entity shooter = this.getShooter();
        Damage damage = new Damage(
            DamageType.EXPLOSION,
            this,
            shooter,
            shooter == null ? null : shooter.getPosition(),
            5.0F + explosions.size() * 2.0f
        );

        if (!meta.isShotAtAngle() && meta.getShooter() instanceof LivingEntity elytraShooter) {
            elytraShooter.damage(damage);
        }

        this.getInstance().getNearbyEntities(this.getPosition(), 5)
            .stream()
            .filter(entity -> entity instanceof LivingEntity && entity != meta.getShooter())
            .map(entity -> (LivingEntity) entity)
            .forEach(entity -> {
                Vec knockback = this.getKnockBack(entity);

                if (this.getBoundingBox().expand(0.5, 0.5, 0.5)
                    .intersectEntity(this.getPosition(), entity)) {

                    if (!blockFeature.isDamageBlocked(entity, damage))
                        entity.takeKnockback(0.4f, knockback.x(), knockback.z());
                    entity.damage(damage);

                } else if (this.hasConfigurableLineOfSight(entity, 0.0)
                    || this.hasConfigurableLineOfSight(entity, entity.getEyeHeight() / 2)) {

                    double distance = this.getDistance(entity);
                    Damage calculatedDamage = new Damage(
                        damage.getType(),
                        damage.getSource(),
                        damage.getAttacker(),
                        damage.getSourcePosition(),
                        (float) (damage.getAmount() * Math.sqrt((5.0f - distance) / 5.0f))
                    );
                    if (!blockFeature.isDamageBlocked(entity, calculatedDamage))
                        entity.takeKnockback(0.4f, knockback.x(), knockback.z());
                    entity.damage(calculatedDamage);
                }
            });
    }

    protected Vec getKnockBack(LivingEntity target) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double x = (this.getPosition().x() - target.getPosition().x());
        double z = this.getPosition().z() - target.getPosition().z();

        while (x * x + z * z < 1.0E-5F) { // 1.0E-5F == 0.00001
            x = (random.nextDouble(-1.0, 1.0)) * 0.01;
            z = (random.nextDouble(-1.0, 1.0)) * 0.01;
        }
        return new Vec(x, z);
    }

    /*
      The regular `hasLineOfSight()` method ray-casts to the target's eye level, while the firework for its explosion
      damage check in vanilla ray-casts to the target's feet level and mid-body level, and if any of those ray-cast
      checks pass, the target is dealt damage
     */
    protected boolean hasConfigurableLineOfSight(Entity entity, double addedHeight) {
        Instance instance = getInstance();
        if (instance == null) {
            return false;
        }

        final Pos start = position.withY(position.y() + getEyeHeight());
        final Pos end = entity.getPosition().withY(entity.getPosition().y() + addedHeight);
        final Vec direction = end.sub(start).asVec().normalize();
        if (!entity.getBoundingBox().boundingBoxRayIntersectionCheck(start.asVec(), direction, entity.getPosition())) {
            return false;
        }
        return CollisionUtils.isLineOfSightReachingShape(instance, currentChunk, start, end, entity.getBoundingBox(), entity.getPosition());
    }
}