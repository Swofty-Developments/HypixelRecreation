package net.swofty.type.murdermysterygame;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.role.GameRole;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MurderMysteryGameScoreboard {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
	private static final Text BLANK = Text.literal(" ");
	private static final HypixelScoreboard scoreboard = new HypixelScoreboard();
	private static Integer animationFrame = 0;
	private static final long GAME_DURATION_MS = 5 * 60 * 1000;

	public static void start() {
		Scheduler scheduler = MinecraftServer.getSchedulerManager();

		scheduler.submitTask(() -> {
			animationFrame++;
			if (animationFrame > 50) {
				animationFrame = 0;
			}

			for (Game game : TypeMurderMysteryGameLoader.getGames()) {
				for (MurderMysteryPlayer player : game.getPlayers()) {
					if (player.getInstance() == null) continue;
					Locale l = player.getLocale();

					List<Text> lines = new ArrayList<>();
					lines.add(Text.key("scoreboard.common.date_line",
						DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
						HypixelConst.getServerName()));
					lines.add(BLANK);

					if (game.getState() == GameState.WAITING || game.getState() == GameState.COUNTDOWN) {
						lines.add(Text.key("scoreboard.murdermystery_game.map_line", game.getMapEntry().getName()));
						lines.add(Text.key("scoreboard.murdermystery_game.players_line",
							game.getPlayers().size(),
							game.getGameType().getMaxPlayers()));
						lines.add(BLANK);
						if (game.getCountdown().isActive()) {
							lines.add(Text.key("scoreboard.murdermystery_game.starting_in_line",
								game.getCountdown().getRemainingSeconds()));
						} else {
							lines.add(Text.key("scoreboard.murdermystery_game.waiting_for_players"));
						}
						lines.add(BLANK);
						lines.add(Text.key("scoreboard.murdermystery_game.mode_line", game.getGameType().getDisplayName()));

						int playerCount = game.getPlayers().size();
						int murdererChance = playerCount > 0 ? Math.round(100f / playerCount) : 0;
						int detectiveChance = playerCount > 0 ? Math.round(100f / playerCount) : 0;
						player.sendActionBar(Text.key("scoreboard.murdermystery_game.actionbar.murderer_chance", murdererChance)
							.append("    ")
							.append(Text.key("scoreboard.murdermystery_game.actionbar.detective_chance", detectiveChance)));
					} else if (game.getState() == GameState.IN_PROGRESS) {
						GameRole role = game.getRoleManager().getRole(player.getUuid());

						if (player.isEliminated()) {
							lines.add(Text.key("scoreboard.murdermystery_game.spectating_label"));
							lines.add(BLANK);

							if (role != null) {
								lines.add(Text.key("scoreboard.murdermystery_game.your_role_line", Text.key(getScoreboardRoleDisplayKey(role))));
							}
							lines.add(BLANK);

							int playersAlive = game.getRoleManager().countAliveWithRole(GameRole.INNOCENT)
									+ game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE)
									+ game.getRoleManager().countAliveWithRole(GameRole.MURDERER);
							lines.add(Text.key("scoreboard.murdermystery_game.players_alive_line", playersAlive));

							lines.add(Text.key("scoreboard.murdermystery_game.time_left_line",
								formatTimeRemaining(game.getGameStartTime())));
							lines.add(BLANK);

							boolean detectiveAlive = game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE) > 0;
							Text detectiveStatus = detectiveAlive
								? Text.key("scoreboard.murdermystery_game.detective_alive")
								: Text.key("scoreboard.murdermystery_game.detective_dead");
							lines.add(Text.key("scoreboard.murdermystery_game.detective_line", detectiveStatus));
							lines.add(BLANK);

							lines.add(Text.key("scoreboard.murdermystery_game.map_line", game.getMapEntry().getName()));
						} else {
							if (role != null) {
								lines.add(Text.key("scoreboard.murdermystery_game.role_line", Text.key(getScoreboardRoleDisplayKey(role))));
							}
							lines.add(BLANK);

							int innocentsLeft = game.getRoleManager().countAliveWithRole(GameRole.INNOCENT)
									+ game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE);
							lines.add(Text.key("scoreboard.murdermystery_game.innocents_left_line", innocentsLeft));

							lines.add(Text.key("scoreboard.murdermystery_game.time_left_line",
								formatTimeRemaining(game.getGameStartTime())));
							lines.add(BLANK);

							boolean detectiveAlive = game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE) > 0;
							Text detectiveStatus = detectiveAlive
								? Text.key("scoreboard.murdermystery_game.detective_alive")
								: Text.key("scoreboard.murdermystery_game.detective_dead");
							lines.add(Text.key("scoreboard.murdermystery_game.detective_line", detectiveStatus));
							lines.add(BLANK);

							lines.add(Text.key("scoreboard.murdermystery_game.map_line", game.getMapEntry().getName()));
						}
					} else if (game.getState() == GameState.ENDING) {
						lines.add(Text.key("scoreboard.murdermystery_game.game_over"));
						lines.add(BLANK);

						GameRole role = game.getRoleManager().getRole(player.getUuid());
						if (role != null) {
							lines.add(Text.key("scoreboard.murdermystery_game.your_role_line", Text.key(getScoreboardRoleDisplayKey(role))));
						}
						lines.add(BLANK);

						int kills = player.getKillsThisGame();
						if (kills > 0) {
							lines.add(Text.key("scoreboard.murdermystery_game.your_kills_line", kills));
						}

						int tokens = player.getTokensEarnedThisGame();
						lines.add(Text.key("scoreboard.murdermystery_game.tokens_earned_line", tokens));
						lines.add(BLANK);

						lines.add(Text.key("scoreboard.murdermystery_game.map_line", game.getMapEntry().getName()));
						lines.add(Text.key("scoreboard.murdermystery_game.mode_line", game.getGameType().getDisplayName()));
					}

					lines.add(BLANK);
					lines.add(Text.key("scoreboard.common.footer"));

					scoreboard.update(player, getSidebarName(animationFrame, l), lines);
				}
			}
			return TaskSchedule.tick(4);
		});
	}

	private static String getScoreboardRoleDisplayKey(GameRole role) {
		return switch (role) {
			case MURDERER -> "scoreboard.murdermystery_game.role_display.murderer";
			case DETECTIVE -> "scoreboard.murdermystery_game.role_display.detective";
			case INNOCENT -> "scoreboard.murdermystery_game.role_display.innocent";
			case ASSASSIN -> "scoreboard.murdermystery_game.role_display.assassin";
		};
	}

	private static Text formatTimeRemaining(long gameStartTime) {
		if (gameStartTime == 0) return Text.key("scoreboard.murdermystery_game.time_left_default");
		long elapsed = System.currentTimeMillis() - gameStartTime;
		long remaining = GAME_DURATION_MS - elapsed;
		if (remaining < 0) remaining = 0;

		long minutes = remaining / 60000;
		long seconds = (remaining % 60000) / 1000;
		return Text.literal(String.format("%d:%02d", minutes, seconds));
	}

	public static void removeCache(Player player) {
		scoreboard.removeScoreboard(player);
	}

	private static Text getSidebarName(int counter, Locale locale) {
		String plain = Text.key("scoreboard.murdermystery_game.title_base").plain(locale);

		if (counter > 0 && counter <= 14) {
			return Text.of("<f><l>{}<6>{}<e>{}<a>",
					plain.substring(0, Math.min(counter - 1, plain.length())),
					counter <= plain.length() ? String.valueOf(plain.charAt(counter - 1)) : "",
					counter < plain.length() ? plain.substring(counter) : "");
		} else if ((counter >= 15 && counter <= 25) || (counter >= 35 && counter <= 45)) {
			return Text.of("<f><l>{}<a>", plain);
		} else {
			return Text.of("<e><l>{}<a>", plain);
		}
	}
}
