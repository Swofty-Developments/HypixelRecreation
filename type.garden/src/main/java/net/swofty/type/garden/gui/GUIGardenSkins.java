package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

public class GUIGardenSkins extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Skins", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 31, ctx);

        layout.slot(11, ItemStackCreator.getStack(
            "§aBarn Skins",
            Material.OAK_PLANKS,
            1,
            "§7View and select different skins for",
            "§7your Barn.",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIBarnSkins()));

        layout.slot(15, ItemStackCreator.getStack(
            "§aGreenhouse Skins",
            Material.WHITE_STAINED_GLASS,
            1,
            "§7Select unlocked Greenhouse skins",
            "§7from your persisted Garden profile",
            "§7cosmetics data.",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIGreenhouseSkins()));
    }
}
