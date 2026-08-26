package net.swofty.type.bedwarsgame.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.bedwarsgame.events.custom.BedWarsGameEventAdvanceEvent;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.event.HypixelEventHandler;

@RequiredArgsConstructor
public class BedWarsGameEventManager {
    private final BedWarsGame game;

    @Getter
    private GamePhase currentPhase = GamePhase.NONE;
    @Getter
    private long secondsUntilNextPhase = 0;

    private Task ticker;

    public enum GamePhase {
        NONE("None", 0, 30, 60),
        DIAMOND_II("Diamond II", 6 * 60, 30, 60),
        EMERALD_II("Emerald II", 2 * 6 * 60, 30, 60),
        DIAMOND_III("Diamond III", 3 * 6 * 60, 30, 60),
        EMERALD_III("Emerald III", 4 * 6 * 60, 30, 60),
        BED_DESTRUCTION("Bed Destruction", 5 * 6 * 60, 30, 60),
        SUDDEN_DEATH("Sudden Death", 5 * 6 * 60 + 10 * 60, 30, 60),
        GAME_END("Game End", 5 * 6 * 60 + 20 * 60, 30, 60);

        @Getter
        private final String displayName;
        @Getter
        private final int triggerTimeSeconds;
        @Getter
        private final int diamondSpawnSeconds;
        @Getter
        private final int emeraldSpawnSeconds;

        GamePhase(String displayName, int triggerTime, int diamondSpawn, int emeraldSpawn) {
            this.displayName = displayName;
            this.triggerTimeSeconds = triggerTime;
            this.diamondSpawnSeconds = diamondSpawn;
            this.emeraldSpawnSeconds = emeraldSpawn;
        }

        public GamePhase next() {
            int ordinal = this.ordinal();
            GamePhase[] values = values();
            return ordinal < values.length - 1 ? values[ordinal + 1] : this;
        }
    }

    public void start() {
        if (ticker != null) return;

        currentPhase = GamePhase.NONE;
        if (game.getGameType().isOneBlock()) {
            currentPhase = GamePhase.SUDDEN_DEATH;
            secondsUntilNextPhase = 5 * 60;
            ticker = MinecraftServer.getSchedulerManager().buildTask(this::tick)
                .delay(TaskSchedule.seconds(1))
                .repeat(TaskSchedule.seconds(1))
                .schedule();
            return;
        }
        secondsUntilNextPhase =
            currentPhase.next().getTriggerTimeSeconds()
                - currentPhase.getTriggerTimeSeconds();

        ticker = MinecraftServer.getSchedulerManager().buildTask(this::tick)
            .delay(TaskSchedule.seconds(1))
            .repeat(TaskSchedule.seconds(1))
            .schedule();
    }

    public void stop() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    private void tick() {
        if (game.getState() != GameState.IN_PROGRESS) {
            stop();
            return;
        }

        secondsUntilNextPhase--;

        if (game.getGameType().isOneBlock()) return;
        if (secondsUntilNextPhase <= 0) {
            advancePhase();
        }
    }

    public void endOneBlockGame() {
        currentPhase = GamePhase.GAME_END;
        secondsUntilNextPhase = 0;
        game.forceGameEnd();
    }

    private void advancePhase() {
        GamePhase previous = currentPhase;
        currentPhase = currentPhase.next();

        if (currentPhase != previous) {
            secondsUntilNextPhase = currentPhase.next() != currentPhase
                ? currentPhase.getTriggerTimeSeconds()
                : Integer.MAX_VALUE;

            // Fire event
            HypixelEventHandler.callCustomEvent(new BedWarsGameEventAdvanceEvent(
                    game,
                previous.getDisplayName(),
                currentPhase.getDisplayName(),
                secondsUntilNextPhase
            ));

            // Handle special phases
            if (currentPhase == GamePhase.BED_DESTRUCTION) {
                destroyAllBeds();
            }
        }
    }

    private void destroyAllBeds() {
        // Would destroy all remaining beds and fire events
        for (BedWarsTeam team : game.getTeams()) {
            if (team.isBedAlive()) {
                team.destroyBed();
            }
        }
    }

    public int getDiamondSpawnSeconds() {
        return currentPhase.getDiamondSpawnSeconds();
    }

    public int getEmeraldSpawnSeconds() {
        return currentPhase.getEmeraldSpawnSeconds();
    }

    @Deprecated(forRemoval = true)
    public GamePhase getNextEvent() {
        GamePhase next = currentPhase.next();
        return next != currentPhase ? next : null;
    }

    public GamePhase getCurrentEvent() {
        return currentPhase;
    }
}
