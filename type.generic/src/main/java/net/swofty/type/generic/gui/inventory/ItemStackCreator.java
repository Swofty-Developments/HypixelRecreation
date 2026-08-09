package net.swofty.type.generic.gui.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.item.component.TooltipDisplay;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.tag.Tag;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for creating {@link ItemStack} builders with various customizations,
 * including item materials, names, lore, and player skins.
 */
public class ItemStackCreator {
	private static final TooltipDisplay DEFAULT_TOOLTIP_DISPLAY = new TooltipDisplay(false, Set.of(
			DataComponents.UNBREAKABLE,
			DataComponents.POTION_CONTENTS,
			DataComponents.POTION_DURATION_SCALE
	));
	private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

	/**
	 * Creates an {@link ItemStack.Builder} with a specified material and custom name.
	 *
	 * @param material the material of the item stack
	 * @param name     the custom name of the item stack
	 * @return an {@link ItemStack.Builder} with the specified properties
	 */
	public static ItemStack.Builder createNamedItemStack(Material material, String name) {
		return clearAttributes(ItemStack.builder(material)
				.set(DataComponents.CUSTOM_NAME, LEGACY_SERIALIZER.deserialize(replaceColorCodes(name)).decoration(TextDecoration.ITALIC, false))
				.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
	}

	/**
	 * Creates an {@link ItemStack.Builder} with a specified material and custom name.
	 *
	 * @param material the material of the item stack
	 * @param name     the custom name of the item stack
	 * @return an {@link ItemStack.Builder} with the specified properties
	 */
	public static ItemStack.Builder createNamedItemStack(Material material, Component name) {
		return clearAttributes(ItemStack.builder(material)
			.set(DataComponents.CUSTOM_NAME, name.decoration(TextDecoration.ITALIC, false))
			.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
	}

	/**
	 * Clears attribute modifiers from the given {@link ItemStack.Builder}.
	 *
	 * @param builder the {@link ItemStack.Builder} to clear attributes from
	 * @return the modified {@link ItemStack.Builder}
	 */
	public static ItemStack.Builder clearAttributes(ItemStack.Builder builder) {
		builder.set(DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(List.of()));
		builder.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY);
		return builder;
	}

	/**
	 * Creates an {@link ItemStack.Builder} with a specified material and an empty custom name.
	 *
	 * @param material the material of the item stack
	 * @return an {@link ItemStack.Builder} with the specified material and an empty name
	 */
	public static ItemStack.Builder createNamedItemStack(Material material) {
		return createNamedItemStack(material, "");
	}

	/**
	 * Creates an {@link ItemStack.Builder} with a single lore line.
	 *
	 * @param name     the name of the item stack
	 * @param color    the color to apply to the lore
	 * @param material the material of the item stack
	 * @param amount   the amount of items in the stack
	 * @param lore     the lore to display
	 * @return an {@link ItemStack.Builder} with the specified properties
	 */
	public static ItemStack.Builder getSingleLoreStack(String name, String color, Material material, int amount, String lore) {
		List<String> l = new ArrayList<>();
		for (String line : StringUtility.splitByWordAndLength(lore, 30)) {
			l.add(color + line);
		}
		return getStack(name, material, amount, l.toArray(new String[]{}));
	}

    /**
     * Creates an {@link ItemStack.Builder} with a single lore line. Will split the lore into multiple lines on every \n.
     *
     * @param name     the name of the item stack
     * @param color    the color to apply to the lore
     * @param material the material of the item stack
     * @param amount   the number of items in the stack
     * @param lore     the lore to display
     * @return an {@link ItemStack.Builder} with the specified properties
     */
    public static ItemStack.Builder getSingleLoreStackLineSplit(String name, String color, Material material, int amount, String lore) {
        List<String> l = new ArrayList<>();
        for (String line : lore.split("\n")) {
            l.add(color + line);
        }
        return getStack(name, material, amount, l.toArray(new String[]{}));
    }

	/**
	 * Creates an {@link ItemStack.Builder} with specified name, material, amount, and lore.
	 *
	 * @param name     the name of the item stack
	 * @param material the material of the item stack
	 * @param amount   the amount of items in the stack
	 * @param lore     the lore of the item stack
	 * @return an {@link ItemStack.Builder} with the specified properties
	 */
	public static ItemStack.Builder getStack(String name, Material material, int amount, String... lore) {
		return getStack(name, material, amount, Arrays.asList(lore));
	}

	public static ItemStack.Builder getStack(String name, Material material, int amount) {
		return getStack(name, material, amount, new String[0]);
	}

	public static ItemStack.Builder getStack(String name, Material material, int amount, Component... lore) {
		return getStack(Component.text(name), material, amount, Arrays.asList(lore));
	}

	/**
	 * Creates an {@link ItemStack.Builder} with specified name, material, amount, and lore.
	 *
	 * @param name     the name of the item stack
	 * @param material the material of the item stack
	 * @param amount   the amount of items in the stack
	 * @param lore     the lore of the item stack
	 * @return an {@link ItemStack.Builder} with the specified properties
	 */
	public static ItemStack.Builder getStack(Component name, Material material, int amount, Component... lore) {
		return getStack(name, material, amount, Arrays.asList(lore));
	}

	/**
	 * Updates the lore of the given {@link ItemStack.Builder} with the specified lore lines.
	 *
	 * @param builder the {@link ItemStack.Builder} to update
	 * @param lore    the new lore lines to set
	 * @return the updated {@link ItemStack.Builder}
	 */
	public static ItemStack.Builder updateLore(ItemStack.Builder builder, List<String> lore) {
		List<String> copiedLore = new ArrayList<>();
		for (String s : lore) {
			copiedLore.add(replaceColorCodes(s));
		}

		return clearAttributes(builder.set(DataComponents.LORE, copiedLore.stream()
						.map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
						.collect(Collectors.toList()))
				.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
	}

	/**
	 * Updates the lore of the given {@link ItemStack.Builder} with the specified lore lines.
	 *
	 * @param builder the {@link ItemStack.Builder} to update
	 * @param lore    the new lore lines to set
	 * @return the updated {@link ItemStack.Builder}
	 */
	public static ItemStack.Builder updateComponentLore(ItemStack.Builder builder, List<Component> lore) {
		return clearAttributes(builder.set(DataComponents.LORE, lore.stream()
						.map(line -> line.decoration(TextDecoration.ITALIC, false))
						.collect(Collectors.toList()))
				.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
	}

	/**
	 * Appends lore lines to the existing lore of the given {@link ItemStack.Builder}.
	 *
	 * @param builder the {@link ItemStack.Builder} to modify
	 * @param lore    the lore lines to append
	 * @return the modified {@link ItemStack.Builder}
	 */
	public static ItemStack.Builder appendLore(ItemStack.Builder builder, List<String> lore) {
		List<Component> existingLore = new ArrayList<>(builder.build().get(DataComponents.LORE));
		for (String s : lore) {
			existingLore.add(Component.text(replaceColorCodes(s)).decoration(TextDecoration.ITALIC, false));
		}

		return clearAttributes(builder.set(DataComponents.LORE, existingLore)
				.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
	}

	/**
	 * Marks the given {@link ItemStack.Builder} as not editable.
	 *
	 * @param builder the {@link ItemStack.Builder} to modify
	 * @return the modified {@link ItemStack.Builder}
	 */
	public static ItemStack.Builder setNotEditable(ItemStack.Builder builder) {
		return builder.set(Tag.Boolean("uneditable"), true);
	}

	/**
	 * Applies an enchantment glint to the given {@link ItemStack.Builder}.
	 *
	 * @param builder the {@link ItemStack.Builder} to modify
	 * @return the modified {@link ItemStack.Builder}
	 */
	public static ItemStack.Builder enchant(ItemStack.Builder builder) {
		return clearAttributes(builder.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
				.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
	}

	/**
	 * Creates an {@link ItemStack.Builder} from an existing {@link ItemStack}.
	 *
	 * @param stack the original {@link ItemStack} to create a builder from
	 * @return an {@link ItemStack.Builder} with the properties of the original stack
	 */
	public static ItemStack.Builder getFromStack(ItemStack stack) {
		return clearAttributes(ItemStack.builder(stack.material())
				.amount(stack.amount())
                .set(DataComponents.PROFILE, stack.get(DataComponents.PROFILE))
				.set(DataComponents.LORE, stack.get(DataComponents.LORE))
				.set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME))
				.set(DataComponents.CUSTOM_DATA, stack.get(DataComponents.CUSTOM_DATA)));
	}

	/**
	 * Creates an {@link ItemStack.Builder} with the specified name, material, amount, and lore list.
	 *
	 * @param name     the name of the item stack
	 * @param material the material of the item stack
	 * @param amount   the amount of items in the stack
	 * @param lore     the list of lore lines for the item stack
	 * @return an {@link ItemStack.Builder} with the specified properties
	 */
	public static ItemStack.Builder getStack(String name, Material material, int amount, List<?> lore) {
		return getStack(LEGACY_SERIALIZER.deserialize(replaceColorCodes(name)), material, amount, literalLoreComponents(lore));
	}

	/**
	 * Creates an {@link ItemStack.Builder} with the specified name, material, amount, and lore list.
	 *
	 * @param name     the name of the item stack
	 * @param material the material of the item stack
	 * @param amount   the amount of items in the stack
	 * @param lore     the list of lore lines for the item stack
	 * @return an {@link ItemStack.Builder} with the specified properties
	 */
	public static ItemStack.Builder getStack(Component name, Material material, int amount, List<Component> lore) {
		List<Component> copiedLore = new ArrayList<>(lore);

		return clearAttributes(ItemStack.builder(material).amount(amount).set(DataComponents.LORE, copiedLore.stream()
				.map(line -> line.decoration(TextDecoration.ITALIC, false))
				.collect(Collectors.toList()))
			.set(DataComponents.CUSTOM_NAME, name.decoration(TextDecoration.ITALIC, false))
			.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
	}

	/**
	 * Creates an {@link ItemStack.Builder} for a player head with the specified properties.
	 *
	 * @param name    the name of the item stack
	 * @param texture the texture URL of the player skin
	 * @param amount  the number of items in the stack
	 * @param lore    the lore of the item stack
	 * @return an {@link ItemStack.Builder} for a player head with the specified properties
	 */
	public static ItemStack.Builder getStackHead(String name, String texture, int amount, String... lore) {
		return getStackHead(name, texture, amount, Arrays.asList(lore));
	}

	public static ItemStack.Builder getStackHead(String name, String texture, int amount) {
		return getStackHead(name, texture, amount, new String[0]);
	}

	public static ItemStack.Builder getStackHead(String name, String texture, int amount, Component... lore) {
		return getStackHead(Component.text(name), texture, amount, Arrays.asList(lore));
	}

	public static ItemStack.Builder getStackHead(Component name, String texture, int amount, Component... lore) {
		return getStackHead(name, texture, amount, Arrays.asList(lore));
	}

	/**
	 * Creates an {@link ItemStack.Builder} for a player head with the specified name and texture.
	 *
	 * @param name    the name of the item stack
	 * @param texture the texture URL of the player skin
	 * @return an {@link ItemStack.Builder} for a player head
	 */
	public static ItemStack.Builder getStackHead(String name, String texture) {
		return getStackHead(name, texture, 1, new ArrayList<>());
	}

	/**
	 * Creates an {@link ItemStack.Builder} for a player head with the specified texture.
	 *
	 * @param texture the texture URL of the player skin
	 * @return an {@link ItemStack.Builder} for a player head
	 */
	public static ItemStack.Builder getStackHead(String texture) {
		return getStackHead("", texture, 1, new ArrayList<>());
	}

	/**
	 * Creates an {@link ItemStack.Builder} for a player head using a {@link PlayerSkin}.
	 *
	 * @param name   the name of the item stack
	 * @param skin   the {@link PlayerSkin} to use for the head
	 * @param amount the amount of items in the stack
	 * @param lore   the lore of the item stack
	 * @return an {@link ItemStack.Builder} for a player head
	 */
	public static ItemStack.Builder getStackHead(String name, PlayerSkin skin, int amount, String... lore) {
		return getStackHead(name, skin, amount, Arrays.asList(lore));
	}

	public static ItemStack.Builder getStackHead(String name, PlayerSkin skin, int amount) {
		return getStackHead(name, skin, amount, new String[0]);
	}

	public static ItemStack.Builder getStackHead(String name, PlayerSkin skin, int amount, Component... lore) {
		return getStackHead(Component.text(name), skin, amount, Arrays.asList(lore));
	}

	public static ItemStack.Builder getStackHead(Component name, PlayerSkin skin, int amount, Component... lore) {
		return getStackHead(name, skin, amount, Arrays.asList(lore));
	}

	/**
	 * Creates an {@link ItemStack.Builder} for a player head with a specified name, texture, amount, and lore list.
	 *
	 * @param name    the name of the item stack
	 * @param texture the texture URL of the player skin
	 * @param amount  the amount of items in the stack
	 * @param lore    the list of lore lines for the item stack
	 * @return an {@link ItemStack.Builder} for a player head with the specified properties
	 */
	public static ItemStack.Builder getStackHead(String name, String texture, int amount, List<?> lore) {
		JSONObject json = new JSONObject();
		json.put("isPublic", true);
		json.put("signatureRequired", false);
		json.put("textures", new JSONObject().put("SKIN",
				new JSONObject().put("url", "http://textures.minecraft.net/texture/" + texture).put("metadata", new JSONObject().put("model", "slim"))));

		String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());

		return ItemStack.builder(Material.PLAYER_HEAD)
			.set(DataComponents.LORE, literalLoreComponents(lore).stream()
				.map(line -> line.decoration(TextDecoration.ITALIC, false))
						.collect(Collectors.toList()))
			.set(DataComponents.CUSTOM_NAME, LEGACY_SERIALIZER.deserialize(replaceColorCodes(name)).decoration(TextDecoration.ITALIC, false))
				.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY)
				.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(texturesEncoded, null)))
			.amount(amount);
	}

	public static ItemStack.Builder getStackHead(Component name, String texture, int amount, List<Component> lore) {
		JSONObject json = new JSONObject();
		json.put("isPublic", true);
		json.put("signatureRequired", false);
		json.put("textures", new JSONObject().put("SKIN",
			new JSONObject().put("url", "http://textures.minecraft.net/texture/" + texture).put("metadata", new JSONObject().put("model", "slim"))));

		String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());

		return ItemStack.builder(Material.PLAYER_HEAD)
			.set(DataComponents.LORE, new ArrayList<>(lore).stream()
				.map(line -> line.decoration(TextDecoration.ITALIC, false))
				.collect(Collectors.toList()))
			.set(DataComponents.CUSTOM_NAME, name.decoration(TextDecoration.ITALIC, false))
			.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY)
			.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(texturesEncoded, null)))
			.amount(amount);
	}

	public static ItemStack.Builder getStackHeadSL(Component name, String texture, int amount, List<String> lore) {
		JSONObject json = new JSONObject();
		json.put("isPublic", true);
		json.put("signatureRequired", false);
		json.put("textures", new JSONObject().put("SKIN",
			new JSONObject().put("url", "http://textures.minecraft.net/texture/" + texture).put("metadata", new JSONObject().put("model", "slim"))));

		String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());

		List<Component> copiedLore = new ArrayList<>();
		for (String s : lore) {
			copiedLore.add(Component.text(replaceColorCodes(s)).decoration(TextDecoration.ITALIC, false));
		}

		return ItemStack.builder(Material.PLAYER_HEAD)
			.set(DataComponents.LORE, copiedLore)
			.set(DataComponents.CUSTOM_NAME, name.decoration(TextDecoration.ITALIC, false))
			.set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY)
			.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(texturesEncoded, null)))
			.amount(amount);
	}

	/**
	 * Creates an {@link ItemStack.Builder} for a player head with a specified name, {@link PlayerSkin}, amount, and lore list.
	 *
	 * @param name   the name of the item stack
	 * @param skin   the {@link PlayerSkin} to use for the head
	 * @param amount the amount of items in the stack
	 * @param lore   the list of lore lines for the item stack
	 * @return an {@link ItemStack.Builder} for a player head with the specified properties
	 */
	public static ItemStack.Builder getStackHead(String name, PlayerSkin skin, int amount, List<?> lore) {
		return clearAttributes(ItemStack.builder(Material.PLAYER_HEAD)
			.set(DataComponents.LORE, literalLoreComponents(lore).stream()
				.map(line -> line.decoration(TextDecoration.ITALIC, false))
						.collect(Collectors.toList()))
				.set(DataComponents.CUSTOM_NAME, Component.text(name).decoration(TextDecoration.ITALIC, false))
				.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, Set.of(
						DataComponents.CONSUMABLE,
						DataComponents.DAMAGE,
						DataComponents.BASE_COLOR,
						DataComponents.UNBREAKABLE
				)))
				.set(DataComponents.PROFILE, new ResolvableProfile(skin))
			.amount(amount));
	}

	public static ItemStack.Builder getStackHead(Component name, PlayerSkin skin, int amount, List<Component> lore) {
		return clearAttributes(ItemStack.builder(Material.PLAYER_HEAD)
			.set(DataComponents.LORE, new ArrayList<>(lore).stream()
				.map(line -> line.decoration(TextDecoration.ITALIC, false))
				.collect(Collectors.toList()))
			.set(DataComponents.CUSTOM_NAME, name.decoration(TextDecoration.ITALIC, false))
			.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, Set.of(
				DataComponents.CONSUMABLE,
				DataComponents.DAMAGE,
				DataComponents.BASE_COLOR,
				DataComponents.UNBREAKABLE
			)))
			.set(DataComponents.PROFILE, new ResolvableProfile(skin))
				.amount(amount));
	}

	public static ItemStack.Builder getUsingGUIMaterial(String name, GUIMaterial material, int amount, List<?> lore) {
		if (material.hasTexture()) {
			return ItemStackCreator.getStackHead(name, material.texture(), amount, lore);
		} else {
			return ItemStackCreator.getStack(name, material.material(), amount, lore);
		}
	}

	public static ItemStack.Builder getUsingGUIMaterial(String name, GUIMaterial material, int amount, String... lore) {
		return getUsingGUIMaterial(name, material, amount, Arrays.asList(lore));
	}

	/**
	 * Replaces color codes in the given string with Minecraft color codes.
	 *
	 * @param string the input string with color codes
	 * @return the string with color codes replaced
	 */
	public static String replaceColorCodes(String string) {
		return string.replace("&", "§");
	}

	public static List<Component> literalLoreComponents(List<?> lore) {
		List<Component> loreComponents = new ArrayList<>();
		for (Object line : lore) {
			if (line == null) {
				continue;
			}

			if (line instanceof Component component) {
				loreComponents.add(component);
				continue;
			}

			loreComponents.add(LEGACY_SERIALIZER.deserialize(replaceColorCodes(String.valueOf(line))));
		}
		return loreComponents;
	}
}
