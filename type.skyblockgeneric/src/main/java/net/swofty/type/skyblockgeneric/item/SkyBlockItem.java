package net.swofty.type.skyblockgeneric.item;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeRarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeStatistics;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.item.components.EnchantedComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SkyBlockItem {
	public List<ItemAttribute> attributes = new ArrayList<>();
	@Getter
	@Setter
	private int amount = 1;
	@Getter
	private ConfigurableSkyBlockItem config = null;

	public SkyBlockItem(UnderstandableSkyBlockItem item) {
		if (item.itemKey() == null) {
			loadAsMaterial(item.material());
		} else {
			loadAsItemType(item.itemKey());
		}
		setAmount(item.amount());

		item.attributes().forEach(attribute -> {
			ItemAttribute attributeOnItem = getAttribute(attribute.getKey());
			attributeOnItem.setValue(attribute.getValue());
		});
	}

	public SkyBlockItem(Material material) {
		String materialName = material.key().value();
		ItemType itemType = ItemType.get(materialName);
		if (itemType != null) {
			loadAsItemType(itemType);
		} else {
			loadAsMaterial(material);
		}
	}

	public SkyBlockItem(ItemType type, int amount) {
		loadAsItemType(type);
		setAmount(amount);
	}

	public SkyBlockItem(ItemType type) {
		loadAsItemType(type);
	}

	public SkyBlockItem(String type) {
		ItemType itemType = ItemType.get(type);
		if (itemType == null) {
			Material material = Material.fromKey(type.toLowerCase());
			loadAsMaterial(material);
			return;
		}
		loadAsItemType(itemType);
	}

	public SkyBlockItem(ItemStack item) {
		loadAsStack(item);
		setAmount(item.amount());
	}

	public static boolean isSkyBlockItem(ItemStack item) {
		return item.hasTag(Tag.String("item_type"));
	}

	private void loadAsItemType(ItemType type) {
        String id = type.name();
        config = ConfigurableSkyBlockItem.getFromID(id);
		for (ItemAttribute attribute : ItemAttribute.getPossibleAttributes()) {
            if (config != null) {
                ItemStatistics statistics = config.getDefaultStatistics();
                attribute.setValue(attribute.getDefaultValue(statistics));
                attributes.add(attribute);
                continue;
            }
			attribute.setValue(attribute.getDefaultValue(null));
			attributes.add(attribute);
		}

		ItemAttributeType typeAttribute = (ItemAttributeType) getAttribute("item_type");
		typeAttribute.setValue(type.name());

		ItemAttributeRarity rarityAttribute = (ItemAttributeRarity) getAttribute("rarity");
		try {
			rarityAttribute.setValue(type.rarity);
		} catch (IllegalArgumentException e) {
			Logger.error("Invalid rarity for type " + type.name() + ": " + e.getMessage());
			rarityAttribute.setValue(Rarity.COMMON);
		}

		if (config == null) {
			config = new ConfigurableSkyBlockItem(id,
					getMaterial(), List.of(), new HashMap<>());
		} else {
			ItemStatistics statistics = config.getDefaultStatistics();
			ItemAttributeStatistics statisticsAttribute = (ItemAttributeStatistics) getAttribute("statistics");
			statisticsAttribute.setValue(statistics.clone());
		}

	}

	private void loadAsMaterial(Material material) {
		for (ItemAttribute attribute : ItemAttribute.getPossibleAttributes()) {
			attribute.setValue(attribute.getDefaultValue(null));
			attributes.add(attribute);
		}

		ItemAttributeType typeAttribute = (ItemAttributeType) getAttribute("item_type");
		typeAttribute.setValue(material.key().asString());

		String materialId = material.key().value();
		config = ConfigurableSkyBlockItem.getFromID(materialId);
		if (config == null) {
			config = new ConfigurableSkyBlockItem(materialId,
					material, List.of(), new HashMap<>());
			config.register();
		} else {
			ItemStatistics statistics = config.getDefaultStatistics();
			ItemAttributeStatistics statisticsAttribute = (ItemAttributeStatistics) getAttribute("statistics");

			statisticsAttribute.setValue(statistics.clone());
		}
	}

	@SneakyThrows
	private void loadAsStack(ItemStack item) {
		if (item.material() == Material.AIR) {
			loadAsMaterial(Material.AIR);
			return;
		}
		String itemType = item.getTag(Tag.String("item_type"));
		ItemStatistics statistics = ItemStatistics.empty();

		ItemType type = ItemType.get(itemType);
		if (type != null) {
			loadAsItemType(type);
		} else {
			Material material = item.material();
			loadAsMaterial(material);
		}

		ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(itemType);

		for (ItemAttribute attribute : ItemAttribute.getPossibleAttributes()) {
			if (item.hasTag(Tag.String(attribute.getKey()))) {
				attribute.setValue(attribute.loadFromString(item.getTag(Tag.String(attribute.getKey()))));
				attributes.removeIf(a -> a.getKey().equals(attribute.getKey()));
				attributes.add(attribute);
			} else {
				attribute.setValue(attribute.getDefaultValue(statistics));
				attributes.removeIf(a -> a.getKey().equals(attribute.getKey()));
				attributes.add(attribute);
			}
		}

        migrateLegacyFishingRodState(item);

		if (config != null) {
			ItemAttributeStatistics statisticsAttribute = (ItemAttributeStatistics) getAttribute("statistics");
			statisticsAttribute.setValue(config.getDefaultStatistics());
		}
	}

    private void migrateLegacyFishingRodState(ItemStack item) {
        String legacy = item.getTag(Tag.String("fishing_rod_state"));
        if (legacy == null || legacy.isBlank()) {
            return;
        }

        String[] parts = legacy.split(";", -1);
        getAttributeHandler().setFishingHook(readLegacyFishingPart(parts, 0));
        getAttributeHandler().setFishingLine(readLegacyFishingPart(parts, 1));
        getAttributeHandler().setFishingSinker(readLegacyFishingPart(parts, 2));
        if (parts.length > 3 && !parts[3].isBlank()) {
            getAttributeHandler().setFishingExpertiseKills(Long.parseLong(parts[3]));
        }
    }

    private @Nullable String readLegacyFishingPart(String[] parts, int index) {
        if (index >= parts.length) {
            return null;
        }
        String value = parts[index];
        return value == null || value.isBlank() || value.equalsIgnoreCase("none") ? null : value;
    }

	public ItemStack.Builder getDisplayItem() {
		ItemStack.Builder builder;

		if (hasComponent(SkullHeadComponent.class)) {
			builder = ItemStacks.head(getComponent(SkullHeadComponent.class).getSkullTexture(this), getAmount(),
					Text.literal(getDisplayName()), List.of());
		} else {
			builder = ItemStacks.item(getMaterial(), getAmount(), Text.literal(getDisplayName()), List.of());
		}

		if (hasComponent(EnchantedComponent.class)) return ItemStacks.enchanted(builder);
		else return builder;
	}

	public ItemAttribute getAttribute(String key) {
		return attributes.stream().filter(attribute -> attribute.getKey().equals(key)).findFirst().orElse(null);
	}

	public @Nullable ConfigurableSkyBlockItem toConfigurableItem() {
		String type = getAttributeHandler().getTypeAsString();
		ConfigurableSkyBlockItem configItem = ConfigurableSkyBlockItem.getFromID(type);
		// Update our own config reference if we found one
		if (configItem != null) {
			this.config = configItem;
		}
		return configItem;
	}

	@Override
	public SkyBlockItem clone() {
		SkyBlockItem item = new SkyBlockItem(getMaterial());
		List<ItemAttribute> attributesForClone = new ArrayList<>();
		item.config = config;  // Add debug here
		item.amount = amount;
		ItemAttribute.getPossibleAttributes().forEach(attribute -> {
			attribute.setValue(getAttribute(attribute.getKey()).getValue());
			attributesForClone.add(attribute);
		});
		item.attributes = attributesForClone;
		return item;
	}

	public List<String> getConfigLore() {
		return config.getLore();
	}

	public Material getMaterial() {
		ItemAttributeType typeAttribute = (ItemAttributeType) getAttribute("item_type");
		try {
			return ItemType.valueOf(typeAttribute.getValue()).material;
		} catch (IllegalArgumentException e) {
			if (typeAttribute.getValue().equalsIgnoreCase("N/A"))
				return Material.BEDROCK;
			Material material = Material.fromKey(typeAttribute.getValue());
			if (material == null)
				return Material.AIR;
			return material;
		}
	}

	public ItemStack getItemStack() {
		return getItemStackBuilder().build();
	}

	public ItemStack.Builder getItemStackBuilder() {
		ItemStack.Builder itemStackBuilder = ItemStack.builder(getMaterial()).amount(amount);

		for (ItemAttribute attribute : attributes) {
			itemStackBuilder.setTag(Tag.String(attribute.getKey()), attribute.saveIntoString());
		}

		return ItemStacks.clearAttributes(itemStackBuilder);
	}

	public <T extends SkyBlockItemComponent> boolean hasComponent(Class<T> componentClass) {
		if (config == null) {
			return false;
		}
		return config.hasComponent(componentClass);
	}

	public <T extends SkyBlockItemComponent> T getComponent(Class<T> componentClass) {
		return config.getComponent(componentClass);
	}

	public boolean isSimilar(SkyBlockItem item) {
		boolean allAttributesMatch = true;

		for (ItemAttribute attribute : attributes) {
			// If both are null, skip
			if (attribute.getValue() == null && item.getAttribute(attribute.getKey()).getValue() == null)
				continue;

			if (attribute.getValue() != null && item.getAttribute(attribute.getKey()).getValue() != null) {
				if (!attribute.saveIntoString().equals(item.getAttribute(attribute.getKey()).saveIntoString())) {
					allAttributesMatch = false;
					break;
				}
			} else {
				allAttributesMatch = false;
				break;
			}
		}

		boolean sameAmount = item.getAmount() == amount;
		boolean sameMaterial = item.getMaterial() == getMaterial();

		return allAttributesMatch && sameAmount && sameMaterial;
	}

	public ItemAttributeHandler getAttributeHandler() {
		return new ItemAttributeHandler(this);
	}

	public boolean isNA() {
		return getMaterial() == Material.BEDROCK;
	}

	public boolean isAir() {
		return getMaterial() == Material.AIR;
	}

	public UnderstandableSkyBlockItem toUnderstandable() {
		return new UnderstandableSkyBlockItem(
				getAttributeHandler().getPotentialType(),
				attributes, amount, getMaterial()
		);
	}

	@Nullable
	public ItemType getItemType() {
		ItemAttributeType typeAttribute = (ItemAttributeType) getAttribute("item_type");
		try {
			return ItemType.valueOf(typeAttribute.getValue());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "SkyBlockItem{" +
				"type=" + getMaterial().name() +
				", itemType=" + getAttributeHandler().getTypeAsString() +
				", config=" + config +
				", amount=" + amount +
				", attributes=" + attributes.stream().map(attribute -> attribute.getKey() + "=" + attribute.getValue()).reduce((s, s2) -> s + ", " + s2).orElse("null") +
				'}';
	}

	public String getDisplayName() {
		return StringUtility.getTextFromComponent(new NonPlayerItemUpdater(this).getUpdatedItem().build().get(DataComponents.CUSTOM_NAME));
	}

	public String getCleanName() {
		return getDisplayName();
	}

	public Text getDisplayNameText() {
		return ItemStacks.nameText(new NonPlayerItemUpdater(this).getUpdatedItem());
	}

	public Text getDisplayNameText(final @NotNull SkyBlockPlayer player) {
		return ItemStacks.nameText(PlayerItemUpdater.playerUpdate(player, getItemStackBuilder().build(), false));
	}

	public List<Text> getLoreText() {
		return ItemStacks.loreText(new NonPlayerItemUpdater(this).getUpdatedItem());
	}

	public List<Text> getLoreText(final @NotNull SkyBlockPlayer player) {
		return ItemStacks.loreText(PlayerItemUpdater.playerUpdate(player, getItemStackBuilder().build(), false));
	}

	/**
	 * Gets the lore of the item, formatted as a list of strings
	 *
	 * @return the lore of the item, formatted as a list of strings
	 * @deprecated use {@link #getLoreText()}
	 */
	@Deprecated
	public List<String> getLore() {
		return new NonPlayerItemUpdater(this).getUpdatedItem().build().get(DataComponents.LORE).stream().map(
				StringUtility::getTextFromComponent
		).toList();
	}

	/**
	 * Gets the lore of the item, formatted as a list of strings
	 *
	 * @deprecated use {@link #getLoreText(SkyBlockPlayer)}
	 * @param player the player to get the lore for
	 * @return the lore of the item, formatted as a list of strings
	 */
	@Deprecated
	public List<String> getLore(@NotNull SkyBlockPlayer player) {
		return PlayerItemUpdater.playerUpdate(player, getItemStackBuilder().build(), false).build()
				.get(DataComponents.LORE).stream().map(
						StringUtility::getTextFromComponent).toList();
	}
}
