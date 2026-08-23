package net.swofty.type.generic.gui.v2.context;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import net.swofty.type.generic.user.HypixelPlayer;

public record RawClickContext<S>(
        InventoryPreClickEvent event,
        boolean inViewInventory,
        HypixelPlayer player,
        S state
) {

    public Click click() {
        return event.getClick();
    }

    public int slot() {
        return event.getSlot();
    }

    public void cancel() {
        event.setCancelled(true);
    }
}
