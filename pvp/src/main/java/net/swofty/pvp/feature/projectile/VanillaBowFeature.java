package net.swofty.pvp.feature.projectile;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.sound.SoundEvent;
import net.swofty.pvp.entity.projectile.AbstractArrow;
import net.swofty.pvp.entity.projectile.Arrow;
import net.swofty.pvp.entity.projectile.SpectralArrow;
import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.feature.RegistrableFeature;
import net.swofty.pvp.feature.config.DefinedFeature;
import net.swofty.pvp.feature.config.FeatureConfiguration;
import net.swofty.pvp.feature.effect.EffectFeature;
import net.swofty.pvp.feature.enchantment.EnchantmentFeature;
import net.swofty.pvp.feature.item.ItemDamageFeature;
import net.swofty.pvp.utils.ViewUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vanilla implementation of {@link BowFeature}
 */
public class VanillaBowFeature implements BowFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaBowFeature> DEFINED = new DefinedFeature<>(
			FeatureType.BOW, VanillaBowFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.EFFECT, FeatureType.ENCHANTMENT, FeatureType.PROJECTILE_ITEM
	);

	private final FeatureConfiguration configuration;

	private ItemDamageFeature itemDamageFeature;
	private EffectFeature effectFeature;
	private EnchantmentFeature enchantmentFeature;
	private ProjectileItemFeature projectileItemFeature;

	public VanillaBowFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void initDependencies() {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.effectFeature = configuration.get(FeatureType.EFFECT);
		this.enchantmentFeature = configuration.get(FeatureType.ENCHANTMENT);
		this.projectileItemFeature = configuration.get(FeatureType.PROJECTILE_ITEM);
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerBeginItemUseEvent.class, event -> {
			if (event.getAnimation() == ItemAnimation.BOW) {
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE
						&& projectileItemFeature.getBowProjectile(event.getPlayer()) == null) {
					event.setCancelled(true);
				}
			}
		});

		node.addListener(PlayerCancelItemUseEvent.class, event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			if (stack.material() != Material.BOW) return;

			EnchantmentList enchantmentList = stack.get(DataComponents.ENCHANTMENTS);
			assert enchantmentList != null;

			boolean infinite = player.getGameMode() == GameMode.CREATIVE
					|| enchantmentList.level(Enchantment.INFINITY) > 0;

			ItemStack projectileItem;
			int projectileSlot;

			ProjectileItemFeature.ProjectileItem projectile = projectileItemFeature.getBowProjectile(player);
			if (!infinite && projectile == null) return;
			if (projectile == null) {
				projectileItem = Arrow.DEFAULT_ARROW;
				projectileSlot = -1;
			} else {
				projectileItem = projectile.stack();
				projectileSlot = projectile.slot();
			}

			long useTicks = player.getCurrentItemUseTime();
			double power = getBowPower(useTicks);
			if (power < 0.1) return;

			// Arrow creation
			AbstractArrow arrow = createArrow(projectileItem.withAmount(1), player);

			if (power >= 1) arrow.setCritical(true);

			int powerEnchantment = enchantmentList.level(Enchantment.POWER);
			if (powerEnchantment > 0)
				arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchantment * 0.5 + 0.5);

			int punchEnchantment = enchantmentList.level(Enchantment.PUNCH);
			if (punchEnchantment > 0) arrow.setKnockback(punchEnchantment);

			if (enchantmentList.level(Enchantment.FLAME) > 0)
				arrow.setFireTicksLeft(100 * ServerProperties.SERVER_TICKS_PER_SECOND.get()); // 100 seconds

			itemDamageFeature.damageEquipment(player, event.getHand() == PlayerHand.MAIN ?
					EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);

			boolean reallyInfinite = infinite && projectileItem.material() == Material.ARROW;
			if (reallyInfinite || player.getGameMode() == GameMode.CREATIVE
					&& (projectileItem.material() == Material.SPECTRAL_ARROW
					|| projectileItem.material() == Material.TIPPED_ARROW)) {
				arrow.setPickupMode(AbstractArrow.PickupMode.CREATIVE_ONLY);
			}

			// Arrow shooting
			Pos position = player.getPosition().add(0D, player.getEyeHeight() - 0.1, 0D);
			arrow.shootFromRotation(position.pitch(), position.yaw(), 0, power * 3, 1.0, 0.0);
			Vec playerVel = player.getVelocity();
			arrow.setVelocity(arrow.getVelocity().add(playerVel.x(),
					player.isOnGround() ? 0.0D : playerVel.y(), playerVel.z()));
			arrow.setInstance(Objects.requireNonNull(player.getInstance()), position.withView(arrow.getPosition()));

			ThreadLocalRandom random = ThreadLocalRandom.current();
			ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
					SoundEvent.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER,
					1.0f, 1.0f / (random.nextFloat() * 0.4f + 1.2f) + (float) power * 0.5f
			), player);

			if (!reallyInfinite && player.getGameMode() != GameMode.CREATIVE && projectileSlot >= 0) {
				player.getInventory().setItemStack(projectileSlot,
						projectileItem.withAmount(projectileItem.amount() - 1));
			}
		});
	}

	protected double getBowPower(long ticks) {
		double seconds = ticks / (double) ServerProperties.SERVER_TICKS_PER_SECOND.get();
		double power = (seconds * seconds + seconds * 2.0) / 3.0;
		if (power > 1) {
			power = 1;
		}

		return power;
	}

	protected AbstractArrow createArrow(ItemStack stack, @Nullable Entity shooter) {
		if (stack.material() == Material.SPECTRAL_ARROW) {
			return new SpectralArrow(shooter, enchantmentFeature);
		} else {
			Arrow arrow = new Arrow(shooter, effectFeature, enchantmentFeature);
			arrow.setItemStack(stack);
			return arrow;
		}
	}
}