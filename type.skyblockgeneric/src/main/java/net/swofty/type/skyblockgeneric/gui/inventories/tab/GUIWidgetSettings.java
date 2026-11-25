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

public final class GUIWidgetSettings implements StatefulView<GUIWidgetSettings.State> {
    public record State(TablistLocation location, TablistWidget widget) {
    }

    @Override
    public State initialState() {
        return new State(TablistLocation.current(), TablistWidget.GENERAL_INFO);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((s, c) -> s.widget().display + " Widget Settings", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> l, State s, ViewContext ctx) {
        Components.fill(l);
        Components.close(l, 49);
        TabWidgetGuiComponents.preview(l, s.location());
        SkyBlockPlayer p = (SkyBlockPlayer) ctx.player();
        var o = TablistSettingsStore.get(p).options(s.location(), s.widget());
        l.slot(13, ItemStackCreator.getStack("§a" + s.widget().display + " Widget Preview", s.widget().material, 1, TabWidgetGuiComponents.previewLine(s.widget())));
        int slot = 19;
        switch (s.widget()) {
            case GENERAL_INFO ->
                    toggle(l, slot, s, "Show Fairy Souls", "Shows Fairy Soul progress.", "fairy_souls", o.showFairySouls(), p);
            case PROFILE -> {
                toggle(l, slot++, s, "Show SkyBlock Level", "Shows your SkyBlock Level.", "skyblock_level", o.showSkyBlockLevel(), p);
                toggle(l, slot++, s, "Show Bank Balance", "Shows your bank balance.", "bank_balance", o.showBankBalance(), p);
                toggle(l, slot++, s, "Show Next Interest", "Shows the next bank interest timer.", "next_interest", o.showNextInterest(), p);
                toggle(l, slot++, s, "Show Soulflow", "Shows your Soulflow.", "soulflow", o.showSoulflow(), p);
                slot = 25;
            }
            case PET -> {
                toggle(l, slot++, s, "Show Pet Item", "Show equipped pet item.", "pet_item", o.showPetItem(), p);
                toggle(l, slot++, s, "Show Pet XP", "Show your pet's XP.", "pet_xp", o.showPetXp(), p);
                toggle(l, slot++, s, "Show Pet Overflow XP", "Show XP even at maximum level.", "pet_overflow", o.showPetOverflowXp(), p);
                toggle(l, slot++, s, "Show Pet Training Slots", "Shows your pet training slots.", "training", o.showPetTrainingSlots(), p);
                toggle(l, slot++, s, "Hide Empty And Locked Training Slots", "Hides empty or locked training slots.", "hide_training", o.hideEmptyTrainingSlots(), p);
                toggle(l, slot++, s, "Show Pet Sitter", "Show the pet at the pet sitter.", "pet_sitter", o.showPetSitter(), p);
                slot = 25;
            }
            case ELECTION -> {
                mayorAmount(l, slot++, s, o.mayorAmount(), p);
                toggle(l, slot++, s, "Show Amount Of Perks", "Shows the amount of perks each mayor has.", "mayor_perks", o.showMayorPerks(), p);
                toggle(l, slot++, s, "Show Votes Bar", "Shows each mayor's votes as a bar.", "votes_bar", o.showVotesBar(), p);
            }
            case EVENTS -> {
                l.slot(slot++, ItemStackCreator.getStack("§aShown Events", Material.COMPARATOR, 1, "§7Choose which calendar events are shown.", "", "§eClick to edit!"), (_, c) -> c.push(new GUIShownEvents(), new GUIShownEvents.State(s.location(), 0)));
            }
            case STATS -> {
                l.slot(slot++, ItemStackCreator.getStack("§aShown Stats", Material.COMPARATOR, 1, "§7Choose which player stats are shown.", "", "§eClick to edit!"), (_, c) -> c.push(new GUIShownStats(), new GUIShownStats.State(s.location(), 0)));
            }
            default -> {
            }
        }
        if (s.widget() != TablistWidget.GENERAL_INFO) {
            toggle(l, slot++, s, "Spacing", "Adds an empty line after this widget and before headers.", "spacing", o.spacing(), p);
            toggle(l, slot, s, "Wrapping", "Allow the content to wrap to a new column.", "wrapping", o.wrapping(), p);
        } else toggle(l, 20, s, "Spacing", "Adds an empty line after this widget.", "spacing", o.spacing(), p);
        l.slot(48, ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Widgets in " + s.location().display()), (_, c) -> c.pop());
        if (s.location() != TablistLocation.THE_RIFT)
            l.slot(50, ItemStackCreator.getStack("§aApply Settings To Other Modes", Material.STONE_BUTTON, 1, "§7Apply these settings to all other", "§7modes except the Rift.", "", "§eClick to apply settings!"), (_, c) -> {
                var d = TablistSettingsStore.get(p);
                d.apply(s.location(), s.widget());
                TablistSettingsStore.save(p, d);
                c.session().refresh();
            });
    }

    private void mayorAmount(ViewLayout<State> l, int slot, State s, int amount, SkyBlockPlayer p) {
        String[] lore = {"§7Select how many mayors are shown.", "", "   §75 Mayors", "   §74 Mayors", "   §73 Mayors", "   §72 Mayors", "   §71 Mayor", "", "§8Changing this widget's setting will", "§8only apply to this mode.", "", "§eLeft/Right-click to change!"};
        lore[7 - amount] = "§a➠ §7" + amount + (amount == 1 ? " Mayor" : " Mayors");
        l.slot(slot, ItemStackCreator.getStack("§aMayor Amount", Material.LADDER, 1, lore), (click, c) -> {
            var d = TablistSettingsStore.get(p);
            d.changeMayorAmount(s.location(), click.click() instanceof Click.Right ? -1 : 1);
            TablistSettingsStore.save(p, d);
            c.session().refresh();
        });
    }

    private void toggle(ViewLayout<State> l, int slot, State s, String name, String desc, String key, boolean on, SkyBlockPlayer p) {
        l.slot(slot, ItemStackCreator.getStack((on ? "§a✔ " : "§c✖ ") + name, on ? Material.LIME_DYE : Material.GRAY_DYE, 1, "§7" + desc, "", "§8Changing this widget's setting will", "§8only apply to being in " + s.location().display() + ".", "", on ? "§eClick to disable!" : "§eClick to enable!"), (_, c) -> {
            var d = TablistSettingsStore.get(p);
            d.toggleOption(s.location(), s.widget(), key);
            TablistSettingsStore.save(p, d);
            c.session().refresh();
        });
    }
}
