package net.swofty.type.skywarslobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skywars.SkyWarsLevelColor;
import net.swofty.commons.skywars.SkywarsLeaderboardMode;
import net.swofty.commons.skywars.SkywarsLeaderboardPeriod;
import net.swofty.commons.skywars.SkywarsModeStats;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsModeStats;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.level.SkywarsLevelRegistry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkyWarsLobbyScoreboard {
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
				Locale l = player.getLocale();
				SkywarsDataHandler swDataHandler = SkywarsDataHandler.getUser(player);

				if (swDataHandler == null) {
					continue;
				}

				long experience = swDataHandler.get(SkywarsDataHandler.Data.EXPERIENCE, DatapointLong.class).getValue();
				long souls = swDataHandler.get(SkywarsDataHandler.Data.SOULS, DatapointLong.class).getValue();
				long coins = swDataHandler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();
				long tokens = swDataHandler.get(SkywarsDataHandler.Data.TOKENS, DatapointLong.class).getValue();

				SkywarsModeStats modeStats = swDataHandler.get(SkywarsDataHandler.Data.MODE_STATS, DatapointSkywarsModeStats.class).getValue();

				long soloKills = modeStats.getKills(SkywarsLeaderboardMode.SOLO_NORMAL, SkywarsLeaderboardPeriod.LIFETIME)
						+ modeStats.getKills(SkywarsLeaderboardMode.SOLO_INSANE, SkywarsLeaderboardPeriod.LIFETIME);
				long soloWins = modeStats.getWins(SkywarsLeaderboardMode.SOLO_NORMAL, SkywarsLeaderboardPeriod.LIFETIME)
						+ modeStats.getWins(SkywarsLeaderboardMode.SOLO_INSANE, SkywarsLeaderboardPeriod.LIFETIME);

				long doublesKills = modeStats.getKills(SkywarsLeaderboardMode.DOUBLES, SkywarsLeaderboardPeriod.LIFETIME);
				long doublesWins = modeStats.getWins(SkywarsLeaderboardMode.DOUBLES, SkywarsLeaderboardPeriod.LIFETIME);

				int level = SkywarsLevelRegistry.calculateLevel(experience);

				List<Text> lines = new ArrayList<>();
				lines.add(Text.key("scoreboard.common.date_line",
					DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
					HypixelConst.getServerName()));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.skywars_lobby.your_level_line",
					SkyWarsLevelColor.getLevelDisplay(level)));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.skywars_lobby.solo_kills_line", soloKills));
				lines.add(Text.key("scoreboard.skywars_lobby.solo_wins_line", soloWins));
				lines.add(Text.key("scoreboard.skywars_lobby.doubles_kills_line", doublesKills));
				lines.add(Text.key("scoreboard.skywars_lobby.doubles_wins_line", doublesWins));
				lines.add(BLANK);
				lines.add(Text.key("scoreboard.skywars_lobby.coins_line", coins));
				lines.add(Text.key("scoreboard.skywars_lobby.souls_line", souls));
				lines.add(Text.key("scoreboard.skywars_lobby.tokens_line", tokens));
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
		String baseText = Text.key("scoreboard.skywars_lobby.title_base").plain(locale);

		if (counter > 0 && counter <= 7) {
			return Text.of("<f><l>{}<e>{}<6>{}",
					baseText.substring(0, counter - 1),
					String.valueOf(baseText.charAt(counter - 1)),
					baseText.substring(counter));
		} else if ((counter >= 8 && counter <= 18) ||
				(counter >= 25 && counter <= 29)) {
			return Text.of("<f><l>{}", baseText);
		} else {
			return Text.of("<e><l>{}", baseText);
		}
	}
}
