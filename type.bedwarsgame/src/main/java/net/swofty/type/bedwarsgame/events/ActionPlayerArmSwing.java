package net.swofty.type.bedwarsgame.events;

import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionPlayerArmSwing implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerHandAnimationEvent event) {
        if (!(event.getPlayer() instanceof BedWarsPlayer player)) {
            return;
        }

        BedWarsGame game = player.getGame();
        if (game == null || game.getReplayManager() == null) {
            return;
        }

        if (!game.getReplayManager().isRecording()) {
            return;
        }

        var dispatcher = game.getReplayManager().getEntityLifecycleDispatcher();
        if (dispatcher != null) {
            boolean mainHand = event.getHand().name().equals("MAIN");
            dispatcher.recordArmSwing(player.getEntityId(), mainHand);
        }
    }
}
