package net.swofty.type.game.game;

import lombok.Getter;
import net.minestom.server.instance.Instance;
import net.swofty.type.game.game.event.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class AbstractGame<P extends GameParticipant> implements Game<P> {
    protected final String gameId;
    protected final Instance instance;
    protected final Map<UUID, P> players = new ConcurrentHashMap<>();
    protected final Map<UUID, DisconnectedPlayerData> disconnectedPlayers = new ConcurrentHashMap<>();

    @Getter
    protected final Consumer<Object> eventDispatcher;

    protected volatile GameState state = GameState.WAITING;
    protected DefaultGameCountdown countdown;

    /**
     * Stores minimal data about disconnected players for rejoin.
     */
    public record DisconnectedPlayerData(
        UUID uuid,
        String username,
        long disconnectTime,
        Map<String, Object> savedData
    ) {
    }

    protected AbstractGame(Instance instance, Consumer<Object> eventDispatcher) {
        this.gameId = UUID.randomUUID().toString();
        this.instance = instance;
        this.eventDispatcher = eventDispatcher;
        this.countdown = createCountdown();
    }

    /**
     * Creates the countdown controller for this game.
     * Override to customize countdown behavior.
     */
    protected DefaultGameCountdown createCountdown() {
        return new DefaultGameCountdown(
                this,
            getCountdownConfig(),
            eventDispatcher,
            this::onCountdownComplete,
            this::onCountdownCancelled,
            this::hasMinimumPlayers
        );
    }

    /**
     * @return The countdown configuration for this game type
     */
    protected CountdownConfig getCountdownConfig() {
        return CountdownConfig.DEFAULT;
    }

    /**
     * Called when countdown completes. Triggers game start.
     */
    protected void onCountdownComplete() {
        start();
    }

    protected void onCountdownCancelled() {
        if (state == GameState.COUNTDOWN) {
            setState(GameState.WAITING);
        }
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public Collection<P> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    @Override
    public Optional<P> getPlayer(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public GameCountdownController getCountdown() {
        return countdown;
    }

    @Override
    public JoinResult join(P player) {
        if (players.containsKey(player.getUuid())) {
            return new JoinResult.Denied("Player is already in this game");
        }

        PlayerPreJoinGameEvent event = new PlayerPreJoinGameEvent(this, player.getServerPlayer());
        eventDispatcher.accept(event);

        if (event.isCancelled()) {
            return new JoinResult.Denied(event.getCancelReason() != null ? event.getCancelReason() : "Join cancelled");
        }

        if (!state.isWaiting()) {
            return new JoinResult.Denied("Game already in progress");
        }

        if (getAvailableSlots() <= 0) {
            return new JoinResult.Denied("Game is full");
        }

        players.put(player.getUuid(), player);
        player.setGameId(gameId);

        eventDispatcher.accept(new PlayerPostJoinGameEvent(
                this,
            player.getServerPlayer(),
            players.size(),
            getMaxPlayers()
        ));

        if (hasMinimumPlayers() && !countdown.isActive()) {
            setState(GameState.COUNTDOWN);
            countdown.start();
        }

        return new JoinResult.Success();
    }

    @Override
    public void leave(P player) {
        if (!players.containsKey(player.getUuid())) return;

        players.remove(player.getUuid());
        player.setGameId(null);

        if (this instanceof AbstractTeamGame<P, ?> teamGame) {
            teamGame.removeFromTeam(player);
        }

        eventDispatcher.accept(new PlayerLeaveGameEvent(
                this,
            player.getServerPlayer(),
            PlayerLeaveGameEvent.LeaveReason.VOLUNTARY
        ));

        if (state == GameState.IN_PROGRESS) {
            checkWinConditions();
        }
    }

    /**
     * Handles player disconnection during an active game.
     */
    public void handleDisconnect(P player) {
        if (state != GameState.IN_PROGRESS) {
            leave(player);
            return;
        }

        UUID uuid = player.getUuid();
        if (!players.containsKey(uuid)) return;
        boolean canRejoin = canPlayerRejoin(player);

        if (canRejoin) {
            disconnectedPlayers.put(uuid, new DisconnectedPlayerData(
                uuid,
                player.getServerPlayer().getUsername(),
                System.currentTimeMillis(),
                savePlayerData(player)
            ));
        }

        players.remove(uuid);

        eventDispatcher.accept(new PlayerDisconnectGameEvent(
                this,
            player.getServerPlayer(),
            canRejoin
        ));

        checkWinConditions();
    }

    public boolean handleRejoin(P player) {
        if (state != GameState.IN_PROGRESS) {
            return false;
        }

        UUID uuid = player.getUuid();
        if (players.containsKey(uuid)) {
            return false;
        }

        DisconnectedPlayerData data = disconnectedPlayers.remove(uuid);
        if (data == null) {
            return false;
        }

        players.put(player.getUuid(), player);
        player.setGameId(gameId);

        eventDispatcher.accept(new PlayerRejoinGameEvent(
                this,
            player.getServerPlayer(),
            data
        ));

        return true;
    }

    /**
     * Checks if a disconnected player can rejoin.
     */
    public boolean hasDisconnectedPlayer(UUID uuid) {
        return disconnectedPlayers.containsKey(uuid);
    }

    public List<UUID> getDisconnectedPlayerUuids() {
        return List.copyOf(disconnectedPlayers.keySet());
    }

    @Override
    public void start() {
        if (!state.isWaiting()) return;
        if (countdown.isActive()) {
            countdown.terminate();
        }

        setState(GameState.IN_PROGRESS);

        eventDispatcher.accept(new GameStartEvent(this));
    }

    @Override
    public void end() {
        if (state == GameState.ENDING || state == GameState.TERMINATED) return;
        countdown.terminate();

        setState(GameState.ENDING);
    }

    @Override
    public void dispose() {
        if (state == GameState.TERMINATED) return;

        setState(GameState.TERMINATED);
        countdown.terminate();
        players.values().forEach(player -> player.setGameId(null));
        players.clear();
        disconnectedPlayers.clear();
        eventDispatcher.accept(new GameDisposeEvent(
                this
        ));
    }

    /**
     * Sets the game state and fires a state change event.
     */
    protected void setState(GameState newState) {
        if (this.state == newState) return;

        GameState oldState = this.state;
        this.state = newState;

        eventDispatcher.accept(new GameStateChangeEvent(this, oldState, newState));
    }

    protected void checkWinConditions() {
        eventDispatcher.accept(
            new GameWinConditionEvent(
                    this
            )
        );
    }

    /**
     * Determines if a player can rejoin after disconnecting.
     * Override for game-specific logic (e.g., bed status in BedWars).
     */
    protected boolean canPlayerRejoin(P player) {
        return true;
    }

    /**
     * Saves player data when they disconnect for potential rejoin.
     * Override to save game-specific data.
     */
    protected Map<String, Object> savePlayerData(P player) {
        return new HashMap<>();
    }

}
