package net.swofty.type.lobby;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for validating whether a player can join a game queue.
 * Handles common validation checks like existing searches and party status.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GameQueueValidator {

    public record QueueRequirements(String gameTypeDisplayName, String modeDisplayName, int maxPartySize) {
        public QueueRequirements {
            if (maxPartySize < 1) {
                throw new IllegalArgumentException("maxPartySize must be at least 1");
            }
        }
    }

    /**
     * Validates whether a player can join a game queue.
     * Checks if the player is already searching or if they're in a party
     * and not the leader.
     *
     * @param player The player to validate
     * @return true if the player can queue for a game, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canPlayerQueue(HypixelPlayer player) {
        return canPlayerQueue(player, null);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canPlayerQueue(HypixelPlayer player, @Nullable QueueRequirements requirements) {
        if (LobbyOrchestratorConnector.isSearching(player.getUuid())) {
            player.sendMessage("<c>You are already searching for a game!");
            return false;
        }

        @Nullable FullParty party = null;
        if (PartyManager.isInParty(player)) {
            party = PartyManager.getPartyFromPlayer(player);
            if (party == null) {
                player.sendMessage("<c>Failed to read your party state. Please try again.");
                return false;
            }
            if (!party.getLeader().getUuid().equals(player.getUuid())) {
                player.sendMessage("<c>You are in a party! Ask your leader to start the game, or /p leave");
                return false;
            }
        }

        if (requirements == null || party == null) {
            return true;
        }

        for (FullParty.Member member : party.getMembers()) {
            if (!member.isJoined()) {
                player.sendMessage(Text.key("mcp_miscellaneous.364",
                        requirements.gameTypeDisplayName(),
                        requirements.modeDisplayName(),
                        HypixelPlayer.getDisplayName(member.getUuid())));
                return false;
            }
        }

        int partySize = party.getMembers().size();
        if (partySize > requirements.maxPartySize()) {
            if (requirements.maxPartySize() == 1) {
                player.sendMessage(Text.key("mcp_miscellaneous.468",
                        requirements.gameTypeDisplayName(),
                        requirements.modeDisplayName()));
            } else {
                player.sendMessage(Text.key("mcp_miscellaneous.469",
                        requirements.gameTypeDisplayName(),
                        requirements.modeDisplayName(),
                        requirements.maxPartySize()));
            }
            return false;
        }

        return true;
    }
}
