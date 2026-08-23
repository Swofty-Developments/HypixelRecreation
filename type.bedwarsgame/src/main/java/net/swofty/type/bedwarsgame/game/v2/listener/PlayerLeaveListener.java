package net.swofty.type.bedwarsgame.game.v2.listener;

import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.event.PlayerLeaveGameEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

import java.util.Random;

public class PlayerLeaveListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onPlayerLeave(PlayerLeaveGameEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.player();
        player.sendTo(ServerType.BEDWARS_LOBBY);
        BedWarsGame game = (BedWarsGame) event.game();
        if (game == null) return;
        if (!game.getState().isWaiting()) return;

        game.broadcastMessage(Text.of("<k>{} <e>has quit!",
            player.getFakeUuid().toString().substring(0, new Random().nextInt(10) + 4)));
    }
}
