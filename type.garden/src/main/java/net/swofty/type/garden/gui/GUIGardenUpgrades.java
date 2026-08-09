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

public class GUIGardenUpgrades extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Upgrades", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 31, ctx);

        layout.slot(11, ItemStackCreator.getStack(
            "§aCrop Upgrades",
            Material.WHEAT,
            1,
            "§7Increase your §6☘ Farming Fortune",
            "§7for each crop by upgrading them!",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUICropUpgrades()));

        layout.slot(15, ItemStackCreator.getStack(
            "§aGreenhouse Upgrades",
            Material.WHITE_STAINED_GLASS,
            1,
            "§7Unlock Greenhouses, track mutation",
            "§7analyzer progress, and review slot",
            "§7and bounty data.",
            "",
            "§eClick to view!"
        ), (click, c) -> c.push(new GUIGreenhouseUpgrades()));
    }
}
