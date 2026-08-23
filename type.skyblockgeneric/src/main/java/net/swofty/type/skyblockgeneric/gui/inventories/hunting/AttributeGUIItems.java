package net.swofty.type.skyblockgeneric.gui.inventories.hunting;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
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
        TextColor rarityColour = definition.rarity().itemRarity().getColor();
        List<Text> lore = new ArrayList<>();
        if (definition.family() != AttributeDefinition.AttributeFamily.NONE)
            lore.add(Text.of("<8>{} Family", familyName(definition)));
        lore.add(Text.empty());
        lore.add(Text.of("<6>{}", definition.name())
                .appendIf(level > 0, "<6> {:roman}", level)
                .append("<6> <8>({})", title(definition.skill().name())));
        lore.addAll(description(definition, level));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Owned: <b>{} {}", owned, owned == 1 ? "Shard" : "Shards"));
        if (level >= 10) lore.add(Text.of("<a><l>Attribute Maxed!"));
        else lore.add(Text.of("<7>Syphon <b>{} <7>more to level up!",
                Math.max(0, definition.rarity().nextRequirement(data.syphoned(definition.id()))
                        - data.syphoned(definition.id()))));
        lore.add(Text.empty());
        if (level < 10) {
            lore.add(Text.of("<e>Left-click to syphon!"));
            lore.add(Text.of("<e>Shift Left-click to syphon all!"));
        }
        lore.add(Text.of("<e>Right-click to convert to an item!"));
        lore.add(Text.of("<e>Shift Right-click to convert to a stack of items!"));
        lore.add(Text.empty());
        lore.add(Text.of("<color:{}><l>{} {} SHARD </l><8>(ID {})", rarityColour,
                definition.rarity(), definition.category(), definition.id()));
        ItemStack.Builder builder = new NonPlayerItemUpdater(AttributeShardComponent.create(definition, 1)).getUpdatedItem();
        ItemStacks.name(builder, "<color:{}>{}", rarityColour, definition.shard());
        return ItemStacks.lore(builder, lore);
    }

    static ItemStack.Builder attribute(AttributeDefinition definition, DatapointHunting.HuntingData data,
                                       boolean advanced) {
        int level = data.level(definition.id());
        int syphoned = data.syphoned(definition.id());
        TextColor rarityColour = definition.rarity().itemRarity().getColor();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{}", title(definition.skill().name())));
        lore.add(Text.empty());
        lore.addAll(description(definition, level));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Source: <color:{}>{} <8>({})", rarityColour, definition.shardName(), definition.id()));
        lore.add(Text.of("<7>Rarity: <color:{}><l>{}", rarityColour, definition.rarity()));
        if (level > 0) {
            lore.add(Text.of("<7>Enabled: {}", data.enabled(definition.id())
                    ? Text.of("<a>Yes") : Text.of("<c>No")));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Attribute Level: <a>{}", level));
            if (level < 10)
                lore.add(Text.of("<7>Syphon <b>{} <7>shards to level up!",
                        definition.rarity().nextRequirement(syphoned) - syphoned));
            lore.add(Text.of("<7>Syphon <b>{} <7>shards to max!",
                    definition.rarity().cumulativeForLevel(10) - syphoned));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Left-Click to open!"));
            lore.add(Text.of("<e>Right-Click to toggle!"));
        } else {
            lore.add(Text.empty());
            lore.add(Text.of("<7>Syphon <b>1 <7>shard to unlock!"));
            lore.add(Text.of("<7>Syphon <b>{} <7>more to max!", definition.rarity().cumulativeForLevel(10)));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Left-Click to open!"));
        }
        Text name = Text.of("<6>{}", definition.name());
        if (level == 0) return ItemStacks.item(Material.GRAY_DYE, 1, name, lore);
        ItemStack.Builder builder = new NonPlayerItemUpdater(AttributeShardComponent.create(definition, 1)).getUpdatedItem();
        ItemStacks.name(builder, name);
        return ItemStacks.lore(builder, lore);
    }

    static int pages(int size) {
        return Math.max(1, (size + CONTENT_SLOTS.length - 1) / CONTENT_SLOTS.length);
    }

    static List<Text> description(AttributeDefinition definition, int level) {
        return wrapped(level >= 10 ? AttributeText.atLevel(definition, 10)
                : AttributeText.upgrade(definition, level, level + 1));
    }

    static List<Text> wrapped(String markup) {
        List<Text> result = new ArrayList<>();
        for (Text line : AttributeText.wrapTexts(markup, 34))
            result.add(Text.of("<7>{}", line));
        return result;
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
