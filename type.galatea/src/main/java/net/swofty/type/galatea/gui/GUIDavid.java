package net.swofty.type.galatea.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;

public class GUIDavid extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("David", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(20, ItemStacks.item(Material.GOLDEN_AXE, """
                <a>Hunting Tools
                <7>You don't know which Hunting Gear to
                <7>buy, or how to use them? I’ll
                <7>explain everything!

                <e>Click to open!"""));
        layout.slot(22, ItemStacks.item(Material.LEAD, """
                <a>Hunting Lessons
                <7>You still have questions regarding
                <7>Shards, Attributes, or Fusions? I can
                <7>explain it all!

                <e>Click to open!"""));
        layout.slot(24, ItemStacks.head("3582fcde68921b14bf6ebda6d63fec81b2c0e668e2fc2fbfe9af6f4b7d72e2f2", """
                <a>David's Cloak
                <7>The more you Hunt, the stronger my
                <7>cloak gets, check it out!

                <e>Click to view!"""));
    }
}
