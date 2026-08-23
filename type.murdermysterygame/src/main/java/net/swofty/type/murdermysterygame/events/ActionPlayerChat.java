package net.swofty.type.murdermysterygame.events;

import net.minestom.server.event.player.PlayerChatEvent;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.chat.StaffChat;
import net.swofty.type.generic.data.datapoints.DatapointChatType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.game.GameStatus;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

public class ActionPlayerChat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        MurderMysteryPlayer player = (MurderMysteryPlayer) event.getPlayer();
        event.setCancelled(true);

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;

        String message = event.getRawMessage();
        Rank rank = player.getRank();

        // Sanitize message
        if (!rank.isStaff()) {
            message = message.replaceAll("[^\\x00-\\x7F]", "");
        }

        String finalMessage = message;

        DatapointChatType.Chats chatType = player.getChatType().currentChatType;
        if (chatType == DatapointChatType.Chats.STAFF) {
            if (!rank.isStaff()) {
                player.sendMessage("<c>Unknown chat type.");
                player.getChatType().switchTo(DatapointChatType.Chats.ALL);
                return;
            }
            StaffChat.sendMessage(player, finalMessage);
            return;
        }

        // Dead players can only talk to other dead players
        if (player.isEliminated() && game.getGameStatus() == GameStatus.IN_PROGRESS) {
            for (MurderMysteryPlayer gamePlayer : game.getPlayers()) {
                if (gamePlayer.isEliminated()) {
                    gamePlayer.sendMessage("<7>[DEAD] <f>{}: <7>{}", player.getUsername(), finalMessage);
                }
            }
            return;
        }

        // Normal chat
        for (MurderMysteryPlayer gamePlayer : game.getPlayers()) {
            gamePlayer.sendMessage("{}<f>{}: {}",
                    player.getRankPrefix(),
                    player.getUsername(),
                    finalMessage);
        }
    }
}
