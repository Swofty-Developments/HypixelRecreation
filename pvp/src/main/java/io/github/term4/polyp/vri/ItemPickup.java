package io.github.term4.polyp.vri;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.world.WorldPolicy;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PickupItemEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Player item pickup - Minestom fires {@link PickupItemEvent} + the animation but adds nothing to the inventory.
 * Adds the stack (cancels when full, so the item stays), vanilla pop sound; non-player pickups untouched.
 */
public final class ItemPickup {

    private ItemPickup() {}

    public static void install(EventNode<@NotNull Event> node, Vri vri) {
        node.addListener(PickupItemEvent.class, e -> {
            if (!(e.getLivingEntity() instanceof Player player)) return;
            if (!vri.configFor(player).itemPickup) return;
            // vanilla gates the collision sweep on health > 0 && !spectator (1.8 EntityPlayer.onUpdate); Minestom's scan doesn't
            if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR) {
                e.setCancelled(true);
                return;
            }
            if (!WorldPolicy.canAffect(player, e.getItemEntity())) { // Minestom's pickup scan is instance-wide
                e.setCancelled(true);
                return;
            }
            if (!player.getInventory().addItemStack(e.getItemStack())) {
                e.setCancelled(true);
                return;
            }
            if (player.getInstance() == null) return;
            // pitch/volume live in the ITEM_PICKUP effect, resolved from the item's scope + world
            Polyp polyp = Polyp.getInstance();
            if (polyp.isInitialized()) Fx.play(polyp.services(), Fx.ITEM_PICKUP, FxContext.of(e.getItemEntity()));
        });
    }
}
