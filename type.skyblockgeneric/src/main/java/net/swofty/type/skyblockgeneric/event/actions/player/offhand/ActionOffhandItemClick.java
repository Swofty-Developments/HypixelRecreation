package net.swofty.type.skyblockgeneric.event.actions.player.offhand;

import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionOffhandItemClick implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSwapItemEvent event) {
        ((SkyBlockPlayer) event.getPlayer()).sendMessage("<c>You cannot use your offhand!");
        event.setCancelled(true);
    }
}
