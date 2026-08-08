package net.swofty.type.bedwarslobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.BedWarsModeStats;
import net.swofty.commons.bedwars.BedwarsLeaderboardMode;
import net.swofty.commons.bedwars.BedwarsLeaderboardPeriod;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsModeStats;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.user.HypixelPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.swofty.commons.bedwars.BedwarsLevelUtil.suffix;

public class BedWarsLobbyScoreboard {
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
                int filledSquares = Math.clamp((int) Math.round(percentage * 10), 0, 10);
                Component progressBar = Component.text(
                        "■".repeat(filledSquares),
                        NamedTextColor.AQUA
                ).append(Component.text(
                        "■".repeat(10 - filledSquares),
                        NamedTextColor.GRAY
                ));

                long tokens = bwDataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class).getValue();
                long tickets = bwDataHandler.get(BedWarsDataHandler.Data.SLUMBER_TICKETS, DatapointLeaderboardLong.class).getValue();
                BedWarsModeStats modeStats = bwDataHandler.get(BedWarsDataHandler.Data.MODE_STATS, DatapointBedWarsModeStats.class).getValue();
                long totalWins = modeStats.getWins(BedwarsLeaderboardMode.ALL, BedwarsLeaderboardPeriod.LIFETIME);
                long totalKills = modeStats.getKills(BedwarsLeaderboardMode.ALL, BedwarsLeaderboardPeriod.LIFETIME);

                List<Component> lines = new ArrayList<>();
                lines.add(I18n.t("scoreboard.common.date_line", Argument.tagResolver(Formatter.date("date", LocalDateTime.now(ZoneId.systemDefault()))), Argument.string("id", HypixelConst.getServerName())));
                lines.add(Component.space());
                lines.add(I18n.t("scoreboard.bedwars_lobby.level_line",
                        Component.text(BedWarsPrestigeRenderer.renderString(player, BedwarsLevelUtil.calculateLevel(experience)))));
                lines.add(Component.space());
                lines.add(I18n.t("scoreboard.bedwars_lobby.progress_line",
                        Component.text(suffix(progress)),
                        Component.text(suffix(maxExperience))));
                lines.add(Component.space().append(I18n.t("scoreboard.bedwars_lobby.progress_bar", Argument.component("progress", progressBar))));
                lines.add(Component.space());
                lines.add(I18n.t("scoreboard.bedwars_lobby.tokens_line", Argument.tagResolver(Formatter.number("tokens", tokens))));
                if (tickets <= 75) {
                    lines.add(I18n.t("scoreboard.bedwars_lobby.slumber_tickets_line", Argument.tagResolver(Formatter.number("tickets", tickets))));
				} else {
                    lines.add(I18n.t("scoreboard.bedwars_lobby.tickets_line", Argument.tagResolver(Formatter.number("tickets", tickets)), Argument.numeric("ticket_max", 75)));
				}
                lines.add(Component.space());
                lines.add(I18n.t("scoreboard.bedwars_lobby.total_kills_label", Argument.tagResolver(Formatter.number("total_kills", totalKills))));
                lines.add(I18n.t("scoreboard.bedwars_lobby.total_wins_label", Argument.tagResolver(Formatter.number("total_wins", totalWins))));
                lines.add(Component.space());
                lines.add(I18n.t("scoreboard.common.footer"));

                if (!scoreboard.hasScoreboard(player)) {
                    scoreboard.createScoreboard(player, Component.text(getSidebarName(prototypeName, l)));
                }

                scoreboard.updateLines(player, lines);
                scoreboard.updateTitle(player, Component.text(getSidebarName(prototypeName, l)));
            }
            return TaskSchedule.tick(4);
        });
    }

    public static void removeCache(Player player) {
        scoreboard.removeScoreboard(player);
    }

    private static String getSidebarName(int counter, Locale locale) {
        String baseText = I18n.string("scoreboard.bedwars_lobby.title_base", locale);
        String[] colors = {"§f§l", "§6§l", "§e§l"};
        String endColor = "§a§l";

        if (counter > 0 && counter <= 8) {
            return colors[0] + baseText.substring(0, counter - 1) +
                    colors[1] + baseText.charAt(counter - 1) +
                    colors[2] + baseText.substring(counter) +
                    endColor;
        } else if ((counter >= 9 && counter <= 19) ||
                (counter >= 25 && counter <= 29)) {
            return colors[0] + baseText + endColor;
        } else {
            return colors[2] + baseText + endColor;
        }
    }
}
