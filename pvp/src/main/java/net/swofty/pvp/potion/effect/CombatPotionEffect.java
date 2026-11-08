package net.swofty.pvp.potion.effect;

import net.swofty.pvp.enchantment.EntityGroup;
import net.swofty.pvp.feature.food.ExhaustionFeature;
import net.swofty.pvp.feature.food.FoodFeature;
import net.swofty.pvp.utils.CombatVersion;
import net.kyori.adventure.key.Key;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CombatPotionEffect {
	private final Map<Attribute, AttributeModifier> attributeModifiers = new HashMap<>();
	private Map<Attribute, AttributeModifier> legacyAttributeModifiers;
	private final PotionEffect potionEffect;
	private final Function<Potion, Particle> particleSupplier;
	
	public CombatPotionEffect(PotionEffect potionEffect) {
		this.potionEffect = potionEffect;
		this.particleSupplier = potion -> {
			int alpha = potion.isAmbient() ? 38 : 255;
			return Particle.ENTITY_EFFECT.withColor(new AlphaColor(alpha, new Color(potion.effect().color())));
		};
	}
	
	public CombatPotionEffect(PotionEffect potionEffect, Function<Potion, Particle> particleSupplier) {
		this.potionEffect = potionEffect;
		this.particleSupplier = particleSupplier;
	}
	
	public PotionEffect getPotionEffect() {
		return potionEffect;
	}
	
	public Particle getParticle(Potion potion) {
		return particleSupplier.apply(potion);
	}
	
	public CombatPotionEffect addAttributeModifier(Attribute attribute, Key id,
	                                               double amount, AttributeOperation operation) {
		attributeModifiers.put(attribute, new AttributeModifier(id, amount, operation));
		return this;
	}
	
	public CombatPotionEffect addLegacyAttributeModifier(Attribute attribute, Key id,
	                                                     double amount, AttributeOperation operation) {
		if (legacyAttributeModifiers == null)
			legacyAttributeModifiers = new HashMap<>();
		legacyAttributeModifiers.put(attribute, new AttributeModifier(id, amount, operation));
		return this;
	}
	
	public void applyUpdateEffect(LivingEntity entity, int amplifier,
	                              ExhaustionFeature exhaustionFeature, FoodFeature foodFeature) {
		if (potionEffect == PotionEffect.REGENERATION) {
			if (entity.getHealth() < entity.getAttributeValue(Attribute.MAX_HEALTH)) {
				entity.setHealth(entity.getHealth() + 1);
			}
			return;
		} else if (potionEffect == PotionEffect.POISON) {
			if (entity.getHealth() > 1.0F) {
				entity.damage(DamageType.MAGIC, 1.0F);
			}
			return;
		} else if (potionEffect == PotionEffect.WITHER) {
			entity.damage(DamageType.WITHER, 1.0F);
			return;
		}
		
		if (entity instanceof Player player) {
			if (potionEffect == PotionEffect.HUNGER) {
				exhaustionFeature.applyHungerEffect(player, amplifier);
				return;
			} else if (potionEffect == PotionEffect.SATURATION) {
				foodFeature.applySaturationEffect(player, amplifier);
				return;
			}
		}
		
		if (potionEffect == PotionEffect.INSTANT_DAMAGE || potionEffect == PotionEffect.INSTANT_HEALTH) {
			EntityGroup entityGroup = EntityGroup.ofEntity(entity);
			
			if (shouldHeal(entityGroup)) {
				entity.setHealth(entity.getHealth() + (float) Math.max(4 << amplifier, 0));
			} else {
				entity.damage(DamageType.MAGIC, (float) (6 << amplifier));
			}
		}
	}
	
	public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target,
	                               int amplifier, double proximity, ExhaustionFeature exhaustionFeature, FoodFeature foodFeature) {
		EntityGroup targetGroup = EntityGroup.ofEntity(target);
		
		if (potionEffect != PotionEffect.INSTANT_DAMAGE && potionEffect != PotionEffect.INSTANT_HEALTH) {
			applyUpdateEffect(target, amplifier, exhaustionFeature, foodFeature);
			return;
		}
		
		if (shouldHeal(targetGroup)) {
			int amount = (int) (proximity * (double) (4 << amplifier) + 0.5D);
			target.setHealth(target.getHealth() + (float) amount);
		} else {
			int amount = (int) (proximity * (double) (6 << amplifier) + 0.5D);
			if (source == null) {
				target.damage(DamageType.MAGIC, (float) amount);
			} else {
				target.damage(new Damage(DamageType.INDIRECT_MAGIC, source, attacker, null, (float) amount));
			}
		}
	}
	
	private boolean shouldHeal(EntityGroup group) {
		return (group.isUndead() && potionEffect == PotionEffect.INSTANT_DAMAGE)
				|| (!group.isUndead() && potionEffect == PotionEffect.INSTANT_HEALTH);
	}
	
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		if (isInstant() || potionEffect == PotionEffect.SATURATION) return duration >= 1;
		
		int applyInterval;
		if (potionEffect == PotionEffect.REGENERATION) {
			applyInterval = 50 >> amplifier;
		} else if (potionEffect == PotionEffect.POISON) {
			applyInterval = 25 >> amplifier;
		} else if (potionEffect == PotionEffect.WITHER) {
			applyInterval = 40 >> amplifier;
		} else {
			return potionEffect == PotionEffect.HUNGER;
		}
		
		if (applyInterval > 0) {
			return duration % applyInterval == 0;
		} else {
			return true;
		}
	}
	
	public boolean isInstant() {
		return potionEffect.instantaneous();
	}
	
	public void onApplied(LivingEntity entity, int amplifier, CombatVersion version) {
		Map<Attribute, AttributeModifier> modifiers;
		if (version.legacy() && legacyAttributeModifiers != null) {
			modifiers = legacyAttributeModifiers;
		} else {
			modifiers = attributeModifiers;
		}
		
		modifiers.forEach((attribute, modifier) -> {
			AttributeInstance instance = entity.getAttribute(attribute);
			instance.removeModifier(modifier);
			instance.addModifier(new AttributeModifier(modifier.id(), adjustModifierAmount(amplifier, modifier), modifier.operation()));
		});
	}
	
	public void onRemoved(LivingEntity entity, int amplifier, CombatVersion version) {
		Map<Attribute, AttributeModifier> modifiers;
		if (version.legacy() && legacyAttributeModifiers != null) {
			modifiers = legacyAttributeModifiers;
		} else {
			modifiers = attributeModifiers;
		}
		
		modifiers.forEach((attribute, modifier) ->
				entity.getAttribute(attribute).removeModifier(modifier));
	}
	
	private double adjustModifierAmount(int amplifier, AttributeModifier modifier) {
		return modifier.amount() * (amplifier + 1);
	}
}
