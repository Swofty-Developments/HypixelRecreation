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

public class GUIHotelGuide extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Hotel Guide", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.back(layout, 31, ctx);

        layout.slot(10, ItemStacks.item(Material.SAND, """
                <3>The Slumber Hotel

                <7>The <3>Slumber Hotel <7>exists in a unique
                <7>dimension between the waking world
                <7>and the dream world.

                <7>Residents at the hotel come from all
                <7>sorts of <b>universes <7>and <d>timelines<7>. No
                <7>one physically exists in the hotel, it
                <7>is merely where their dreams go."""));
        layout.slot(12, ItemStacks.item(Material.SAND, 2, """
                <e>Slumber Tickets

                <7>Earned by playing Bed Wars games,
                <e>Slumber Tickets <7>will help you unlock
                <7>new rooms in the hotel.

                <7>You have a <e>Platinum Membership
                <e>Wallet<7>, so you can hold up to <e>100,000
                <e>Slumber Tickets<7>!

                <b>Kill <7><l>⮕ </l><e>1 Slumber Ticket
                <b>Final Kill <7><l>⮕ </l><e>5 Slumber Tickets
                <b>Bed Destroyed <7><l>⮕ </l><e>10 Slumber Tickets
                <b>Win <7><l>⮕ </l><e>20 Slumber Tickets
                <b>Time Played <7><l>⮕ </l><e>5 Slumber Tickets"""));
        layout.slot(14, ItemStacks.item(Material.SAND, 3, """
                <a>Quests & Objectives

                <7>Helping enough residents will add to
                <7>the reputation of the hotel, bringing
                <7>in even more residents!

                <7>Residents can reward <e>Tickets <7>and
                <7>even unique <d>cosmetics <7>for purchase!"""));
        layout.slot(16, ItemStacks.item(Material.SAND, 4, """
                <6>The Hotel Owner

                <7>Most will never meet the <6>Hotel Owner<7>,
                <7>but if you help enough residents he
                <7>might start to take notice of you."""));
    }
}
