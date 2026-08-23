package net.swofty.type.murdermysterylobby.hologram;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardMode;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardPeriod;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardView;
import net.swofty.commons.murdermystery.MurderMysteryStatType;
import net.swofty.commons.murdermystery.MurderMysteryTextAlignment;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.murdermysterylobby.hologram.LeaderboardHologramManager.PlayerLeaderboardState;

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
public enum MurderMysteryLeaderboardHologram {
    WINS(MurderMysteryStatType.WINS, new Pos(15, 67.3, -26.5)),
    KILLS(MurderMysteryStatType.KILLS, new Pos(21, 67.3, -26.5)),
    KILLS_AS_MURDERER(MurderMysteryStatType.KILLS_AS_MURDERER, new Pos(27, 67.3, -26.5));

    private final MurderMysteryStatType statType;
    private final Pos position;

    MurderMysteryLeaderboardHologram(MurderMysteryStatType statType, Pos position) {
        this.statType = statType;
        this.position = position;
    }

    public List<Text> getHologramLines(HypixelPlayer player, PlayerLeaderboardState state) {
        MurderMysteryLeaderboardPeriod period = state.period();
        MurderMysteryLeaderboardMode mode = state.mode();
        MurderMysteryLeaderboardView view = state.view();
        MurderMysteryTextAlignment alignment = state.textAlignment();

        List<Text> lines = new ArrayList<>();

        lines.add(Text.of("<b><l>{} {}", period.getDisplayName(), statType.getDisplayName()));
        lines.add(Text.of("<7>{}", mode.getDisplayName()));

        String leaderboardKey = getLeaderboardKey(period, mode);
        List<LeaderboardService.LeaderboardEntry> entries;

        if (view == MurderMysteryLeaderboardView.PLAYERS_AROUND_YOU) {
            entries = getPlayersInLobby(player, leaderboardKey);
        } else {
            entries = LeaderboardService.getTopN(leaderboardKey, 10);
        }

        int maxNameWidth = 0;
        if (alignment == MurderMysteryTextAlignment.BLOCK && !entries.isEmpty()) {
            for (LeaderboardService.LeaderboardEntry entry : entries) {
                Text name = HypixelPlayer.getDisplayName(entry.playerUuid());
                maxNameWidth = Math.max(maxNameWidth, getMinecraftStringWidth(name.plain()));
            }
        }

        if (entries.isEmpty()) {
            lines.add(Text.of("<7>No data available"));
        } else {
            int displayRank = 1;
            for (LeaderboardService.LeaderboardEntry entry : entries) {
                Text playerName = HypixelPlayer.getDisplayName(entry.playerUuid());
                String formattedScore = formatScore(entry.scoreAsLong());

                Text paddedName = playerName;
                if (alignment == MurderMysteryTextAlignment.BLOCK) {
                    paddedName = padToWidth(playerName, maxNameWidth);
                }

                int rank = view == MurderMysteryLeaderboardView.PLAYERS_AROUND_YOU ? displayRank : entry.rank();
                lines.add(Text.of("<e>{}. <f>{} <7>- <a>{}", rank, paddedName, formattedScore));
                displayRank++;
            }
        }

        while (lines.size() < 12) {
            lines.add(Text.of("<8>---"));
        }

        LeaderboardService.LeaderboardEntry playerEntry = LeaderboardService.getPlayerRankEntry(leaderboardKey, player.getUuid());

        if (playerEntry != null) {
            String formattedScore = formatScore(playerEntry.scoreAsLong());
            lines.add(Text.of("<e>You: <f>{}. <7>(<a>{}</a>)", playerEntry.rank(), formattedScore));
        } else {
            lines.add(Text.of("<e>You: <7>Not ranked"));
        }

        if (period != MurderMysteryLeaderboardPeriod.LIFETIME) {
            lines.add(Text.of("<7>Resets in <f>{}", getTimeUntilReset(period)));
        }

        lines.add(Text.of("<6>Click to change filters!"));

        return lines;
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

    private String getTimeUntilReset(MurderMysteryLeaderboardPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime resetTime;

        switch (period) {
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

    private String getLeaderboardKey(MurderMysteryLeaderboardPeriod period, MurderMysteryLeaderboardMode mode) {
        return String.format("murdermystery:%s:%s:%s", statType.getKey(), mode.getKey(), period.getKey());
    }

    private String formatScore(long score) {
        return NumberFormat.getNumberInstance(Locale.US).format(score);
    }
}
