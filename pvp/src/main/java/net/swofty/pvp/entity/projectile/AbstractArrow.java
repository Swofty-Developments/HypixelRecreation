package net.swofty.pvp.entity.projectile;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import net.swofty.pvp.events.PickupEntityEvent;
import net.swofty.pvp.feature.enchantment.EnchantmentFeature;
import net.swofty.pvp.feature.provider.SoundProvider;
import net.swofty.pvp.utils.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractArrow extends CustomEntityProjectile {
	private static final double ARROW_BASE_DAMAGE = 2.0;

	protected int pickupDelay;
	protected int stuckTime;
	protected PickupMode pickupMode = PickupMode.DISALLOWED;
	protected int ticks;
	private double baseDamage = ARROW_BASE_DAMAGE;
	private int knockback;
	private SoundEvent soundEvent = getDefaultSound();
	private SoundProvider soundProvider;

	private final Set<Integer> piercingIgnore = new HashSet<>();
	private int fireTicksLeft = 0;

	protected final EnchantmentFeature enchantmentFeature;

	public AbstractArrow(@Nullable Entity shooter, @NotNull EntityType entityType,
	                     EnchantmentFeature enchantmentFeature) {
		super(shooter, entityType);
		this.enchantmentFeature = enchantmentFeature;

		if (shooter instanceof Player) {
			pickupMode = ((Player) shooter).getGameMode() == GameMode.CREATIVE ? PickupMode.CREATIVE_ONLY : PickupMode.ALLOWED;
		}
	}

	@Override
	public void update(long time) {
		if (onGround) {
			stuckTime++;
		} else {
			stuckTime = 0;
		}

		if (pickupDelay > 0) {
			pickupDelay--;
		}

		if (fireTicksLeft > 0) {
			if (entityMeta.isOnFire()) {
				fireTicksLeft--;
				if (fireTicksLeft == 0) {
					entityMeta.setOnFire(false);
				}
			} else {
				fireTicksLeft = 0;
			}
		}

        // Pickup
        if (canBePickedUp(null)) {
            instance.getEntityTracker().nearbyEntities(position, 5, EntityTracker.Target.PLAYERS,
                    player -> {
                        if (!player.canPickupItem()) return;

                        // Do not pickup if not visible
                        if (!isViewer(player))
                            return;

                        if (isRemoved() || !canBePickedUp(player))
                            return;

                        if (player.getBoundingBox().expand(1, 0.5f, 1)
                                .intersectEntity(player.getPosition(), this)) {
                            PickupEntityEvent event = new PickupEntityEvent(player, this);
                            EventDispatcher.callCancellable(event, () -> {
                                if (pickup(player)) {
                                    player.sendPacketToViewersAndSelf(new CollectItemPacket(
                                            getEntityId(), player.getEntityId(), 1
                                    ));
                                    remove();
                                }
                            });
                        }
                    });
        }

		//TODO water (also for other projectiles?)

		tickRemoval();
	}

	public void setFireTicksLeft(int fireTicksLeft) {
		this.fireTicksLeft = fireTicksLeft;
		if (fireTicksLeft > 0) entityMeta.setOnFire(true);
	}

	protected void tickRemoval() {
		ticks++;
		if (ticks >= 1200) {
			remove();
		}
	}

	@Override
	public void onUnstuck() {
		((AbstractArrowMeta) getEntityMeta()).setInGround(false);
		ThreadLocalRandom random = ThreadLocalRandom.current();
		setVelocity(velocity.mul(
				random.nextDouble() * 0.2,
				random.nextDouble() * 0.2,
				random.nextDouble() * 0.2
		));
		ticks = 0;
	}

	@Override
	protected boolean canHit(Entity entity) {
		return super.canHit(entity) && !piercingIgnore.contains(entity.getEntityId());
	}

	@Override
	public boolean onHit(@NotNull Entity entity) {
		if (piercingIgnore.contains(entity.getEntityId())) return false;
		if (!(entity instanceof LivingEntity living)) return false;

		ThreadLocalRandom random = ThreadLocalRandom.current();

		double movementSpeed = getVelocity().length() / ServerProperties.SERVER_TICKS_PER_SECOND.get();
		int damage = (int) Math.ceil(MathUtils.clamp(
				movementSpeed * baseDamage, 0.0, 2.147483647E9D));

		if (getPiercingLevel() > 0) {
			if (piercingIgnore.size() >= getPiercingLevel() + 1) {
				return true;
			}

			piercingIgnore.add(entity.getEntityId());
		}

		if (isCritical()) {
			int randomDamage = random.nextInt(damage / 2 + 2);
			damage = (int) Math.min(randomDamage + damage, 2147483647L);
		}

		Entity shooter = getShooter();
		Damage damageObj = new Damage(
				DamageType.ARROW,
				this, Objects.requireNonNullElse(shooter, this),
				null, damage
		);

		Pos position = getPosition();
		if (living.damage(damageObj)) {
			if (entity.getEntityType() == EntityType.ENDERMAN) return false;

			if (isOnFire()) {
				living.setFireTicks(5 * ServerProperties.SERVER_TICKS_PER_SECOND.get());
			}

			if (getPiercingLevel() <= 0) {
				living.setArrowCount(living.getArrowCount() + 1);
			}

			if (knockback > 0) {
				Vec knockbackVec = getVelocity()
						.mul(1, 0, 1)
						.normalize().mul(knockback * 0.6);
				knockbackVec = knockbackVec.add(0, 0.1, 0)
						.mul(ServerProperties.SERVER_TICKS_PER_SECOND.get() / 2.0);

				if (knockbackVec.lengthSquared() > 0) {
					Vec newVel = living.getVelocity().add(knockbackVec);
					living.setVelocity(newVel);
				}
			}

			if (shooter instanceof LivingEntity livingShooter) {
				enchantmentFeature.onUserDamaged(living, livingShooter);
				enchantmentFeature.onTargetDamaged(livingShooter, living);
			}

			onHurt(living);

			if (living != shooter && living instanceof Player
					&& shooter instanceof Player shooterPlayer && !isSilent()) {
				shooterPlayer.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.ARROW_HIT_PLAYER, 0.0F));
			}

			if (!isSilent()) {
				getViewersAsAudience().playSound(Sound.sound(
						getSound(), Sound.Source.NEUTRAL,
						1.0f, 1.2f / (random.nextFloat() * 0.2f + 0.9f)
				), position.x(), position.y(), position.z());
			}

			return getPiercingLevel() <= 0;
		} else {
			setVelocity(getVelocity().mul(-0.5 * 0.2));
			refreshPosition(position.withYaw(position.yaw() + 170.0f + 20.0f * ThreadLocalRandom.current().nextFloat()));

			if (getVelocity().lengthSquared() < 1.0E-7D) {
				if (pickupMode == PickupMode.ALLOWED) {
					EntityUtil.spawnItemAtLocation(this, getPickupItem(), 0.1);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onStuck() {
		if (!isSilent()) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			getViewersAsAudience().playSound(Sound.sound(
					getSound(), Sound.Source.NEUTRAL,
					1.0f, 1.2f / (random.nextFloat() * 0.2f + 0.9f)
			), position.x(), position.y(), position.z());
		}

		pickupDelay = 7;
		((AbstractArrowMeta) getEntityMeta()).setInGround(true);
		setCritical(false);
		setPiercingLevel((byte) 0);
		setSound(SoundEvent.ENTITY_ARROW_HIT);
		piercingIgnore.clear();

		return false;
	}

	public boolean canBePickedUp(@Nullable Player player) {
		if (!(onGround || hasNoGravity()) || pickupDelay > 0) {
			return false;
		}

		return switch (pickupMode) {
			case ALLOWED -> true;
			case CREATIVE_ONLY -> player == null || player.getGameMode() == GameMode.CREATIVE;
			default -> false;
		};
	}

	public boolean pickup(Player player) {
		return player.getGameMode() == GameMode.CREATIVE || player.getInventory().addItemStack(getPickupItem());
	}

	protected abstract ItemStack getPickupItem();

	protected void onHurt(LivingEntity entity) {
	}

	public SoundEvent getSound() {
		return soundEvent;
	}

	public void setSound(SoundEvent soundEvent) {
		this.soundEvent = soundEvent;
	}

	protected SoundEvent getDefaultSound() {
		return SoundEvent.ENTITY_ARROW_HIT;
	}

	public int getKnockback() {
		return knockback;
	}

	public void setKnockback(int knockback) {
		this.knockback = knockback;
	}

	public double getBaseDamage() {
		return baseDamage;
	}

	public void setBaseDamage(double baseDamage) {
		this.baseDamage = baseDamage;
	}

	public boolean isCritical() {
		return ((AbstractArrowMeta) getEntityMeta()).isCritical();
	}

	public void setCritical(boolean critical) {
		((AbstractArrowMeta) getEntityMeta()).setCritical(critical);
	}

	public byte getPiercingLevel() {
		return ((AbstractArrowMeta) getEntityMeta()).getPiercingLevel();
	}

	public void setPiercingLevel(byte piercingLevel) {
		((AbstractArrowMeta) getEntityMeta()).setPiercingLevel(piercingLevel);
	}

	public boolean isNoClip() {
		return ((AbstractArrowMeta) getEntityMeta()).isNoClip();
	}

	public void setNoClip(boolean noClip) {
		((AbstractArrowMeta) getEntityMeta()).setNoClip(noClip);
		super.hasPhysics = !noClip;
		super.noClip = noClip;
	}

	public PickupMode getPickupMode() {
		return pickupMode;
	}

	public void setPickupMode(PickupMode pickupMode) {
		this.pickupMode = pickupMode;
	}

	public enum PickupMode {
		DISALLOWED,
		ALLOWED,
		CREATIVE_ONLY
	}
}
