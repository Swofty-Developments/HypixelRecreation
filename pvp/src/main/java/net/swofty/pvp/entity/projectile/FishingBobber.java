package net.swofty.pvp.entity.projectile;

import net.swofty.pvp.feature.projectile.VanillaFishingRodFeature;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

public class FishingBobber extends CustomEntityProjectile {
	private final boolean legacy;
	private int stuckTime;
	private Entity hooked;
	private State state = State.IN_AIR;
	private Pos prevPos = Pos.ZERO;
	
	private final double customGravity;
	
	public FishingBobber(@Nullable Entity shooter, boolean legacy) {
		super(shooter, EntityType.FISHING_BOBBER);
		this.legacy = legacy;
		setOwnerEntity(shooter);
		
		// Custom gravity logic: gravity is applied before movement
		customGravity = legacy ? 0.04 : 0.03;
		setAerodynamics(getAerodynamics().withGravity(0));
		
		// Minestom seems to like having wrong values in its registries
		setAerodynamics(getAerodynamics().withHorizontalAirResistance(0.92).withVerticalAirResistance(0.92));
	}
	
	@Override
	public void tick(long time) {
		prevPos = getPosition();
		velocity = velocity.add(0, -customGravity * ServerProperties.SERVER_TICKS_PER_SECOND.get(), 0);
		super.tick(time);
	}
	
	@Override
	public void update(long time) {
		if (!(getShooter() instanceof Player shooter)) {
			remove();
			return;
		}
		if (shouldStopFishing(shooter)) return;
		
		if (onGround) {
			stuckTime++;
			if (stuckTime >= 1200) {
				remove();
				return;
			}
		} else {
			stuckTime = 0;
		}
		
		if (state == State.IN_AIR) {
			if (hooked != null) {
				velocity = Vec.ZERO;
				setNoGravity(true);
				state = State.HOOKED_ENTITY;
			}
		} else {
			if (state == State.HOOKED_ENTITY) {
				if (hooked != null) {
					if (hooked.isRemoved() || hooked.getInstance() != getInstance()) {
						setHookedEntity(null);
						setNoGravity(false);
						state = State.IN_AIR;
					} else {
						Pos hookedPos = hooked.getPosition();
						teleport(hookedPos.withY(hookedPos.y() + hooked.getBoundingBox().height() * 0.8));
					}
				}
			}
		}
	}
	
	@Override
	public boolean onHit(Entity entity) {
		if (hooked != null) return false;
		setHookedEntity(entity);
		
		if (legacy) {
			if (entity instanceof Player player
					&& (player == getShooter() || player.getGameMode() == GameMode.CREATIVE))
				return false;
			
			Pos posNow = this.position;
			this.position = prevPos;
			if (((LivingEntity) entity).damage(new Damage(DamageType.GENERIC, null, null, null, 0))) {
				entity.setVelocity(calculateLegacyKnockback(entity.getVelocity(), entity.getPosition()));
			}
			this.position = posNow;
		}
		
		return false;
	}
	
	private void setHookedEntity(@Nullable Entity entity) {
		this.hooked = entity;
		((FishingHookMeta) getEntityMeta()).setHookedEntity(entity);
	}
	
	private void setOwnerEntity(@Nullable Entity entity) {
		((FishingHookMeta) getEntityMeta()).setOwnerEntity(entity);
	}
	
	private boolean shouldStopFishing(Player player) {
		boolean main = player.getItemInMainHand().material() == Material.FISHING_ROD;
		boolean off = player.getItemInOffHand().material() == Material.FISHING_ROD;
		if (player.isRemoved() || player.isDead() || (!main && !off)
				|| (!legacy && getDistanceSquared(player) > 1024)) {
			setOwnerEntity(null);
			remove();
			return true;
		}
		
		return false;
	}
	
	public int retrieve() {
		if (!(getShooter() instanceof Player shooter)) return 0;
		if (shouldStopFishing(shooter)) return 0;
		
		int durability = 0;
		if (hooked != null) {
			if (!legacy) {
				pullEntity(hooked);
				triggerStatus((byte) 31);
			}
			durability = hooked instanceof ItemEntity ? 3 : 5;
		}
		
		remove();
		
		return durability;
	}
	
	private void pullEntity(Entity entity) {
		Entity shooter = getShooter();
		if (shooter == null) return;
		
		Pos shooterPos = shooter.getPosition();
		Pos pos = getPosition();
		Vec velocity = new Vec(shooterPos.x() - pos.x(), shooterPos.y() - pos.y(),
				shooterPos.z() - pos.z()).mul(0.1);
		velocity = velocity.mul(ServerProperties.SERVER_TICKS_PER_SECOND.get());
		entity.setVelocity(entity.getVelocity().add(velocity));
	}
	
	private Vec calculateLegacyKnockback(Vec currentVelocity, Pos entityPos) {
		currentVelocity = currentVelocity.div(ServerProperties.SERVER_TICKS_PER_SECOND.get());
		
		Pos position = getPosition();
		double dx = position.x() - entityPos.x();
		double dz = position.z() - entityPos.z();
		
		while (dx * dx + dz * dz < 0.0001) {
			dx = (Math.random() - Math.random()) * 0.01;
			dz = (Math.random() - Math.random()) * 0.01;
		}
		
		double distance = Math.sqrt(dx * dx + dz * dz);
		
		double x = currentVelocity.x() / 2;
		double y = currentVelocity.y() / 2;
		double z = currentVelocity.z() / 2;
		
		// Normalize to have similar knockback on every distance
		x -= dx / distance * 0.4;
		y += 0.4;
		z -= dz / distance * 0.4;
		
		if (y > 0.4)
			y = 0.4;
		
		return new Vec(x, y, z).mul(ServerProperties.SERVER_TICKS_PER_SECOND.get());
	}
	
	@Override
	public void remove() {
		Entity shooter = getShooter();
		if (shooter != null) {
			if (shooter.getTag(VanillaFishingRodFeature.FISHING_BOBBER) == this) {
				shooter.removeTag(VanillaFishingRodFeature.FISHING_BOBBER);
			}
		}
		
		super.remove();
	}
	
	private enum State {
		IN_AIR,
		HOOKED_ENTITY,
		BOBBING
	}
}
