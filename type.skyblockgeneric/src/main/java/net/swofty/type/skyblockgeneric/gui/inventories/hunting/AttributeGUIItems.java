package net.swofty.type.skyblockgeneric.gui.inventories.hunting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHunting;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.hunting.AttributeText;
import net.swofty.type.skyblockgeneric.item.components.AttributeShardComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;

import java.util.ArrayList;
import java.util.List;

final class AttributeGUIItems {
    static final int[] CONTENT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private AttributeGUIItems() {
    }

    static ItemStack.Builder shard(AttributeDefinition definition, int amount) {
        return AttributeShardComponent.create(definition, Math.max(1, amount)).getItemStackBuilder();
    }

    static ItemStack.Builder huntingShard(AttributeDefinition definition, DatapointHunting.HuntingData data) {
        int level = data.level(definition.id());
        int owned = data.shardCount(definition.id());
        List<String> lore = new ArrayList<>();
        if (definition.family() != AttributeDefinition.AttributeFamily.NONE)
            lore.add("§8" + familyName(definition) + " Family");
        lore.add("");
        lore.add("§6" + definition.name() + (level > 0 ? " " + StringUtility.getAsRomanNumeral(level) : "") + " §8(" + title(definition.skill().name()) + ")");
        lore.addAll(AttributeText.wrap(level >= 10 ? AttributeText.atLevel(definition, 10)
                : AttributeText.upgrade(definition, level, level + 1), "§7", 34));
        lore.add("");
        lore.add("§7Owned: §b" + owned + (owned == 1 ? " Shard" : " Shards"));
        if (level >= 10) lore.add("§a§lAttribute Maxed!");
        else lore.add("§7Syphon §b" + Math.max(0, definition.rarity().nextRequirement(data.syphoned(definition.id()))
                - data.syphoned(definition.id())) + " §7more to level up!");
        lore.add("");
        if (level < 10) lore.addAll(List.of("§eLeft-click to syphon!", "§eShift Left-click to syphon all!"));
        lore.addAll(List.of("§eRight-click to convert to an item!", "§eShift Right-click to convert to a stack of items!", "",
                definition.rarity().itemRarity().getLegacyColor() + "§l" + definition.rarity() + " " + definition.category() + " SHARD §8(ID " + definition.id() + ")"));
        ItemStack.Builder builder = new NonPlayerItemUpdater(AttributeShardComponent.create(definition, 1)).getUpdatedItem();
        builder.set(DataComponents.CUSTOM_NAME, Component.text(definition.shard(), definition.rarity().itemRarity().getColor())
                .decoration(TextDecoration.ITALIC, false));
        return ItemStackCreator.updateLore(builder, lore);
    }

    static ItemStack.Builder attribute(AttributeDefinition definition, DatapointHunting.HuntingData data,
                                       boolean advanced) {
        int level = data.level(definition.id());
        int syphoned = data.syphoned(definition.id());
        List<String> lore = new ArrayList<>();
        lore.add("§8" + title(definition.skill().name()));
        lore.add("");
        lore.addAll(AttributeText.wrap(level >= 10 ? AttributeText.atLevel(definition, 10)
                : AttributeText.upgrade(definition, level, level + 1), "§7", 34));
        lore.add("");
        lore.add("§7Source: " + definition.rarity().itemRarity().getLegacyColor() + definition.shardName() + " §8(" + definition.id() + ")");
        lore.add("§7Rarity: " + definition.rarity().itemRarity().getLegacyColor() + "§l" + definition.rarity());
        if (level > 0) {
            lore.add("§7Enabled: " + (data.enabled(definition.id()) ? "§aYes" : "§cNo"));
            lore.add("");
            lore.add("§7Attribute Level: §a" + level);
            if (level < 10)
                lore.add("§7Syphon §b" + (definition.rarity().nextRequirement(syphoned) - syphoned) + " §7shards to level up!");
            lore.add("§7Syphon §b" + (definition.rarity().cumulativeForLevel(10) - syphoned) + " §7shards to max!");
            lore.add("");
            lore.add("§eLeft-Click to open!");
            lore.add("§eRight-Click to toggle!");
        } else {
            lore.add("");
            lore.add("§7Syphon §b1 §7shard to unlock!");
            lore.add("§7Syphon §b" + definition.rarity().cumulativeForLevel(10) + " §7more to max!");
            lore.add("");
            lore.add("§eLeft-Click to open!");
        }
        String color = "§6";
        if (level == 0) return ItemStackCreator.getStack(color + definition.name(), Material.GRAY_DYE, 1, lore);
        ItemStack.Builder builder = new NonPlayerItemUpdater(AttributeShardComponent.create(definition, 1)).getUpdatedItem();
        builder.set(DataComponents.CUSTOM_NAME, Component.text(color + definition.name())
                .decoration(TextDecoration.ITALIC, false));
        return ItemStackCreator.updateLore(builder, lore);
    }

    static int pages(int size) {
        return Math.max(1, (size + CONTENT_SLOTS.length - 1) / CONTENT_SLOTS.length);
    }

    private static String familyName(AttributeDefinition definition) {
        return title(definition.family().name().replace('_', ' '));
    }

    private static String title(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.toLowerCase().split(" "))
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        return result.toString().trim();
    }
}
