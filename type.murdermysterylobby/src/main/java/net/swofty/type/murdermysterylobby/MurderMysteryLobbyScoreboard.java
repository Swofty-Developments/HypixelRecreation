package net.swofty.type.murdermysterylobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardPeriod;
import net.swofty.commons.murdermystery.MurderMysteryModeStats;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.data.datapoints.DatapointMurderMysteryModeStats;
import net.swofty.type.generic.data.handlers.MurderMysteryDataHandler;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MurderMysteryLobbyScoreboard {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
	private static final Text BLANK = Text.literal(" ");
	private static final HypixelScoreboard scoreboard = new HypixelScoreboard();
	private static Integer animationFrame = 0;

	public static void start() {
		Scheduler scheduler = MinecraftServer.getSchedulerManager();

		scheduler.submitTask(() -> {
			animationFrame++;
			if (animationFrame > 50) {
				animationFrame = 0;
			}

			for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
				if (player.getDataHandler() == null) {
					continue;
				}
				Locale l = player.getLocale();

				MurderMysteryDataHandler handler = MurderMysteryDataHandler.getUser(player);
				long totalKills = 0;
				long totalWins = 0;
				long detectiveWins = 0;
				long murdererWins = 0;
				long tokens = 0;

				if (handler != null) {
					DatapointMurderMysteryModeStats statsDP = handler.get(
							MurderMysteryDataHandler.Data.MODE_STATS,
							DatapointMurderMysteryModeStats.class);
					MurderMysteryModeStats stats = statsDP.getValue();
					totalKills = stats.getTotalKills(MurderMysteryLeaderboardPeriod.LIFETIME);
					totalWins = stats.getTotalWins(MurderMysteryLeaderboardPeriod.LIFETIME);
					detectiveWins = stats.getTotalDetectiveWins(MurderMysteryLeaderboardPeriod.LIFETIME);
					murdererWins = stats.getTotalMurdererWins(MurderMysteryLeaderboardPeriod.LIFETIME);
					tokens = stats.getTotalTokens(MurderMysteryLeaderboardPeriod.LIFETIME);
				}

				List<Text> lines = new ArrayList<>();
				lines.add(Text.key("scoreboard.common.date_line",
					DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
					HypixelConst.getServerName()));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.murdermystery_lobby.total_kills_line", totalKills));
				lines.add(Text.key("scoreboard.murdermystery_lobby.total_wins_line", totalWins));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.murdermystery_lobby.wins_as_detective_line", detectiveWins));
				lines.add(Text.key("scoreboard.murdermystery_lobby.wins_as_murderer_line", murdererWins));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.murdermystery_lobby.tokens_line", tokens));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.common.footer"));

				scoreboard.update(player, getSidebarName(animationFrame, l), lines);
			}
			return TaskSchedule.tick(4);
		});
	}

	public static void removeCache(Player player) {
		scoreboard.removeScoreboard(player);
	}

	private static Text getSidebarName(int counter, Locale locale) {
		String plain = Text.key("scoreboard.murdermystery_lobby.title_base").plain(locale);

		if (counter > 0 && counter <= 14) {
			return Text.of("<f><l>{}<6>{}<e>{}<a>",
					plain.substring(0, counter - 1),
					String.valueOf(plain.charAt(counter - 1)),
					plain.substring(counter));
		} else if ((counter >= 15 && counter <= 25) || (counter >= 35 && counter <= 45)) {
			return Text.of("<f><l>{}<a>", plain);
		} else {
			return Text.of("<e><l>{}<a>", plain);
		}
	}
}
