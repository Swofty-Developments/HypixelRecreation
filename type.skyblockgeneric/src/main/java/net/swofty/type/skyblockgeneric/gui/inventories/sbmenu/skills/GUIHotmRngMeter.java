package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.skills;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

public class GUIHotmRngMeter extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crystal Nucleus RNG Meter", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 22);
        layout.slot(11, ItemStacks.item(Material.PAPER, 1, """
                <d>Progress
                <7>The selected drop is guaranteed when
                <7>the meter reaches <d>1M Nucleus XP<7>.

                <7>Progress: <d>1.1%
                <d><m>                         <f>  <d>11,000<5>/<d>1M"""));
        layout.slot(13, ItemStacks.item(Material.GOLD_BLOCK, 1, """
                <6>Divan's Alloy
                <7>Selected RNG Drop

                <e>This meter is currently a stub."""));
        layout.slot(15, ItemStacks.item(Material.PAPER, 1, """
                <d>Crystal Nucleus RNG Meter
                <7>Complete the Crystal Nucleus to gain
                <9>1,000 Nucleus XP<7> toward this meter.

                <e>Click to view!"""));
        Components.backOrClose(layout, 18, ctx);
    }
}
