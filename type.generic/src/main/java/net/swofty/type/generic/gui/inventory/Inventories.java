package net.swofty.type.generic.gui.inventory;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.text.Text;

/**
 * Markup-first factory for raw Minestom {@link Inventory} instances, for the handful of places that
 * cannot go through {@link HypixelInventoryGUI} or the v2 view system.
 */
public final class Inventories {

    private Inventories() {
    }

    public static Inventory of(InventoryType type, Text title) {
        return new Inventory(type, title.asComponent());
    }

    public static Inventory of(InventoryType type, String titleMarkup, Object... arguments) {
        return of(type, Text.of(titleMarkup, arguments));
    }

    public static void setTitle(Inventory inventory, Text title) {
        inventory.setTitle(title.asComponent());
    }

    public static void setTitle(Inventory inventory, String titleMarkup, Object... arguments) {
        setTitle(inventory, Text.of(titleMarkup, arguments));
    }
}
