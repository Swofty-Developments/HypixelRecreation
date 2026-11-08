package net.swofty.pvp.feature.food;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.item.component.ConsumeEffect;
import net.minestom.server.item.component.ConsumeEffect.ApplyEffects;
import net.minestom.server.item.component.ConsumeEffect.ClearAllEffects;
import net.minestom.server.item.component.ConsumeEffect.PlaySound;
import net.minestom.server.item.component.ConsumeEffect.RemoveEffects;
import net.minestom.server.item.component.ConsumeEffect.TeleportRandomly;
import net.minestom.server.item.component.Food;
import net.minestom.server.item.component.SuspiciousStewEffects;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.sound.SoundEvent;
import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.feature.RegistrableFeature;
import net.swofty.pvp.feature.config.DefinedFeature;
import net.swofty.pvp.feature.config.FeatureConfiguration;
import net.swofty.pvp.feature.cooldown.ItemCooldownFeature;
import net.swofty.pvp.feature.provider.SoundProvider;
import net.swofty.pvp.utils.PotionFlags;
import net.swofty.pvp.utils.ViewUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vanilla implementation of {@link FoodFeature}
 * <p>
 * This also includes eating of food items.
 */
public class VanillaFoodFeature implements FoodFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaFoodFeature> DEFINED = new DefinedFeature<>(
			FeatureType.FOOD, VanillaFoodFeature::new,
			FeatureType.ITEM_COOLDOWN, FeatureType.SOUND
	);

	private final FeatureConfiguration configuration;

	private ItemCooldownFeature itemCooldownFeature;
	private SoundProvider soundProvider;

	public VanillaFoodFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}

	public static void applyConsumeEffect(Entity entity, ConsumeEffect effect, Random random, SoundProvider soundProvider) {
		switch (effect) {
			case ApplyEffects(List<CustomPotionEffect> effects, float probability) -> {
				if (random.nextFloat() >= probability) return;
				for (CustomPotionEffect potionEffect : effects) {
					entity.addEffect(new Potion(
							potionEffect.id(), (byte) potionEffect.amplifier(),
							potionEffect.duration(),
							PotionFlags.create(
									potionEffect.isAmbient(),
									potionEffect.showParticles(),
									potionEffect.showIcon()
							)
					));
				}
			}
			case RemoveEffects(RegistryTag<PotionEffect> potionEffects) -> entity.getActiveEffects().stream()
					.map(TimedPotion::potion)
					.map(Potion::effect)
					.filter(potionEffects::contains)
					.forEach(entity::removeEffect);
			case ClearAllEffects ignored -> entity.clearEffects();
			case TeleportRandomly(float diameter, boolean directionalParticles) ->
                    ChorusFruitUtil.tryChorusTeleport(entity, diameter, directionalParticles);
			case PlaySound(SoundEvent sound) -> soundProvider.playSound(
                ViewUtil.viewersAndSelf(entity),
                Sound.sound(
                    sound, Sound.Source.PLAYER,
                    1.0f, 1.0f
                ),
                entity.getPosition().x(), entity.getPosition().y(), entity.getPosition().z() // TODO: emitter
            );
			default -> throw new IllegalArgumentException("Unexpected value: " + effect);
		}
	}

	@Override
	public void initDependencies() {
		this.itemCooldownFeature = configuration.get(FeatureType.ITEM_COOLDOWN);
		this.soundProvider = configuration.get(FeatureType.SOUND);
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerBeginItemUseEvent.class, event -> {
			if (!event.getItemStack().has(DataComponents.CONSUMABLE))
				return;
			@Nullable Food foodComponent = event.getItemStack().get(DataComponents.FOOD);
			@Nullable Consumable consumableComponent = event.getItemStack().get(DataComponents.CONSUMABLE);

			// If the players hunger is full and the food is not always edible, cancel
			// For some reason vanilla doesn't say honey is always edible but just overrides the method to always consume it
			boolean alwaysEat = foodComponent == null || foodComponent.canAlwaysEat() || event.getItemStack().material() == Material.HONEY_BOTTLE;
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE
					&& !alwaysEat && event.getPlayer().getFood() == 20) {
				event.setCancelled(true);
				return;
			}

			if (consumableComponent != null) event.setItemUseDuration(consumableComponent.consumeTicks());
		});

		node.addListener(PlayerFinishItemUseEvent.class, event -> {
			if (event.getItemStack().material() != Material.MILK_BUCKET
					&& !(event.getItemStack().has(DataComponents.FOOD) || event.getItemStack().has(DataComponents.CONSUMABLE)))
				return;

			onFinishEating(event.getPlayer(), event.getItemStack(), event.getHand());
		});

		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			if (player.isSilent() || !player.isEating()) return;

			tickEatingSounds(player);
		});
	}

	protected void onFinishEating(Player player, ItemStack stack, PlayerHand hand) {
		this.eat(player, stack);

		Food food = stack.get(DataComponents.FOOD);
		Consumable consumable = stack.get(DataComponents.CONSUMABLE);
		if (consumable == null) return;
		ThreadLocalRandom random = ThreadLocalRandom.current();

		triggerEatingSound(player, consumable);

		if (food != null) {
			soundProvider.playSound(
				ViewUtil.viewersAndSelf(player),
				Sound.sound(
					SoundEvent.ENTITY_PLAYER_BURP, Sound.Source.PLAYER,
					0.5f, random.nextFloat() * 0.1f + 0.9f
				),
				player.getPosition().x(), player.getPosition().y(), player.getPosition().z() // TODO: emitter
			);
		}

		List<ConsumeEffect> effectList = consumable.effects();

		for (ConsumeEffect effect : effectList) {
			applyConsumeEffect(player, effect, random, soundProvider);
		}

		if (stack.has(DataComponents.SUSPICIOUS_STEW_EFFECTS)) {
			SuspiciousStewEffects effects = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
			assert effects != null;
			for (SuspiciousStewEffects.Effect effect : effects.effects()) {
				player.addEffect(new Potion(effect.id(), (byte) 0, effect.durationTicks(), PotionFlags.defaultFlags()));
			}
		}

		if (player.getGameMode() != GameMode.CREATIVE) {
			ItemStack remainder = stack.get(DataComponents.USE_REMAINDER);

			if (remainder != null && !remainder.isAir()) {
				if (stack.amount() == 1) {
					player.setItemInHand(hand, remainder);
				} else {
					player.setItemInHand(hand, stack.withAmount(stack.amount() - 1));
					player.getInventory().addItemStack(remainder);
				}
			} else {
				player.setItemInHand(hand, stack.withAmount(stack.amount() - 1));
			}
		}
	}

	@Override
	public void addFood(Player player, int food, float saturation) {
		player.setFood(Math.min(food + player.getFood(), 20));
		player.setFoodSaturation(Math.min(player.getFoodSaturation() + saturation, player.getFood()));
	}

	@Override
	public void eat(Player player, int food, float saturationModifier) {
		addFood(player, food, (float) food * saturationModifier * 2.0f);
	}

	@Override
	public void eat(Player player, ItemStack stack) {
		Food foodComponent = stack.get(DataComponents.FOOD);
		if (foodComponent == null) return;
		addFood(player, foodComponent.nutrition(), foodComponent.saturationModifier());
	}

	@Override
	public void applySaturationEffect(Player player, int amplifier) {
		eat(player, amplifier + 1, 1.0f);
	}

	protected void tickEatingSounds(Player player) {
		ItemStack stack = player.getItemInHand(Objects.requireNonNull(player.getItemUseHand()));

		Consumable component = stack.get(DataComponents.CONSUMABLE);
		if (component == null) return;

		long useTime = component.consumeTicks();
		long usedTicks = player.getCurrentItemUseTime();
		long remainingUseTicks = useTime - usedTicks;

		boolean canTrigger = component.consumeTicks() < 32 || remainingUseTicks <= useTime - 7;
		boolean shouldTrigger = canTrigger && remainingUseTicks % 4 == 0;
		if (!shouldTrigger) return;

		triggerEatingSound(player, component);
	}

	protected void triggerEatingSound(Player player, Consumable consumable) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		soundProvider.playSound(
			player.getViewersAsAudience(),
			Sound.sound(
				consumable.sound(), Sound.Source.PLAYER,
				0.5f + 0.5f * random.nextInt(2),
				(random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f
			),
			player.getPosition().x(), player.getPosition().y(), player.getPosition().z()
		);
	}
}
