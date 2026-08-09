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
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIGardenMilestones extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenData.GardenCoreData core = GardenGuiSupport.core(player);

        layout.slot(21, ItemStackCreator.getStack(
            "§aCrop Milestones",
            Material.WHEAT,
            1,
            "§7View progress and rewards for",
            "§7crop milestones on your garden!",
            "",
            "§7Tracked crops: §e" + core.getCropMilestones().size(),
            "§eClick to view!"
        ), (click, c) -> c.push(new GUICropMilestones()));

        layout.slot(23, ItemStackCreator.getStackHead(
            "§aVisitor Milestones",
            "8d34f38c1bb106e11908ad3cc90162c18b863d678265c84a84a358903f8f7a1c",
            1,
            "§7View progress and rewards for",
            "§7visitor milestones on your garden!",
            "",
            "§7Tracked visitors: §e" + core.getVisitorMilestones().size(),
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIVisitorMilestones()));
    }
}
