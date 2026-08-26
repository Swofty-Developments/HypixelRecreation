package net.swofty.type.game.replay.api;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.type.game.replay.model.ReplayTeam;

import java.io.IOException;
import java.util.List;

public interface ReplayViewerAdapter<M extends ReplayGameMetadata, S extends ReplayGameState> {
    String gameType();

    int metadataSchemaVersion();

    M readMetadata(ReplayDataReader reader) throws IOException;

    S readState(ReplayDataReader reader) throws IOException;

    default List<ReplayTeam> teams(ReplayGameMetadata metadata) {
        return List.of();
    }

    void restoreState(ReplayPlaybackContext context, S state);

    void applyDelta(ReplayPlaybackContext context, ReplayStateDelta delta);

    void renderEvent(ReplayPlaybackContext context, ReplayEvent event);

    ReplayScoreboard createScoreboard(ReplayPlaybackContext context);
}
