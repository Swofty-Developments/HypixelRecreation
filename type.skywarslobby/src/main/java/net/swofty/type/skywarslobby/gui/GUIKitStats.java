package net.swofty.type.skywarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsKitStats;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.kit.SkywarsKit;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI showing per-kit statistics for a player.
 * Matches Hypixel's kit stats layout.
 */
public class GUIKitStats extends HypixelInventoryGUI {
    private final SkywarsKit kit;
    private final String mode;

    public GUIKitStats(SkywarsKit kit, String mode) {
        super(Text.of("{} Stats", kit.getName()), InventoryType.CHEST_6_ROW);
        this.kit = kit;
        this.mode = mode;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) return;

        long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();

        DatapointSkywarsKitStats.SkywarsKitStats kitStatsData = handler.get(
                SkywarsDataHandler.Data.KIT_STATS,
                DatapointSkywarsKitStats.class
        ).getValue();

        DatapointSkywarsKitStats.KitStatistics stats = kitStatsData.getStatsForKit(kit.getId());

        // Kit icon with starting items (slot 13)
        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>(kit.getItemsLore(mode));

                Text name = Text.of("<a>{}", kit.getName());
                if (kit.hasCustomTexture()) {
                    return ItemStacks.head(kit.getIconTexture(), name, lore);
                } else {
                    return ItemStacks.item(kit.getIconMaterial(), 1, name, lore);
                }
            }
        });

        // General Stats (slot 28)
        set(new GUIItem(28) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String timePlayed = stats.getFormattedTimePlayed();
                String fastestWin = stats.getFormattedFastestWin();

                return ItemStacks.item(Material.ITEM_FRAME, 1, """
                        <a>General Stats with {}
                        <7>Time Played: <a>{}

                        <7>Wins: <a>{}
                        <7>Fastest Win: <a>{}

                        <7>Most Kills in a Game: <a>{}
                        <7>Mobs Killed: <a>{}
                        <7>Chests Opened: <a>{}

                        <7>Heads Gathered: <a>{}""",
                        kit.getName(), timePlayed, stats.getWins(), fastestWin,
                        stats.getMostKillsInGame(), stats.getMobsKilled(), stats.getChestsOpened(),
                        stats.getHeadsGathered());
            }
        });

        // Kills stats (slot 30)
        set(new GUIItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.REDSTONE, 1, """
                        <a>Kills with {}
                        <7>Kills: <a>{}
                        <7>Assists: <a>{}

                        <7>Melee Kills: <a>{}
                        <7>Bow Kills: <a>{}
                        <7>Void Kills: <a>{}
                        <7>Kills by Mobs: <a>{}""",
                        kit.getName(), stats.getKills(), stats.getAssists(), stats.getMeleeKills(),
                        stats.getBowKills(), stats.getVoidKills(), stats.getMobKills());
            }
        });

        // Archery stats (slot 32)
        set(new GUIItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BOW, 1, """
                        <a>Archery with {}
                        <7>Accuracy: <a>{}%
                        <7>Bow Kills: <a>{}

                        <7>Longest Bow Kill: <a>{} blocks
                        <7>Longest Bow Shot: <a>{} blocks""",
                        kit.getName(), String.format("%.0f", stats.getAccuracy()), stats.getBowKills(),
                        stats.getLongestBowKill(), stats.getLongestBowShot());
            }
        });

        // SkyWars Challenges (slot 34)
        set(new GUIItem(34) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BLAZE_POWDER, 1, """
                        <a>SkyWars Challenges with {}
                        <7>Archer Wins: <a>{}
                        <7>Half Health Wins: <a>{}
                        <7>No Block Wins: <a>{}
                        <7>No Chest Wins: <a>{}
                        <7>Paper Wins: <a>{}
                        <7>Rookie Wins: <a>{}
                        <7>UHC Wins: <a>{}
                        <7>Ultimate Warrior Wins: <a>{}""",
                        kit.getName(), stats.getArcherWins(), stats.getHalfHealthWins(),
                        stats.getNoBlockWins(), stats.getNoChestWins(), stats.getPaperWins(),
                        stats.getRookieWins(), stats.getUhcWins(), stats.getUltimateWarriorWins());
            }
        });

        // Go Back (slot 48)
        set(new GUIClickableItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, 1, """
                        <a>Go Back
                        <7>To {} Kit""", kit.getName());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitBreakdown(kit, mode).open(player);
            }
        });

        // Total Coins (slot 49)
        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.EMERALD, 1, """
                        <7>Total Coins: <6>{:,}
                        <6>https://store.hypixel.net""", coins);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
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
