package net.swofty.type.replayviewer.event;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.item.ReplayItemHandler;

public class ReplayItemEvents implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onInventoryClick(InventoryPreClickEvent event) {
        if (!(event.getInventory() instanceof PlayerInventory)) return;
        ReplayItemHandler handler = TypeReplayViewerLoader.getItemHandler();
        if (handler != null && handler.onInventoryClick(event)) {
            event.setCancelled(true);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onItemFinishUse(PlayerFinishItemUseEvent event) {
        ReplayItemHandler handler = TypeReplayViewerLoader.getItemHandler();
        if (handler != null) {
            handler.onItemFinishUse(event);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onItemUseOnBlock(PlayerUseItemOnBlockEvent event) {
        ReplayItemHandler handler = TypeReplayViewerLoader.getItemHandler();
        if (handler != null) {
            handler.onItemUseOnBlock(event);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onItemUse(PlayerUseItemEvent event) {
        ReplayItemHandler handler = TypeReplayViewerLoader.getItemHandler();
        if (handler != null) {
            handler.onItemUse(event);
        }
    }

    @PhasedEvent(node = EventNodes.ITEM, requireDataLoaded = false)
    public void onItemDrop(ItemDropEvent event) {
        ReplayItemHandler handler = TypeReplayViewerLoader.getItemHandler();
        if (handler != null) {
            handler.onItemDrop(event);
        }
    }

    @PhasedEvent(node = EventNodes.ITEM, requireDataLoaded = false)
    public void onHandAnimatiom(PlayerHandAnimationEvent event) {
        ReplayItemHandler handler = TypeReplayViewerLoader.getItemHandler();
        if (handler != null) {
            handler.onHandAnimation(event);
        }
    }

}
