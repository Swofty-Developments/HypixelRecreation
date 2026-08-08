package net.swofty.type.game.replay.api;

import net.minestom.server.entity.Entity;

@FunctionalInterface
public interface ReplayEntityVisibilityPolicy {
    boolean isReplayVisible(Entity entity);
}
