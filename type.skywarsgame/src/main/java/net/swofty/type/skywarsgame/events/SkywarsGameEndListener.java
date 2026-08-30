package net.swofty.type.skywarsgame.events;

import net.swofty.type.game.game.event.GameTeamWinConditionEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.game.SkywarsTeam;
import net.swofty.type.skywarsgame.game.SkywarsWinCondition;

public final class SkywarsGameEndListener implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onWinCondition(GameTeamWinConditionEvent<SkywarsTeam> event) {
        if (!(event.game() instanceof SkywarsGame game)) return;

        SkywarsWinCondition condition = game.getGameType().isSolo()
                ? SkywarsWinCondition.LAST_PLAYER_STANDING
                : SkywarsWinCondition.LAST_TEAM_STANDING;
        game.endGame(condition, event.team().orElse(null));
    }
}
