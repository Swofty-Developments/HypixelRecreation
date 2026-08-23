package net.swofty.type.skywarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skywars.SkywarsLeaderboardMode;
import net.swofty.commons.skywars.SkywarsLeaderboardPeriod;
import net.swofty.commons.skywars.SkywarsLeaderboardView;
import net.swofty.commons.skywars.SkywarsTextAlignment;
import net.swofty.commons.text.Text;
import net.swofty.type.skywarslobby.hologram.LeaderboardHologramManager;
import net.swofty.type.skywarslobby.hologram.LeaderboardHologramManager.PlayerLeaderboardState;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILeaderboardSettings extends HypixelInventoryGUI {
    private SkywarsLeaderboardMode selectedMode;
    private SkywarsLeaderboardPeriod selectedPeriod;
    private SkywarsLeaderboardView selectedView;
    private SkywarsTextAlignment selectedAlignment;

    public GUILeaderboardSettings() {
        super("Leaderboard Settings", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();

        PlayerLeaderboardState currentState = LeaderboardHologramManager.getState(player.getUuid());
        selectedMode = currentState.mode();
        selectedPeriod = currentState.period();
        selectedView = currentState.view();
        selectedAlignment = currentState.textAlignment();

        setupItems(player);
        updateItemStacks(getInventory(), player);
    }

    private void setupItems(HypixelPlayer player) {
        set(new GUIClickableItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.empty());
                for (SkywarsLeaderboardMode mode : SkywarsLeaderboardMode.values()) {
                    if (mode == selectedMode) {
                        lore.add(Text.of("<a>\u279C <7>{}", mode.getDisplayName()));
                    } else {
                        lore.add(Text.of("   <7>{}", mode.getDisplayName()));
                    }
                }
                lore.add(Text.empty());
                lore.add(Text.of("<8><o>This setting will save across lobbies."));
                lore.add(Text.empty());
                lore.add(Text.of("<8><o>Leaderboard data is cached and"));
                lore.add(Text.of("<8><o>does not update immediately."));
                lore.add(Text.empty());
                lore.add(Text.of("<e>Left/Right Click to change!"));

                return ItemStacks.item(Material.BOW, 1, Text.of("<a>Select the Mode!"), lore);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (e.getClick() instanceof Click.Left) {
                    selectedMode = selectedMode.next();
                } else if (e.getClick() instanceof Click.Right) {
                    selectedMode = selectedMode.previous();
                }
                setupItems(player);
                updateItemStacks(getInventory(), player);
            }
        });

        set(new GUIClickableItem(12) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.empty());
                for (SkywarsLeaderboardPeriod period : SkywarsLeaderboardPeriod.values()) {
                    if (period == selectedPeriod) {
                        lore.add(Text.of("<a>\u279C <7>{}", period.getDisplayName()));
                    } else {
                        lore.add(Text.of("   <7>{}", period.getDisplayName()));
                    }
                }
                lore.add(Text.empty());
                lore.add(Text.of("<8><o>This setting will save across lobbies."));
                lore.add(Text.empty());
                lore.add(Text.of("<8><o>Leaderboard data is cached and"));
                lore.add(Text.of("<8><o>does not update immediately."));
                lore.add(Text.empty());
                lore.add(Text.of("<e>Left/Right Click to change!"));

                return ItemStacks.item(Material.CLOCK, 1, Text.of("<a>Select the Time!"), lore);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (e.getClick() instanceof Click.Left) {
                    selectedPeriod = selectedPeriod.next();
                } else if (e.getClick() instanceof Click.Right) {
                    selectedPeriod = selectedPeriod.previous();
                }
                setupItems(player);
                updateItemStacks(getInventory(), player);
            }
        });

        set(new GUIClickableItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.empty());
                for (SkywarsLeaderboardView view : SkywarsLeaderboardView.values()) {
                    if (view == selectedView) {
                        lore.add(Text.of("<a>\u279C <7>{}", view.getDisplayName()));
                    } else {
                        lore.add(Text.of("   <7>{}", view.getDisplayName()));
                    }
                }
                lore.add(Text.empty());
                lore.add(Text.of("<8><o>Leaderboard data is cached and"));
                lore.add(Text.of("<8><o>does not update immediately."));
                lore.add(Text.empty());
                lore.add(Text.of("<e>Left/Right Click to change!"));

                return ItemStacks.item(Material.LADDER, 1, Text.of("<a>Select the View!"), lore);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (e.getClick() instanceof Click.Left) {
                    selectedView = selectedView.next();
                } else if (e.getClick() instanceof Click.Right) {
                    selectedView = selectedView.previous();
                }
                setupItems(player);
                updateItemStacks(getInventory(), player);
            }
        });

        set(new GUIClickableItem(14) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.SKELETON_SKULL, 1, """
                        <a>Select the Players!

                        <a>\u279C <7>All
                           <7>Friends
                           <7>Best Friends
                           <7>Guild Members

                        <8><o>Leaderboard data is cached and</o>
                        <8><o>does not update immediately.""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.sendMessage("<c>This feature is coming soon!");
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.empty());
                for (SkywarsTextAlignment alignment : SkywarsTextAlignment.values()) {
                    if (alignment == selectedAlignment) {
                        lore.add(Text.of("<a>\u279C <7>{}", alignment.getDisplayName()));
                    } else {
                        lore.add(Text.of("   <7>{}", alignment.getDisplayName()));
                    }
                }
                lore.add(Text.empty());
                lore.add(Text.of("<c>Block alignment is showing correctly"));
                lore.add(Text.of("<c>only for Vanilla Minecraft font sizes."));
                lore.add(Text.empty());
                lore.add(Text.of("<8><o>This setting will save across the"));
                lore.add(Text.of("<8><o>network."));
                lore.add(Text.empty());
                lore.add(Text.of("<8><o>Leaderboard data is cached and"));
                lore.add(Text.of("<8><o>does not update immediately."));
                lore.add(Text.empty());
                lore.add(Text.of("<e>Left/Right Click to change!"));

                return ItemStacks.item(Material.ITEM_FRAME, 1,
                        Text.of("<a>Select the Text Alignment!"), lore);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (e.getClick() instanceof Click.Left) {
                    selectedAlignment = selectedAlignment.next();
                } else if (e.getClick() instanceof Click.Right) {
                    selectedAlignment = selectedAlignment.previous();
                }
                setupItems(player);
                updateItemStacks(getInventory(), player);
            }
        });

        set(new GUIClickableItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GREEN_TERRACOTTA, 1, """
                        <a>Apply changes
                        <e>Click to apply the changes!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                PlayerLeaderboardState newState = new PlayerLeaderboardState(
                        selectedPeriod, selectedMode, selectedView, selectedAlignment);

                LeaderboardHologramManager.setState(player.getUuid(), newState);
                LeaderboardHologramManager.refreshAllHologramsForPlayer(player);

                player.sendMessage("<a>Leaderboard settings applied!");
                player.closeInventory();
            }
        });

        set(new GUIClickableItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.RED_TERRACOTTA, 1, """
                        <c>Discard changes
                        <e>Close the menu without applying
                        <e>changes!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.closeInventory();
            }
        });
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
