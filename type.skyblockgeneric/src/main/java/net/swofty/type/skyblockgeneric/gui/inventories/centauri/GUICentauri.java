package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.inventories.GUICreative;

import java.util.List;

public final class GUICentauri implements View<GUICentauri.State> {
    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Centauri", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (s, c) -> item("§aItem List", Material.STICK,
                "§7Claim featured items related to this", "§7update.", " ", "§eClick to view!"),
                (click, c) -> c.push(new GUICreative(), GUICreative.createInitialState()));
        layout.slot(13, (s, c) -> item("§aCoin Generator", Material.GOLD_BLOCK,
                "§7Generate Coins at the click of a", "§7button!", " ", "§eClick to view!"),
                (click, c) -> c.push(new GUICentauriCoinGenerator(), new GUICentauriCoinGenerator.State()));
        layout.slot(15, (s, c) -> item("§aToy Box", Material.CHEST,
                "§7A variety of useful and cheaty", "§7utilities to make your life easier!", " ", "§eClick to view!"),
                (click, c) -> c.push(new GUICentauriToyBox(), new GUICentauriToyBox.State()));
        Components.close(layout, 31);
    }

    static net.minestom.server.item.ItemStack.Builder item(String name, Material material, String... lore) {
        return ItemStackCreator.getStack(name, material, 1, List.of(lore));
    }

    public record State() {}
}
