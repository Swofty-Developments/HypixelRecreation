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
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUISkyMart extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("SkyMart", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        layout.slot(11, ItemStackCreator.getStack(
            "§aFarming Essentials",
            Material.GREEN_DYE,
            1,
            "§7All the farming supplies you could",
            "§7ever need!",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUISkyMartCategory("farming_essentials", "Farming Essentials")));

        layout.slot(12, ItemStackCreator.getStack(
            "§aFarming Tools",
            Material.DIAMOND_HOE,
            1,
            "§7Purchase tools made specifically for",
            "§7each crop!",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUISkyMartCategory("farming_tools", "Farming Tools")));

        layout.slot(13, ItemStackCreator.getStack(
            "§aBarn Skins",
            Material.OAK_PLANKS,
            1,
            "§7Spruce up your Barn!",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUISkyMartCategory("barn_skins", "Barn Skins")));

        layout.slot(14, ItemStackCreator.getStack(
            "§aGreenhouse Skins",
            Material.WHITE_STAINED_GLASS,
            1,
            "§7Make your Greenhouse look fresh!",
            "",
            "§eClick to view!"
        ));

        layout.slot(15, ItemStackCreator.getStack(
            "§aPests",
            Material.CHEST_MINECART,
            1,
            "§7Got pests? We got you.",
            "",
            "§eClick to view!"
        ));

        Components.back(layout, 30, ctx);
    }
}
