package net.swofty.velocity.gamemanager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerPingTracker {
    private final int missesBeforeRemoval;
    private final Map<UUID, Integer> misses = new ConcurrentHashMap<>();

    public ServerPingTracker(int missesBeforeRemoval) {
        if (missesBeforeRemoval < 1) {
            throw new IllegalArgumentException("A server has to miss at least one ping before it is removed");
        }
        this.missesBeforeRemoval = missesBeforeRemoval;
    }

    public int missesBeforeRemoval() {
        return missesBeforeRemoval;
    }

    public void recordSuccess(UUID server) {
        misses.remove(server);
    }

    public boolean recordFailure(UUID server) {
        Integer failures = misses.compute(server, (ignored, current) -> {
            int next = current == null ? 1 : current + 1;
            return next < missesBeforeRemoval ? next : null;
        });

        return failures == null;
    }

    public int missed(UUID server) {
        return misses.getOrDefault(server, 0);
    }

    public void forget(UUID server) {
        misses.remove(server);
    }
}
