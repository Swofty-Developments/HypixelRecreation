package net.swofty.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.List;

/**
 * Shared chrome for the 3-page sea creature guide. Subclasses only need to
 * declare which page they are and which entries to show; layout, navigation,
 * sort/filter/category buttons all live here.
 *
 * Entries are modelled as a sealed {@link Entry} hierarchy: {@code Head} for
 * player heads (the common case — most creatures use one) and {@code Block}
 * for the handful that render as a regular item (dragon eggs, etc.). Each
 * entry carries one markup text block whose first line is the item name.
 */
public abstract sealed class SeaCreatureGuidePage extends StatelessView
    permits GUI13SeaCreatureGuide, GUI23SeaCreatureGuide, GUI33SeaCreatureGuide {

    private static final int TOTAL_PAGES = 3;

    protected abstract int pageNumber();

    protected abstract List<Entry> entries();

    @Override
    public final ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(
            Text.of("({}/{}) Sea Creature Guide", pageNumber(), TOTAL_PAGES),
            InventoryType.CHEST_6_ROW
        );
    }

    @Override
    public final void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStacks.item(Material.BOOK, """
                <a>Sea Creature Guide
                <7>Your guide to the creatures of the
                <7>deep! Can also be accessed with
                <a>/scg<7>!

                <7>Beware, Sea Creatures spawn much
                <7>less often on your private island.

                <7>Your Fishing: <a>Level XVIII"""));

        for (Entry entry : entries()) {
            layout.slot(entry.slot(), entry.render());
        }

        if (pageNumber() > 1) {
            layout.slot(45, ItemStacks.item(Material.ARROW, """
                    <a>Previous Page
                    <e>Page {}""", pageNumber() - 1));
        }

        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Fishing Skill"""));

        layout.slot(50, ItemStacks.item(Material.HOPPER, """
                <a>Sort

                <b>▶ Fishing Level Req
                <7>  Alphabetical
                <7>  Mob Level
                <7>  Killed Most
                <7>  Ascending Rarity
                <7>  Descending Rarity

                <b>Right-click to go backwards!
                <e>Click to switch!"""));

        layout.slot(51, ItemStacks.item(Material.ENDER_EYE, """
                <a>Filter

                <f>▶ All Sea Creatures
                <7>  Has Level Requirement
                <7>  Has Never Killed

                <b>Right-click to go backwards!
                <e>Click to switch!"""));

        layout.slot(52, ItemStacks.item(Material.CAULDRON, """
                <a>Category

                <a>▶ Any Category
                <7>  Water
                <7>  Lava
                <7>  Winter
                <7>  Spooky
                <7>  Shark
                <7>  Oasis
                <7>  Bayou
                <7>  Hotspot
                <7>  Galatea

                <b>Right-click to go backwards!
                <e>Click to switch!"""));

        if (pageNumber() < TOTAL_PAGES) {
            layout.slot(53, ItemStacks.item(Material.ARROW, """
                    <a>Next Page
                    <e>Page {}""", pageNumber() + 1));
        }
    }

    protected static Entry head(int slot, String texture, String textBlock) {
        return new Entry.Head(slot, texture, textBlock);
    }

    protected static Entry block(int slot, Material material, String textBlock) {
        return new Entry.Block(slot, material, textBlock);
    }

    public sealed interface Entry {
        int slot();

        ItemStack.Builder render();

        record Head(int slot, String texture, String textBlock) implements Entry {
            @Override
            public ItemStack.Builder render() {
                return ItemStacks.head(texture, textBlock);
            }
        }

        record Block(int slot, Material material, String textBlock) implements Entry {
            @Override
            public ItemStack.Builder render() {
                return ItemStacks.item(material, textBlock);
            }
        }
    }
}
