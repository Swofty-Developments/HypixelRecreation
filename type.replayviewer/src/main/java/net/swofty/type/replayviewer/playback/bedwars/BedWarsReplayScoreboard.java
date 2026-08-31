package net.swofty.type.replayviewer.playback.bedwars;

import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.text.Text;
import net.swofty.type.game.replay.bedwars.BedWarsReplayMetadata;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.playback.scoreboard.GenericReplayScoreboard;

import java.util.List;
import java.util.Locale;

public final class BedWarsReplayScoreboard extends GenericReplayScoreboard {
    public BedWarsReplayScoreboard(ReplaySession session) {
        super(session);
    }

    @Override
    protected List<Text> getGameLines(ReplaySession session) {
        String mode = session.getGameMetadata() instanceof BedWarsReplayMetadata metadata
                ? metadata.modeId()
                : session.getMetadata().descriptor().gameType();
        return List.of(
                Text.of("<f>{}<a>{}", Text.key("replays.game"), Text.key("replays.bedwars")),
                Text.of("<f>{}<a>{}", Text.key("replays.mode"), formatMode(mode))
        );
    }

    private String formatMode(String mode) {
        try {
            return BedWarsGameType.valueOf(mode.toUpperCase(Locale.ROOT)).getDisplayName();
        } catch (IllegalArgumentException ignored) {
            return mode.replace('_', ' ');
        }
    }
}
