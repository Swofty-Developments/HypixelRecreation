package net.swofty.type.replayviewer.playback.scoreboard;

import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenericReplayScoreboard implements ReplayScoreboard {
    private final ReplaySession session;
    private Sidebar sidebar;

    public GenericReplayScoreboard(ReplaySession session) {
        this.session = session;
    }

    @Override
    public void create(Player viewer) {
        sidebar = new Sidebar(getTitle().asComponent());
        sidebar.addViewer(viewer);
        update(session);
    }

    @Override
    public void update(ReplaySession session) {
        if (sidebar == null) return;

        List<Text> lines = getLines(session);
        for (int i = 0; i < 15; i++) {
            sidebar.removeLine("line_" + i);
        }

        for (int i = 0; i < lines.size() && i < 15; i++) {
            sidebar.createLine(new Sidebar.ScoreboardLine(
                    "line_" + i,
                    lines.get(i).asComponent(),
                    lines.size() - i,
                    Sidebar.NumberFormat.blank()
            ));
        }
    }

    @Override
    public void remove(Player viewer) {
        if (sidebar != null) {
            sidebar.removeViewer(viewer);
        }
    }

    @Override
    public Text getTitle() {
        return Text.key("replays.replay_scoreboard_title");
    }

    @Override
    public List<Text> getLines(ReplaySession session) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<7>{}  <8>{}", new SimpleDateFormat("MM/dd/yyyy").format(new Date()), HypixelConst.getServerName()));
        lines.add(Text.key("replays.replay_scoreboard_from",
                Text.of("<7>{}", session.getMetadata().descriptor().serverId())));

        lines.add(Text.empty());

        lines.add(Text.of("<f>{}<a>{}", Text.key("replays.date"), new SimpleDateFormat("MM/dd/yyyy").format(new Date(session.getMetadata().descriptor().startTime()))));
        lines.add(Text.of("<f>{}<a>{} {}", Text.key("replays.time"),
                new SimpleDateFormat("HH:mm").format(new Date(session.getMetadata().descriptor().startTime())), Text.key("replays.est")));

        List<Text> gameLines = getGameLines(session);
        if (!gameLines.isEmpty()) {
            lines.add(Text.empty());
            lines.addAll(gameLines);
        }
        lines.add(Text.empty());

        lines.add(Text.of("<f>{}<a>{}", Text.key("replays.map"), session.getMetadata().descriptor().mapName()));
        lines.add(Text.key("replays.website"));

        return lines;
    }

    protected List<Text> getGameLines(ReplaySession session) {
        return List.of();
    }

}
