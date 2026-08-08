package net.swofty.type.bedwarsgame.game.v2.listener;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.events.custom.BedDestroyedEvent;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.replay.BedWarsReplayMessages;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class BedBrokenListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onBedDestroyed(BedDestroyedEvent event) {
        BedWarsGame game = (BedWarsGame) event.game();
        BedWarsMapsConfig.TeamKey teamKey = event.teamKey();
        if (game == null) return;

        BedWarsPlayer destroyer = event.destroyer();
        BedWarsMapsConfig.TeamKey destroyerTeamKey = destroyer.getTeamKey();
        if (destroyerTeamKey == null) {
            throw new IllegalStateException("Destroyer team key is null for player " + destroyer.getUsername());
        }

        game.broadcastMessage(Component.newline().append(BedWarsReplayMessages.bedDestroyed(teamKey, destroyer)).appendNewline());

        for (BedWarsPlayer player : game.getPlayers()) {
            player.playSound(Sound.sound(Key.key("minecraft:entity.wither.death"),
                Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
        }

        game.checkWinConditions();
    }

}
