package net.swofty.type.garden.events.traditional;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.SharedInstance;
import net.swofty.type.garden.user.SkyBlockGarden;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public class ActionPlayerTeleport implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.POST_SPAWN)
    public void run(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!event.isFirstSpawn()) {
            return;
        }
        UUID profileId = player.getSkyblockDataHandler().getCurrentProfileId();
        SkyBlockGarden garden = SkyBlockGarden.getGarden(profileId);
        if (garden == null) {
            garden = new SkyBlockGarden(profileId);
        }
        player.setSkyBlockGarden(garden);

        SharedInstance instance = garden.getSharedInstance().join();
        player.setInstance(instance, player.getRespawnPoint());
        player.teleport(player.getRespawnPoint());
    }
}
