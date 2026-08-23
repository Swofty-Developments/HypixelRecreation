package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIQuestsChallenges extends HypixelInventoryGUI {

    public GUIQuestsChallenges() {
        super("Quests & Challenges", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();

        set(new GUIClickableItem(10) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.IRON_BARS, """
                        <a>Cops and Crims Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Cops and
                        <7>Crims.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.COPS_AND_CRIMS).open(player);
            }
        });

        set(new GUIClickableItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND_SWORD, """
                        <a>Blitz SG Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Blitz SG.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.BLITZ_SG).open(player);
            }
        });

        set(new GUIClickableItem(12) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.SAND, """
                        <a>The Walls Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing The Walls.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.THE_WALLS).open(player);
            }
        });

        set(new GUIClickableItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.SNOWBALL, """
                        <a>Paintball Warfare Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Paintball
                        <7>Warfare.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.PAINTBALL).open(player);
            }
        });

        set(new GUIClickableItem(14) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.WITHER_SKELETON_SKULL, """
                        <a>VampireZ Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing VampireZ.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.VAMPIREZ).open(player);
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.SLIME_BALL, """
                        <a>Arcade Games Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Arcade
                        <7>Games.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.ARCADE).open(player);
            }
        });

        set(new GUIClickableItem(16) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.FIREWORK_ROCKET, """
                        <a>Quakecraft Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing
                        <7>Quakecraft.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.QUAKECRAFT).open(player);
            }
        });

        set(new GUIClickableItem(19) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.SOUL_SAND, """
                        <a>Mega Walls Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Mega
                        <7>Walls.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.MEGA_WALLS).open(player);
            }
        });

        set(new GUIClickableItem(20) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.TNT, """
                        <a>The TNT Games Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing The TNT
                        <7>Games.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.TNT_GAMES).open(player);
            }
        });

        set(new GUIClickableItem(21) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BLAZE_POWDER, """
                        <a>Arena Brawl Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Arena
                        <7>Brawl.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.ARENA_BRAWL).open(player);
            }
        });

        set(new GUIClickableItem(22) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GOLDEN_APPLE, """
                        <a>UHC Champions Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing UHC
                        <7>Champions.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.UHC_CHAMPIONS).open(player);
            }
        });

        set(new GUIClickableItem(23) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.STONE_AXE, """
                        <a>Warlords Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Warlords.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.WARLORDS).open(player);
            }
        });

        set(new GUIClickableItem(24) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ENDER_EYE, """
                        <a>SkyWars Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing SkyWars.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.SKYWARS).open(player);
            }
        });

        set(new GUIClickableItem(25) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.MINECART, """
                        <a>Turbo Kart Racers Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Turbo
                        <7>Kart Racers.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.TURBO_KART_RACERS).open(player);
            }
        });

        set(new GUIClickableItem(28) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("d29a9f57267ed342a13e3ad3a240c4c5af5a1a36ab2de0d6c2a31af0e3cdde", """
                        <a>Smash Heroes Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Smash
                        <7>Heroes.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.SMASH_HEROES).open(player);
            }
        });

        set(new GUIClickableItem(29) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GOLDEN_CARROT, """
                        <a>Speed UHC Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Speed
                        <7>UHC.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.SPEED_UHC).open(player);
            }
        });

        set(new GUIClickableItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.RED_BED, """
                        <a>Bed Wars Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Bed Wars.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.BEDWARS).open(player);
            }
        });

        set(new GUIClickableItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BOW, """
                        <a>Murder Mystery Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Murder
                        <7>Mystery.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.MURDER_MYSTERY).open(player);
            }
        });

        set(new GUIClickableItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.CRAFTING_TABLE, """
                        <a>Build Battle Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Build
                        <7>Battle.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.BUILD_BATTLE).open(player);
            }
        });

        set(new GUIClickableItem(33) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.FISHING_ROD, """
                        <a>Duels Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Duels.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.DUELS).open(player);
            }
        });

        set(new GUIClickableItem(34) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIRT, """
                        <a>Pit Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Pit.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.PIT).open(player);
            }
        });

        set(new GUIClickableItem(37) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.WHITE_WOOL, """
                        <a>Wool Games Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing Wool
                        <7>Games.

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameQuests(AchievementCategory.WOOL_GAMES).open(player);
            }
        });

        set(new GUIClickableItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To My Profile""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIMyProfile().open(player);
            }
        });

        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int challengesCompleted = player.getQuestHandler().getQuestData().getDailyChallengesCompleted();
                return ItemStacks.item(Material.ENCHANTED_BOOK, """
                        <a>Quests & Challenges
                        <7>Completing quests and challenges
                        <7>will reward you with <6>Coins<7>, <3>Hypixel
                        <3>Experience<7> and more!

                        <7>You can complete a maximum of <a>15\s
                        <7>challenges every day.

                        <7>Challenges completed today: <a>{}

                        <e>Click to view Quests & Challenges.""", challengesCompleted);
            }
        });

        set(new GUIClickableItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BREWING_STAND, """
                        <a>Hypixel Leveling
                        <7>Playing games and completing quests
                        <7>will reward you with <3>Hypixel
                        <3>Experience<7>, which is required to
                        <7>level up and acquire new perks and
                        <7>rewards!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIHypixelLeveling().open(player);
            }
        });

        set(new GUIClickableItem(53) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                boolean hasMvpPlus = player.getRank().isEqualOrHigherThan(Rank.MVP_PLUS);

                if (hasMvpPlus) {
                    boolean isEnabled = player.getToggles().get(DatapointToggles.Toggles.ToggleType.AUTO_ACCEPT_QUESTS);
                    if (isEnabled) {
                        return ItemStacks.item(Material.LIME_DYE, """
                        <a>Auto-Accept Quests: <a>ON
                        <7>Quests will be automatically
                        <7>accepted whenever you join a
                        <7>game lobby.

                        <e>Click to disable!""");
                    } else {
                        return ItemStacks.item(Material.GRAY_DYE, """
                        <a>Auto-Accept Quests: <c>OFF
                        <7>Click to automatically accept
                        <7>quests whenever you join a
                        <7>game lobby.

                        <e>Click to enable!""");
                    }
                } else {
                    return ItemStacks.item(Material.GRAY_DYE, """
                        <a>Auto-Accept Quests: <c>OFF
                        <7>Click to automatically accept
                        <7>quests whenever you join a
                        <7>game lobby.

                        <7>Requires <b>MVP<c>+""");
                }
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (player.getRank().isEqualOrHigherThan(Rank.MVP_PLUS)) {
                    boolean newValue = player.getToggles().inverse(DatapointToggles.Toggles.ToggleType.AUTO_ACCEPT_QUESTS);
                    if (newValue) {
                        player.sendMessage("<a>Auto-Accept Quests enabled!");
                    } else {
                        player.sendMessage("<c>Auto-Accept Quests disabled.");
                    }
                    open(player);
                } else {
                    player.sendMessage("<c>This feature requires MVP+!");
                }
            }
        });

        updateItemStacks(getInventory(), player);
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
