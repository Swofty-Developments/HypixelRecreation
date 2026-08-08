package net.swofty.type.game.replay.api;

import net.minestom.server.instance.InstanceContainer;

public interface ReplayPlaybackContext {
    int tick();

    InstanceContainer instance();
}
