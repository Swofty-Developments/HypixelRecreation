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

public class GUIRodPartGuide extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Rod Part Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStacks.item(Material.BOOK, """
                <9>Rod Part Guide
                <7>View all <9>Rod Parts <7>that can be applied
                <7>to your upgraded fishing rods."""));
        layout.slot(20, ItemStacks.head(
            "9809753cbab0380c7a1c18925faf9b51e44caadd1e5748542b0f23835f4ef64e", """
                <9>ථ Hooks
                <9>Hooks <7>make you more likely to catch
                <7>certain things.

                <e>Click to view!"""), (_, viewCtx) -> viewCtx.push(new GUIHookGuide()));
        layout.slot(22, ItemStacks.head(
            "9a850a4f721bc150bb72b067e5074c8251058a6b9111691da315b393467c1aa9", """
                <9>ꨃ Lines
                <9>Lines <7>grant you stat bonuses
                <7>everywhere.

                <e>Click to view!"""), (_, viewCtx) -> viewCtx.push(new GUILineGuide()));
        layout.slot(24, ItemStacks.head(
            "d24892a3142d2e130e5feb88b805b83de905489d2ccd1d031b9d7a2922b96500", """
                <9>࿉ Sinkers
                <9>Sinkers <7>add special fishing effects
                <7>to your rod.

                <e>Click to view!"""), (_, viewCtx) -> viewCtx.push(new GUISinkerGuide()));
        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Fishing Rod Parts"""), (_, viewCtx) -> viewCtx.pop());
    }
}
