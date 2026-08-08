package net.swofty.type.murdermysterygame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.role.GameRole;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MurderMysteryGameScoreboard {
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

					List<Component> lines = new ArrayList<>();
					lines.add(I18n.t("scoreboard.common.date_line", Argument.tagResolver(Formatter.date("date", LocalDateTime.now(ZoneId.systemDefault()))), Argument.string("id", HypixelConst.getServerName())));
					lines.add(Component.space());

					if (game.getState() == GameState.WAITING || game.getState() == GameState.COUNTDOWN) {
						lines.add(I18n.t("scoreboard.murdermystery_game.map_line", Component.text(game.getMapEntry().getName())));
						lines.add(I18n.t("scoreboard.murdermystery_game.players_line",
							Component.text(String.valueOf(game.getPlayers().size())),
							Component.text(String.valueOf(game.getGameType().getMaxPlayers()))));
						lines.add(Component.space());
						if (game.getCountdown().isActive()) {
							lines.add(I18n.t("scoreboard.murdermystery_game.starting_in_line",
								Component.text(String.valueOf(game.getCountdown().getRemainingSeconds()))));
						} else {
							lines.add(I18n.t("scoreboard.murdermystery_game.waiting_for_players"));
						}
						lines.add(Component.space());
						lines.add(I18n.t("scoreboard.murdermystery_game.mode_line", Component.text(game.getGameType().getDisplayName())));

						int playerCount = game.getPlayers().size();
						int murdererChance = playerCount > 0 ? Math.round(100f / playerCount) : 0;
						int detectiveChance = playerCount > 0 ? Math.round(100f / playerCount) : 0;
						Component actionBar = Component.empty()
							.append(I18n.t("scoreboard.murdermystery_game.actionbar.murderer_chance", Component.text(String.valueOf(murdererChance))))
							.append(Component.text("    "))
							.append(I18n.t("scoreboard.murdermystery_game.actionbar.detective_chance", Component.text(String.valueOf(detectiveChance))));
						player.sendActionBar(actionBar);
					} else if (game.getState() == GameState.IN_PROGRESS) {
						GameRole role = game.getRoleManager().getRole(player.getUuid());

						if (player.isEliminated()) {
							lines.add(I18n.t("scoreboard.murdermystery_game.spectating_label"));
							lines.add(Component.space());

							if (role != null) {
								lines.add(I18n.t("scoreboard.murdermystery_game.your_role_line", I18n.t(getScoreboardRoleDisplayKey(role))));
							}
							lines.add(Component.space());

							int playersAlive = game.getRoleManager().countAliveWithRole(GameRole.INNOCENT)
									+ game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE)
									+ game.getRoleManager().countAliveWithRole(GameRole.MURDERER);
							lines.add(I18n.t("scoreboard.murdermystery_game.players_alive_line", Component.text(String.valueOf(playersAlive))));

							String timeLeft = formatTimeRemaining(game.getGameStartTime(), l);
							lines.add(I18n.t("scoreboard.murdermystery_game.time_left_line", Component.text(timeLeft)));
							lines.add(Component.space());

							boolean detectiveAlive = game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE) > 0;
							Component detectiveStatus = detectiveAlive
								? I18n.t("scoreboard.murdermystery_game.detective_alive")
								: I18n.t("scoreboard.murdermystery_game.detective_dead");
							lines.add(I18n.t("scoreboard.murdermystery_game.detective_line", detectiveStatus));
							lines.add(Component.space());

							lines.add(I18n.t("scoreboard.murdermystery_game.map_line", Component.text(game.getMapEntry().getName())));
						} else {
							if (role != null) {
								lines.add(I18n.t("scoreboard.murdermystery_game.role_line", I18n.t(getScoreboardRoleDisplayKey(role))));
							}
							lines.add(Component.space());

							int innocentsLeft = game.getRoleManager().countAliveWithRole(GameRole.INNOCENT)
									+ game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE);
							lines.add(I18n.t("scoreboard.murdermystery_game.innocents_left_line", Component.text(String.valueOf(innocentsLeft))));

							String timeLeft = formatTimeRemaining(game.getGameStartTime(), l);
							lines.add(I18n.t("scoreboard.murdermystery_game.time_left_line", Component.text(timeLeft)));
							lines.add(Component.space());

							boolean detectiveAlive = game.getRoleManager().countAliveWithRole(GameRole.DETECTIVE) > 0;
							Component detectiveStatus = detectiveAlive
								? I18n.t("scoreboard.murdermystery_game.detective_alive")
								: I18n.t("scoreboard.murdermystery_game.detective_dead");
							lines.add(I18n.t("scoreboard.murdermystery_game.detective_line", detectiveStatus));
							lines.add(Component.space());

							lines.add(I18n.t("scoreboard.murdermystery_game.map_line", Component.text(game.getMapEntry().getName())));
						}
					} else if (game.getState() == GameState.ENDING) {
						lines.add(I18n.t("scoreboard.murdermystery_game.game_over"));
						lines.add(Component.space());

						GameRole role = game.getRoleManager().getRole(player.getUuid());
						if (role != null) {
							lines.add(I18n.t("scoreboard.murdermystery_game.your_role_line", I18n.t(getScoreboardRoleDisplayKey(role))));
						}
						lines.add(Component.space());

						int kills = player.getKillsThisGame();
						if (kills > 0) {
							lines.add(I18n.t("scoreboard.murdermystery_game.your_kills_line", Component.text(String.valueOf(kills))));
						}

						int tokens = player.getTokensEarnedThisGame();
						lines.add(I18n.t("scoreboard.murdermystery_game.tokens_earned_line", Component.text(String.valueOf(tokens))));
						lines.add(Component.space());

						lines.add(I18n.t("scoreboard.murdermystery_game.map_line", Component.text(game.getMapEntry().getName())));
						lines.add(I18n.t("scoreboard.murdermystery_game.mode_line", Component.text(game.getGameType().getDisplayName())));
					}

					lines.add(Component.space());
					lines.add(I18n.t("scoreboard.common.footer"));

					if (!scoreboard.hasScoreboard(player)) {
						scoreboard.createScoreboard(player, Component.text(getSidebarName(animationFrame, l)));
					}

					scoreboard.updateLines(player, lines);
					scoreboard.updateTitle(player, Component.text(getSidebarName(animationFrame, l)));
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

	private static String formatTimeRemaining(long gameStartTime, Locale locale) {
		if (gameStartTime == 0) return I18n.string("scoreboard.murdermystery_game.time_left_default", locale);
		long elapsed = System.currentTimeMillis() - gameStartTime;
		long remaining = GAME_DURATION_MS - elapsed;
		if (remaining < 0) remaining = 0;

		long minutes = remaining / 60000;
		long seconds = (remaining % 60000) / 1000;
		return String.format("%d:%02d", minutes, seconds);
	}

	public static void removeCache(Player player) {
		scoreboard.removeScoreboard(player);
	}

	private static String getSidebarName(int counter, Locale locale) {
		String baseText = I18n.string("scoreboard.murdermystery_game.title_base", locale);
		String[] colors = {"§f§l", "§6§l", "§e§l"};
		String endColor = "§a§l";

		if (counter > 0 && counter <= 14) {
			return colors[0] + baseText.substring(0, Math.min(counter - 1, baseText.length())) +
					colors[1] + (counter <= baseText.length() ? String.valueOf(baseText.charAt(counter - 1)) : "") +
					colors[2] + (counter < baseText.length() ? baseText.substring(counter) : "") +
					endColor;
		} else if ((counter >= 15 && counter <= 25) || (counter >= 35 && counter <= 45)) {
			return colors[0] + baseText + endColor;
		} else {
			return colors[2] + baseText + endColor;
		}
	}
}
