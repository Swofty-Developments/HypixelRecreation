package net.swofty.type.generic.gui.v2.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

public class ActionInventoryOpen implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.INVENTORY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onPlayerInventoryOpen(InventoryOpenEvent event) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            HypixelPlayer player = (HypixelPlayer) event.getPlayer();
            ViewNavigator.find(player).ifPresent(navigator -> {
                if (navigator.getCurrentSession() == null) {
                    // could also be that this is a legacy view. However, why is ViewNavigator lingering then?
                    Logger.warn("Current session is null for player {} when opening inventory, this should not happen.", player.getUsername());
                    return;
                }
                navigator.getCurrentSession().onOpenEvent(event);
            });
        });

    }

}