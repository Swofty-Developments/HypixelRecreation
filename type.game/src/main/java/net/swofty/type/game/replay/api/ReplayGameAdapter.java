package net.swofty.type.game.replay.api;

import net.swofty.commons.replay.protocol.ReplayDataWriter;

import java.io.IOException;

public interface ReplayGameAdapter<M extends ReplayGameMetadata, S extends ReplayGameState> {
    String gameType();

    int metadataSchemaVersion();

    M captureMetadata();

    S captureState();

    void writeMetadata(ReplayDataWriter writer, M metadata) throws IOException;

    void writeState(ReplayDataWriter writer, S state) throws IOException;
}
