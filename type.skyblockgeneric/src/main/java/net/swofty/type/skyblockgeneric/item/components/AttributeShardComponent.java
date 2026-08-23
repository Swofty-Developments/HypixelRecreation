package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.hunting.AttributeRegistry;
import net.swofty.type.skyblockgeneric.hunting.AttributeText;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AttributeShardComponent extends SkyBlockItemComponent {
    private final String presetId;

    public AttributeShardComponent() {
        this(null);
    }

    public AttributeShardComponent(String presetId) {
        this.presetId = presetId;
        addInheritedComponent(new CustomDisplayNameComponent(item -> {
            AttributeDefinition definition = definition(item);
            return definition == null ? Text.literal("Attribute Shard") : Text.of("<color:{}>{}",
                    definition.rarity().itemRarity().getColor(), definition.shardName());
        }));
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig((item, player) -> lore(item), null), false));
        addInheritedComponent(new ExtraRarityComponent(item -> {
            AttributeDefinition definition = definition(item);
            return definition == null ? "SHARD" : definition.category().name() + " SHARD <r><8>(ID " + definition.id() + ")";
        }));
        addInheritedComponent(new InteractableComponent((player, item) -> {
            AttributeDefinition definition = definition(item);
            if (definition == null) return;
            player.getHuntingData().addShards(definition.id(), item.getAmount());
            player.sendMessage("<a>Added <e>{}x <a>{} to your Hunting Box!", item.getAmount(), definition.shardName());
            item.setAmount(0);
        }, null, null));
    }

    public static SkyBlockItem create(AttributeDefinition definition, int amount) {
        String variant = "SHARD_" + definition.shard().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_|_$", "");
        net.swofty.commons.skyblock.item.ItemType type;
        try {
            type = net.swofty.commons.skyblock.item.ItemType.valueOf(variant);
        } catch (IllegalArgumentException ignored) {
            type = net.swofty.commons.skyblock.item.ItemType.ATTRIBUTE_SHARD;
            Logger.error("Could not load attribute item");
        }
        SkyBlockItem item = new SkyBlockItem(type, amount);
        item.getAttributeHandler().setAttributeShardId(definition.id().toString());
        item.getAttributeHandler().setRarity(definition.rarity().itemRarity());
        return item;
    }

    private AttributeDefinition definition(SkyBlockItem item) {
        AttributeDefinition definition = AttributeRegistry.get(item.getAttributeHandler().getAttributeShardId());
        return definition == null ? AttributeRegistry.get(presetId) : definition;
    }

    private List<String> lore(SkyBlockItem item) {
        AttributeDefinition definition = definition(item);
        if (definition == null) return List.of("<7>An unknown Attribute Shard.");
        List<String> lore = new ArrayList<>();
        lore.add("<6>" + definition.name() + " I");
        String levelOneEffect = definition.effect().replaceAll(
                "([+-]?\\d+(?:\\.\\d+)?%?)[–-][+-]?\\d+(?:\\.\\d+)?%?", "$1");
        for (Text line : AttributeText.wrapTexts(levelOneEffect, 34)) {
            lore.add("<7>" + line.serialize());
        }
        lore.add("");
        lore.add("<7>You can Syphon this shard from");
        lore.add("<7>your <a>Hunting Box</a>.");
        lore.add("");
        lore.add("<e>Right-click to send to Hunting Box!");
        lore.add("<e>Shift Right-click to move all!");
        return lore;
    }
}
