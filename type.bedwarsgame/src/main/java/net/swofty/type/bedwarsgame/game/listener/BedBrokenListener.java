package net.swofty.type.bedwarsgame.game.listener;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.events.custom.BedDestroyedEvent;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.messages.BedWarsMessages;
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

        game.broadcastMessage(Text.of("\n{}\n", BedWarsMessages.bedDestroyed(teamKey, destroyer)));

        for (BedWarsPlayer player : game.getPlayers()) {
            player.playSound(Sound.sound(Key.key("minecraft:entity.wither.death"),
                Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
        }

        game.checkWinConditions();
    }

}
