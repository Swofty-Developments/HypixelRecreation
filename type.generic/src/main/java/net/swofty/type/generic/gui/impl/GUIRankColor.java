package net.swofty.type.generic.gui.impl;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.user.categories.RankColor;

public class GUIRankColor extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Rank Color", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 40);
        RankColor[] colors = RankColor.values();
        for (int i = 0; i < colors.length; i++) addColor(layout, SLOTS[i], colors[i]);

        if (ctx.player().getRank() == Rank.MVP_PLUS_PLUS) {
            layout.slot(44, (_, c) -> {
                boolean aqua = c.player().isMvpPlusPlusAqua();
                return ItemStacks.item(Material.NETHER_STAR, 1, aqua
                    ? """
                        <a>Toggle Prefix Color
                        <7>Selected: <b>Aqua

                        <7>Click to change the color to <6>Gold"""
                    : """
                        <a>Toggle Prefix Color
                        <7>Selected: <6>Gold

                        <7>Click to change the color to <b>Aqua""");
            }, (_, c) -> {
                var data = c.player().getDataHandler().get(net.swofty.type.generic.data.HypixelDataHandler.Data.MVP_PLUS_PLUS_AQUA,
                    net.swofty.type.generic.data.datapoints.DatapointBoolean.class);
                data.setValue(!data.getValue());
                c.session().refresh();
            });
        }
    }

    private void addColor(ViewLayout<DefaultState> layout, int slot, RankColor color) {
        layout.slot(slot, (_, ctx) -> render(ctx.player(), color), (_, ctx) -> {
            HypixelPlayer player = ctx.player();
            if (!player.getRank().isEqualOrHigherThan(Rank.MVP_PLUS)) {
                player.sendMessage("<c>You must be MVP+ or higher to use rank colors!");
                return;
            }
            if (!color.isUnlocked(player)) {
                if (color == RankColor.DARK_BLUE) {
                    player.sendMessage("<c>You must gift 100 ranks to unlock this rank color!");
                } else {
                    player.sendMessage("<c>You must be Hypixel Level {} to unlock this rank color!", color.getRequiredLevel());
                }
                return;
            }
            player.setRankColor(color);
            ctx.session().refresh();
        });
    }

    private ItemStack.Builder render(HypixelPlayer player, RankColor color) {
        boolean hasRank = player.getRank().isEqualOrHigherThan(Rank.MVP_PLUS);
        boolean unlocked = hasRank && color.isUnlocked(player);
        boolean selected = player.getRankColor() == color;
        String code = legacyCode(color.getColor());
        String ending = selected ? "<a>Currently selected!"
                : unlocked ? "<e>Click to select!"
                : color == RankColor.DARK_BLUE ? "<6>Unlock by claiming 100 Ranks Gifted Reward!"
                : "<3>Unlocked at Hypixel Level {}";

        return ItemStacks.item(unlocked ? color.getMaterial() : Material.GRAY_DYE, 1,
                (unlocked ? "<a>" : "<c>") + "{} Rank Color\n"
                        + "<7>Changes the color of the plus in <b>MVP<c>+\n"
                        + "<7>to {}, turning it into <b>MVP" + code + "+\n"
                        + "\n"
                        + "<7>Shown in tab list also when chatting\n"
                        + "<7>and joining lobbies.\n"
                        + "\n"
                        + ending,
                color.getDisplayName(), color.getDisplayName().toLowerCase(), color.getRequiredLevel());
    }

    private String legacyCode(NamedTextColor color) {
        return "<" + Integer.toHexString(color.value() == 0x000000 ? 0 : switch (color.toString()) {
            case "dark_blue" -> 1;
            case "dark_green" -> 2;
            case "dark_aqua" -> 3;
            case "dark_red" -> 4;
            case "dark_purple" -> 5;
            case "gold" -> 6;
            case "gray" -> 7;
            case "dark_gray" -> 8;
            case "blue" -> 9;
            case "green" -> 10;
            case "aqua" -> 11;
            case "red" -> 12;
            case "light_purple" -> 13;
            case "yellow" -> 14;
            default -> 15;
        }) + ">";
    }
}
