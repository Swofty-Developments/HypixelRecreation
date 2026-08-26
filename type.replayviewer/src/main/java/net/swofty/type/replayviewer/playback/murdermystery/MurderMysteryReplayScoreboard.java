package net.swofty.type.replayviewer.playback.murdermystery;

import net.swofty.commons.text.Text;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.playback.scoreboard.GenericReplayScoreboard;

import java.util.List;

public final class MurderMysteryReplayScoreboard extends GenericReplayScoreboard {
    public MurderMysteryReplayScoreboard(ReplaySession session) {
        super(session);
    }

    @Override
    protected List<Text> getGameLines(ReplaySession session) {
        return List.of(
                Text.of("<f>{}<a>Murder Mystery", Text.key("replays.game")),
                Text.of("<f>{}<a>{}", Text.key("replays.mode"), session.gameModeId().replace('_', ' '))
        );
    }
}
