package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;

import java.util.List;

public class Fireball implements LuckyBlockItem {

    @Override
    public String getId() {
        return "fireball";
    }

    @Override
    public String getDisplayName() {
        return "Fireball";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder(Material.FIRE_CHARGE)
                .amount(8)
                .customName(Component.text(getDisplayName(), NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(List.of(
                        Component.text("Launch an explosive fireball!", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Right-click to use!", NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, false)
                ))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

}
