package net.swofty.type.generic.game;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GameStatTracker<S extends Enum<S>> {
    private final Class<S> statType;
    private final Map<UUID, EnumMap<S, Long>> playerStats = new HashMap<>();

    public GameStatTracker(Class<S> statType) {
        this.statType = statType;
    }

    public void increment(UUID playerUuid, S stat) {
        add(playerUuid, stat, 1);
    }

    public void add(UUID playerUuid, S stat, long amount) {
        playerStats.computeIfAbsent(playerUuid, ignored -> new EnumMap<>(statType))
                .merge(stat, amount, Long::sum);
    }

    public long get(UUID playerUuid, S stat) {
        Map<S, Long> stats = playerStats.get(playerUuid);
        return stats == null ? 0 : stats.getOrDefault(stat, 0L);
    }
}
