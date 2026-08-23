package net.swofty.type.prototypelobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.PrototypeLobbyDataHandler;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.commons.text.Text;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrototypeLobbyScoreboard {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final Text BLANK = Text.literal(" ");
    private static final HypixelScoreboard scoreboard = new HypixelScoreboard();
    private static Integer prototypeName = 0;

    public static void start() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();

        scheduler.submitTask(() -> {
            prototypeName++;
            if (prototypeName > 50) {
                prototypeName = 0;
            }

            for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
                Locale l = player.getLocale();
                HypixelDataHandler dataHandler = player.getDataHandler();
                PrototypeLobbyDataHandler prototypeDataHandler = PrototypeLobbyDataHandler.getUser(player);

                if (dataHandler == null || prototypeDataHandler == null) {
                    continue;
                }

                long hype = prototypeDataHandler.get(PrototypeLobbyDataHandler.Data.HYPE, DatapointLeaderboardLong.class).getValue();

                List<Text> lines = new ArrayList<>();
                lines.add(Text.key("scoreboard.common.date_line",
                    DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
                    HypixelConst.getServerName()));
                lines.add(BLANK);
                lines.add(Text.key("scoreboard.prototype_lobby.dev_notice_line1"));
                lines.add(Text.key("scoreboard.prototype_lobby.dev_notice_line2"));
                lines.add(BLANK);
                lines.add(Text.key("scoreboard.prototype_lobby.bug_report_line1"));
                lines.add(Text.key("scoreboard.prototype_lobby.bug_report_line2"));
                lines.add(Text.key("scoreboard.prototype_lobby.bug_report_url"));
                lines.add(BLANK);
                lines.add(Text.key("scoreboard.prototype_lobby.hype", hype));
                lines.add(BLANK);
                lines.add(Text.key("scoreboard.common.footer"));

                scoreboard.update(player, getSidebarName(prototypeName, l), lines);
            }
            return TaskSchedule.tick(4);
        });
    }

    public static void removeCache(Player player) {
        scoreboard.removeScoreboard(player);
    }

    private static Text getSidebarName(int counter, Locale locale) {
        String baseText = Text.key("scoreboard.prototype_lobby.title_base").plain(locale);

        if (counter > 0 && counter <= 8) {
            return Text.of("<f><l>{}", baseText.substring(0, counter - 1))
                    .append("<6><l>{}", String.valueOf(baseText.charAt(counter - 1)))
                    .append("<e><l>{}", baseText.substring(counter))
                    .append("<a><l>");
        } else if ((counter >= 9 && counter <= 19) ||
                (counter >= 25 && counter <= 29)) {
            return Text.of("<f><l>{}", baseText).append("<a><l>");
        } else {
            return Text.of("<e><l>{}", baseText).append("<a><l>");
        }
    }
}
