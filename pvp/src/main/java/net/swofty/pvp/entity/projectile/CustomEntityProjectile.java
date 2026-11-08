package net.swofty.pvp.entity.projectile;

import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.swofty.pvp.utils.ProjectileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class CustomEntityProjectile extends Entity {
	private static final BoundingBox POINT_BOX = new BoundingBox(0, 0, 0);
	private static final BoundingBox UNSTUCK_BOX = new BoundingBox(0.12, 0.6, 0.12);

	private final Entity shooter;
	protected boolean noClip;

	protected Vec collisionDirection;

	private PhysicsResult previousPhysicsResult = null;

	/**
	 * Constructs new projectile.
	 *
	 * @param shooter          shooter of the projectile: may be null.
	 * @param entityType       type of the projectile.
	 */
	public CustomEntityProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
		super(entityType);
		this.shooter = shooter;
		setup();
	}

	private void setup() {
		collidesWithEntities = false;
		preventBlockPlacement = false;
		setAerodynamics(new Aerodynamics(getAerodynamics().gravity(), 0.99, 0.99));
		if (getEntityMeta() instanceof ProjectileMeta) {
			((ProjectileMeta) getEntityMeta()).setShooter(shooter);
		}
		setSynchronizationTicks(getUpdateInterval());
	}

	public @Nullable Entity getShooter() {
		return shooter;
	}

	/**
	 * Called when this projectile is stuck in blocks.
	 * Probably you want to do nothing with arrows in such case and to remove other types of projectiles.
	 *
	 * @return Whether this entity should be removed
	 */
	public boolean onStuck() {
		return false;
	}

	/**
	 * Called when this projectile unstucks.
	 * Probably you want to add some random velocity to arrows in such case.
	 */
	public void onUnstuck() {

	}

	/**
	 * @return Whether this entity should be removed
	 */
	public boolean onHit(Entity entity) {
		return false;
	}

	public void shootFrom(Pos from, double power, double spread) {
		Point to = from.add(shooter.getPosition().direction());
		shoot(from, to, power, spread);
	}

	@Deprecated
	public void shootTo(Point to, double power, double spread) {
		final var from = this.shooter.getPosition().add(0D, shooter.getEyeHeight(), 0D);
		shoot(from, to, power, spread);
	}

	public void shootFromRotation(float pitch, float yaw, float yBias, double power, double spread, double yawOffset) {
		double dx = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		double dy = -Math.sin(Math.toRadians(pitch + yBias));
		double dz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

		var direction = new Vec(dx, dy, dz).normalize();

		var up = new Vec(0, 1, 0);
		var rightVector = direction.cross(up);
		var orbitalAxis = direction.cross(rightVector);
		direction = direction.rotateAroundAxis(orbitalAxis, Math.toRadians(yawOffset));

		this.shoot(direction.x(), direction.y(), direction.z(), power, spread);
	}

	private void shoot(double dx, double dy, double dz, double power, double spread) {
		//TODO custom shoot event
//		EntityShootEvent shootEvent = new EntityShootEvent(shooter == null ? this : shooter, this, from, power, spread);
//		EventDispatcher.call(shootEvent);
//		if (shootEvent.isCancelled()) {
//			remove();
//			return;
//		}
//		power = shootEvent.getPower();
//		spread = shootEvent.getSpread();

		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		dx /= length;
		dy /= length;
		dz /= length;
		Random random = ThreadLocalRandom.current();
		spread *= 0.007499999832361937D;
		dx += random.nextGaussian() * spread;
		dy += random.nextGaussian() * spread;
		dz += random.nextGaussian() * spread;

		final double mul = ServerProperties.SERVER_TICKS_PER_SECOND.get() * power;
		this.velocity = new Vec(dx * mul, dy * mul, dz * mul);
		setView(
			(float) Math.toDegrees(Math.atan2(dx, dz)),
			(float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
		);
	}

	private void shoot(@NotNull Pos from, @NotNull Point to, double power, double spread) {
		// Mostly copied from Minestom PlayerProjectile
		float pitch = -from.pitch();
		double pitchDiff = pitch - 45;
		if (pitchDiff == 0) pitchDiff = 0.0001;
		double pitchAdjust = pitchDiff * 0.002145329238474369D;

		double dx = to.x() - from.x();
		double dy = to.y() - from.y() + pitchAdjust;
		double dz = to.z() - from.z();
		if (!hasNoGravity()) {
			final double xzLength = Math.sqrt(dx * dx + dz * dz);
			dy += xzLength * 0.20000000298023224D;
		}

		shoot(dx, dy, dz, power, spread);
	}

	@Override
	public void remove() {
		super.remove();
	}

	@Override
	public void tick(long time) {
		super.tick(time);
		if (isRemoved()) return;

		if (isStuck() && shouldUnstuck()) {
			EventDispatcher.call(new ProjectileUncollideEvent(this));
			collisionDirection = null;
			setNoGravity(false);
			onUnstuck();
		}
	}

	public boolean isStuck() {
		return collisionDirection != null;
	}

	private boolean shouldUnstuck() {
		Point collidedPoint = position.add(collisionDirection.mul(0.003)); // Move slightly inside the collided block
		Point collidedBlockVec = collidedPoint.asBlockVec();
		Block block = instance.getBlock(collidedPoint);

		return !block.collisionShape().intersectBox(collidedPoint.sub(collidedBlockVec).sub(0, 0.6, 0), UNSTUCK_BOX);
	}

	protected boolean canHit(Entity entity) {
		return entity instanceof LivingEntity && !(entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR);
	}

	@Override
	protected void synchronizePosition() {
		// For some reason, sending a synchronization when stuck means the position of the arrow will change slightly
		// on the client even though the position on the server has not changed at all. Why? No clue.
		// This check does solve the issue though.
		if (isStuck()) return;

		super.synchronizePosition();
	}

	protected float prevYaw;
	protected float prevPitch;

	@Override
	protected void movementTick() {
		// Mostly copied from Minestom
		this.gravityTickCount = isStuck() ? 0 : gravityTickCount + 1;
		if (vehicle != null) return;

		if (!isStuck()) {
			Vec diff = velocity.div(ServerProperties.SERVER_TICKS_PER_SECOND.get());
			// Prevent entity infinitely in the void
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
				// We won't check collisions with self for first ticks of projectile's life, because it spawns in the
				// shooter and will immediately be triggered by him.
				boolean noCollideShooter = getAliveTicks() < 6;
				Collection<EntityCollisionResult> entityResult = CollisionUtils.checkEntityCollisions(instance, boundingBox.expand(0.1, 0.3, 0.1),
					position.add(0, -0.3, 0), diff, 3, e -> {
						if (noCollideShooter && e == shooter) return false;
						return e != this && canHit(e);
					}, physicsResult);

				if (!entityResult.isEmpty()) {
					Vec prevVelocity = velocity;
					EntityCollisionResult collided = entityResult.stream().findFirst().orElse(null);

					var event = new ProjectileCollideWithEntityEvent(this, collided.collisionPoint().asPos(), collided.entity());
					EventDispatcher.call(event);
					if (!event.isCancelled()) {
						if (onHit(collided.entity())) {
							// Don't remove now because rest of Entity#tick might throw errors
							scheduler().scheduleNextProcess(this::remove);
							// Prevent hitting blocks
							return;
						} else {
							// If velocity has been changed because of bounce, prevent projectile from moving further
							if (velocity != prevVelocity) newPosition = position;
						}
					}
				}
			}

			Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, physicsResult.newPosition());
			if (!ChunkUtils.isLoaded(finalChunk)) return;

			if (physicsResult.hasCollision() && !isStuck()) {
				double signumX = physicsResult.collisionX() ? Math.signum(velocity.x()) : 0;
				double signumY = physicsResult.collisionY() ? Math.signum(velocity.y()) : 0;
				double signumZ = physicsResult.collisionZ() ? Math.signum(velocity.z()) : 0;
				Vec collisionDirection = new Vec(signumX, signumY, signumZ);

				Point collidedPosition = collisionDirection.add(physicsResult.newPosition()).apply(Vec.Operator.FLOOR);
				Block block = instance.getBlock(collidedPosition);

				var event = new ProjectileCollideWithBlockEvent(this, physicsResult.newPosition().withCoord(collidedPosition), block);
				EventDispatcher.call(event);
				if (!event.isCancelled()) {
					setNoGravity(true);
					setVelocity(Vec.ZERO);
					this.collisionDirection = collisionDirection;

					if (onStuck()) {
						// Don't remove now because rest of Entity#tick might throw errors
						scheduler().scheduleNextProcess(this::remove);
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

				// Vanilla really likes to use variables from the render code
				// on the server side in a way that does not make sense at all
				yaw = lerp(prevYaw, yaw);
				pitch = lerp(prevPitch, pitch);
			}

			this.prevYaw = yaw;
			this.prevPitch = pitch;

			refreshPosition(newPosition.withView(yaw, pitch), noClip, isStuck());
		}
	}

	protected static float lerp(float first, float second) {
		return first + (second - first) * 0.2f;
	}

	@Override
	public void setView(float yaw, float pitch) {
		this.prevYaw = yaw;
		this.prevPitch = pitch;

		super.setView(yaw, pitch);
	}

	@Override
	public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position) {
		this.prevYaw = position.yaw();
		this.prevPitch = position.pitch();

		return super.teleport(position);
	}

	protected int getUpdateInterval() {
		return 20;
	}
}
