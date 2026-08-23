package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.commands.replay.ReplaysCommand;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.guild.GuildManager;
import net.swofty.type.generic.quest.QuestData;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIMyProfile extends HypixelInventoryGUI {
    private static final int[] COLOURED_PANE_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17
    };

    public GUIMyProfile() {
        super("My Profile", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        PlayerExperienceHandler xpHandler = player.getExperienceHandler();
        QuestData questData = player.getQuestHandler().getQuestData();

        int level = xpHandler.getLevel();
        int achievementPoints = player.getAchievementHandler().getTotalPoints();
        double progress = xpHandler.getProgressToNextLevel();
        long xpNeeded = xpHandler.getXPForNextLevel() - xpHandler.getXPInCurrentLevel();

        for (int slot : COLOURED_PANE_SLOTS) {
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.named(Material.ORANGE_STAINED_GLASS_PANE, "");
                }
            });
        }

        set(new GUIItem(2) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head(
                    player.getSkin(),
                    player.getFullDisplayName(),
                    Text.of("""
                            <7>Hypixel Level: <6>{}
                            <7>Achievement Points: <e>{:,}
                            <7>Guild: <b>None""", level, achievementPoints).lines()
                );
            }
        });
        set(new GUIClickableItem(3) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("e063eedb2184354bd43a19deffba51b53dd6b7222f8388caa239cabcdce84", """
                        <a>Friends
                        <7>View your Hypixel friends' profiles,
                        <7>and interact with your online friends!

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIFriends().open(player);
            }
        });
        set(new GUIClickableItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("667963ca1ffdc24a10b397ff8161d0da82d6a3f4788d5f67f1a9f9bfbc1eb1", """
                        <a>Party
                        <7>Create a party and join up with
                        <7>other players to play games
                        <7>together!

                        <e>Click to manage!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIParty().open(player);
            }
        });
        set(new GUIClickableItem(5) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                Thread.startVirtualThread(() -> {
                    player.openView(new GUIGuild(), new GUIGuild.GuildState(GuildManager.getGuildFromPlayer(player)));
                });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("fe8b59f8cce510809427c3843cf575fae8fe6a8b7d1560dd46958d148563815", """
                        <a>Guild
                        <7>Form a guild with other Hypixel
                        <7>players to conquer game modes and
                        <7>work towards common Hypixel
                        <7>rewards.""");
            }
        });
        set(new GUIItem(6) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("9993a356809532d696841a37a0549b81b159b79a7b2919cff4e5abdfea83d66", """
                        <a>Recent Players
                        <7>View players you have played recent
                        <7>games with.""");
            }
        });
        set(new GUIItem(20) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DARK_OAK_DOOR, "<a>Go to Housing");
            }
        });
        set(new GUIItem(21) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("3685a0be743e9067de95cd8c6d1ba21ab21d37371b3d597211bb75e43279", """
                        <a>Social Media
                        <7>Click to edit your Social Media links.""");
            }
        });
        set(new GUIItem(22) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head(player.getSkin(), """
                        <a>Character Information
                        <7>Rank: {}
                        <7>Level: <6>{}
                        <7>Experience until next Level: <6>{:,}
                        <7>Achievement Points: <e>{:,}
                        <7>Mystery Dust: <b>0
                        <7>Quests Completed: <6>0
                        <7>Karma: <d>0
                        <7>Hypixel Gold: <6>0

                        <e>Click to see the Hypixel Store link.""",
                        player.getRankPrefix(), level, xpNeeded, achievementPoints);
            }
        });
        set(new GUIItem(23) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.PAPER, """
                        <a>Stats Viewer
                        <7>Showcases your stats for each
                        <7>game and an overview of all.

                        <7>Players ranked <b>MVP <7>or higher
                        <7>can use <f>/stats (username) <7>to view
                        <7>other players' stats.

                        <e>Click to view your stats!""");
            }
        });
        set(new GUIItem(24) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.POTION, """
                        <a>Coin Boosters
                        <7>Activate your personal and
                        <7>network boosters for extra
                        <7>coins.

                        <e>Click to activate boosters!""");
            }
        });
        set(new GUIItem(29) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.LEATHER_CHESTPLATE, """
                        <a>Customize Appearances

                        <7>Customize the following visual options
                        <7>for your player!
                        <f>∙ MVP+ Rank Color
                        <f>∙ Punch Messages
                        <f>∙ Glow
                        <f>∙ Status

                        <e>Click to view!""");
            }
        });

        set(new GUIClickableItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND, """
                        <a>Achievements
                        <7>Track your progress as you unlock
                        <7>Achievements and rack up points.

                        <7>Total Points: <e>{:,}

                        <e>Click to view your achievements!""", achievementPoints);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIAchievementsMenu().open(player);
            }
        });

        set(new GUIClickableItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                Text progressBar = createProgressBar(progress, 40);
                int progressPercent = (int) (progress * 100);

                return ItemStacks.item(Material.BREWING_STAND, """
                        <a>Hypixel Leveling
                        <7>Playing games and completing quests
                        <7>will reward you with <3>Hypixel
                        <3>Experience<7>, which is required to
                        <7>level up and acquire new perks and
                        <7>rewards!

                        <3>Hypixel Level <a>{} {} <3>{}%

                        <7>Experience until next level: <3>{:,}

                        <e>Click to see your rewards!""",
                        level, progressBar, progressPercent, xpNeeded);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIHypixelLeveling().open(player);
            }
        });

        set(new GUIClickableItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int challengesCompleted = 15 - questData.getRemainingChallenges();

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

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIQuestsChallenges().open(player);
            }
        });

        set(new GUIItem(33) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.COMPARATOR, """
                        <a>Settings & Visibility
                        <7>Allows you to edit and control
                        <7>various personal settings.

                        <e>Click to edit your settings!""");
            }
        });

        set(new GUIClickableItem(39) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                ReplaysCommand.displaySendReplay(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BOOK, """
                        <a>Recent Games
                        <7>View your recently played games.

                        <e>Click to view!""");
            }
        });
        set(new GUIItem(40) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ANVIL, """
                        <a>Account Status
                        <7>Check your punishment history and
                        <7>see where you stand.

                        <e>Click to view!""");
            }
        });
        set(new GUIClickableItem(41) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.openView(new GUISelectLanguage(), new GUISelectLanguage.State());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("98daa1e3ed94ff3e33e1d4c6e43f024c47d78a57ba4d38e75e7c9264106", """
                        <a>Select Language
                        <7>Change your language.

                        <7>Currently available:
                        <7>   ∙ <f>English
                        <7>   ∙ <f>Finnish

                        <7>More languages coming soon!

                        <e>Click to change your language!""");
            }
        });
        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GOLD_INGOT, """
                        <a>Hypixel Store
                        <7>View the Hypixel Store from right
                        <7>here in-game!

                        <7>Your Hypixel Gold: <6>0

                        <e>Click to view!""");
            }
        });
        set(new GUIItem(53) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.EMERALD, """
                        <a>Event Shop
                        <7>Level up during events by playing
                        <7>games and completing quests.

                        <7>Earn <f>Event Silver <7>when you gain an
                        <7>Event Level. <f>Silver <7>can be used to
                        <7>purchase event-themed cosmetics!

                        <e>Click to view shop!""");
            }
        });
        updateItemStacks(getInventory(), player);
    }

    private Text createProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        return Text.of("<3>{}<8>{}", "|".repeat(filled), "|".repeat(length - filled));
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
