package net.swofty.type.game.game;

import net.minestom.server.instance.Instance;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Game<P extends GameParticipant> {

    String getGameId();

    GameState getState();

    Instance getInstance();

    Collection<P> getPlayers();

    /**
     * @return The maximum number of players this game can hold
     */
    int getMaxPlayers();

    /**
     * @return The minimum number of players required to start
     */
    int getMinPlayers();

    /**
     * @return Number of available slots
     */
    default int getAvailableSlots() {
        return Math.max(0, getMaxPlayers() - getPlayers().size());
    }

    /**
     * @return Whether new players can join this game
     */
    default boolean canAcceptPlayers() {
        return getState().isWaiting() && getAvailableSlots() > 0;
    }

    /**
     * @return Whether the game has minimum players to start
     */
    default boolean hasMinimumPlayers() {
        return getPlayers().size() >= getMinPlayers();
    }

    /**
     * Attempts to add a player to the game.
     * @param player The player to add
     * @return Result of the join attempt
     */
    JoinResult join(P player);

    /**
     * Removes a player from the game.
     * @param player The player to remove
     */
    void leave(P player);

    /**
     * Gets a player by their UUID.
     * @param uuid The player's UUID
     * @return The player, if present
     */
    Optional<P> getPlayer(UUID uuid);

    /**
     * Starts the game. Called when countdown finishes.
     */
    void start();

    /**
     * Ends the game.
     */
    void end();

    /**
     * Cleans up resources when the game is disposed.
     */
    void dispose();

    /**
     * @return The countdown controller for this game
     */
    GameCountdownController getCountdown();

    sealed interface JoinResult {
        record Success() implements JoinResult {}
        record Denied(String reason) implements JoinResult {}
        record Redirect(String targetServer) implements JoinResult {}
    }
}
