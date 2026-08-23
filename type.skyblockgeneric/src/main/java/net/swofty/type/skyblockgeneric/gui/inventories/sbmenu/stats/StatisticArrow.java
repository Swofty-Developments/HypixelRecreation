package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.stats;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;

import java.util.List;
import java.util.Map;

public final class StatisticArrow {
    private static final String RED = "e44736f86be74deae5886a323df59d995aa39bea76c17b45baf832f4448c021c";
    private static final Map<TextColor, String> TEXTURES = Map.ofEntries(
        Map.entry(NamedTextColor.RED, RED),
        Map.entry(NamedTextColor.GREEN, "8cd690ae9d4f09745fb9a55579df72b7a0aebc9653aa42ed490c6d036580f4ca"),
        Map.entry(NamedTextColor.AQUA, "9b02d8d0645d2f6e0caa8c3fa4facde0ecf8d5c4c92511be69577a12ad9ebe88"),
        Map.entry(NamedTextColor.YELLOW, "17fa3bae5d8a844594c98ff87791a7c0d1b9e1370c21b6b04354e3ecf5b6a3a5"),
        Map.entry(NamedTextColor.WHITE, "33f4b333f1c6ff8d9a13747dfc5a047c77c079ab6480f9ef64d5c85ec740fce4"),
        Map.entry(NamedTextColor.DARK_RED, "580b4c9a1f7976da0d09b8394bb19f34257a5acd82906aaeba8ab020f825acbf"),
        Map.entry(NamedTextColor.BLUE, "add45dceae3989edff0f93c22da51884370ddf6096aa708a054c0515d62bf675")
    );

    public static ItemStack.Builder create(ItemStatistic statistic) {
        return ItemStacks.of(new GUIMaterial(TEXTURES.getOrDefault(statistic.getDisplayColor(), RED)), 1,
            Text.of("<color:{}>➭", statistic.getDisplayColor()), List.of());
    }
}
