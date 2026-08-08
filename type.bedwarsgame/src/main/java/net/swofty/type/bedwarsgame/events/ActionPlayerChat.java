package net.swofty.type.bedwarsgame.events;

import net.kyori.adventure.text.Component;

import net.minestom.server.event.player.PlayerChatEvent;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.chat.StaffChat;
import net.swofty.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import net.swofty.type.generic.data.datapoints.DatapointChatType;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.categories.Rank;

import java.util.ArrayList;
import java.util.List;

public class ActionPlayerChat implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerChatEvent event) {
		if (event.isCancelled()) return;
		final BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
		event.setCancelled(true);

		BedWarsGame game = player.getGame();
		if (game == null) return;

		BedWarsDataHandler bedWarsDataHandler = BedWarsDataHandler.getUser(player);
		if (bedWarsDataHandler == null) {
			player.sendMessage("§cAn error occurred while processing your chat message. Please try again later.");
			return;
		}

		String message = event.getRawMessage();
		Rank rank = player.getRank();

		// Sanitize a message to remove any special Unicode characters
		if (!rank.isStaff())
			message = message.replaceAll("[^\\x00-\\x7F]", "");

		String finalMessage = message;

		DatapointChatType.Chats chatType = player.getChatType().currentChatType;
		if (chatType == DatapointChatType.Chats.STAFF) {
			if (!rank.isStaff()) {
				player.sendMessage("§cUnknown chat type.");
				player.getChatType().switchTo(DatapointChatType.Chats.ALL);
				return;
			}
			StaffChat.sendMessage(player, finalMessage);
			return;
		}

		if (chatType == DatapointChatType.Chats.PARTY) {
			if (!PartyManager.isInParty(player)) {
				player.sendMessage("§cYou are not in a party and were moved to the ALL channel.");
				player.getChatType().switchTo(DatapointChatType.Chats.ALL);
				return;
			}

			PartyManager.sendChat(player, message);
			return;
		}

		if (game.getState().isWaiting()) {
			String textColor = rank.equals(Rank.DEFAULT) ? "§7" : "§f";

			game.getPlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(player.getLegacyRankPrefix() + player.getUsername() + textColor + ": " + finalMessage));
			return;
		}

		if (game.getReplayManager().isRecording()) {
			game.getReplayManager().recordPlayerChat(player, Component.text(finalMessage), false);
		}

		List<BedWarsPlayer> receivers;
		if (game.getGameType() == BedWarsGameType.ONE_EIGHT || game.getGameType() == BedWarsGameType.ONE_BLOCK) {
			receivers = new ArrayList<>(game.getPlayers());
		} else {
			receivers = game.getPlayersOnTeam(player.getTeamKey());
		}

		String levelPrefix = BedWarsPrestigeRenderer.renderBrackets(player) + " ";
		String textColor = rank.equals(Rank.DEFAULT) ? "§7" : "§f";

		receivers.forEach(onlinePlayer -> onlinePlayer.sendMessage(levelPrefix + player.getLegacyRankPrefix() + player.getUsername() + textColor + ": " + finalMessage));
	}
}
