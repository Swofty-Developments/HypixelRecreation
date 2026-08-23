package net.swofty.type.game.game.team;

import net.kyori.adventure.text.format.TextColor;

import java.util.Collection;
import java.util.UUID;

public interface GameTeam {
    String getId();
    String getName();

    TextColor getColor();

    Collection<UUID> getPlayerIds();

    default int getPlayerCount() {
        return getPlayerIds().size();
    }

    /**
     * @return Whether this team has any players
     */
    default boolean hasPlayers() {
        return !getPlayerIds().isEmpty();
    }

    void addPlayer(UUID playerId);

    void removePlayer(UUID playerId);

    default boolean hasPlayer(UUID playerId) {
        return getPlayerIds().contains(playerId);
    }
}
