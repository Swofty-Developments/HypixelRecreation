package net.swofty.type.skywarslobby.hologram;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skywars.SkywarsLeaderboardMode;
import net.swofty.commons.skywars.SkywarsLeaderboardPeriod;
import net.swofty.commons.skywars.SkywarsLeaderboardView;
import net.swofty.commons.skywars.SkywarsStatType;
import net.swofty.commons.skywars.SkywarsTextAlignment;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.hologram.LeaderboardHologramManager.PlayerLeaderboardState;

import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
public enum SkywarsLeaderboardHologram {
    LEVEL(SkywarsStatType.LEVEL, new Pos(17.5, 65, -32)),
    WINS(SkywarsStatType.WINS, new Pos(23.5, 65, -32)),
    KILLS(SkywarsStatType.KILLS, new Pos(29.5, 65, -32));

    private final SkywarsStatType statType;
    private final Pos position;

    SkywarsLeaderboardHologram(SkywarsStatType statType, Pos position) {
        this.statType = statType;
        this.position = position;
    }

    public String[] getHologramLines(HypixelPlayer player, PlayerLeaderboardState state) {
        SkywarsLeaderboardPeriod period = statType == SkywarsStatType.LEVEL
                ? SkywarsLeaderboardPeriod.LIFETIME
                : state.period();
        SkywarsLeaderboardMode mode = state.mode();
        SkywarsLeaderboardView view = state.view();
        SkywarsTextAlignment alignment = state.textAlignment();

        List<String> lines = new ArrayList<>();

        if (statType == SkywarsStatType.LEVEL) {
            lines.add(Text.of("<b><l>{}", statType.getDisplayName()).serialize());
        } else {
            lines.add(Text.of("<b><l>{} {}", period.getDisplayName(), statType.getDisplayName()).serialize());
        }
        lines.add(Text.of("<7>{}", mode.getDisplayName()).serialize());

        String leaderboardKey = getLeaderboardKey(period, mode);
        List<LeaderboardService.LeaderboardEntry> entries;

        if (view == SkywarsLeaderboardView.PLAYERS_AROUND_YOU) {
            entries = getPlayersInLobby(player, leaderboardKey);
        } else {
            entries = LeaderboardService.getTopN(leaderboardKey, 10);
        }

        int maxNameWidth = 0;
        if (alignment == SkywarsTextAlignment.BLOCK && !entries.isEmpty()) {
            for (LeaderboardService.LeaderboardEntry entry : entries) {
                Text name = HypixelPlayer.getDisplayName(entry.playerUuid());
                maxNameWidth = Math.max(maxNameWidth, getMinecraftStringWidth(name.plain()));
            }
        }

        if (entries.isEmpty()) {
            lines.add("<7>No data available");
        } else {
            int displayRank = 1;
            for (LeaderboardService.LeaderboardEntry entry : entries) {
                Text playerName = HypixelPlayer.getDisplayName(entry.playerUuid());
                String formattedScore = formatScore(entry.scoreAsLong());

                Text paddedName = playerName;
                if (alignment == SkywarsTextAlignment.BLOCK) {
                    paddedName = padToWidth(playerName, maxNameWidth);
                }

                int rank = view == SkywarsLeaderboardView.PLAYERS_AROUND_YOU ? displayRank : entry.rank();
                lines.add(Text.of("<e>{}. <f>{} <7>- <a>{}", rank, paddedName, formattedScore).serialize());
                displayRank++;
            }
        }

        while (lines.size() < 12) {
            lines.add("<8>---");
        }

        LeaderboardService.LeaderboardEntry playerEntry = LeaderboardService.getPlayerRankEntry(leaderboardKey, player.getUuid());

        if (playerEntry != null) {
            String formattedScore = formatScore(playerEntry.scoreAsLong());
            lines.add(Text.of("<e>You: <f>{}. <7>(<a>{}<7>)", playerEntry.rank(), formattedScore).serialize());
        } else {
            lines.add("<e>You: <7>Not ranked");
        }

        if (period != SkywarsLeaderboardPeriod.LIFETIME) {
            lines.add(Text.of("<7>Resets in <f>{}", getTimeUntilReset(period)).serialize());
        }

        lines.add("<6>Click to change filters!");

        return lines.toArray(new String[0]);
    }

    private List<LeaderboardService.LeaderboardEntry> getPlayersInLobby(HypixelPlayer viewer, String leaderboardKey) {
        List<LeaderboardService.LeaderboardEntry> lobbyEntries = new ArrayList<>();

        for (HypixelPlayer lobbyPlayer : HypixelGenericLoader.getLoadedPlayers()) {
            if (lobbyPlayer.getInstance() != null &&
                    lobbyPlayer.getInstance().equals(viewer.getInstance())) {
                LeaderboardService.LeaderboardEntry entry =
                        LeaderboardService.getPlayerRankEntry(leaderboardKey, lobbyPlayer.getUuid());
                lobbyEntries.add(Objects.requireNonNullElseGet(entry, () -> new LeaderboardService.LeaderboardEntry(
                    lobbyPlayer.getUuid(), -1, 0)));
            }
        }

        lobbyEntries.sort((a, b) -> Double.compare(b.score(), a.score()));

        if (lobbyEntries.size() > 10) {
            lobbyEntries = lobbyEntries.subList(0, 10);
        }

        return lobbyEntries;
    }

    private static final int[] CHAR_WIDTHS = new int[256];
    static {
        for (int i = 0; i < 256; i++) {
            CHAR_WIDTHS[i] = 6;
        }
        CHAR_WIDTHS['i'] = 2;
        CHAR_WIDTHS['l'] = 3;
        CHAR_WIDTHS['I'] = 4;
        CHAR_WIDTHS['t'] = 4;
        CHAR_WIDTHS['!'] = 2;
        CHAR_WIDTHS['.'] = 2;
        CHAR_WIDTHS[','] = 2;
        CHAR_WIDTHS[':'] = 2;
        CHAR_WIDTHS[';'] = 2;
        CHAR_WIDTHS['\''] = 3;
        CHAR_WIDTHS['|'] = 2;
        CHAR_WIDTHS['@'] = 7;
        CHAR_WIDTHS[' '] = 4;
    }

    private int getMinecraftStringWidth(String text) {
        int width = 0;
        boolean skipNext = false;
        for (char c : text.toCharArray()) {
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == '\u00A7') {
                skipNext = true;
                continue;
            }
            if (c < 256) {
                width += CHAR_WIDTHS[c];
            } else {
                width += 6;
            }
        }
        return width;
    }

    private Text padToWidth(Text text, int targetWidth) {
        int currentWidth = getMinecraftStringWidth(text.plain());
        int spaceWidth = CHAR_WIDTHS[' '];
        int spacesNeeded = (targetWidth - currentWidth) / spaceWidth;
        if (spacesNeeded <= 0) return text;
        return text.append(Text.literal(" ".repeat(spacesNeeded)));
    }

    private String getTimeUntilReset(SkywarsLeaderboardPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime resetTime;

        switch (period) {
            case DAILY -> resetTime = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case WEEKLY -> resetTime = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case MONTHLY -> resetTime = now.with(TemporalAdjusters.firstDayOfNextMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default -> {
                return "";
            }
        }

        Duration duration = Duration.between(now, resetTime);
        long totalHours = duration.toHours();
        long days = totalHours / 24;
        long hours = totalHours % 24;
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            return String.format("%dd %dh", days, hours);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    private String getLeaderboardKey(SkywarsLeaderboardPeriod period, SkywarsLeaderboardMode mode) {
        if (statType == SkywarsStatType.LEVEL) {
            return "skywars:experience";
        }
        return String.format("skywars:%s:%s:%s", statType.getKey(), mode.getKey(), period.getKey());
    }

    private String formatScore(long score) {
        return NumberFormat.getNumberInstance(Locale.US).format(score);
    }
}
