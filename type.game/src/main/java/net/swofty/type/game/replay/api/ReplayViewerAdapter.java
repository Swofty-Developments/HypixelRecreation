package net.swofty.type.game.replay.api;

import net.swofty.commons.replay.protocol.ReplayDataReader;

import java.io.IOException;

public interface ReplayViewerAdapter<M extends ReplayGameMetadata, S extends ReplayGameState> {
    String gameType();

    int metadataSchemaVersion();

    M readMetadata(ReplayDataReader reader) throws IOException;

    S readState(ReplayDataReader reader) throws IOException;

    void restoreState(ReplayPlaybackContext context, S state);

    void applyDelta(ReplayPlaybackContext context, ReplayStateDelta delta);

    void renderEvent(ReplayPlaybackContext context, ReplayEvent event);

    ReplayScoreboard createScoreboard(ReplayPlaybackContext context);
}
