package net.swofty.type.game.game;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameContractTest {
    @Test
    void waitingStatesAcceptPlayersUntilTheyAreFull() {
        StubGame game = new StubGame();

        game.state = GameState.WAITING;
        assertTrue(game.canAcceptPlayers());

        game.state = GameState.COUNTDOWN;
        assertTrue(game.canAcceptPlayers());

        game.players.add(new StubParticipant(UUID.randomUUID()));
        assertFalse(game.canAcceptPlayers());

        game.state = GameState.IN_PROGRESS;

        assertFalse(game.canAcceptPlayers());
    }

    private static final class StubGame implements Game<StubParticipant> {
        private GameState state = GameState.COUNTDOWN;
        private final List<StubParticipant> players = new ArrayList<>();

        @Override
        public String getGameId() {
            return "stub";
        }

        @Override
        public GameState getState() {
            return state;
        }

        @Override
        public Instance getInstance() {
            return null;
        }

        @Override
        public Collection<StubParticipant> getPlayers() {
            return List.copyOf(players);
        }

        @Override
        public int getMaxPlayers() {
            return 1;
        }

        @Override
        public int getMinPlayers() {
            return 1;
        }

        @Override
        public JoinResult join(StubParticipant player) {
            return new JoinResult.Success();
        }

        @Override
        public void leave(StubParticipant player) {
        }

        @Override
        public Optional<StubParticipant> getPlayer(UUID uuid) {
            return Optional.empty();
        }

        @Override
        public void start() {
        }

        @Override
        public void end() {
        }

        @Override
        public void dispose() {
        }

        @Override
        public GameCountdownController getCountdown() {
            return null;
        }
    }

    private record StubParticipant(UUID uuid) implements GameParticipant {
        @Override
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public boolean isOnline() {
            return true;
        }

        @Override
        public String getGameId() {
            return null;
        }

        @Override
        public void setGameId(String gameId) {
        }

        @Override
        public Player getServerPlayer() {
            return null;
        }
    }
}
