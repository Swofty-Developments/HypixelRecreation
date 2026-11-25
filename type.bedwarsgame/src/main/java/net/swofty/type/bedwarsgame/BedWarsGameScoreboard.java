package net.swofty.type.bedwarsgame;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.VersionConst;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.game.BedWarsGameEventManager;
import net.swofty.type.bedwarsgame.game.BedWarsTeam;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BedWarsGameScoreboard {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final Text BLANK = Text.literal(" ");
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

                Text tag = Text.empty();
                if (game.getGameType().isDream()) {
                    tag = Text.of(" <8>[D]");
                }

                List<Text> lines = new ArrayList<>();
                lines.add(Text.key("scoreboard.bedwars_game.date_line",
                    DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
                    HypixelConst.getServerName(),
                    tag));
                lines.add(BLANK);

                if (game.getState().isWaiting()) {
                    lines.add(Text.key("scoreboard.bedwars_game.map_line", game.getMapEntry().getName()));
                    lines.add(Text.key("scoreboard.bedwars_game.players_line",
                        game.getPlayers().size(),
                        game.getMapEntry().getConfiguration().getTeams().size()));
                    lines.add(BLANK);

                    if (game.getState() == GameState.COUNTDOWN) {
                        long seconds = game.getCountdown().getRemainingSeconds();
                        lines.add(Text.of("<f>Starting in <a>{}s", seconds));
                    } else {
                        lines.add(Text.of("<f>Waiting..."));
                    }
                    lines.add(BLANK);
                    lines.add(Text.key("scoreboard.bedwars_game.mode_line", game.getGameType().getDisplayName()));
                    lines.add(Text.of("<f>Version: <7>v{}", VersionConst.BED_WARS_VERSION));
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
                    lines.add(Text.key("scoreboard.bedwars_game.event_in_line",
                        eventName,
                        timeLeft));
                    lines.add(BLANK);

                    for (BedWarsTeam team : game.getTeams().stream()
                            .sorted(Comparator.comparingInt(value -> value.getTeamKey().ordinal()))
                            .toList()) {
                        TeamKey teamKey = team.getTeamKey();
                        String teamName = teamKey.getName();
                        String teamInitial = team.firstLetter().toUpperCase();

                        Text bedStatus;
                        if (!team.hasPlayers()) {
                            bedStatus = Text.of("<c>✖");
                        } else if (team.isBedAlive()) {
                            bedStatus = Text.of("<a>✔");
                        } else {
                            int alivePlayers = game.getPlayersOnTeam(teamKey).stream()
                                .filter(p -> !Boolean.TRUE.equals(p.getTag(BedWarsGame.ELIMINATED_TAG)))
                                .toList()
                                .size();
                            if (alivePlayers > 0) {
                                bedStatus = Text.of("<c>{}", alivePlayers);
                            } else {
                                bedStatus = Text.of("<c>✖");
                            }
                        }
                        boolean isYourTeam = game.getPlayerTeam(player.getUuid())
                            .map(t -> t.getTeamKey() == teamKey)
                            .orElse(false);
                        Text isYourTeamSuffix = isYourTeam ? Text.of(" <7>YOU") : Text.empty();
                        lines.add(Text.of("<color:{}>{}", teamKey.chatColor(), teamInitial)
                            .append(" <f>{} ", teamName)
                            .append(bedStatus)
                            .append(isYourTeamSuffix));
                    }
                }

                lines.add(BLANK);
                lines.add(Text.key("scoreboard.common.footer"));

                scoreboard.update(player, getSidebarName(prototypeName), lines);
			}
			return TaskSchedule.tick(4);
		});
	}

    public static void removeCache(Player player) {
        scoreboard.removeScoreboard(player);
    }

    private static Text getSidebarName(int counter) {
        String baseText = "BED WARS";

        if (counter > 0 && counter <= 8) {
            return Text.of("<f><l>{}<6><l>{}<e><l>{}<a><l>",
                baseText.substring(0, counter - 1),
                baseText.charAt(counter - 1),
                baseText.substring(counter));
        } else if ((counter >= 9 && counter <= 19) ||
            (counter >= 25 && counter <= 29)) {
            return Text.of("<f><l>{}<a><l>", baseText);
        } else {
            return Text.of("<e><l>{}<a><l>", baseText);
        }
    }
}
