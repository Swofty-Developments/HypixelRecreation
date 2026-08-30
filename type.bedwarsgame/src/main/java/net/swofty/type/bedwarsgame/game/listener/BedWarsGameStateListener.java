package net.swofty.type.bedwarsgame.game.listener;

import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.game.game.event.GameStateChangeEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import org.tinylog.Logger;

// todo: move functionality here
public class BedWarsGameStateListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onGameStateChange(GameStateChangeEvent event) {
        BedWarsGame game = (BedWarsGame) event.game();
        if (game == null) return;

        Logger.info("BedWars game {} state changed: {} -> {}",
                event.game().getGameId(), event.previousState(), event.newState());

        if (event.isCountdownStart()) {
            onCountdownStarted(game);
        } else if (event.isGameStart()) {
            onGameStarted(game);
        } else if (event.isGameEnd()) {
            onGameEnded(game);
        }
    }

    private void onCountdownStarted(BedWarsGame game) {
        // Could update scoreboard, play sounds, etc.
    }

    private void onGameStarted(BedWarsGame game) {
        Logger.info("BedWars game {} has officially started with {} players",
            game.getGameId(), game.getPlayers().size());
    }

    private void onGameEnded(BedWarsGame game) {
        Logger.info("BedWars game {} has ended", game.getGameId());
    }
}
