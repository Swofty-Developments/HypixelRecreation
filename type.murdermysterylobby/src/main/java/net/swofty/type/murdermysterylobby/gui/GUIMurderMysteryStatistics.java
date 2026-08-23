package net.swofty.type.murdermysterylobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardMode;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardPeriod;
import net.swofty.commons.murdermystery.MurderMysteryModeStats;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointMurderMysteryModeStats;
import net.swofty.type.generic.data.handlers.MurderMysteryDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIMurderMysteryStatistics extends HypixelInventoryGUI {

    public GUIMurderMysteryStatistics() {
        super("Murder Mystery Statistics", InventoryType.CHEST_5_ROW);
    }

    private static String formatTime(long millis) {
        if (millis <= 0) return "00:00";
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes >= 60) {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        MurderMysteryDataHandler handler = MurderMysteryDataHandler.getUser(player);

        MurderMysteryModeStats stats;
        if (handler != null) {
            stats = handler.get(MurderMysteryDataHandler.Data.MODE_STATS, DatapointMurderMysteryModeStats.class).getValue();
        } else {
            stats = MurderMysteryModeStats.empty();
        }

        // Total Statistics
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                long lifetimeBowKills = stats.getTotalBowKills(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyBowKills = stats.getTotalBowKills(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeKnifeKills = stats.getTotalKnifeKills(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyKnifeKills = stats.getTotalKnifeKills(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeThrownKnifeKills = stats.getTotalThrownKnifeKills(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyThrownKnifeKills = stats.getTotalThrownKnifeKills(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeTrapKills = stats.getTotalTrapKills(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyTrapKills = stats.getTotalTrapKills(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeKills = stats.getTotalKills(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyKills = stats.getTotalKills(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeGames = stats.getTotalGamesPlayed(MurderMysteryLeaderboardPeriod.LIFETIME);
                long lifetimeDetectiveWins = stats.getTotalDetectiveWins(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyDetectiveWins = stats.getTotalDetectiveWins(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeMurdererWins = stats.getTotalMurdererWins(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyMurdererWins = stats.getTotalMurdererWins(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeKillsAsHero = stats.getTotalKillsAsHero(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyKillsAsHero = stats.getTotalKillsAsHero(MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeWins = stats.getTotalWins(MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyWins = stats.getTotalWins(MurderMysteryLeaderboardPeriod.WEEKLY);

                // Get best quickest wins across all modes
                long quickestDetective = Long.MAX_VALUE;
                long quickestMurderer = Long.MAX_VALUE;
                for (MurderMysteryLeaderboardMode mode : MurderMysteryLeaderboardMode.values()) {
                    long det = stats.getQuickestDetectiveWin(mode);
                    long mur = stats.getQuickestMurdererWin(mode);
                    if (det > 0 && det < quickestDetective) quickestDetective = det;
                    if (mur > 0 && mur < quickestMurderer) quickestMurderer = mur;
                }

                return ItemStacks.item(Material.PAPER, """
                                <a>Total Statistics
                                <7>Bow Kills: <a>{:,} <8>(Weekly: {:,})
                                <7>Knife Kills: <a>{:,} <8>(Weekly: {:,})
                                <7>Thrown Knife Kills: <a>{:,} <8>(Weekly: {:,})
                                <7>Trap Kills: <a>{:,} <8>(Weekly: {:,})
                                <7>Lifetime Kills: <a>{:,}
                                <7>Weekly Kills: <a>{:,}

                                <7>Games: <a>{:,}
                                <7>Detective Wins: <a>{:,} <8>(Weekly: {:,})
                                <7>Murderer Wins: <a>{:,} <8>(Weekly: {:,})
                                <7>Kills as Hero: <a>{:,} <8>(Weekly: {:,})
                                <7>Lifetime Wins: <a>{:,}
                                <7>Weekly Wins: <a>{:,}

                                <7>Quickest Detective Win Time: <a>{}
                                <7>Quickest Murderer Win Time: <a>{}""",
                        lifetimeBowKills, weeklyBowKills,
                        lifetimeKnifeKills, weeklyKnifeKills,
                        lifetimeThrownKnifeKills, weeklyThrownKnifeKills,
                        lifetimeTrapKills, weeklyTrapKills,
                        lifetimeKills,
                        weeklyKills,
                        lifetimeGames,
                        lifetimeDetectiveWins, weeklyDetectiveWins,
                        lifetimeMurdererWins, weeklyMurdererWins,
                        lifetimeKillsAsHero, weeklyKillsAsHero,
                        lifetimeWins,
                        weeklyWins,
                        quickestDetective == Long.MAX_VALUE ? "N/A" : formatTime(quickestDetective),
                        quickestMurderer == Long.MAX_VALUE ? "N/A" : formatTime(quickestMurderer));
            }
        });

        // Classic Mode Statistics
        set(createModeStatsItem(19, Text.of("<a>Classic Mode Statistics"), MurderMysteryLeaderboardMode.CLASSIC, stats, true));

        // Double Up! Mode Statistics
        set(createModeStatsItem(21, Text.of("<a>Double Up! Mode Statistics"), MurderMysteryLeaderboardMode.DOUBLE_UP, stats, true));

        // Assassins Mode Statistics
        set(createModeStatsItem(23, Text.of("<a>Assassins Mode Statistics"), MurderMysteryLeaderboardMode.ASSASSINS, stats, false));

        // Infection Mode Statistics
        set(new GUIItem(25) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                MurderMysteryLeaderboardMode mode = MurderMysteryLeaderboardMode.INFECTION;
                long lifetimeSurvivorWins = stats.getSurvivorWins(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklySurvivorWins = stats.getSurvivorWins(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeAlphaWins = stats.getAlphaWins(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyAlphaWins = stats.getAlphaWins(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeKillsAsInfected = stats.getKillsAsInfected(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyKillsAsInfected = stats.getKillsAsInfected(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeKillsAsSurvivor = stats.getKillsAsSurvivor(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyKillsAsSurvivor = stats.getKillsAsSurvivor(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeTimeSurvived = stats.getTimeSurvived(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyTimeSurvived = stats.getTimeSurvived(mode, MurderMysteryLeaderboardPeriod.WEEKLY);

                return ItemStacks.item(Material.PAPER, """
                                <a>Infection Mode Statistics
                                <7>Survivor Wins: <a>{:,} <8>(Weekly: {:,})
                                <7>Alpha Wins: <a>{:,} <8>(Weekly: {:,})

                                <7>Lifetime Kills as Infected: <a>{:,}
                                <7>Weekly Kills as Infected: <a>{:,}

                                <7>Lifetime Kills as Survivor: <a>{:,}
                                <7>Weekly Kills as Survivor: <a>{:,}

                                <7>Total Time Survived: <a>{}
                                <7>Weekly Time Survived: <a>{}""",
                        lifetimeSurvivorWins, weeklySurvivorWins,
                        lifetimeAlphaWins, weeklyAlphaWins,
                        lifetimeKillsAsInfected,
                        weeklyKillsAsInfected,
                        lifetimeKillsAsSurvivor,
                        weeklyKillsAsSurvivor,
                        formatTime(lifetimeTimeSurvived),
                        formatTime(weeklyTimeSurvived));
            }
        });

        set(GUIClickableItem.getCloseItem(40));
        updateItemStacks(getInventory(), getPlayer());
    }

    private GUIItem createModeStatsItem(int slot, Text title, MurderMysteryLeaderboardMode mode, MurderMysteryModeStats stats, boolean includeRoleStats) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                long lifetimeBowKills = stats.getBowKills(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyBowKills = stats.getBowKills(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeKnifeKills = stats.getKnifeKills(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyKnifeKills = stats.getKnifeKills(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeThrownKnifeKills = stats.getThrownKnifeKills(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyThrownKnifeKills = stats.getThrownKnifeKills(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeTrapKills = stats.getTrapKills(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyTrapKills = stats.getTrapKills(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeKills = stats.getKills(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyKills = stats.getKills(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                long lifetimeGames = stats.getGamesPlayed(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long lifetimeWins = stats.getWins(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                long weeklyWins = stats.getWins(mode, MurderMysteryLeaderboardPeriod.WEEKLY);

                if (includeRoleStats) {
                    long lifetimeDetectiveWins = stats.getDetectiveWins(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                    long weeklyDetectiveWins = stats.getDetectiveWins(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                    long lifetimeMurdererWins = stats.getMurdererWins(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                    long weeklyMurdererWins = stats.getMurdererWins(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                    long lifetimeKillsAsHero = stats.getKillsAsHero(mode, MurderMysteryLeaderboardPeriod.LIFETIME);
                    long weeklyKillsAsHero = stats.getKillsAsHero(mode, MurderMysteryLeaderboardPeriod.WEEKLY);
                    long quickestDetective = stats.getQuickestDetectiveWin(mode);
                    long quickestMurderer = stats.getQuickestMurdererWin(mode);

                    return ItemStacks.item(Material.PAPER, 1, title, Text.of("""
                                    <7>Bow Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Knife Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Thrown Knife Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Trap Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Lifetime Kills: <a>{:,}
                                    <7>Weekly Kills: <a>{:,}

                                    <7>Games: <a>{:,}
                                    <7>Detective Wins: <a>{:,} <8>(Weekly: {:,})
                                    <7>Murderer Wins: <a>{:,} <8>(Weekly: {:,})
                                    <7>Kills as Hero: <a>{:,} <8>(Weekly: {:,})
                                    <7>Lifetime Wins: <a>{:,}
                                    <7>Weekly Wins: <a>{:,}

                                    <7>Quickest Detective Win Time: <a>{}
                                    <7>Quickest Murderer Win Time: <a>{}""",
                            lifetimeBowKills, weeklyBowKills,
                            lifetimeKnifeKills, weeklyKnifeKills,
                            lifetimeThrownKnifeKills, weeklyThrownKnifeKills,
                            lifetimeTrapKills, weeklyTrapKills,
                            lifetimeKills,
                            weeklyKills,
                            lifetimeGames,
                            lifetimeDetectiveWins, weeklyDetectiveWins,
                            lifetimeMurdererWins, weeklyMurdererWins,
                            lifetimeKillsAsHero, weeklyKillsAsHero,
                            lifetimeWins,
                            weeklyWins,
                            quickestDetective <= 0 ? "N/A" : formatTime(quickestDetective),
                            quickestMurderer <= 0 ? "N/A" : formatTime(quickestMurderer)).lines());
                } else {
                    // Assassins mode - no role-specific stats
                    return ItemStacks.item(Material.PAPER, 1, title, Text.of("""
                                    <7>Bow Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Knife Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Thrown Knife Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Trap Kills: <a>{:,} <8>(Weekly: {:,})
                                    <7>Lifetime Kills: <a>{:,}
                                    <7>Weekly Kills: <a>{:,}

                                    <7>Lifetime Wins: <a>{:,}
                                    <7>Weekly Wins: <a>{:,}""",
                            lifetimeBowKills, weeklyBowKills,
                            lifetimeKnifeKills, weeklyKnifeKills,
                            lifetimeThrownKnifeKills, weeklyThrownKnifeKills,
                            lifetimeTrapKills, weeklyTrapKills,
                            lifetimeKills,
                            weeklyKills,
                            lifetimeWins,
                            weeklyWins).lines());
                }
            }
        };
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
