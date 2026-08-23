package net.swofty.type.bedwarslobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.BedWarsModeStats;
import net.swofty.commons.bedwars.BedwarsLeaderboardMode;
import net.swofty.commons.bedwars.BedwarsLeaderboardPeriod;
import net.swofty.commons.StringUtility;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsModeStats;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.swofty.commons.bedwars.BedwarsLevelUtil.suffix;

public class BedWarsLobbyScoreboard {
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
				BedWarsDataHandler bwDataHandler = BedWarsDataHandler.getUser(player);

				if (dataHandler == null || bwDataHandler == null) {
					continue;
				}

				long experience = bwDataHandler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class).getValue();
				int progress = BedwarsLevelUtil.calculateExperienceSinceLastLevel(experience);
				int maxExperience = BedwarsLevelUtil.calculateMaxExperienceFromExperience(experience);

				double percentage = Math.min(1.0, (double) progress / maxExperience);
				int filledSquares = (int) Math.round(percentage * 10);
				Text progressBar = Text.empty();
				for (int i = 0; i < 10; i++) {
					progressBar = progressBar.append(i < filledSquares ? "<b>■" : "<7>■");
				}

				long tokens = bwDataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class).getValue();
				long tickets = bwDataHandler.get(BedWarsDataHandler.Data.SLUMBER_TICKETS, DatapointLeaderboardLong.class).getValue();
				BedWarsModeStats modeStats = bwDataHandler.get(BedWarsDataHandler.Data.MODE_STATS, DatapointBedWarsModeStats.class).getValue();
				long totalWins = modeStats.getWins(BedwarsLeaderboardMode.ALL, BedwarsLeaderboardPeriod.LIFETIME);
				long totalKills = modeStats.getKills(BedwarsLeaderboardMode.ALL, BedwarsLeaderboardPeriod.LIFETIME);

				List<Text> lines = new ArrayList<>();
				lines.add(Text.key("scoreboard.common.date_line",
					DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
					HypixelConst.getServerName()));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.bedwars_lobby.level_line",
                    BedWarsPrestigeRenderer.renderString(player, BedwarsLevelUtil.calculateLevel(experience))));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.bedwars_lobby.progress_line",
					suffix(progress),
					suffix(maxExperience)));
				lines.add(BLANK.append(Text.key("scoreboard.bedwars_lobby.progress_bar",
					progressBar)));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.bedwars_lobby.tokens_line", StringUtility.commaify(tokens)));
				lines.add(Text.key("scoreboard.bedwars_lobby.tickets_line", StringUtility.commaify(tickets)));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.bedwars_lobby.total_kills_label", StringUtility.commaify(totalKills)));
				lines.add(Text.key("scoreboard.bedwars_lobby.total_wins_label", StringUtility.commaify(totalWins)));
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
		String plain = Text.key("scoreboard.bedwars_lobby.title_base").plain(locale);

		if (counter > 0 && counter <= 8) {
			return Text.of("<f><l>{}<6>{}<e>{}<a>",
					plain.substring(0, counter - 1),
					String.valueOf(plain.charAt(counter - 1)),
					plain.substring(counter));
		} else if ((counter >= 9 && counter <= 19) ||
				(counter >= 25 && counter <= 29)) {
			return Text.of("<f><l>{}<a>", plain);
		} else {
			return Text.of("<e><l>{}<a>", plain);
		}
	}
}
