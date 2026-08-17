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
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class GUIWidgetPriority implements StatefulView<GUIWidgetPriority.State> {
    public record State(TablistLocation location, int page, TablistWidget selected) {
    }

    private static final int[] SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public State initialState() {
        return new State(TablistLocation.current(), 0, null);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Widget Priority Changer", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> l, State s, ViewContext ctx) {
        Components.fill(l);
        Components.close(l, 49);
        TabWidgetGuiComponents.preview(l, s.location());
        SkyBlockPlayer p = (SkyBlockPlayer) ctx.player();
        List<TablistWidget> widgets = TablistSettingsStore.get(p).order(s.location());
        int from = s.page() * SLOTS.length;
        l.slot(13, ItemStackCreator.getStack("§aPriority Changer", Material.HOPPER, 1, "§7Select a widget, then shift-click it", "§7to move it through the priority order.", "", "§cGeneral Info is always locked first."));
        for (int i = 0; i < SLOTS.length && from + i < widgets.size(); i++) {
            TablistWidget w = widgets.get(from + i);
            boolean selected = w == s.selected();
            l.slot(SLOTS[i], ItemStackCreator.getStack((w == TablistWidget.GENERAL_INFO ? "§c" : selected ? "§b" : "§7") + "▶ " + w.display + (w == TablistWidget.GENERAL_INFO ? " (LOCKED)" : selected ? " (EDITING)" : ""), w.material, 1, "§7Priority: §e" + (from + i + 1), "", w == TablistWidget.GENERAL_INFO ? "§cThis widget cannot be moved." : "§eClick to select!", "§eShift left/right-click to move!"), (click, c) -> {
                if (w == TablistWidget.GENERAL_INFO) return;
                if (click.click() instanceof Click.LeftShift || click.click() instanceof Click.RightShift) {
                    var d = TablistSettingsStore.get(p);
                    d.move(s.location(), w, click.click() instanceof Click.LeftShift ? -1 : 1);
                    TablistSettingsStore.save(p, d);
                    c.session().refresh();
                } else c.session(State.class).update(x -> new State(x.location(), x.page(), w));
            });
        }
        if (s.page() > 0)
            l.slot(45, ItemStackCreator.getStack("§aPrevious Page", Material.ARROW, 1), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() - 1, x.selected())));
        if (from + SLOTS.length < widgets.size())
            l.slot(53, ItemStackCreator.getStack("§aNext Page", Material.ARROW, 1), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() + 1, x.selected())));
        l.slot(48, ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Widgets in " + s.location().display()), (_, c) -> c.pop());
    }
}
