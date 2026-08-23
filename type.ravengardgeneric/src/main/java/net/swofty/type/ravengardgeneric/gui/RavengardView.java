package net.swofty.type.ravengardgeneric.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.TooltipDisplay;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.Set;
import java.util.function.BiConsumer;
import net.swofty.commons.text.Text;

/**
 * Base of every Ravengard menu. Pages declare their content; the chrome does the rest, replaying
 * exactly what the captured menus carry: the title structure, the invisible stick every live menu
 * keeps in its first slot, footprint-aware button placement and the shared back button.
 */
public abstract class RavengardView extends StatelessView {
    private static final int PANEL_GLYPH = 0xF00F;
    private static final int PANEL_ICON = 0xE237;
    // captured titles use space(-4088) after the panel glyph; anything else shifts the
    // icon and title text off the visible panel
    private static final int PANEL_LEAD_SPACE = -4088;
    private static final int PANEL_TRAIL_SPACE = -13;
    private static final TextColor TITLE_COLOR = TextColor.color(0xFEFD1A);
    protected static final int SLOT_BACK = 45;

    protected abstract String title();

    protected abstract void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx);

    protected InventoryType inventoryType() {
        return InventoryType.CHEST_6_ROW;
    }

    /** Glyph shown in the panel header; each menu has its own. */
    protected int panelIcon() {
        return PANEL_ICON;
    }

    /**
     * Menus that stage items from the player's own inventory fill every free slot with sticks,
     * as the captured sell screen does, so the client cannot predict item moves into them.
     */
    protected boolean stagesFromInventory() {
        return false;
    }

    @Override
    public final void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        content(layout, state, ctx);
        if (!usesChrome()) {
            return;
        }

        // every captured menu keeps an invisible stick in its first free slot
        java.util.Set<Integer> occupied = layout.occupiedSlots();
        int size = inventoryType().getSize();
        if (stagesFromInventory()) {
            for (int slot = 0; slot < size; slot++) {
                if (!occupied.contains(slot)) {
                    layout.slot(slot, chromeStick());
                }
            }
            return;
        }
        for (int slot = 0; slot < size; slot++) {
            if (!occupied.contains(slot)) {
                layout.slot(slot, chromeStick());
                return;
            }
        }
    }

    /** Menus without a captured panel render as the pack's plain themed chest. */
    protected boolean usesChrome() {
        return true;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(usesChrome() ? chrome(title())
                : Text.literal(title()), inventoryType());
    }

    /** The invisible stick every captured menu holds in slot zero. */
    private static ItemStack.Builder chromeStick() {
        return ItemStack.builder(Material.STICK)
                .set(DataComponents.ITEM_MODEL, "minecraft:empty")
                .set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, Set.of()));
    }

    /**
     * Places a button at its origin slot and repeats it across the rest of its footprint, so a
     * multi-slot sprite reads as one button and shares a single hover glow.
     */
    protected void place(ViewLayout<DefaultState> layout, int originSlot, RavengardItems.Builder button) {
        button.origin(originSlot);
        for (int slot : button.coveredSlots(originSlot)) {
            layout.slot(slot, button.toBuilder());
        }
    }

    /** Same as {@link #place} with a click handler on every covered slot. */
    protected void interactive(ViewLayout<DefaultState> layout, int originSlot,
                               RavengardItems.Builder button,
                               BiConsumer<ClickContext<DefaultState>, ViewContext> onClick) {
        button.origin(originSlot);
        for (int slot : button.coveredSlots(originSlot)) {
            layout.slot(slot, button.toBuilder(), onClick);
        }
    }

    /** The standard back button, verbatim from the captures, wired to the view stack. */
    protected void backButton(ViewLayout<DefaultState> layout) {
        interactive(layout, SLOT_BACK, RavengardItems.button(RavengardButton.BACK)
                        .label("Go Back")
                        .lore("<7>Return to the previous menu.")
                        .blankLine()
                        .lore("<e>Click to go back!"),
                (click, viewContext) -> viewContext.backOrClose());
    }

    protected Text chrome(String title) {
        return Text.of("{}", Component.text(RavengardFont.glyph(PANEL_GLYPH))
                .font(RavengardFont.RAVENGARD)
                .append(Component.text(RavengardFont.space(PANEL_LEAD_SPACE)))
                .append(Component.text(RavengardFont.glyph(panelIcon())).color(NamedTextColor.WHITE))
                .append(Component.text(RavengardFont.space(PANEL_TRAIL_SPACE)))
                .append(Component.text(title).font(RavengardFont.HALF))
                .append(Component.text(title).color(TITLE_COLOR).font(RavengardFont.DEFAULT)));
    }
}
