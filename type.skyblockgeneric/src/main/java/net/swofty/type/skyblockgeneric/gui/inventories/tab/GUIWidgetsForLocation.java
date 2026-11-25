package net.swofty.type.skyblockgeneric.gui.inventories.tab;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistLocation;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistSettingsStore;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistWidget;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistWidgetSettings;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class GUIWidgetsForLocation implements StatefulView<GUIWidgetsForLocation.State> {
    public record State(TablistLocation location, int page) {
    }

    private static final int[] SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public State initialState() {
        return new State(TablistLocation.current(), 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((s, c) -> "Widgets in " + s.location().display(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> l, State state, ViewContext ctx) {
        Components.fill(l);
        Components.close(l, 49);
        TabWidgetGuiComponents.preview(l, state.location());
        SkyBlockPlayer p = (SkyBlockPlayer) ctx.player();
        TablistWidgetSettings settings = TablistSettingsStore.get(p);
        List<TablistWidget> widgets = settings.order(state.location());
        int from = state.page() * SLOTS.length;
        l.slot(13, ItemStackCreator.getStack("§aPriority Changer", Material.HOPPER, 1, "§7Change the order of the widgets", "§7shown in " + state.location().display() + ".", "", "§eClick to edit priority!"), (_, c) -> c.push(new GUIWidgetPriority(), new GUIWidgetPriority.State(state.location(), 0, null)));
        for (int i = 0; i < SLOTS.length && from + i < widgets.size(); i++) {
            TablistWidget w = widgets.get(from + i);
            boolean on = settings.enabled(state.location(), w);
            String current = w == TablistWidget.GENERAL_INFO ? "§aALWAYS ENABLED" : on ? "§aENABLED" : "§cDISABLED";
            l.slot(SLOTS[i], ItemStackCreator.getStack((on ? "§a✔ " : "§c✖ ") + w.display, w.material, 1, "§7Currently: " + current, "", "§7" + w.description, "", w == TablistWidget.GENERAL_INFO ? "§eRight-click to change settings!" : on ? "§eLeft-click to disable this widget!" : "§eLeft-click to enable this widget!", "§eRight-click to change settings!"), (click, c) -> {
                var data = TablistSettingsStore.get(p);
                if (click.click() instanceof Click.Right)
                    c.push(new GUIWidgetSettings(), new GUIWidgetSettings.State(state.location(), w));
                else data.toggle(state.location(), w);
                TablistSettingsStore.save(p, data);
                c.session().refresh();
            });
        }
        if (state.page() > 0)
            l.slot(45, ItemStackCreator.getStack("§ePrevious Page", Material.ARROW, 1), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() - 1)));
        if (from + SLOTS.length < widgets.size())
            l.slot(53, ItemStackCreator.getStack("§eNext Page", Material.ARROW, 1), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() + 1)));
        l.slot(48, ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Tablist Widgets"), (_, c) -> c.pop());
        boolean third = settings.thirdColumn(state.location());
        l.slot(50, ItemStackCreator.getStack((third ? "§a" : "§c") + (third ? "✔ Third Column" : "✖ Third Column"), Material.BOOKSHELF, 1, "§7Currently: " + (third ? "§aENABLED" : "§cDISABLED"), "", "§7Uses an additional tablist column for", "§7widgets instead of the player list.", "", "§8Only applies to " + state.location().display() + ".", "", third ? "§eClick to disable!" : "§eClick to enable!"), (_, c) -> {
            var d = TablistSettingsStore.get(p);
            d.toggleThirdColumn(state.location());
            TablistSettingsStore.save(p, d);
            c.session().refresh();
        });
        l.slot(51, ItemStackCreator.getStackHead("§cReset All " + state.location().display() + " Settings", "7c8489c03357d6dabd9f4a3bd8824eb0f2841685ade95ff987ebe15b2e65fad", 1, "§7Reset this mode's widget settings", "§7back to their defaults.", "", "§eClick to reset ALL mode settings!"), (_, c) -> {
            var d = TablistSettingsStore.get(p);
            d.reset(state.location());
            TablistSettingsStore.save(p, d);
            c.session().refresh();
        });
    }
}
