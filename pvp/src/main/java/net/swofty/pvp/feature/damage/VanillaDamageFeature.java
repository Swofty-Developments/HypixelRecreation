package net.swofty.pvp.feature.damage;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.swofty.pvp.damage.DamageTypeInfo;
import net.swofty.pvp.events.EntityPreDeathEvent;
import net.swofty.pvp.events.FinalDamageEvent;
import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.feature.RegistrableFeature;
import net.swofty.pvp.feature.armor.ArmorFeature;
import net.swofty.pvp.feature.block.BlockFeature;
import net.swofty.pvp.feature.config.DefinedFeature;
import net.swofty.pvp.feature.config.FeatureConfiguration;
import net.swofty.pvp.feature.food.ExhaustionFeature;
import net.swofty.pvp.feature.item.ItemDamageFeature;
import net.swofty.pvp.feature.knockback.KnockbackFeature;
import net.swofty.pvp.feature.provider.DifficultyProvider;
import net.swofty.pvp.feature.totem.TotemFeature;
import net.swofty.pvp.feature.tracking.TrackingFeature;
import net.swofty.pvp.utils.CombatVersion;
import net.swofty.pvp.utils.EntityUtil;

import java.util.Objects;

/**
 * Vanilla implementation of {@link DamageFeature}.
 * Supports blocking, knockback, totems, armor, etc.
 */
public class VanillaDamageFeature implements DamageFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaDamageFeature> DEFINED = new DefinedFeature<>(
			FeatureType.DAMAGE, VanillaDamageFeature::new,
			FeatureType.DIFFICULTY, FeatureType.BLOCK, FeatureType.ARMOR, FeatureType.TOTEM,
			FeatureType.EXHAUSTION, FeatureType.KNOCKBACK, FeatureType.TRACKING,
			FeatureType.ITEM_DAMAGE, FeatureType.VERSION
	);

	public static final Tag<Long> NEW_DAMAGE_TIME = Tag.Long("newDamageTime");
	public static final Tag<Float> LAST_DAMAGE_AMOUNT = Tag.Float("lastDamageAmount");

	private final FeatureConfiguration configuration;

	private DifficultyProvider difficultyProvider;

	private BlockFeature blockFeature;
	private ArmorFeature armorFeature;
	private TotemFeature totemFeature;
	private ExhaustionFeature exhaustionFeature;
	private KnockbackFeature knockbackFeature;
	private TrackingFeature trackingFeature;
	private ItemDamageFeature itemDamageFeature;

	private CombatVersion version;

	public VanillaDamageFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}

	private static void damageManually(LivingEntity entity, float damage) {
		// Additional hearts support
		if (entity instanceof Player player) {
			final float additionalHearts = player.getAdditionalHearts();
			if (additionalHearts > 0) {
				if (damage > additionalHearts) {
					damage -= additionalHearts;
					player.setAdditionalHearts(0);
				} else {
					player.setAdditionalHearts(additionalHearts - damage);
					damage = 0;
				}
			}
		}

		// Set the final entity health
		entity.setHealth(entity.getHealth() - damage);
	}

	@Override
	public void initDependencies() {
		this.difficultyProvider = configuration.get(FeatureType.DIFFICULTY);
		this.blockFeature = configuration.get(FeatureType.BLOCK);
		this.armorFeature = configuration.get(FeatureType.ARMOR);
		this.totemFeature = configuration.get(FeatureType.TOTEM);
		this.exhaustionFeature = configuration.get(FeatureType.EXHAUSTION);
		this.knockbackFeature = configuration.get(FeatureType.KNOCKBACK);
		this.trackingFeature = configuration.get(FeatureType.TRACKING);
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.version = configuration.get(FeatureType.VERSION);
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(EntityDamageEvent.class, this::handleDamage);
	}

	protected void handleDamage(EntityDamageEvent event) {
		boolean shouldAnimate = event.shouldAnimate();
		// We will handle sound and animation ourselves
		event.setAnimation(false);
		SoundEvent sound = event.getSound();
		event.setSound(null);

		LivingEntity entity = event.getEntity();
		Damage damage = event.getDamage();
		Entity attacker = damage.getAttacker();

		// UNOFFICIAL -- START
		if (entity instanceof Player player) {
			if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
				event.setCancelled(true);
				return;
			}
		}

		if (attacker instanceof Player player) {
			if (player.getGameMode() == GameMode.SPECTATOR) {
				event.setCancelled(true);
				return;
			}
		}

		if (entity.getEntityType() == EntityType.FIREBALL) {
			event.setCancelled(true);
			return;
		}
		// UNOFFICIAL -- END

		DamageType damageType = MinecraftServer.getDamageTypeRegistry().get(damage.getType());
		assert damageType != null;

		DamageTypeInfo typeInfo = DamageTypeInfo.of(damage.getType());
		if (event.getEntity() instanceof Player player && typeInfo.shouldScaleWithDifficulty(damage))
			damage.setAmount(scaleWithDifficulty(player, damage.getAmount()));

		if (typeInfo.fire() && entity.hasEffect(PotionEffect.FIRE_RESISTANCE)) {
			event.setCancelled(true);
			return;
		}

		// This will be used to determine whether knockback should be applied
		// We can't just check if the remaining damage is 0 because this would apply no knockback for snowballs & eggs
		boolean fullyBlocked = false;
		if (blockFeature.isDamageBlocked(entity, damage)) {
			fullyBlocked = blockFeature.applyBlock(entity, damage);
		}

		float amount = damage.getAmount();

		var key = entity.getEntityType().asKey();
		assert key != null;
		if (typeInfo.freeze() && Objects.requireNonNull(MinecraftServer.process().entityType().getTag(Key.key("minecraft:freeze_hurts_extra_types")))
				.contains(key)) {
			amount *= 5.0F;
		}

		if (typeInfo.damagesHelmet() && !entity.getEquipment(EquipmentSlot.HELMET).isAir()) {
			itemDamageFeature.damageArmor(entity, damageType, amount, EquipmentSlot.HELMET);
			amount *= 0.75F;
		}

		float amountBeforeProcessing = amount;

		// Invulnerability ticks
		boolean hurtSoundAndAnimation = true;
		long newDamageTime = entity.hasTag(NEW_DAMAGE_TIME) ? entity.getTag(NEW_DAMAGE_TIME) : -10000;
		if (entity.getAliveTicks() - newDamageTime < 0) {
			float lastDamage = entity.hasTag(LAST_DAMAGE_AMOUNT) ? entity.getTag(LAST_DAMAGE_AMOUNT) : 0;

			if (amount <= lastDamage) {
				event.setCancelled(true);
				return;
			}

			hurtSoundAndAnimation = false;
			amount = amount - lastDamage;
		}

		// Process armor and effects
		amount = armorFeature.getDamageWithProtection(entity, damageType, amount);

		damage.setAmount(amount);
		FinalDamageEvent finalDamageEvent = new FinalDamageEvent(entity, damage, 10, shouldAnimate);
		EventDispatcher.call(finalDamageEvent);
		// New amount has been set in the Damage class
		amount = damage.getAmount();

		if (finalDamageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		// Register damage to tracking feature
		boolean register = version.legacy() || amount > 0;
		if (register && entity instanceof Player player)
			trackingFeature.recordDamage(player, attacker, damage);

		// Exhaustion from damage
		if (amountBeforeProcessing != 0 && entity instanceof Player player)
			exhaustionFeature.addDamageExhaustion(player, damageType);

		if (register) entity.setTag(LAST_DAMAGE_AMOUNT, amountBeforeProcessing);

		if (hurtSoundAndAnimation) {
			entity.setTag(NEW_DAMAGE_TIME, entity.getAliveTicks() + finalDamageEvent.getInvulnerabilityTicks());

			if (fullyBlocked) {
				// Shield status
				entity.triggerStatus((byte) 29);
			} else if (finalDamageEvent.shouldAnimate()) {
				entity.sendPacketToViewersAndSelf(new DamageEventPacket(
						entity.getEntityId(),
						damage.getType(),
						damage.getAttacker() == null ? null : damage.getAttacker().getEntityId(),
						damage.getSource() == null ? null : damage.getSource().getEntityId(),
						null
				));
			}

			if (!fullyBlocked && damage.getType() != DamageType.DROWN) {
				if (attacker != null && !typeInfo.explosive()) {
					knockbackFeature.applyDamageKnockback(damage, entity);
				} else {
					// Update velocity
					entity.setVelocity(entity.getVelocity());
				}
			}
		}

		if (fullyBlocked) {
			event.setCancelled(true);
			return;
		}

		boolean death = false;
		float totalHealth = entity.getHealth() +
				(entity instanceof Player player ? player.getAdditionalHearts() : 0);
		if (totalHealth - amount <= 0) {
			boolean totem = totemFeature.tryProtect(entity, damageType);

			if (totem) {
				event.setCancelled(true);
			} else {
				death = true;
				if (hurtSoundAndAnimation) {
					// Death sound
					sound = entity instanceof Player ? SoundEvent.ENTITY_PLAYER_DEATH : SoundEvent.ENTITY_GENERIC_DEATH;
				}
			}
		} else if (hurtSoundAndAnimation) {
			// Workaround to have different types make a different sound,
			// but only if the sound has not been changed by damage#getSound
			if (entity instanceof Player && sound == SoundEvent.ENTITY_PLAYER_HURT) {
				String effects = damageType.effects();
				if (effects != null) sound = switch (effects) {
					case "thorns" -> SoundEvent.ENCHANT_THORNS_HIT;
					case "drowning" -> SoundEvent.ENTITY_PLAYER_HURT_DROWN;
					case "burning" -> SoundEvent.ENTITY_PLAYER_HURT_ON_FIRE;
					case "poking" -> SoundEvent.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH;
					case "freezing" -> SoundEvent.ENTITY_PLAYER_HURT_FREEZE;
					default -> sound;
				};
			}
		}

		if (hurtSoundAndAnimation) {
			// Play sound (copied from Minestom, because of complications with cancelling)
			if (sound != null) entity.sendPacketToViewersAndSelf(new SoundEffectPacket(
					sound, entity instanceof Player ? Sound.Source.PLAYER : Sound.Source.HOSTILE,
					entity.getPosition(),
					//TODO seed randomizing?
					1.0f, 1.0f, 0
			));
		}

		damage.setAmount(amount);

		if (death && !event.isCancelled()) {
			EntityPreDeathEvent entityPreDeathEvent = new EntityPreDeathEvent(entity, damage);
			EventDispatcher.call(entityPreDeathEvent);
			if (entityPreDeathEvent.isCancelled()) event.setCancelled(true);
			if (entityPreDeathEvent.isCancelDeath()) amount = 0;
		}

		damage.setAmount(amount);

		// lastDamage field is set when event is not cancelled but should also when cancelled
		if (register) EntityUtil.setLastDamage(entity, damage);

		// The Minestom damage method should return false if there was no hurt animation,
		// because otherwise the attack feature will deal extra knockback
		if (!event.isCancelled() && !hurtSoundAndAnimation) {
			event.setCancelled(true);
			damageManually(entity, amount);
		}
	}

	protected float scaleWithDifficulty(Player player, float amount) {
		return switch (difficultyProvider.getValue(player)) {
			case PEACEFUL -> -1;
			case EASY -> Math.min(amount / 2.0f + 1.0f, amount);
			case HARD -> amount * 3.0f / 2.0f;
			default -> amount;
		};
	}
}
