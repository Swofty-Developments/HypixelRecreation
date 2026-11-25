package net.swofty.type.skyblockgeneric.gui.inventories.tab;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistLocation;
import net.swofty.type.skyblockgeneric.tabwidgets.TablistSettingsStore;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class GUIShownEvents implements StatefulView<GUIShownEvents.State> {
    public record State(TablistLocation location, int page) {
    }

    private static final int[] SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public State initialState() {
        return new State(TablistLocation.current(), 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Shown Events Setting - Events Widget", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> l, State s, ViewContext ctx) {
        Components.fill(l);
        Components.close(l, 49);
        TabWidgetGuiComponents.preview(l, s.location());
        l.slot(13, ItemStackCreator.getStack("§aEvents Widget Preview", Material.CAKE, 1, "§8⬛§e§lEvent: §6New Year Celebration", "§8⬛§f Ends In: §e9h"));
        SkyBlockPlayer p = (SkyBlockPlayer) ctx.player();
        List<CalendarEvent> events = CalendarEvent.getAllEvents();
        List<String> defaults = events.stream().map(CalendarEvent::id).toList();
        int from = s.page() * SLOTS.length;
        var settings = TablistSettingsStore.get(p);
        for (int i = 0; i < SLOTS.length && from + i < events.size(); i++) {
            CalendarEvent event = events.get(from + i);
            String name = event.getDisplayName(0).replaceAll("§.", "").replaceFirst("^\\d+(st|nd|rd|th) ", "");
            boolean on = settings.eventShown(s.location(), event.id(), defaults);
            l.slot(SLOTS[i], ItemStackCreator.getStack((on ? "§a✔ " : "§c✖ ") + name, event.representation().material(), 1, "§7Shows " + name + " events when in", "§7" + s.location().display() + ".", "", on ? "§eClick to disable!" : "§eClick to enable!"), (_, c) -> {
                var d = TablistSettingsStore.get(p);
                d.toggleEvent(s.location(), event.id(), defaults);
                TablistSettingsStore.save(p, d);
                c.session().refresh();
            });
        }
        l.slot(48, ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Events Widget Settings"), (_, c) -> c.pop());
        l.slot(50, ItemStackCreator.getStack("§cDeselect All", Material.BUCKET, 1, "§7Deselects all §8Shown Events §7options.", "", "§eClick to deselect all!"), (_, c) -> {
            var d = TablistSettingsStore.get(p);
            d.clearEvents(s.location());
            TablistSettingsStore.save(p, d);
            c.session().refresh();
        });
        if (s.page() > 0)
            l.slot(45, ItemStackCreator.getStack("§aPrevious Page", Material.ARROW, 1, "§ePage " + s.page()), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() - 1)));
        if (from + SLOTS.length < events.size())
            l.slot(53, ItemStackCreator.getStack("§aNext Page", Material.ARROW, 1, "§ePage " + (s.page() + 2)), (_, c) -> c.session(State.class).update(x -> new State(x.location(), x.page() + 1)));
    }
}
