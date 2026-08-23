package net.swofty.type.bedwarslobby.hologram;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.bedwars.BedwarsLeaderboardMode;
import net.swofty.commons.bedwars.BedwarsLeaderboardPeriod;
import net.swofty.commons.bedwars.BedwarsLeaderboardView;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.bedwars.BedwarsStatType;
import net.swofty.commons.bedwars.BedwarsTextAlignment;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarslobby.hologram.LeaderboardHologramManager.PlayerLeaderboardState;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.leaderboard.BedWarsLeaderboardAggregator;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.user.HypixelPlayer;

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
import java.util.UUID;

@Getter
public enum BedWarsLeaderboardHologram {
	LEVEL(BedwarsStatType.LEVEL, new Pos(-17, 69.5, -28)),
	WINS(BedwarsStatType.WINS, new Pos(-11.5, 69.5, -28)),
	FINAL_KILLS(BedwarsStatType.FINAL_KILLS, new Pos(-5.5, 69.5, -28)),
	BEDS_BROKEN(BedwarsStatType.BEDS_BROKEN, new Pos(0.7, 69.5, -28));

	private final BedwarsStatType statType;
	private final Pos position;

	BedWarsLeaderboardHologram(BedwarsStatType statType, Pos position) {
		this.statType = statType;
		this.position = position;
	}

	public List<Text> getHologramLines(HypixelPlayer player, PlayerLeaderboardState state) {
		BedwarsLeaderboardPeriod period = statType == BedwarsStatType.LEVEL
				? BedwarsLeaderboardPeriod.LIFETIME
				: state.period();
		BedwarsLeaderboardMode mode = state.mode();
		BedwarsLeaderboardView view = state.view();
		BedwarsTextAlignment alignment = state.textAlignment();

		List<Text> lines = new ArrayList<>();

		if (statType == BedwarsStatType.LEVEL) {
			lines.add(Text.of("<b><l>{}", statType.getDisplayName()));
		} else {
			lines.add(Text.of("<b><l>{} {}", period.getDisplayName(), statType.getDisplayName()));
		}

		lines.add(Text.of("<7>{}", mode.getDisplayName()));

		String leaderboardKey = getLeaderboardKey(period, mode);
		List<LeaderboardService.LeaderboardEntry> entries;

		if (view == BedwarsLeaderboardView.PLAYERS_AROUND_YOU) {
			entries = getPlayersInLobby(player, leaderboardKey);
		} else {
			entries = LeaderboardService.getTopN(leaderboardKey, 10);
		}

		int maxNameWidth = 0;
		if (alignment == BedwarsTextAlignment.BLOCK && !entries.isEmpty()) {
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
				if (alignment == BedwarsTextAlignment.BLOCK) {
					paddedName = padToWidth(playerName, maxNameWidth);
				}

				int rank = view == BedwarsLeaderboardView.PLAYERS_AROUND_YOU ? displayRank : entry.rank();
				if (statType == BedwarsStatType.LEVEL) {
					int level = BedwarsLevelUtil.calculateLevel(entry.scoreAsLong());
					lines.add(Text.of("<e>{}. <f>{} {}", rank,
							renderEntryLevel(entry.playerUuid(), level),
							paddedName));
				} else {
					lines.add(Text.of("<e>{}. <f>{} <7>- <a>{}", rank,
							paddedName,
							formattedScore));
				}
				displayRank++;
			}
		}

		while (lines.size() < 12) {
			lines.add(Text.of("<8>---"));
		}

		long playerScore = getPlayerScore(player, period, mode);
		LeaderboardService.LeaderboardEntry playerEntry = LeaderboardService.getPlayerRankEntry(leaderboardKey, player.getUuid());

		if (playerEntry != null) {
			String formattedScore = formatScore(playerScore);
			if (statType == BedwarsStatType.LEVEL) {
				int level = BedwarsLevelUtil.calculateLevel(playerScore);
				lines.add(Text.of("<e>You: <f>{}. {}", playerEntry.rank(),
						BedWarsPrestigeRenderer.renderBrackets(player, level)));
			} else {
				lines.add(Text.of("<e>You: <f>{}. <7>(<a>{}<7>)", playerEntry.rank(), formattedScore));
			}
		} else {
			lines.add(Text.of("<e>You: <7>Not ranked"));
		}

		if (period != BedwarsLeaderboardPeriod.LIFETIME) {
			lines.add(Text.of("<7>Resets in <f>{}", getTimeUntilReset(period)));
		}

		lines.add(Text.of("<6>Click to change filters!"));

		return lines;
	}

	private Text renderEntryLevel(UUID uuid, int level) {
		try {
			BedWarsDataHandler handler = BedWarsDataHandler.getOfOfflinePlayer(uuid);
			return BedWarsPrestigeRenderer.renderForData(handler, level, true);
		} catch (RuntimeException exception) {
			return BedWarsPrestigeRenderer.renderPreview(null, level, null, null, null);
		}
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
		for (char c : text.toCharArray()) {
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

	private String getTimeUntilReset(BedwarsLeaderboardPeriod period) {
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

	private String getLeaderboardKey(BedwarsLeaderboardPeriod period, BedwarsLeaderboardMode mode) {
		if (statType == BedwarsStatType.LEVEL) {
			return "bedwars:experience";
		}
		return BedWarsLeaderboardAggregator.buildLeaderboardKey(statType, mode, period);
	}

	private long getPlayerScore(HypixelPlayer player, BedwarsLeaderboardPeriod period, BedwarsLeaderboardMode mode) {
		BedWarsDataHandler handler = BedWarsDataHandler.getUser(player);
		if (handler == null) return 0;

		if (statType == BedwarsStatType.LEVEL) {
			DatapointLeaderboardLong expDatapoint = handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
			return expDatapoint.getValue();
		}

		String leaderboardKey = getLeaderboardKey(period, mode);
		Double score = LeaderboardService.getPlayerScore(leaderboardKey, player.getUuid());
		return score != null ? score.longValue() : 0;
	}

	private String formatScore(long score) {
		return NumberFormat.getNumberInstance(Locale.US).format(score);
	}
}
