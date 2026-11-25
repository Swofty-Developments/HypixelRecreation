package net.swofty.type.skywarsgame.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skywarsgame.SkywarsGameScoreboard;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ActionPlayerDisconnect implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);

        if (game != null) {
            game.disconnect(player);
        }
        SkywarsGameScoreboard.removeCache(player);
    }
}
