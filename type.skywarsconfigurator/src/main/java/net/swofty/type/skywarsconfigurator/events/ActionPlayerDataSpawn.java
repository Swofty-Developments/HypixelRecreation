package net.swofty.type.skywarsconfigurator.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class ActionPlayerDataSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, isAsync = true, phase = EventPhase.POST_SPAWN)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;

        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        player.sendMessage("<6><l>=== SkyWars Configurator ===");
        player.sendMessage("<e>Use <b>/choosemap \\<mapId> <e>to load a map");
        player.sendMessage("<e>Use <b>/swconfig <e>to configure the map");
        player.sendMessage("<7>Anvil worlds will be automatically converted to Polar format");
    }
}
