package net.swofty.type.ravengardgeneric.event.actions.player;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.ravengardgeneric.gui.GUIRavengardMenu;
import net.swofty.type.ravengardgeneric.item.RavengardMenuItem;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

public class ActionPlayerMenuItem implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.SPAWN)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!(event.getPlayer() instanceof RavengardPlayer player)) {
            return;
        }
        if (net.swofty.type.generic.HypixelConst.getTypeLoader().getType()
                == net.swofty.commons.ServerType.RAVENGARD_DUNGEON) {
            return;
        }
        RavengardMenuItem.give(player);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onUse(PlayerUseItemEvent event) {
        if (!(event.getPlayer() instanceof RavengardPlayer player)) {
            return;
        }
        if (!RavengardMenuItem.isMenuItem(event.getItemStack())) {
            return;
        }

        event.setCancelled(true);

        if (player.getRavengardClass() == null) {
            player.sendMessage("<c>You must select a class to use this menu!");
            return;
        }

        ViewNavigator.get(player).push(new GUIRavengardMenu());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onClick(InventoryPreClickEvent event) {
        // display-only items in the player's own inventory: the accessory slot panes and the
        // ability buttons living in the crafting grid. scoped to the player inventory so the
        // same models stay clickable as menu buttons.
        if (event.getInventory() instanceof net.minestom.server.inventory.PlayerInventory) {
            String model = event.getClickedItem().get(net.minestom.server.component.DataComponents.ITEM_MODEL);
            if (model != null && (model.startsWith("hypixel_ravengard:gui/sprites/container/slot/")
                    || model.startsWith("hypixel_ravengard:ui/menu/button/"))) {
                event.setCancelled(true);
                return;
            }
        }
        if (RavengardMenuItem.isMenuItem(event.getClickedItem()) || event.getSlot() == RavengardMenuItem.SLOT) {
            event.setCancelled(true);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onDrop(ItemDropEvent event) {
        if (RavengardMenuItem.isMenuItem(event.getItemStack())) {
            event.setCancelled(true);
        }
    }
}
