package net.swofty.type.skyblockgeneric.experimentation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class GameSession {
    private final UUID playerId;
    private final ExperimentType type;
    private final ExperimentTier tier;
    private final long startTime;
    private GameState state;
    private int bestScore;

    public GameSession(UUID playerId, ExperimentType type, ExperimentTier tier) {
        this.playerId = playerId;
        this.type = type;
        this.tier = tier;
        this.startTime = System.currentTimeMillis();
        ExperimentRules.Rule rule = ExperimentRules.forExperiment(type, tier);
        this.state = switch (type) {
            case SUPERPAIRS -> new SuperPairsState(rule.baseClicks(), rule.pairCount(),
                    startTime + rule.deadlineSeconds() * 1_000L);
            case CHRONOMATRON -> new ChronomatronState();
            case ULTRASEQUENCER -> new UltraSequencerState(rule.boardSize());
        };
    }

    public void state(GameState state) {
        this.state = state;
    }

    public void bestScore(int score) {
        bestScore = Math.max(bestScore, score);
    }

    public abstract static class GameState {
    }

    public enum GamePhase {
        READY,
        WATCHING,
        PLAYING,
        COMPLETE
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static final class ChronomatronState extends GameState {
        private final List<Integer> sequence = new ArrayList<>();
        private GamePhase phase = GamePhase.READY;
        private int inputIndex;
        private long lastInput;
        private long deadline;

    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static final class UltraSequencerState extends GameState {
        private final List<Integer> sequence = new ArrayList<>();
        private final List<Integer> boardNumbers = new ArrayList<>();
        private GamePhase phase = GamePhase.READY;
        private int inputIndex;
        private long deadline;

        public UltraSequencerState(int boardSize) {
            for (int number = 1; number <= boardSize; number++) boardNumbers.add(number);
            java.util.Collections.shuffle(boardNumbers);
        }

    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static final class SuperPairsState extends GameState {
        private final List<SuperPairTile> board = new ArrayList<>();
        private final Set<Integer> matchedTiles = new java.util.HashSet<>();
        private final Set<String> matchedPairs = new java.util.HashSet<>();
        private final int totalClicks;
        private final int rewardPairCount;
        private final long deadline;
        private int clicksRemaining;
        private int bonusXp;
        private boolean nextClickFree;
        private int firstFlip = -1;
        private int mismatchFirst = -1;
        private int mismatchSecond = -1;
        private long mismatchUntil;

        public SuperPairsState(int totalClicks, int rewardPairCount, long deadline) {
            this.totalClicks = totalClicks;
            this.rewardPairCount = rewardPairCount;
            this.deadline = deadline;
            this.clicksRemaining = totalClicks;
        }

        public int pairsFound() {
            return matchedPairs.size();
        }
    }
}
