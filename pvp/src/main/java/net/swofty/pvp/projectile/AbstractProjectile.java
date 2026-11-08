package net.swofty.pvp.projectile;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractProjectile extends Entity implements Projectile {
    @Getter
	protected final Entity shooter;
    protected PhysicsResult previousPhysicsResult;
    protected Pos previousPosition;
    protected boolean inBlock = false;

    public AbstractProjectile(EntityType type, Entity shooter) {
        super(type);
        this.shooter = shooter;
    }

    @Override
    public void tick(long time) {
        if (removed || inBlock) return;
        updatePosition(time);

        if (!callEntityCollision()) {
            callBlockCollision();
        }
    }

    @Override
    public void shoot(Point from, double power, double spread) {
        var to = from.add(shooter.getPosition().direction());
        shoot(from, to, power, spread);
    }

    protected PhysicsResult computePhysics(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Block.Getter blockGetter, @NotNull Aerodynamics aerodynamics) {
        var newVelocity = updateVelocity(entityPosition, currentVelocity, blockGetter, aerodynamics, true, false, onGround, false);

        var newPhysicsResult = CollisionUtils.handlePhysics(
                blockGetter,
                this.boundingBox,
                entityPosition, newVelocity,
                previousPhysicsResult, true
        );

        previousPhysicsResult = newPhysicsResult;
        return newPhysicsResult;
    }

    @Override
    protected void movementTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
        if (vehicle != null) return;

        this.previousPosition = position;
        final Block.Getter chunkCache = new ChunkCache(instance, currentChunk);
        PhysicsResult result = computePhysics(
                position, velocity.div(ServerProperties.SERVER_TICKS_PER_SECOND.get()),
                chunkCache, getAerodynamics());

        Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, result.newPosition());
        if (!ChunkUtils.isLoaded(finalChunk)) return;

        onGround = result.isOnGround();

        if (!result.hasCollision()) {
            velocity = previousPhysicsResult.newVelocity().mul(ServerProperties.SERVER_TICKS_PER_SECOND.get()).mul(0.99);
        }

        refreshPosition(result.newPosition(), true, false);
        if (hasVelocity()) sendPacketToViewers(getVelocityPacket());
    }

    protected void callBlockCollision() {
        if (previousPhysicsResult.hasCollision()) {
            Block hitBlock = null;
            Point hitPoint = null;
            if (previousPhysicsResult.collisionShapes()[0] instanceof ShapeImpl) {
                hitBlock = instance.getBlock(previousPhysicsResult.collisionPoints()[0].sub(0, Vec.EPSILON, 0), Block.Getter.Condition.TYPE);
                hitPoint = previousPhysicsResult.collisionPoints()[0];
            }
            if (previousPhysicsResult.collisionShapes()[1] instanceof ShapeImpl) {
                hitBlock = instance.getBlock(previousPhysicsResult.collisionPoints()[1].sub(0, Vec.EPSILON, 0), Block.Getter.Condition.TYPE);
                hitPoint = previousPhysicsResult.collisionPoints()[1];
            }
            if (previousPhysicsResult.collisionShapes()[2] instanceof ShapeImpl) {
                hitBlock = instance.getBlock(previousPhysicsResult.collisionPoints()[2].sub(0, Vec.EPSILON, 0), Block.Getter.Condition.TYPE);
                hitPoint = previousPhysicsResult.collisionPoints()[2];
            }

            if (hitBlock == null) return;
            handleBlockCollision(hitBlock, hitPoint, previousPosition);
        }
    }

    protected void callBlockCollisionEvent(@NotNull Pos pos, Block hitBlock) {
        var e = new ProjectileCollideWithBlockEvent(this, pos, hitBlock);
        EventDispatcher.call(e);
    }

    protected boolean callEntityCollisionEvent(@NotNull Pos pos, @NotNull Entity entity) {
        ProjectileCollideWithEntityEvent e = new ProjectileCollideWithEntityEvent(this, pos, entity);
        EventDispatcher.call(e);
        if (!e.isCancelled()) {
            remove();
            return true;
        }

        return false;
    }

    protected boolean callEntityCollision() {
        return callEntityCollision(boundingBox);
    }

    protected boolean callEntityCollision(BoundingBox boundingBox) {
        if (previousPhysicsResult == null) return false;
        var diff = previousPhysicsResult.newPosition().sub(previousPosition).asVec();

        var collidedEntities = CollisionUtils.checkEntityCollisions(instance, boundingBox, previousPosition, diff, diff.length(),
                entity -> entity != shooter && entity != this, previousPhysicsResult);

        var arr = collidedEntities.stream().sorted().toList();
        if (!arr.isEmpty()) {
            for (var collision : arr) {
                if (handleEntityCollision(collision, previousPhysicsResult.newPosition(), previousPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void updatePosition(long time) {
        if (instance == null || isRemoved() || !ChunkUtils.isLoaded(currentChunk)) return;

        movementTick();
        super.update(time);
        EventDispatcher.call(new EntityTickEvent(this));
    }

    protected void handleBlockCollision(Block hitBlock, Point hitPos, Pos posBefore) {
        velocity = Vec.ZERO;
        setNoGravity(true);
        inBlock = true;

        // if the value is zero, it will be unlit. If the value is more than 0.01, there will be noticeable pitch change visually
        position = new Pos(hitPos.x(), hitPos.y(), hitPos.z(), posBefore.yaw(), posBefore.pitch());
        MinecraftServer.getSchedulerManager().scheduleNextTick(this::synchronizePosition); // required as in rare situations there will be a slight disagreement with the client and server on if it hit or not | also scheduling next tick so it doesn't jump to the hit position until it has actually hit

        callBlockCollisionEvent(hitPos.asPos(), hitBlock);

        BlockHandler blockHandler = hitBlock.handler();
        if (blockHandler == null) return;
        blockHandler.onTouch(new BlockHandler.Touch(hitBlock, instance, hitPos, this));
    }

    /**
     * Handle entity collision
     * @param result the entity that was hit
     * @param hitPos the position where the entity was hit
     * @param posBefore the position before the collision
     * @return true if block collisions should be ignored (i.e. the entity was removed while handling entity collision during the same tick)
     */
    protected boolean handleEntityCollision(EntityCollisionResult result, Point hitPos, Pos posBefore) {
        return callEntityCollisionEvent(hitPos.asPos(), result.entity());
    }

	protected abstract @NotNull Vec updateVelocity(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Block.@NotNull Getter blockGetter, @NotNull Aerodynamics aerodynamics, boolean positionChanged, boolean entityFlying, boolean entityOnGround, boolean entityNoGravity);
}
