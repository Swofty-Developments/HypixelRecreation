package net.swofty.type.skyblockgeneric.item;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.*;
import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingRodLoreBuilder;
import net.swofty.type.skyblockgeneric.gems.GemRarity;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.item.components.*;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.potion.PotionEffectType;
import net.swofty.type.skyblockgeneric.text.LoreText;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemLore {
	private static final List<ItemStatistic> RENDERED_STATISTICS = List.of(
			ItemStatistic.DAMAGE, ItemStatistic.STRENGTH, ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE,
			ItemStatistic.SEA_CREATURE_CHANCE, ItemStatistic.BONUS_ATTACK_SPEED, ItemStatistic.ABILITY_DAMAGE, ItemStatistic.HEALTH, ItemStatistic.DEFENSE,
			ItemStatistic.SPEED, ItemStatistic.INTELLIGENCE, ItemStatistic.MAGIC_FIND, ItemStatistic.PET_LUCK, ItemStatistic.TRUE_DEFENSE, ItemStatistic.HEALTH_REGENERATION,
			ItemStatistic.MENDING, ItemStatistic.VITALITY, ItemStatistic.FEROCITY, ItemStatistic.MINING_SPEED, ItemStatistic.MINING_FORTUNE,
			ItemStatistic.FARMING_FORTUNE, ItemStatistic.FORAGING_FORTUNE, ItemStatistic.BONUS_PEST_CHANCE, ItemStatistic.COLD_RESISTANCE, ItemStatistic.PRISTINE,
			ItemStatistic.SWING_RANGE, ItemStatistic.RIFT_DAMAGE);

	@Getter
	private ItemStack stack;

	public ItemLore(ItemStack stack) {
		this.stack = stack;
	}

	@SneakyThrows
	public void updateLore(@Nullable SkyBlockPlayer player) {
		SkyBlockItem item = new SkyBlockItem(stack);
		ItemAttributeHandler handler = item.getAttributeHandler();
		Rarity rarity = handler.isRecombobulated() ? handler.getRarity().upgrade() : handler.getRarity();

		Text displayName = displayName(item, stack.material());

		if (item.hasComponent(FishingRodMetadataComponent.class)) {
			FishingRodLoreBuilder.FishingRodLore rodLore = FishingRodLoreBuilder.build(item, player);
			if (rodLore != null) {
				applyFishingRodLore(item, player, rodLore);
				return;
			}
		}

		if (item.hasComponent(LoreUpdateComponent.class)) {
			LoreUpdateComponent loreUpdateComponent = item.getComponent(LoreUpdateComponent.class);
			if (loreUpdateComponent.isAbsolute()) {
				applyAbsoluteLore(item, player, loreUpdateComponent.getHandler());
				return;
			}
		}

		LoreText lore = new LoreText(item, player);
		fillUnderName(lore);
		fillBreakingPower(lore);
		fillStatistics(lore, rarity);
		fillGemstones(lore);
		fillPotion(lore);
		fillEnchantments(lore);
		fillRune(lore);
		fillCustomLore(lore);
		fillConfigLore(lore);
		fillAbilities(lore);
		fillFullSetBonus(lore);
		fillRecipes(lore);
		fillReforgeable(lore);
		fillSoulbound(lore);
		fillStatsWhenShot(lore);
		fillUnfinished(lore);
		fillDyeColor(lore);
		lore.section(LoreText.Sections.RARITY_FOOTER).line("{}", displayRarity(item, rarity));

		this.stack = ItemStacks.lines(stack.builder(), lore.render())
				.amount(item.getAmount())
				.set(DataComponents.CUSTOM_NAME, reforgedName(item, displayName).asComponent()
						.color(rarity.getColor()).decoration(TextDecoration.ITALIC, false))
				.build();
	}

	private void applyFishingRodLore(SkyBlockItem item, @Nullable SkyBlockPlayer player,
									 FishingRodLoreBuilder.FishingRodLore rodLore) {
		LoreText lore = new LoreText(item, player);
		TextBody.Section section = lore.section(LoreText.Sections.ABSOLUTE);
		rodLore.lore().forEach(line -> section.line(Text.parseLenient(line)));

		this.stack = ItemStacks.name(ItemStacks.lines(stack.builder(), lore.render()),
				Text.parseLenient(rodLore.displayName())).build();
	}

	private void applyAbsoluteLore(SkyBlockItem item, @Nullable SkyBlockPlayer player, LoreConfig loreConfig) {
		Text forcedDisplayName;
		if (item.hasComponent(CustomDisplayNameComponent.class)) {
			forcedDisplayName = item.getComponent(CustomDisplayNameComponent.class).getDisplayName(item);
		} else {
			forcedDisplayName = Text.literal(StringUtility.toNormalCase(item.getAttributeHandler().getTypeAsString()));
		}

		if (loreConfig.displayNameGenerator() != null) {
			forcedDisplayName = Text.parseLenient(loreConfig.displayNameGenerator().apply(item, player));
		}

		LoreText lore = new LoreText(item, player);
		if (loreConfig.loreGenerator() != null) {
			TextBody.Section section = lore.section(LoreText.Sections.ABSOLUTE);
			loreConfig.loreGenerator().apply(item, player).forEach(line -> section.line(LoreText.gray(line)));
		}

		this.stack = ItemStacks.name(ItemStacks.lines(stack.builder(), lore.render()),
				forcedDisplayName).build();
	}

	private static void fillUnderName(LoreText lore) {
		SkyBlockItem item = lore.item();
		TextBody.Section section = lore.section(LoreText.Sections.UNDER_NAME);

		if (item.hasComponent(ExtraUnderNameComponent.class)) {
			item.getComponent(ExtraUnderNameComponent.class).getDisplays()
					.forEach(display -> section.line(LoreText.darkGray(display)));
		}

		ItemType type = item.getAttributeHandler().getPotentialType();
		if (type != null && CollectionCategories.getCategory(type) != null) {
			section.line(Text.key("items.lore.collection_item"));
		}
	}

	private static void fillBreakingPower(LoreText lore) {
		ItemAttributeHandler handler = lore.item().getAttributeHandler();
		if (!handler.isMiningTool()) return;

		lore.section(LoreText.Sections.BREAKING_POWER)
				.line(Text.key("items.lore.breaking_power", handler.getBreakingPower()));
	}

	private static void fillStatistics(LoreText lore, Rarity rarity) {
		SkyBlockItem item = lore.item();
		ItemAttributeHandler handler = item.getAttributeHandler();
		ItemStatistics statistics = handler.getStatistics();
		TextBody.Section section = lore.section(LoreText.Sections.STATS);

		for (ItemStatistic statistic : RENDERED_STATISTICS) {
			Text line = statisticLine(item, statistic, statistics.getOverall(statistic), handler.getReforge(), rarity);
			if (line != null) section.line(line);
		}

		if (item.hasComponent(ShortBowComponent.class)) {
			section.line(Text.key("items.lore.shot_cooldown",
					item.getComponent(ShortBowComponent.class).getCooldown()));
		}
	}

	private static @Nullable Text statisticLine(SkyBlockItem item, ItemStatistic statistic, double overallValue,
												Reforge reforge, Rarity rarity) {
		if (!statistic.isRendered()) return null;

		double reforgeValue = 0;
		double gemstoneValue = Gemstone.getExtraStatisticFromGemstone(statistic, item);
		if (reforge != null) {
			reforgeValue = reforge.getAfterCalculation(ItemStatistics.empty(), rarity.ordinal() + 1).getOverall(statistic);
			overallValue += reforgeValue;
		}
		overallValue += gemstoneValue;

		double hpbValue = 0;
		ItemAttributeHotPotatoBookData.HotPotatoBookData hotPotatoBookData = item.getAttributeHandler().getHotPotatoBookData();
		if (hotPotatoBookData.hasAppliedItem()) {
			for (Map.Entry<ItemStatistic, Double> entry : hotPotatoBookData.getPotatoType().stats.entrySet()) {
				if (entry.getKey() == statistic) hpbValue += entry.getValue() * hotPotatoBookData.getTotalAmount();
			}
		}
		overallValue += hpbValue;

		if (overallValue == 0) return null;

		String name = StringUtility.toNormalCase(statistic.getDisplayName());
		TextColor colour = statistic.getLoreColor();
		Text line = statistic.getIsPercentage()
				? Text.of("<7>{}: <color:{}>{}%", name, colour, Math.round(overallValue))
				: Text.of("<7>{}: <color:{}>+{}", name, colour, Math.round(overallValue));

		long hotPotato = Math.round(hpbValue);
		long reforged = Math.round(reforgeValue);
		long gems = Math.round(gemstoneValue);
		line = line.appendIf(hpbValue != 0, "<e> ({}{})", hotPotato >= 1 ? "+" : "", hotPotato);
		line = line.appendIf(reforgeValue != 0, "<9> ({}{})", reforged > 0 ? "+" : "", reforged);
		line = line.appendIf(gemstoneValue != 0, "<d> ({}{})", gems >= 1 ? "+" : "", gems);

		return line;
	}

	private static void fillGemstones(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(GemstoneComponent.class)) return;

		GemstoneComponent gemstoneComponent = item.getComponent(GemstoneComponent.class);
		ItemAttributeGemData.GemData gemData = item.getAttributeHandler().getGemData();
		Text gemstoneLore = Text.of(" ");

		int index = -1;
		for (GemstoneComponent.GemstoneSlot entry : gemstoneComponent.getSlots()) {
			index++;
			Gemstone.Slots gemstone = entry.slot();

			if (gemData.getGem(index) == null || !gemData.getGem(index).isUnlocked()) {
				gemstoneLore = gemstoneLore.append("<8>[{}] ", gemstone.getSymbol());
				continue;
			}

			ItemType filledWith = gemData.getGem(index).filledWith;
			if (filledWith == null) {
				gemstoneLore = gemstoneLore.append("<8>[<7>{}<8>]", gemstone.getSymbol());
				continue;
			}

			GemstoneImplComponent gemstoneImplComponent = new SkyBlockItem(filledWith).getComponent(GemstoneImplComponent.class);
			GemRarity gemRarity = gemstoneImplComponent.getGemRarity();
			Gemstone gemstoneEnum = gemstoneImplComponent.getGemstone();
			Gemstone.Slots gemstoneSlot = Gemstone.Slots.getFromGemstone(gemstoneEnum);

			gemstoneLore = gemstoneLore.append("<color:{0}>[<color:{1}>{2}<color:{0}>] ",
					gemRarity.getBracketColor(), gemstoneEnum.getColor(), gemstoneSlot.getSymbol());
		}

		if (gemstoneLore.plain().isBlank()) return;
		lore.section(LoreText.Sections.GEMSTONES).line(gemstoneLore);
	}

	private static void fillPotion(LoreText lore) {
		ItemAttributePotionData.PotionData potionData = lore.item().getAttributeHandler().getPotionData();
		if (potionData == null) return;

		PotionEffectType effectType = PotionEffectType.fromName(potionData.getEffectType());
		if (effectType == null || effectType.getCategory() == null) return;

		String durationStr = "";
		if (potionData.getBaseDurationSeconds() > 0) {
			int totalSeconds = potionData.getBaseDurationSeconds();
			durationStr = " (" + totalSeconds / 60 + ":" + String.format("%02d", totalSeconds % 60) + ")";
		}

		TextBody.Section section = lore.section(LoreText.Sections.POTION);
		section.line(Text.parseLenient(effectType.getColor()
				+ effectType.getLevelDisplay(potionData.getLevel()) + durationStr));

		String description = effectType.getDescription(potionData.getLevel());
		if (!description.isEmpty()) section.line(LoreText.gray(description));
	}

	private static void fillEnchantments(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(EnchantableComponent.class)) return;
		if (!item.getComponent(EnchantableComponent.class).showEnchantLores()) return;

		List<SkyBlockEnchantment> enchantments = item.getAttributeHandler().getEnchantments().toList();
		if (enchantments.isEmpty()) return;

		TextBody.Section section = lore.section(LoreText.Sections.ENCHANTS);
		if (enchantments.size() >= 4) {
			fillCompactEnchantments(section, enchantments);
			return;
		}

		SkyBlockPlayer viewer = lore.viewer();
		for (SkyBlockEnchantment enchantment : enchantments) {
			section.line(enchantmentTitle(enchantment));
			if (viewer == null) continue;

			Text.of("<7><wrap:34>{}</wrap>",
							Text.parseLenient(enchantment.type().getDescription(enchantment.level(), viewer)))
					.lines().forEach(section::line);
		}
	}

	private static void fillCompactEnchantments(TextBody.Section section, List<SkyBlockEnchantment> enchantments) {
		Text separator = Text.of("<7>, ");
		List<Text> current = new ArrayList<>();
		int length = 0;

		for (SkyBlockEnchantment enchantment : enchantments) {
			Text name = enchantmentTitle(enchantment);
			int nameLength = name.plain().length();

			if (!current.isEmpty() && length + 2 + nameLength > 34) {
				section.line(Text.join(separator, current));
				current = new ArrayList<>();
				length = 0;
			}

			length += current.isEmpty() ? nameLength : nameLength + 2;
			current.add(name);
		}

		if (!current.isEmpty()) section.line(Text.join(separator, current));
	}

	private static Text enchantmentTitle(SkyBlockEnchantment enchantment) {
		return Text.of("<9>{} {:roman}", Text.parseLenient(enchantment.type().getName()), enchantment.level());
	}

	private static void fillRune(LoreText lore) {
		ItemAttributeRuneInfusedWith.RuneData runeData = lore.item().getAttributeHandler().getRuneData();
		if (runeData == null || !runeData.hasRune()) return;

		RuneComponent runeComponent = new SkyBlockItem(runeData.getRuneType()).getComponent(RuneComponent.class);
		lore.section(LoreText.Sections.RUNE)
				.line(Text.parseLenient(runeComponent.getDisplayName(runeData.getRuneType(), runeData.getLevel())));
	}

	private static void fillCustomLore(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(LoreUpdateComponent.class)) return;

		LoreConfig loreConfig = item.getComponent(LoreUpdateComponent.class).getHandler();
		if (loreConfig == null)
			throw new RuntimeException("Lore update handler is null for " + item.getAttributeHandler().getTypeAsString());
		if (loreConfig.loreGenerator() == null) return;

		TextBody.Section section = lore.section(loreConfig.location() == LoreConfig.LoreConfigLocation.AFTER_ABILITY
				? LoreText.Sections.CUSTOM_AFTER_ABILITY
				: LoreText.Sections.CUSTOM_BEFORE_ABILITY);
		loreConfig.loreGenerator().apply(item, lore.viewer()).forEach(line -> section.line(LoreText.gray(line)));
	}

	private static void fillConfigLore(LoreText lore) {
		List<String> configLore = lore.item().getConfigLore();
		if (configLore == null) return;

		TextBody.Section section = lore.section(LoreText.Sections.CONFIG_LORE);
		configLore.forEach(line -> section.line(LoreText.gray(line)));
	}

	private static void fillAbilities(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(AbilityComponent.class)) return;

		TextBody.Section abilities = lore.section(LoreText.Sections.ABILITIES);
		int index = 0;
		for (RegisteredAbility ability : item.getComponent(AbilityComponent.class).getAbilities()) {
			TextBody.Section section = abilities.child("ability_" + index++).separated();

			section.line(Text.key("items.lore.ability_label",
					Text.parseLenient(ability.getName()),
					ability.getActivation().getDisplay()));
			Text.of("<7><wrap:40>{}</wrap>",
							Text.parseLenient(ability.getDescription().apply(lore.viewer(), item)))
					.lines().forEach(section::line);

			String costDisplay = ability.getCost().getLoreDisplay();
			if (costDisplay != null) section.line(Text.parseLenient(costDisplay));

			if (ability.getCooldownTicks() > 20) {
				section.line("<key:'items.lore.ability_cooldown':'{:.1}'>",
						(double) ability.getCooldownTicks() / 20);
			}
		}
	}

	@SneakyThrows
	private static void fillFullSetBonus(LoreText lore) {
		SkyBlockItem item = lore.item();
		ArmorSetRegistry registry = ArmorSetRegistry.getArmorSet(item.getAttributeHandler().getPotentialType());
		if (registry == null || registry.getClazz() == null) return;

		ArmorSet armorSet = registry.getClazz().getDeclaredConstructor().newInstance();
		SkyBlockPlayer viewer = lore.viewer();

		int wearingAmount = 0;
		if (viewer != null && viewer.isWearingItem(item)) {
			for (SkyBlockItem armorItem : viewer.getArmor()) {
				if (armorItem == null) continue;
				ArmorSetRegistry armorSetRegistry = ArmorSetRegistry.getArmorSet(armorItem.getAttributeHandler().getPotentialType());
				if (armorSetRegistry == null) continue;
				if (armorSetRegistry.getClazz() == armorSet.getClass()) {
					wearingAmount++;
				}
			}
		}

		ArmorSetRegistry setRegistry = ArmorSetRegistry.getArmorSet(armorSet.getClass());
		int totalPieces = setRegistry == null ? 4 : ArmorSetRegistry.getPieceCount(setRegistry);

		TextBody.Section section = lore.section(LoreText.Sections.FULL_SET_BONUS);
		section.line(Text.key("items.lore.full_set_bonus", Text.parseLenient(armorSet.getName()), wearingAmount, totalPieces));
		armorSet.getDescription().forEach(line -> section.line(LoreText.gray(line)));
	}

	private static void fillRecipes(LoreText lore) {
		if (!lore.item().hasComponent(RightClickRecipeComponent.class)) return;

		lore.section(LoreText.Sections.RECIPES).line(Text.key("items.lore.right_click_recipes"));
	}

	private static void fillReforgeable(LoreText lore) {
		if (!lore.item().hasComponent(ReforgableComponent.class)) return;

		lore.section(LoreText.Sections.REFORGEABLE).line(Text.key("items.lore.reforgeable"));
	}

	private static void fillSoulbound(LoreText lore) {
		ItemAttributeSoulbound.SoulBoundData bound = lore.item().getAttributeHandler().getSoulBoundData();
		if (bound == null) return;

		lore.section(LoreText.Sections.SOULBOUND).line(Text.key("items.lore.soulbound", bound.isCoopAllowed()
				? Text.key("items.lore.soulbound_coop_prefix")
				: Text.empty()));
	}

	private static void fillStatsWhenShot(LoreText lore) {
		if (!lore.item().hasComponent(ArrowComponent.class)) return;

		lore.section(LoreText.Sections.STATS_WHEN_SHOT).line(Text.key("items.lore.stats_when_shot"));
	}

	private static void fillUnfinished(LoreText lore) {
		if (!lore.item().hasComponent(NotFinishedYetComponent.class)) return;

		lore.section(LoreText.Sections.UNFINISHED)
				.line(Text.key("items.lore.not_finished"))
				.line(Text.empty());
	}

	private static void fillDyeColor(LoreText lore) {
		String dyeColor = lore.item().getAttributeHandler().getDyeColor();
		if (dyeColor == null) return;

		TextBody.Section section = lore.section(LoreText.Sections.DYE_COLOR);
		if (dyeColor.startsWith("animated:")) {
			String[] colors = dyeColor.split(":");
			section.line(Text.of("<7>Color: <d>Animated <8>({} <7>➜ <8>{}<8>)", colors[1], colors[2]));
		} else {
			section.line(Text.of("<7>Color: <8>{}", dyeColor));
		}
	}

	private static Text displayName(SkyBlockItem item, Material material) {
		if (item.hasComponent(CustomDisplayNameComponent.class)) {
			return item.getComponent(CustomDisplayNameComponent.class).getDisplayName(item);
		}

		ItemAttributePotionData.PotionData potionData = item.getAttributeHandler().getPotionData();
		PotionEffectType effect = potionData != null && potionData.getEffectType() != null
				&& !potionData.getEffectType().equals("WATER")
				? PotionEffectType.fromName(potionData.getEffectType())
				: null;
		if (effect == null) {
			return getConfiguredOrMaterialName(item, material);
		}

		Text effectDisplay = Text.parseLenient(effect.getLevelDisplay(potionData.getLevel()));
		return potionData.isSplash()
				? Text.of("{} <key:items.lore.splash_potion_suffix>", effectDisplay)
				: Text.of("{} <key:items.lore.potion_suffix>", effectDisplay);
	}

	private static Text getConfiguredOrMaterialName(SkyBlockItem item, Material material) {
		String name = item.getConfig() == null ? null : item.getConfig().getName();
		return name == null ? Text.of("{}", Component.translatable(material)) : Text.parseLenient(name);
	}

	private static Text reforgedName(SkyBlockItem item, Text displayName) {
		Reforge reforge = item.getAttributeHandler().getReforge();
		if (!item.hasComponent(ReforgableComponent.class) || reforge == null) return displayName;

		return Text.of("{} ", Text.parseLenient(reforge.getPrefix())).append(displayName);
	}

	private static Text displayRarity(SkyBlockItem item, Rarity rarity) {
		Component display = rarity.getDisplay();

		if (item.hasComponent(ExtraRarityComponent.class)) {
			display = display.appendSpace().append(Text.parseLenient(
					item.getComponent(ExtraRarityComponent.class).getExtraRarityDisplay(item)).asComponent());
		}

		Text displayRarity = Text.of("{}", display);

		if (item.getAttributeHandler().isRecombobulated()) {
			displayRarity = Text.of("<color:{0}><k>L</k> {1} <k>L</k>", rarity.getColor(), displayRarity);
		}

		return displayRarity;
	}
}
