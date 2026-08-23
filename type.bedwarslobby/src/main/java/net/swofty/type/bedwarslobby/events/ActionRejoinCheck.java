package net.swofty.type.bedwarslobby.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.orchestrator.RejoinGameProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.ScheduleUtility;

public class ActionRejoinCheck implements HypixelEventClass {
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true)
    public void run(PlayerSpawnEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        // Delay slightly to ensure player is fully spawned
        ScheduleUtility.delay(() -> {
            // Check if player has an active game to rejoin
            RejoinGameProtocol.RejoinGameRequest request =
                    new RejoinGameProtocol.RejoinGameRequest(player.getUuid());

            ORCHESTRATOR.handleRequest(request).thenAccept(response -> {
                if (response instanceof RejoinGameProtocol.RejoinGameResponse resp
                        && resp.hasActiveGame()) {
                    // Show rejoin message
                    player.sendMessage("<c>You have a game currently ongoing! <click:run:'/rejoin'>Click here to rejoin.</click>");
                }
            }).exceptionally(throwable -> {
                // Silently fail - don't bother the player with errors on this check
                return null;
            });
        }, 500); // 500ms delay
    }
}
