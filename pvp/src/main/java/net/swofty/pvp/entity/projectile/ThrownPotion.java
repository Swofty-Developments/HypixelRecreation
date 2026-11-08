package net.swofty.pvp.entity.projectile;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.LingeringPotionMeta;
import net.minestom.server.entity.metadata.item.SplashPotionMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.worldevent.WorldEvent;
import net.swofty.pvp.entity.AreaEffectCloud;
import net.swofty.pvp.feature.effect.EffectFeature;
import net.swofty.pvp.utils.EffectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ThrownPotion extends CustomEntityProjectile implements ItemHoldingProjectile {
	private final EffectFeature effectFeature;
	private final boolean lingering;

	public ThrownPotion(@Nullable Entity shooter, EffectFeature effectFeature, boolean lingering) {
		super(shooter, lingering ? EntityType.LINGERING_POTION : EntityType.SPLASH_POTION);
		this.effectFeature = effectFeature;
		this.lingering = lingering;

		// Why does Minestom have the wrong value 0.03 in its registries?
		setAerodynamics(getAerodynamics().withGravity(0.05));
	}

	@Override
	public boolean onHit(Entity entity) {
		splash(entity);
		return true;
	}

	@Override
	public boolean onStuck() {
		splash(null);
		return true;
	}

	public void splash(@Nullable Entity entity) {
		ItemStack item = getItem();

		PotionContents potionContents = item.get(DataComponents.POTION_CONTENTS);
		List<Potion> potions = effectFeature.getAllPotions(potionContents);

		Pos position = getPosition();
		if (!potions.isEmpty()) {
			if (lingering) {
				AreaEffectCloud areaEffectCloud = new AreaEffectCloud(600, 3.0f, Particle.ENTITY_EFFECT, -0.005f, -0.5f,
					20, 0, 10, potionContents, null, effectFeature);
				areaEffectCloud.setInstance(getInstance(), position);
			} else {
				applySplash(potionContents, entity);
			}
		}

		boolean instantEffect = false;
		for (Potion potion : potions) {
			if (potion.effect().instantaneous()) {
				instantEffect = true;
				break;
			}
		}

		WorldEvent effect = instantEffect ? WorldEvent.PARTICLES_INSTANT_POTION_SPLASH : WorldEvent.PARTICLES_SPELL_POTION_SPLASH;
		EffectUtil.sendNearby(
				Objects.requireNonNull(getInstance()), effect, position.blockX(),
				position.blockY(), position.blockZ(), effectFeature.getPotionColor(potionContents),
				64.0, false
		);
	}

	private void applySplash(PotionContents potionContents, @Nullable Entity hitEntity) {
		BoundingBox boundingBox = getBoundingBox().expand(8.0, 4.0, 8.0);
		List<LivingEntity> entities = Objects.requireNonNull(getInstance()).getEntities().stream()
				.filter(entity -> boundingBox.intersectEntity(getPosition().add(0, -2, 0), entity))
				.filter(entity -> entity instanceof LivingEntity
						&& !(entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR))
				.map(entity -> (LivingEntity) entity).collect(Collectors.toList());

		if (hitEntity instanceof LivingEntity && !entities.contains(hitEntity))
			entities.add((LivingEntity) hitEntity);
		if (entities.isEmpty()) return;

		for (LivingEntity entity : entities) {
			if (entity.getEntityType() == EntityType.ARMOR_STAND) continue;

			double distanceSquared = getDistanceSquared(entity);
			if (distanceSquared >= 16.0) continue;

			double proximity = entity == hitEntity ? 1.0 : (1.0 - Math.sqrt(distanceSquared) / 4.0);
			effectFeature.addSplashPotionEffects(entity, potionContents, proximity, this, getShooter());
		}
	}

	@NotNull
	public ItemStack getItem() {
		if (lingering) {
			return ((LingeringPotionMeta) getEntityMeta()).getItem();
		}
		return ((SplashPotionMeta) getEntityMeta()).getItem();
	}

	@Override
	public void setItem(@NotNull ItemStack item) {
		if (lingering) {
			((LingeringPotionMeta) getEntityMeta()).setItem(item);
		} else {
			((SplashPotionMeta) getEntityMeta()).setItem(item);
		}
	}
}