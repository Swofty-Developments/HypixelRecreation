package net.swofty.type.skywarsgame;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.game.SkywarsGameStat;
import net.swofty.type.skywarsgame.game.SkywarsGameStatus;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkywarsGameScoreboard {
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
				SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
				if (game == null) continue;
				Locale l = player.getLocale();

				SkywarsPlayer swPlayer = (SkywarsPlayer) player;

				List<Text> lines = new ArrayList<>();
				lines.add(Text.key("scoreboard.common.date_line",
					DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
					HypixelConst.getServerName()));
				lines.add(BLANK);

				if (game.getGameStatus() == SkywarsGameStatus.IN_PROGRESS) {
					long elapsedMs = System.currentTimeMillis() - game.getGameStartTime();
					long elapsedSeconds = elapsedMs / 1000;

					Text nextEventLine = getNextEventLine(game.getCurrentEvent(), elapsedSeconds);
					if (nextEventLine != null) {
						lines.add(Text.key("scoreboard.skywars_game.next_event_label"));
						lines.add(nextEventLine);
						lines.add(BLANK);
					}

					int alive = (int) game.getPlayers().stream().filter(p -> !p.isEliminated()).count();
					lines.add(Text.key("scoreboard.skywars_game.players_left_line", alive));
					lines.add(BLANK);
					lines.add(Text.key("scoreboard.skywars_game.kills_line",
						game.getGameStats().get(swPlayer.getUuid(), SkywarsGameStat.KILLS)));
				} else if (game.getGameStatus() == SkywarsGameStatus.ENDING) {
					lines.add(Text.key("scoreboard.skywars_game.top_killers_label"));

					java.util.List<SkywarsPlayer> topKillers = game.getPlayers().stream()
							.sorted((a, b) -> Long.compare(
								game.getGameStats().get(b.getUuid(), SkywarsGameStat.KILLS),
								game.getGameStats().get(a.getUuid(), SkywarsGameStat.KILLS)))
							.limit(3)
							.toList();

					Text[] places = {
						Text.key("scoreboard.skywars_game.place_1st"),
						Text.key("scoreboard.skywars_game.place_2nd"),
						Text.key("scoreboard.skywars_game.place_3rd")
					};
					for (int i = 0; i < topKillers.size(); i++) {
						SkywarsPlayer killer = topKillers.get(i);
						lines.add(Text.key("scoreboard.skywars_game.top_killer_line",
							places[i],
							killer.getUsername(),
							game.getGameStats().get(killer.getUuid(), SkywarsGameStat.KILLS)));
					}

					lines.add(BLANK);
					lines.add(Text.key("scoreboard.skywars_game.your_kills_line",
						game.getGameStats().get(swPlayer.getUuid(), SkywarsGameStat.KILLS)));
				} else if (game.getGameStatus() == SkywarsGameStatus.WAITING) {
					lines.add(Text.key("scoreboard.skywars_game.players_line",
						game.getPlayers().size(),
						game.getGameType().getMaxPlayers()));
					lines.add(BLANK);
					lines.add(Text.key("scoreboard.skywars_game.waiting"));
				} else if (game.getGameStatus() == SkywarsGameStatus.STARTING) {
					lines.add(Text.key("scoreboard.skywars_game.players_line",
						game.getPlayers().size(),
						game.getGameType().getMaxPlayers()));
					lines.add(BLANK);
					lines.add(Text.key("scoreboard.skywars_game.starting_in_line",
						game.getCountdown().getSecondsRemaining()));
				}

				lines.add(BLANK);
				lines.add(Text.key("scoreboard.skywars_game.map_line", game.getMapEntry().getName()));
				lines.add(Text.key("scoreboard.skywars_game.mode_line", game.getGameType().getDisplayName()));
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
		String baseText = Text.key("scoreboard.skywars_game.title_base").plain(locale);

		if (counter > 0 && counter <= 7) {
			return Text.of("<f><l>{}<e>{}<6>{}",
					baseText.substring(0, counter - 1),
					String.valueOf(baseText.charAt(counter - 1)),
					baseText.substring(counter)
			);
		} else if ((counter >= 8 && counter <= 18) ||
				(counter >= 25 && counter <= 29)) {
			return Text.of("<f><l>{}", baseText);
		} else {
			return Text.of("<e><l>{}", baseText);
		}
	}

	private static Text getNextEventLine(SkywarsGame.GameEvent currentEvent, long elapsedSeconds) {
        SkywarsGame.GameEvent nextEvent = currentEvent.getNext();

        return switch (nextEvent) {
            case FIRST_REFILL -> {
                long timeUntil = Math.max(0, SkywarsGame.FIRST_REFILL_SECONDS - elapsedSeconds);
				yield Text.key("scoreboard.skywars_game.event_refill", formatTime(timeUntil));
            }
            case SECOND_REFILL -> {
                long timeUntil = Math.max(0, SkywarsGame.SECOND_REFILL_SECONDS - elapsedSeconds);
				yield Text.key("scoreboard.skywars_game.event_refill", formatTime(timeUntil));
            }
            case DRAGON_SPAWN -> {
                long timeUntil = Math.max(0, SkywarsGame.DRAGON_SPAWN_SECONDS - elapsedSeconds);
				yield Text.key("scoreboard.skywars_game.event_dragon", formatTime(timeUntil));
            }
            case GAME_END, GAME_START -> null;
        };
    }

    private static String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return minutes + ":" + String.format("%02d", secs);
    }
}
