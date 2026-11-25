package net.swofty.type.skyblockgeneric.gui.inventories.tab;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistLocation;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistSettingsStore;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GUITablistWidgets extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Tablist Widgets", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> l, DefaultState s, ViewContext ctx) {
        Components.fill(l);
        Components.close(l, 49);
        l.slot(4, ItemStackCreator.getStack("§aTablist Widgets Information", Material.REDSTONE_TORCH, 1, "§7Tablist Widgets appear in the player", "§7list.", "", "§7You can choose which ones you want", "§7to see for each island.", "", "§7Some widgets have their own settings", "§7that you can adjust to your liking.", "", "§eClick to edit current settings!"), (_, c) -> c.push(new GUIWidgetsForLocation(), new GUIWidgetsForLocation.State(TablistLocation.current(), 0)));
        TablistLocation[] values = TablistLocation.values();
        for (int i = 0; i < values.length; i++) {
            TablistLocation location = values[i];
            l.slot(SLOTS[i], ItemStackCreator.getStackHead("§aWidgets in " + location.display(), location.texture(), 1, "§7Manage the Tablist Widgets you see", "§7in this location.", "", "§eClick to edit!"), (_, c) -> c.push(new GUIWidgetsForLocation(), new GUIWidgetsForLocation.State(location, 0)));
        }
        l.slot(48, ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Settings"), (_, c) -> c.backOrClose());
        l.slot(53, ItemStackCreator.getStack("§cReset All Settings", Material.PLAYER_HEAD, 1, "§7Reset all Tablist Widget settings", "§7back to their defaults.", "", "§eClick to reset ALL settings!"), (_, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            var settings = TablistSettingsStore.get(p);
            settings.resetAll();
            TablistSettingsStore.save(p, settings);
            c.session().refresh();
        });
    }
}
