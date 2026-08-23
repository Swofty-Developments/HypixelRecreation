package net.swofty.type.bedwarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.BedWarsModeStats;
import net.swofty.commons.bedwars.BedwarsLeaderboardMode;
import net.swofty.commons.bedwars.BedwarsLeaderboardPeriod;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsModeStats;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.user.HypixelPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIBedWarsStatistics extends HypixelInventoryGUI {
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");
	private static final DecimalFormat RATIO_FORMAT = new DecimalFormat("#,##0.##");

	public GUIBedWarsStatistics() {
		super("Bed Wars Statistics", InventoryType.CHEST_6_ROW);
	}

	@Override
	public boolean allowHotkeying() {
		return false;
	}

	@Override
	public void onOpen(InventoryGUIOpenEvent e) {
		HypixelPlayer player = getPlayer();
		BedWarsDataHandler handler = BedWarsDataHandler.getUser(player);
		if (handler == null) {
			player.sendMessage("<c>Failed to load your statistics.");
			player.closeInventory();
			return;
		}

		BedWarsModeStats modeStats = handler.get(BedWarsDataHandler.Data.MODE_STATS, DatapointBedWarsModeStats.class).getValue();
		UUID uuid = player.getUuid();

		// Overall Statistics (Slot 4)
		set(createOverallStatisticsItem(uuid, modeStats, 4));

		// Solo Statistics (Slot 19)
		set(createModeStatisticsItem(uuid, modeStats, BedwarsLeaderboardMode.SOLO, 1, 19));

		// Doubles Statistics (Slot 21)
		set(createModeStatisticsItem(uuid, modeStats, BedwarsLeaderboardMode.DOUBLES, 2, 21));

		// 3v3v3v3 Statistics (Slot 23)
		set(createModeStatisticsItem(uuid, modeStats, BedwarsLeaderboardMode.THREE_THREE, 3, 23));

		// 4v4v4v4 Statistics (Slot 25)
		set(createModeStatisticsItem(uuid, modeStats, BedwarsLeaderboardMode.FOUR_FOUR_FOUR, 4, 25));

		// 4v4 Statistics (Slot 31)
		set(createModeStatisticsItem(uuid, modeStats, BedwarsLeaderboardMode.FOUR_FOUR, 4, 31));

		// Dream Statistics (Slot 53)
		set(new GUIItem(53) {
			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				return ItemStacks.item(Material.ENDER_EYE, """
						<a>Dream Statistics

						<e>View your statistics for Dream modes!""");
			}
		});

		// Close button (Slot 49)
		set(GUIClickableItem.getCloseItem(49));

		updateItemStacks(getInventory(), getPlayer());
	}

	private GUIItem createOverallStatisticsItem(UUID uuid, BedWarsModeStats stats, int slot) {
		return new GUIItem(slot) {
			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				BedwarsLeaderboardMode mode = BedwarsLeaderboardMode.ALL;

				long wins = stats.getWins(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long losses = stats.getLosses(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long gamesPlayed = wins + losses;

				long bedsBroken = stats.getBedsBroken(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long bedsLost = stats.getBedsLost(mode, BedwarsLeaderboardPeriod.LIFETIME);

				long kills = stats.getKills(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long deaths = stats.getDeaths(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long finalKills = stats.getFinalKills(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long finalDeaths = stats.getFinalDeaths(mode, BedwarsLeaderboardPeriod.LIFETIME);

				double fkdr = finalDeaths > 0 ? (double) finalKills / finalDeaths : finalKills;
				long winstreak = stats.getWinstreak(mode);

				// Use CORE mode for leaderboard rankings in overall stats
				BedwarsLeaderboardMode coreMode = BedwarsLeaderboardMode.CORE;

				List<Text> lore = new ArrayList<>();
				lore.add(Text.of("<7>Games Played: <a>{}", formatNumber(gamesPlayed)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Wins: <a>{}", formatNumber(wins)));
				lore.add(Text.of("<7>Losses: <a>{}", formatNumber(losses)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Beds Broken: <a>{}", formatNumber(bedsBroken)));
				lore.add(Text.of("<7>Beds Lost: <a>{}", formatNumber(bedsLost)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Kills: <a>{}", formatNumber(kills)));
				lore.add(Text.of("<7>Deaths: <a>{}", formatNumber(deaths)));
				lore.add(Text.of("<7>Final Kills: <a>{}", formatNumber(finalKills)));
				lore.add(Text.of("<7>Final Deaths: <a>{}", formatNumber(finalDeaths)));
				lore.add(Text.of("<7>Final K/D Ratio: <a>{}", RATIO_FORMAT.format(fkdr)));
				lore.add(Text.empty());
				lore.add(Text.of("<a>Core Mode Leaderboards"));
				lore.add(Text.of("<7>Lifetime Wins: <a>{} {}", formatNumber(stats.getWins(coreMode, BedwarsLeaderboardPeriod.LIFETIME)), getRankDisplay(uuid, "wins", coreMode, BedwarsLeaderboardPeriod.LIFETIME)));
				lore.add(Text.of("<7>Monthly Wins: <a>{} {}", formatNumber(stats.getWins(coreMode, BedwarsLeaderboardPeriod.MONTHLY)), getRankDisplay(uuid, "wins", coreMode, BedwarsLeaderboardPeriod.MONTHLY)));
				lore.add(Text.of("<7>Weekly Wins: <a>{} {}", formatNumber(stats.getWins(coreMode, BedwarsLeaderboardPeriod.WEEKLY)), getRankDisplay(uuid, "wins", coreMode, BedwarsLeaderboardPeriod.WEEKLY)));
				lore.add(Text.of("<7>Daily Wins: <a>{} {}", formatNumber(stats.getWins(coreMode, BedwarsLeaderboardPeriod.DAILY)), getRankDisplay(uuid, "wins", coreMode, BedwarsLeaderboardPeriod.DAILY)));
				lore.add(Text.of("<7>Lifetime Beds Broken: <a>{} {}", formatNumber(stats.getBedsBroken(coreMode, BedwarsLeaderboardPeriod.LIFETIME)), getRankDisplay(uuid, "beds_broken", coreMode, BedwarsLeaderboardPeriod.LIFETIME)));
				lore.add(Text.of("<7>Weekly Beds Broken: <a>{} {}", formatNumber(stats.getBedsBroken(coreMode, BedwarsLeaderboardPeriod.WEEKLY)), getRankDisplay(uuid, "beds_broken", coreMode, BedwarsLeaderboardPeriod.WEEKLY)));
				lore.add(Text.of("<7>Daily Beds Broken: <a>{} {}", formatNumber(stats.getBedsBroken(coreMode, BedwarsLeaderboardPeriod.DAILY)), getRankDisplay(uuid, "beds_broken", coreMode, BedwarsLeaderboardPeriod.DAILY)));
				lore.add(Text.of("<7>Lifetime Final Kills: <a>{} {}", formatNumber(stats.getFinalKills(coreMode, BedwarsLeaderboardPeriod.LIFETIME)), getRankDisplay(uuid, "final_kills", coreMode, BedwarsLeaderboardPeriod.LIFETIME)));
				lore.add(Text.of("<7>Weekly Final Kills: <a>{} {}", formatNumber(stats.getFinalKills(coreMode, BedwarsLeaderboardPeriod.WEEKLY)), getRankDisplay(uuid, "final_kills", coreMode, BedwarsLeaderboardPeriod.WEEKLY)));
				lore.add(Text.of("<7>Daily Final Kills: <a>{} {}", formatNumber(stats.getFinalKills(coreMode, BedwarsLeaderboardPeriod.DAILY)), getRankDisplay(uuid, "final_kills", coreMode, BedwarsLeaderboardPeriod.DAILY)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Winstreak: <a>{}", formatNumber(winstreak)));
				lore.add(Text.empty());

				return ItemStacks.item(Material.PAPER, 1, Text.of("<a>Overall Statistics"), lore);
			}
		};
	}

	private GUIItem createModeStatisticsItem(UUID uuid, BedWarsModeStats stats, BedwarsLeaderboardMode mode, int amount, int slot) {
		return new GUIItem(slot) {
			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				long wins = stats.getWins(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long losses = stats.getLosses(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long gamesPlayed = wins + losses;

				long bedsBroken = stats.getBedsBroken(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long bedsLost = stats.getBedsLost(mode, BedwarsLeaderboardPeriod.LIFETIME);

				long kills = stats.getKills(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long deaths = stats.getDeaths(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long finalKills = stats.getFinalKills(mode, BedwarsLeaderboardPeriod.LIFETIME);
				long finalDeaths = stats.getFinalDeaths(mode, BedwarsLeaderboardPeriod.LIFETIME);

				double fkdr = finalDeaths > 0 ? (double) finalKills / finalDeaths : finalKills;
				long winstreak = stats.getWinstreak(mode);

				List<Text> lore = new ArrayList<>();
				lore.add(Text.of("<7>Games Played: <a>{}", formatNumber(gamesPlayed)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Lifetime Wins: <a>{} {}", formatNumber(stats.getWins(mode, BedwarsLeaderboardPeriod.LIFETIME)), getRankDisplay(uuid, "wins", mode, BedwarsLeaderboardPeriod.LIFETIME)));
				lore.add(Text.of("<7>Monthly Wins: <a>{} {}", formatNumber(stats.getWins(mode, BedwarsLeaderboardPeriod.MONTHLY)), getRankDisplay(uuid, "wins", mode, BedwarsLeaderboardPeriod.MONTHLY)));
				lore.add(Text.of("<7>Weekly Wins: <a>{} {}", formatNumber(stats.getWins(mode, BedwarsLeaderboardPeriod.WEEKLY)), getRankDisplay(uuid, "wins", mode, BedwarsLeaderboardPeriod.WEEKLY)));
				lore.add(Text.of("<7>Daily Wins: <a>{} {}", formatNumber(stats.getWins(mode, BedwarsLeaderboardPeriod.DAILY)), getRankDisplay(uuid, "wins", mode, BedwarsLeaderboardPeriod.DAILY)));
				lore.add(Text.of("<7>Losses: <a>{}", formatNumber(losses)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Lifetime Beds Broken: <a>{} {}", formatNumber(stats.getBedsBroken(mode, BedwarsLeaderboardPeriod.LIFETIME)), getRankDisplay(uuid, "beds_broken", mode, BedwarsLeaderboardPeriod.LIFETIME)));
				lore.add(Text.of("<7>Monthly Beds Broken: <a>{} {}", formatNumber(stats.getBedsBroken(mode, BedwarsLeaderboardPeriod.MONTHLY)), getRankDisplay(uuid, "beds_broken", mode, BedwarsLeaderboardPeriod.MONTHLY)));
				lore.add(Text.of("<7>Weekly Beds Broken: <a>{} {}", formatNumber(stats.getBedsBroken(mode, BedwarsLeaderboardPeriod.WEEKLY)), getRankDisplay(uuid, "beds_broken", mode, BedwarsLeaderboardPeriod.WEEKLY)));
				lore.add(Text.of("<7>Daily Beds Broken: <a>{} {}", formatNumber(stats.getBedsBroken(mode, BedwarsLeaderboardPeriod.DAILY)), getRankDisplay(uuid, "beds_broken", mode, BedwarsLeaderboardPeriod.DAILY)));
				lore.add(Text.of("<7>Beds Lost: <a>{}", formatNumber(bedsLost)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Kills: <a>{}", formatNumber(kills)));
				lore.add(Text.of("<7>Deaths: <a>{}", formatNumber(deaths)));
				lore.add(Text.of("<7>Lifetime Final Kills: <a>{} {}", formatNumber(stats.getFinalKills(mode, BedwarsLeaderboardPeriod.LIFETIME)), getRankDisplay(uuid, "final_kills", mode, BedwarsLeaderboardPeriod.LIFETIME)));
				lore.add(Text.of("<7>Monthly Final Kills: <a>{} {}", formatNumber(stats.getFinalKills(mode, BedwarsLeaderboardPeriod.MONTHLY)), getRankDisplay(uuid, "final_kills", mode, BedwarsLeaderboardPeriod.MONTHLY)));
				lore.add(Text.of("<7>Weekly Final Kills: <a>{} {}", formatNumber(stats.getFinalKills(mode, BedwarsLeaderboardPeriod.WEEKLY)), getRankDisplay(uuid, "final_kills", mode, BedwarsLeaderboardPeriod.WEEKLY)));
				lore.add(Text.of("<7>Daily Final Kills: <a>{} {}", formatNumber(stats.getFinalKills(mode, BedwarsLeaderboardPeriod.DAILY)), getRankDisplay(uuid, "final_kills", mode, BedwarsLeaderboardPeriod.DAILY)));
				lore.add(Text.of("<7>Final Deaths: <a>{}", formatNumber(finalDeaths)));
				lore.add(Text.of("<7>Final K/D Ratio: <a>{}", RATIO_FORMAT.format(fkdr)));
				lore.add(Text.empty());
				lore.add(Text.of("<7>Winstreak: <a>{}", formatNumber(winstreak)));
				lore.add(Text.empty());

				return ItemStacks.item(Material.PAPER, amount, Text.of("<a>{} Statistics", mode.getDisplayName()), lore);
			}
		};
	}

	private String getRankDisplay(UUID uuid, String statKey, BedwarsLeaderboardMode mode, BedwarsLeaderboardPeriod period) {
		String leaderboardKey = String.format("bedwars:%s:%s:%s", statKey, mode.getKey(), period.getKey());
		LeaderboardService.LeaderboardEntry entry = LeaderboardService.getPlayerRankEntry(leaderboardKey, uuid);
		if (entry != null && entry.rank() > 0) {
			return "(#" + formatNumber(entry.rank()) + ")";
		}
		return "";
	}

	private String formatNumber(long number) {
		return NUMBER_FORMAT.format(number);
	}

	@Override
	public void onBottomClick(InventoryPreClickEvent e) {
		e.setCancelled(true);
	}
}
