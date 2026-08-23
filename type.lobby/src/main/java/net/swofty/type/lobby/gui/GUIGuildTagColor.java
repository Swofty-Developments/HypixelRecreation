package net.swofty.type.lobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.guild.GuildManager;

import java.util.List;

public class GUIGuildTagColor extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Guild Tag Color", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        add(layout, 10, "Gray", Material.LIGHT_GRAY_DYE, "§7", 5);
        add(layout, 11, "Dark Aqua", Material.CYAN_DYE, "§3", 15);
        add(layout, 12, "Dark Green", Material.GREEN_DYE, "§2", 25);
        layout.slot(31, ItemStacks.item(Material.ARROW, "<a>Go Back"),
            (click, viewCtx) -> viewCtx.navigator().pop());
    }

    private void add(ViewLayout<DefaultState> layout, int slot, String name, Material material, String color, int requiredLevel) {
        layout.slot(slot, (s, c) -> {
                final GuildData data = GuildManager.getGuildFromPlayer(c.player());
                if (data == null) return ItemStacks.item(material, 1, Text.legacy(color + name), List.of(
                    Text.of("<7>Preview: {}", Text.legacy(color + "[GUILD]")),
                    Text.empty(),
                    Text.of("<c>You must be in a guild to preview the tag color!")));

                boolean unlocked = data.getLevel() >= requiredLevel;
                return ItemStacks.item(unlocked ? material : Material.GRAY_DYE, 1,
                    unlocked ? Text.legacy(color + name) : Text.of("<c>{}", name),
                    List.of(
                        Text.of("<7>Preview: {}", Text.legacy(color + "[" + data.getTag() + "]")),
                        Text.empty(),
                        unlocked ? Text.of("<e>Click to pick this color!") : Text.of("<c>Requires Guild Level {}", requiredLevel)));
            },
            (click, ctx) -> {
                GuildData data = GuildManager.getGuildFromPlayer(ctx.player());
                if (data != null && data.getLevel() >= requiredLevel)
                    GuildManager.changeSetting(ctx.player(), "tagcolor", color);
                else ctx.player().sendMessage("<c>This tag color requires Guild Level {}!", requiredLevel);
            });
    }
}
