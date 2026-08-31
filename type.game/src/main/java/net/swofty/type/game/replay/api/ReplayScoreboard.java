package net.swofty.type.game.replay.api;

import net.minestom.server.entity.Player;

public interface ReplayScoreboard {
    void create(Player viewer);

    void update(ReplayPlaybackContext context);

    void remove(Player viewer);
}
