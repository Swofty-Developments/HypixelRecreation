package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;

/**
 * Server-side offhand disabling ({@code CompatConfig.disableOffhand}) - 1.8 clients have no offhand, so this only affects
 * modern ones. Cancels the gameplay F-swap ({@link PlayerSwapItemEvent} - the client only sends it, never predicts the
 * swap, so a cancel is an invisible no-op) and any inventory click targeting the offhand slot (Minestom resyncs the GUI
 * on a cancelled click). Installed once; inert unless the player's config enables it.
 */
public final class CompatOffhand {

    /** Minestom {@code PlayerInventoryUtils.OFFHAND_SLOT}. */
    private static final int OFFHAND_SLOT = 45;

    private CompatOffhand() {}

    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:compat-offhand", EventFilter.PLAYER);
        node.addListener(PlayerSwapItemEvent.class, e -> { if (disabled(e.getPlayer())) e.setCancelled(true); });
        node.addListener(InventoryPreClickEvent.class, e -> { if (disabled(e.getPlayer()) && targetsOffhand(e)) e.setCancelled(true); });
        polyp.install(node);
    }

    private static boolean disabled(Player player) {
        return player instanceof OptimizedPlayer op && op.compat().disableOffhand();
    }

    // TODO(legacy-offhand-emulation) STUB - the inverse of disableOffhand: give a 1.8 client (which has no native offhand)
    // a usable one. Mechanism undecided (per user). Design space, to fill in once chosen:
    //   (a) server-tracked virtual offhand item + a swap trigger 1.8 can actually send - a /offhand command, or a hotbar-slot
    //       convention (1.8 can't send SWAP_ITEM_WITH_OFFHAND), reconciled here;
    //   (b) render the emulated offhand item to the 1.8 client via an equipment slot it shows;
    //   (c) auto-apply the offhand item on the action it backs (totem-on-lethal, shield-on-block) without a real slot.
    // Gate with a new CompatConfig.emulateOffhand knob -> CompatState, enforced from this listener (a legacy-protocol check
    // via ClientInfoTracker, since this only applies to clients without a native offhand). Wire once the mechanism is picked.

    private static boolean targetsOffhand(InventoryPreClickEvent e) {
        Click click = e.getClick();
        if (click instanceof Click.OffhandSwap) return true; // F in any inventory - always swaps with the offhand
        if (!(e.getInventory() instanceof PlayerInventory)) return false; // slot 45 is the offhand only in the player's own inventory
        return switch (click) {
            case Click.HotbarSwap hs -> hs.slot() == OFFHAND_SLOT;
            case Click.Drag d -> d.slots().contains(OFFHAND_SLOT);
            default -> click.slot() == OFFHAND_SLOT;
        };
    }
}
