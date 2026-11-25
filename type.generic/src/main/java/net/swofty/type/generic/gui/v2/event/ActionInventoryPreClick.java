package net.swofty.type.generic.gui.v2.event;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.user.HypixelPlayer;

public class ActionInventoryPreClick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.INVENTORY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onActionPlayerInventoryPreClick(InventoryPreClickEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ViewNavigator.find(player).ifPresent(navigator -> {
            if (navigator.getCurrentSession() == null) {
                return;
            }
            navigator.getCurrentSession().onPreClickEvent(event);
        });
    }

}