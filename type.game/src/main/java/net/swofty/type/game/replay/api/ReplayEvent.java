package net.swofty.type.game.replay.api;

import net.swofty.commons.replay.protocol.ReplayDataWriter;

import java.io.IOException;

public interface ReplayEvent {
    int typeId();

    void write(ReplayDataWriter writer) throws IOException;
}
