package net.swofty.type.bedwarsgame.game.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.event.PlayerPostJoinGameEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.party.PartyManager;

import java.util.Random;
import java.util.UUID;

public class PlayerJoinGameListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onPlayerJoinGame(PlayerPostJoinGameEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.player();
        BedWarsGame game = (BedWarsGame) event.game();

        HypixelPosition waiting = game.getMapEntry().getConfiguration().getLocations().getWaiting();

        if (player.getInstance() == null || player.getInstance().getUuid() != game.getInstance().getUuid()) {
            player.setInstance(game.getInstance(), new Pos(waiting.x(), waiting.y(), waiting.z()));
        }

        player.setEnableRespawnScreen(false);
        player.setFlying(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().setItemStack(8,
            TypeBedWarsGameLoader.getItemHandler().getItem("leave_game").getItemStack());

        HypixelPosition spec = game.getMapEntry().getConfiguration().getLocations().getSpectator();
        player.setRespawnPoint(new Pos(spec.x(), spec.y(), spec.z()));

        player.setGameId(event.game().getGameId());
        String randomLetters = UUID.randomUUID().toString().replace("-", "")
            .substring(0, new Random().nextInt(10) + 4);
        player.setDisplayName("<f><k>{}", randomLetters);

        FullParty party = PartyManager.getPartyFromPlayer(player);

        for (BedWarsPlayer p : game.getPlayers()) {
            boolean shouldObfuscate = true;
            if (party != null) {
                shouldObfuscate = !party.getParticipants().contains(p.getUuid());
            }
            if (shouldObfuscate && p.getUuid().compareTo(player.getUuid()) == 0) {
                shouldObfuscate = false;
            }
            Text name = shouldObfuscate
                ? Text.of("<k>{}", randomLetters)
                : Text.of("{}", player.getColouredName());
            p.sendMessage("{} <e>has joined (<b>{}<e>/<b>{}<e>)!", name, game.getPlayers().size(), game.getMaxPlayers());
        }
    }

}
