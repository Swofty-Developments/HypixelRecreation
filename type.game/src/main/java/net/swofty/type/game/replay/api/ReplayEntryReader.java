package net.swofty.type.game.replay.api;

import net.swofty.commons.replay.protocol.ReplayDataReader;

import java.io.IOException;

@FunctionalInterface
public interface ReplayEntryReader<T> {
    T read(ReplayDataReader reader) throws IOException;
}
