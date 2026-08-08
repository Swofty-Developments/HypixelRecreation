package net.swofty.type.bedwarsgame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.VersionConst;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGameEventManager;
import net.swofty.type.bedwarsgame.game.v2.BedWarsTeam;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BedWarsGameScoreboard {
    private static final HypixelScoreboard scoreboard = new HypixelScoreboard();
    private static Integer prototypeName = 0;

    public static void start(BedWarsGame game) {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            if (game == null) {
                return TaskSchedule.stop();
            }

            prototypeName++;
            if (prototypeName > 50) {
                prototypeName = 0;
            }

            for (HypixelPlayer player : game.getPlayers()) {
                if (player.joined - System.currentTimeMillis() > 5000) {
                    continue;
                }


                HypixelDataHandler dataHandler = player.getDataHandler();
                BedWarsDataHandler bwDataHandler = BedWarsDataHandler.getUser(player);

                if (dataHandler == null || bwDataHandler == null) {
                    continue;
                }

                String tag = "";
                if (game.getGameType().isDream()) {
                    tag = " §8[D]";
                }

                List<Component> lines = new ArrayList<>();
                lines.add(I18n.t("scoreboard.bedwars_game.date_line", Argument.tagResolver(Formatter.date("date", LocalDateTime.now(ZoneId.systemDefault()))), Argument.string("id", HypixelConst.getServerName()), Argument.string("tag", tag)));
                lines.add(Component.space());

                if (game.getState().isWaiting()) {
                    lines.add(I18n.t("scoreboard.bedwars_game.map_line", Component.text(game.getMapEntry().getName())));
                    lines.add(I18n.t("scoreboard.bedwars_game.players_line",
                        Component.text(String.valueOf(game.getPlayers().size())),
                        Component.text(String.valueOf(game.getMapEntry().getConfiguration().getTeams().size()))));
                    lines.add(Component.space());

                    if (game.getState() == GameState.COUNTDOWN) {
                        long seconds = game.getCountdown().getRemainingSeconds();
                        lines.add(Component.text("§fStarting in §a" + seconds + "s"));
                    } else {
                        lines.add(Component.text("§fWaiting..."));
                    }
                    lines.add(Component.space());
                    lines.add(I18n.t("scoreboard.bedwars_game.mode_line", Component.text(game.getGameType().getDisplayName())));
                    lines.add(Component.text("§fVersion: §7v" + VersionConst.BED_WARS_VERSION));
                } else {
                    BedWarsGameEventManager.GamePhase nextGamePhase = game.getGameType().isOneBlock()
                        ? BedWarsGameEventManager.GamePhase.GAME_END
                        : game.getGameEventManager().getCurrentPhase().next();
                    String eventName = nextGamePhase != null
                        ? nextGamePhase.getDisplayName()
                        : game.getGameEventManager().getCurrentEvent().getDisplayName();
                    long seconds = game.getGameEventManager().getSecondsUntilNextPhase();
                    long minutesPart = seconds / 60;
                    long secondsPart = seconds % 60;
                    String timeLeft = String.format("%d:%02d", minutesPart, secondsPart);
                    lines.add(I18n.t("scoreboard.bedwars_game.event_in_line",
                        Component.text(eventName),
                        Component.text(timeLeft)));
                    lines.add(Component.space());

                    for (BedWarsTeam team : game.getTeams().stream()
                            .sorted(Comparator.comparingInt(value -> value.getTeamKey().ordinal()))
                            .toList()) {
                        TeamKey teamKey = team.getTeamKey();
                        String teamName = teamKey.getName();
                        String teamInitial = team.firstLetter().toUpperCase();

                        String bedStatus;
                        if (!team.hasPlayers()) {
                            bedStatus = "§c✖";
                        } else if (team.isBedAlive()) {
                            bedStatus = "§a✔";
                        } else {
                            int alivePlayers = game.getPlayersOnTeam(teamKey).stream()
                                .filter(p -> !Boolean.TRUE.equals(p.getTag(BedWarsGame.ELIMINATED_TAG)))
                                .toList()
                                .size();
                            if (alivePlayers > 0) {
                                bedStatus = "§c" + alivePlayers;
                            } else {
                                bedStatus = "§c✖";
                            }
                        }
                        boolean isYourTeam = game.getPlayerTeam(player.getUuid())
                            .map(t -> t.getTeamKey() == teamKey)
                            .orElse(false);
                        String isYourTeamSuffix = isYourTeam ? " §7YOU" : "";
                        lines.add(Component.text(String.format("%s%s §f%s %s%s", teamKey.chatColor(), teamInitial, teamName, bedStatus, isYourTeamSuffix)));
                    }
                }

                lines.add(Component.space());
                lines.add(I18n.t("scoreboard.common.footer"));

				if (!scoreboard.hasScoreboard(player)) {
                    scoreboard.createScoreboard(player, getSidebarName(prototypeName));
				}

				scoreboard.updateLines(player, lines);
                scoreboard.updateTitle(player, getSidebarName(prototypeName));
			}
			return TaskSchedule.tick(4);
		});
	}

    public static void removeCache(Player player) {
        scoreboard.removeScoreboard(player);
    }

    private static Component getSidebarName(int counter) {
        String baseText = "BED WARS";
		String[] colors = {"§f§l", "§6§l", "§e§l"};
		String endColor = "§a§l";

        if (counter > 0 && counter <= 8) {
            return Component.text(colors[0] + baseText.substring(0, counter - 1) +
                colors[1] + baseText.charAt(counter - 1) +
                colors[2] + baseText.substring(counter) +
                endColor);
        } else if ((counter >= 9 && counter <= 19) ||
            (counter >= 25 && counter <= 29)) {
            return Component.text(colors[0] + baseText + endColor);
        } else {
            return Component.text(colors[2] + baseText + endColor);
        }
    }
}
