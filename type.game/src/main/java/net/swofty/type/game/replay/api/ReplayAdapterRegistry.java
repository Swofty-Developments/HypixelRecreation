package net.swofty.type.game.replay.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplayAdapterRegistry<T> {
    private final Map<String, T> adapters = new ConcurrentHashMap<>();

    public void register(String gameType, T adapter) {
        if (gameType == null || gameType.isBlank()) {
            throw new IllegalArgumentException("gameType must not be blank");
        }
        if (adapters.putIfAbsent(gameType, adapter) != null) {
            throw new IllegalStateException("Replay adapter already registered for " + gameType);
        }
    }

    public T require(String gameType) {
        T adapter = adapters.get(gameType);
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported replay game type: " + gameType);
        }
        return adapter;
    }
}
