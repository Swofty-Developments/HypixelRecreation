package net.swofty.type.skyblockgeneric.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.TextArgRenderers;
import net.swofty.type.generic.text.RenderContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;

import java.util.ArrayList;
import java.util.List;

public final class SkyBlockTextScopes {

    private SkyBlockTextScopes() {
    }

    public static void init() {
        RenderContext.registerScope(SkyBlockItem.class, item -> tag -> switch (tag) {
            case "item" -> displayName(item);
            case "rarity" -> item.getAttributeHandler().getRarity().getDisplay();
            default -> null;
        });
        TextArgRenderers.register(SkyBlockItem.class, SkyBlockTextScopes::describe);
    }

    private static Component displayName(SkyBlockItem item) {
        return nameOf(updated(item));
    }

    private static Component describe(SkyBlockItem item) {
        ItemStack updated = updated(item);
        Component name = nameOf(updated);
        List<Component> lore = updated.get(DataComponents.LORE);
        if (lore == null || lore.isEmpty()) {
            return name;
        }
        return name.hoverEvent(HoverEvent.showText(tooltip(name, lore)));
    }

    private static Component tooltip(Component name, List<Component> lore) {
        List<Component> lines = new ArrayList<>(lore.size() * 2 + 1);
        lines.add(name);
        for (Component line : lore) {
            lines.add(Component.newline());
            lines.add(line);
        }
        return Component.empty().children(lines);
    }

    private static Component nameOf(ItemStack updated) {
        Component name = updated.get(DataComponents.CUSTOM_NAME);
        return name == null ? Component.empty() : name;
    }

    private static ItemStack updated(SkyBlockItem item) {
        return new NonPlayerItemUpdater(item).getUpdatedItem().build();
    }
}
