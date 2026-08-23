package net.swofty.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUINavigator extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Navigator", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        ServerType currentServer = HypixelConst.getTypeLoader().getType();
        boolean inBayou = currentServer == ServerType.SKYBLOCK_BACKWATER_BAYOU;
        boolean unlocked = inBayou || player.getShipState().hasDestination("BACKWATER_BAYOU");

        layout.slots(Layouts.row(0));
        layout.slots(Layouts.row(5));
        layout.slots(Layouts.rectangle(9, 45), (_, _) -> ItemStacks.filler(Material.BLUE_STAINED_GLASS_PANE));
        Components.close(layout, 49);

        if (inBayou) {
            layout.slot(10, ItemStacks.head(
                "d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8", """
                    <b>Fishing Outpost
                    <7>Your base of operations.

                    <e>Click to travel!"""),
                (_, viewCtx) -> ((SkyBlockPlayer) viewCtx.player()).sendTo(ServerType.SKYBLOCK_HUB));
            layout.slot(40, ItemStacks.head(
                "1c0cd33590f64d346d98cdd01606938742e715dda6737353306a44f81c8ba426", """
                    <2>Backwater Bayou
                    <7>A small, marshy outlet in the middle
                    <7>of nowhere. Due to its isolated
                    <7>nature, people frequently come here
                    <7>to dump their trash.

                    <a><l>YOU ARE HERE!"""));
            return;
        }

        layout.slot(10, ItemStacks.head(
            "1c0cd33590f64d346d98cdd01606938742e715dda6737353306a44f81c8ba426", """
                <2>Backwater Bayou
                <7>A small, marshy outlet in the middle
                <7>of nowhere. Due to its isolated
                <7>nature, people frequently come here
                <7>to dump their trash.

                <7>Activities:
                <8> ■ <7>Fish up <2>Junk <7>and trade it with <2>Junker
                    <2>Joel <7>for useful items!
                <8> ■ <7>Apply <9>Rod Parts <7>with <2>Roddy<7>.
                <8> ■ <7>Fish <2>Bayou Sea Creatures<7>.
                <8> ■ <7>Learn about <d>Fishing Hotspots <7>from
                    <d>Hattie<7>.

                {}""",
            unlocked ? Text.of("<e>Click to travel!") : Text.of("<c>Destination Locked")), unlocked
            ? (_, viewCtx) -> ((SkyBlockPlayer) viewCtx.player()).sendTo(ServerType.SKYBLOCK_BACKWATER_BAYOU)
            : (_, viewCtx) -> viewCtx.player().sendMessage("<c>You have not unlocked this destination yet."));
        layout.slot(40, ItemStacks.head(
            "d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8", """
                <b>Fishing Outpost
                <7>Your base of operations.

                <a><l>YOU ARE HERE!"""));
    }
}
