package net.swofty.type.skyblockgeneric.entity;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.projectile.ArrowMeta;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.skyblockgeneric.event.custom.ArrowHitBlockEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ArrowEntityImpl extends LivingEntity {
    private long cooldown = 0;
    @Getter
    private final Entity shooter;
    @Getter
    private final SkyBlockItem arrowItem;
    private boolean hasHit = false;

    public ArrowEntityImpl(Entity player, SkyBlockItem arrowItem) {
        super(EntityType.ARROW);

        this.collidesWithEntities = false;
        this.hasPhysics = false;

        this.shooter = player;
        this.arrowItem = arrowItem;

        ArrowMeta arrowMeta = (ArrowMeta) getEntityMeta();
        arrowMeta.setShooter(player);
        arrowMeta.setPiercingLevel((byte) 5);
    }

    @Override
    public void spawn() {
        super.spawn();
        cooldown = System.currentTimeMillis();

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (!isRemoved()) {
                kill();
                remove();
            }
        }, TaskSchedule.tick(30), TaskSchedule.stop());
    }

    @Override
    public void tick(long time) {
        final Pos posBefore = getPosition();
        super.tick(time);
        final Pos posNow = getPosition();

        Vec diff = posNow.sub(posBefore).asVec();
        PhysicsResult result = CollisionUtils.handlePhysics(
                instance, this.getChunk(),
                this.getBoundingBox(),
                posBefore, diff,
                null, true
        );

        if (cooldown + 200 < System.currentTimeMillis()) {
            float yaw = (float) Math.toDegrees(Math.atan2(diff.x(), diff.z()));
            float pitch = (float) Math.toDegrees(Math.atan2(diff.y(), Math.sqrt(diff.x() * diff.x() + diff.z() * diff.z())));
            super.refreshPosition(new Pos(posNow.x(), posNow.y(), posNow.z(), yaw, pitch));
            cooldown = System.currentTimeMillis();
        }

        @NotNull Collection<EntityCollisionResult> collided = CollisionUtils.checkEntityCollisions(instance, this.getBoundingBox(), posBefore, diff, 3, (e) -> e != this, result);
        for (EntityCollisionResult collisionResult : collided) {
            if (collisionResult != null && collisionResult.entity() != shooter) {
                Entity entity = collisionResult.entity();
                EntityType entityType = entity.getEntityType();
                if (entityType == EntityType.PLAYER ||
                        entityType == EntityType.ARMOR_STAND) {
                    return;
                }

                var e = new ProjectileCollideWithEntityEvent(
                        this,
                    collisionResult.collisionPoint().asPos(),
                        entity
                );
                MinecraftServer.getGlobalEventHandler().call(e);
                if (!e.isCancelled()) {
                    remove();
                    kill();
                }
                return;
            }
        }

        if (result.hasCollision()) {
            if (hasHit) return;

            // Use the collision shape positions directly from physics result
            var shapePositions = result.collisionShapePositions();

            if (shapePositions.length > 0 && shapePositions[0] != null) {
                var hitPosition = shapePositions[0];
                Block hitBlock = getInstance().getBlock(hitPosition);

                if (!hitBlock.air()) {
                    // Fire arrow hit block event
                    ArrowHitBlockEvent arrowHitBlockEvent = new ArrowHitBlockEvent(shooter, arrowItem, hitBlock, hitPosition);
                    HypixelEventHandler.callCustomEvent(arrowHitBlockEvent);
                    hasHit = true;
                }
            }

            // We've hit a block
            ScheduleUtility.delay(() -> {
                if (isDead() || isRemoved()) return;
                remove();
                kill();
            }, 20);
        }
    }
}
