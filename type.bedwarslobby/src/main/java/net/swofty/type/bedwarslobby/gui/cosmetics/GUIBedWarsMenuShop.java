package net.swofty.type.bedwarslobby.gui.cosmetics;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

public class GUIBedWarsMenuShop extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Bed Wars Menu", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        layout.slot(11, ItemStacks.item(Material.SAND, """
                <3>The Slumber Hotel
                <7>Progress through the hotel to earn
                <7>more <d>rewards<7>, <b>xp<7>, and new <a>cosmetics<7>!

                <e>Click to open!"""), (_, c) -> c.player().notImplemented());
        layout.slot(13, ItemStacks.item(Material.ARMOR_STAND, """
                <a>My Cosmetics
                <7>Browse and equip all the available
                <7>in-game Bed Wars cosmetics.

                <e>Click to browse!"""), (_, c) -> {
            c.push(new GUIMyCosmetics());
        });
        layout.slot(15, ItemStacks.item(Material.COMPARATOR, """
                <a>Game Settings
                <7>Adjust game settings for Bed Wars.

                <e>Click to open!"""), (_, c) -> c.player().notImplemented());
    }
}
