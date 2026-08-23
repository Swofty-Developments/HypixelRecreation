package net.swofty.type.mainlobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainLobbyScoreboard {
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

                if (dataHandler == null) {
                    continue;
                }

                List<Text> lines = new ArrayList<>();
                lines.add(Text.key("scoreboard.common.date_line",
                    DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
                    HypixelConst.getServerName()));
                lines.add(BLANK);
                lines.add(Text.key("scoreboard.main_lobby.rank", player.getRankTitle()));
                lines.add(Text.key("scoreboard.main_lobby.achievements", StringUtility.commaify(player.getAchievementHandler().getTotalUnlockedCount())));
                lines.add(Text.key("scoreboard.main_lobby.level", StringUtility.commaify(player.getLevel())));
                lines.add(BLANK);
                lines.add(Text.key("scoreboard.main_lobby.lobby", StringUtility.commaify(0))); // TODO: what lobby is this?
                lines.add(Text.key("scoreboard.main_lobby.online_players", StringUtility.commaify(HypixelGenericLoader.getLoadedPlayers().size()))); // TODO: whole network
                lines.add(BLANK);
                lines.add(Text.key("scoreboard.main_lobby.friends_online", StringUtility.commaify(0))); // TODO: friends
                lines.add(Text.key("scoreboard.main_lobby.guild_online", StringUtility.commaify(0))); // TODO: guilds
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
        String baseText = "Hypixel";
        String[] colors = {"<f><l>", "<6><l>", "<e><l>"};
        String endColor = "<a><l>";

        String markup;
        if (counter > 0 && counter <= 8) {
            markup = colors[0] + baseText.substring(0, counter - 1) +
                colors[1] + baseText.charAt(counter - 1) +
                colors[2] + baseText.substring(counter) +
                endColor;
        } else if ((counter >= 9 && counter <= 19) ||
            (counter >= 25 && counter <= 29)) {
            markup = colors[0] + baseText + endColor;
        } else {
            markup = colors[2] + baseText + endColor;
        }
        return Text.of(markup);
    }
}
