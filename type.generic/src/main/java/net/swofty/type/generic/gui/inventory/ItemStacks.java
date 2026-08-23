package net.swofty.type.generic.gui.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.item.component.TooltipDisplay;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.tag.Tag;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public final class ItemStacks {

    private static final TooltipDisplay DEFAULT_TOOLTIP_DISPLAY = new TooltipDisplay(false, Set.of(
            DataComponents.UNBREAKABLE,
            DataComponents.POTION_CONTENTS,
            DataComponents.POTION_DURATION_SCALE
    ));
    private static final TooltipDisplay HIDDEN_TOOLTIP_DISPLAY = new TooltipDisplay(true, Set.of());

    private ItemStacks() {
    }

    public static ItemStack.Builder item(Material material, String textBlock, Object... arguments) {
        return item(material, 1, textBlock, arguments);
    }

    public static ItemStack.Builder item(Material material, int amount, String textBlock, Object... arguments) {
        List<Text> block = block(textBlock, arguments);
        return item(material, amount, nameOf(block), loreOf(block));
    }

    public static ItemStack.Builder item(Material material, Text name, List<Text> lore) {
        return item(material, 1, name, lore);
    }

    public static ItemStack.Builder item(Material material, int amount, Text name, List<Text> lore) {
        return clearAttributes(ItemStack.builder(material)
                .amount(amount)
                .set(DataComponents.CUSTOM_NAME, line(name))
                .set(DataComponents.LORE, lines(lore))
                .set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
    }

    public static ItemStack.Builder head(String texture, String textBlock, Object... arguments) {
        List<Text> block = block(textBlock, arguments);
        return head(texture, 1, nameOf(block), loreOf(block));
    }

    public static ItemStack.Builder head(PlayerSkin skin, String textBlock, Object... arguments) {
        List<Text> block = block(textBlock, arguments);
        return head(skin, 1, nameOf(block), loreOf(block));
    }

    public static ItemStack.Builder head(String texture, int amount, String textBlock, Object... arguments) {
        List<Text> block = block(textBlock, arguments);
        return head(texture, amount, nameOf(block), loreOf(block));
    }

    public static ItemStack.Builder head(PlayerSkin skin, int amount, String textBlock, Object... arguments) {
        List<Text> block = block(textBlock, arguments);
        return head(skin, amount, nameOf(block), loreOf(block));
    }

    public static ItemStack.Builder head(String texture, Text name, List<Text> lore) {
        return head(texture, 1, name, lore);
    }

    public static ItemStack.Builder head(String texture, int amount, Text name, List<Text> lore) {
        return ItemStack.builder(Material.PLAYER_HEAD)
                .amount(amount)
                .set(DataComponents.CUSTOM_NAME, line(name))
                .set(DataComponents.LORE, lines(lore))
                .set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY)
                .set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(encodeTexture(texture), null)));
    }

    public static ItemStack.Builder head(PlayerSkin skin, Text name, List<Text> lore) {
        return head(skin, 1, name, lore);
    }

    public static ItemStack.Builder head(PlayerSkin skin, int amount, Text name, List<Text> lore) {
        return clearAttributes(ItemStack.builder(Material.PLAYER_HEAD)
                .amount(amount)
                .set(DataComponents.CUSTOM_NAME, line(name))
                .set(DataComponents.LORE, lines(lore))
                .set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY)
                .set(DataComponents.PROFILE, new ResolvableProfile(skin)));
    }

    public static ItemStack.Builder of(GUIMaterial material, String textBlock, Object... arguments) {
        return of(material, 1, textBlock, arguments);
    }

    public static ItemStack.Builder of(GUIMaterial material, int amount, String textBlock, Object... arguments) {
        List<Text> block = block(textBlock, arguments);
        return of(material, amount, nameOf(block), loreOf(block));
    }

    public static ItemStack.Builder of(GUIMaterial material, Text name, List<Text> lore) {
        return of(material, 1, name, lore);
    }

    public static ItemStack.Builder of(GUIMaterial material, int amount, Text name, List<Text> lore) {
        if (material.hasTexture()) {
            return head(material.texture(), amount, name, lore);
        }
        return item(material.material(), amount, name, lore);
    }

    public static ItemStack.Builder named(Material material, String markup, Object... arguments) {
        return named(material, 1, Text.of(markup, arguments));
    }

    public static ItemStack.Builder named(Material material, int amount, String markup, Object... arguments) {
        return named(material, amount, Text.of(markup, arguments));
    }

    public static ItemStack.Builder named(Material material, Text name) {
        return named(material, 1, name);
    }

    public static ItemStack.Builder named(Material material, int amount, Text name) {
        return clearAttributes(ItemStack.builder(material)
                .amount(amount)
                .set(DataComponents.CUSTOM_NAME, line(name))
                .set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY));
    }

    public static ItemStack.Builder filler(Material material) {
        return ItemStack.builder(material)
                .set(DataComponents.CUSTOM_NAME, Component.space())
                .set(DataComponents.TOOLTIP_DISPLAY, HIDDEN_TOOLTIP_DISPLAY);
    }

    public static ItemStack.Builder enchanted(ItemStack.Builder builder) {
        return clearAttributes(builder.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    }

    public static ItemStack.Builder lore(ItemStack.Builder builder, List<Text> lore) {
        return clearAttributes(builder.set(DataComponents.LORE, lines(lore)));
    }

    public static ItemStack.Builder lore(ItemStack.Builder builder, String textBlock, Object... arguments) {
        return lore(builder, block(textBlock, arguments));
    }

    public static List<Text> loreText(ItemStack stack) {
        return textLines(stack.get(DataComponents.LORE));
    }

    public static List<Text> loreText(ItemStack.Builder builder) {
        return loreText(builder.build());
    }

    public static Text nameText(ItemStack stack) {
        return nameText(stack, Text.empty());
    }

    public static Text nameText(ItemStack stack, Text fallback) {
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        return name == null ? fallback : Text.component(name);
    }

    public static Text nameText(ItemStack.Builder builder) {
        return nameText(builder.build(), Text.empty());
    }

    public static Text nameText(ItemStack.Builder builder, Text fallback) {
        return nameText(builder.build(), fallback);
    }

    public static ItemStack.Builder appendLore(ItemStack.Builder builder, List<Text> lore) {
        List<Component> existing = builder.build().get(DataComponents.LORE);
        List<Component> combined = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        combined.addAll(lines(lore));
        return clearAttributes(builder.set(DataComponents.LORE, combined));
    }

    public static ItemStack.Builder appendLore(ItemStack.Builder builder, String textBlock, Object... arguments) {
        return appendLore(builder, block(textBlock, arguments));
    }

    public static ItemStack.Builder name(ItemStack.Builder builder, String markup, Object... arguments) {
        return name(builder, Text.of(markup, arguments));
    }

    public static ItemStack.Builder name(ItemStack.Builder builder, Text name) {
        return builder.set(DataComponents.CUSTOM_NAME, line(name));
    }

    public static ItemStack.Builder customName(ItemStack.Builder builder, String markup, Object... arguments) {
        return customName(builder, Text.of(markup, arguments));
    }

    public static ItemStack.Builder customName(ItemStack.Builder builder, Text name) {
        return builder.set(DataComponents.CUSTOM_NAME, name.asComponent());
    }

    public static ItemStack.Builder lines(ItemStack.Builder builder, String textBlock, Object... arguments) {
        return lines(builder, block(textBlock, arguments));
    }

    public static ItemStack.Builder lines(ItemStack.Builder builder, List<Text> lore) {
        return builder.set(DataComponents.LORE, lines(lore));
    }

    public static ItemStack.Builder text(ItemStack.Builder builder, String textBlock, Object... arguments) {
        List<Text> block = block(textBlock, arguments);
        return text(builder, nameOf(block), loreOf(block));
    }

    public static ItemStack.Builder text(ItemStack.Builder builder, Text name, List<Text> lore) {
        return builder.set(DataComponents.CUSTOM_NAME, line(name))
                .set(DataComponents.LORE, lines(lore));
    }

    public static ItemStack.Builder raw(Material material, String textBlock, Object... arguments) {
        return raw(material, 1, textBlock, arguments);
    }

    public static ItemStack.Builder raw(Material material, int amount, String textBlock, Object... arguments) {
        return text(ItemStack.builder(material).amount(amount), textBlock, arguments);
    }

    public static ItemStack.Builder raw(Material material, Text name, List<Text> lore) {
        return raw(material, 1, name, lore);
    }

    public static ItemStack.Builder raw(Material material, int amount, Text name, List<Text> lore) {
        return text(ItemStack.builder(material).amount(amount), name, lore);
    }

    public static ItemStack.Builder clearAttributes(ItemStack.Builder builder) {
        return builder.set(DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(List.of()))
                .set(DataComponents.TOOLTIP_DISPLAY, DEFAULT_TOOLTIP_DISPLAY);
    }

    public static ItemStack.Builder copy(ItemStack stack) {
        return clearAttributes(ItemStack.builder(stack.material())
                .amount(stack.amount())
                .set(DataComponents.PROFILE, stack.get(DataComponents.PROFILE))
                .set(DataComponents.LORE, stack.get(DataComponents.LORE))
                .set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME))
                .set(DataComponents.CUSTOM_DATA, stack.get(DataComponents.CUSTOM_DATA)));
    }

    public static ItemStack.Builder notEditable(ItemStack.Builder builder) {
        return builder.set(Tag.Boolean("uneditable"), true);
    }

    static List<Text> block(String textBlock, Object... arguments) {
        String source = textBlock.endsWith("\n") ? textBlock.substring(0, textBlock.length() - 1) : textBlock;
        return Text.of(source, arguments).lines();
    }

    private static Text nameOf(List<Text> block) {
        return block.isEmpty() ? Text.empty() : block.getFirst();
    }

    private static List<Text> loreOf(List<Text> block) {
        return block.size() <= 1 ? List.of() : List.copyOf(block.subList(1, block.size()));
    }

    private static Component line(Text text) {
        return text.asComponent().decoration(TextDecoration.ITALIC, false);
    }

    private static List<Component> lines(List<Text> lore) {
        List<Component> out = new ArrayList<>(lore.size());
        for (Text text : lore) {
            out.add(line(text));
        }
        return out;
    }

    private static List<Text> textLines(List<Component> lore) {
        List<Text> out = new ArrayList<>(lore == null ? 0 : lore.size());
        if (lore == null) {
            return out;
        }
        for (Component component : lore) {
            out.add(Text.component(component));
        }
        return out;
    }

    private static String encodeTexture(String texture) {
        JSONObject json = new JSONObject();
        json.put("isPublic", true);
        json.put("signatureRequired", false);
        json.put("textures", new JSONObject().put("SKIN", new JSONObject()
                .put("url", "http://textures.minecraft.net/texture/" + texture)
                .put("metadata", new JSONObject().put("model", "slim"))));
        return Base64.getEncoder().encodeToString(json.toString().getBytes());
    }
}
