package net.swofty.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;

public class GUIBaitGuide extends StatelessView {
    private static final int[] BAIT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Bait Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStacks.item(Material.BOOK, """
                <6>Bait Guide
                <7>View <6>Baits</6> and the effects they add
                <7>to your fishing catches."""));

        int index = 0;
        for (var bait : FishingItemSupport.getBaits()) {
            if (index >= BAIT_SLOTS.length) {
                break;
            }
            layout.slot(BAIT_SLOTS[index++], FishingGuideStackFactory.buildBaitStack(bait));
        }
    }
}
