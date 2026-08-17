package net.swofty.type.skyblockgeneric.gui.inventories.tab;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistLocation;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistSettingsStore;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistStat;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistWidget;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GUIShownStats implements StatefulView<GUIShownStats.State> {
    public record State(TablistLocation location, int page) {
    }

    private static final int[] SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public State initialState() {
        return new State(TablistLocation.current(), 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Shown Stats Setting - Stats Widget", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> l, State s, ViewContext ctx) {
        Components.fill(l);
        Components.close(l, 49);
        TabWidgetGuiComponents.preview(l, s.location());
        l.slot(13, ItemStackCreator.getStack("§aStats Widget Preview", Material.BOOK, 1, "§8⬛§e§lStats:", "§8⬛§f Speed: §f132", "§8⬛§f Strength: §c265", "§8⬛§f Crit Chance: §9117", "§8⬛§f Crit Damage: §9236", "§8⬛§f Attack Speed: §e16"));
        SkyBlockPlayer p = (SkyBlockPlayer) ctx.player();
        var settings = TablistSettingsStore.get(p);
        TablistStat[] stats = TablistStat.values();
        int from = s.page() * SLOTS.length;
        for (int i = 0; i < SLOTS.length && from + i < stats.length; i++) {
            TablistStat stat = stats[from + i];
            boolean on = settings.options(s.location(), TablistWidget.STATS).shownStats().contains(stat.id);
            l.slot(SLOTS[i], ItemStackCreator.getStack((on ? "§a✔ " : "§c✖ ") + stat.display, stat.material, 1, "§7Shows your " + stat.display + " if the widget is", "§7enabled when in " + s.location().display() + ".", "", on ? "§eClick to disable!" : "§eClick to enable!"), (_, c) -> {
                var d = TablistSettingsStore.get(p);
                d.toggleStat(s.location(), stat.id);
                TablistSettingsStore.save(p, d);
                c.session().refresh();
            });
        }
        l.slot(48, ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Stats Widget Settings"), (_, c) -> c.pop());
        l.slot(50, ItemStackCreator.getStack("§cDeselect All", Material.BUCKET, 1, "§7Deselects all §8Shown Stats §7options.", "", "§eClick to deselect all!"), (_, c) -> {
            var d = TablistSettingsStore.get(p);
            d.clearStats(s.location());
            TablistSettingsStore.save(p, d);
            c.session().refresh();
        });
        if (s.page() > 0)
            l.slot(45, ItemStackCreator.getStack("§aPrevious Page", Material.ARROW, 1, "§ePage " + s.page()), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() - 1)));
        if (from + SLOTS.length < stats.length)
            l.slot(53, ItemStackCreator.getStack("§aNext Page", Material.ARROW, 1, "§ePage " + (s.page() + 2)), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() + 1)));
    }
}
