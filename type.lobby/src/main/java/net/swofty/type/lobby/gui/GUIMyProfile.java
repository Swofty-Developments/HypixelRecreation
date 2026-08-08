package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.command.commands.replay.ReplaysCommand;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
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
                    return ItemStackCreator.createNamedItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                }
            });
        }

        set(new GUIItem(2) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String displayName = player.getFullDisplayName();
                return ItemStackCreator.getStackHead(
                    displayName,
                    player.getSkin(),
                    1,
                    "§7Hypixel Level: §6" + level,
                    "§7Achievement Points: §e" + StringUtility.commaify(achievementPoints),
                    "§7Guild: §bNone"
                );
            }
        });
        set(new GUIClickableItem(3) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStackHead(
                    "§aFriends",
                    "e063eedb2184354bd43a19deffba51b53dd6b7222f8388caa239cabcdce84",
                    1,
                    "§7View your Hypixel friends' profiles,",
                    "§7and interact with your online friends!",
                    "",
                    "§eClick to view!"
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIFriends().open(player);
            }
        });
        set(new GUIClickableItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStackHead(
                    "§aParty",
                    "667963ca1ffdc24a10b397ff8161d0da82d6a3f4788d5f67f1a9f9bfbc1eb1",
                    1,
                    "§7Create a party and join up with",
                    "§7other players to play games",
                    "§7together!",
                    "",
                    "§eClick to manage!"
                );
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
                return ItemStackCreator.getStackHead(
                    "§aGuild",
                    "fe8b59f8cce510809427c3843cf575fae8fe6a8b7d1560dd46958d148563815",
                    1,
                    "§7Form a guild with other Hypixel",
                    "§7players to conquer game modes and",
                    "§7work towards common Hypixel",
                    "§7rewards."
                );
            }
        });
        set(new GUIItem(6) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStackHead(
                    "§aRecent Players",
                    "9993a356809532d696841a37a0549b81b159b79a7b2919cff4e5abdfea83d66",
                    1,
                    "§7View players you have played recent",
                    "§7games with."
                );
            }
        });
        set(new GUIItem(20) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aGo to Housing",
                    Material.DARK_OAK_DOOR,
                    1
                );
            }
        });
        set(new GUIItem(21) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStackHead(
                    "§aSocial Media",
                    "3685a0be743e9067de95cd8c6d1ba21ab21d37371b3d597211bb75e43279",
                    1,
                    "§7Click to edit your Social Media links."
                );
            }
        });
        set(new GUIItem(22) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStackHead(
                    "§aCharacter Information",
                    player.getSkin(),
                    1,
                    "§7Rank: " + player.getLegacyRankPrefix().trim(),
                    "§7Level: §6" + level,
                    "§7Experience until next Level: §6" + StringUtility.commaify(xpNeeded),
                    "§7Achievement Points: §e" + StringUtility.commaify(achievementPoints),
                    "§7Mystery Dust: §b0",
                    "§7Quests Completed: §60",
                    "§7Karma: §d0",
                    "§7Hypixel Gold: §60",
                    "",
                    "§eClick to see the Hypixel Store link."
                );
            }
        });
        set(new GUIItem(23) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aStats Viewer",
                    Material.PAPER,
                    1,
                    "§7Showcases your stats for each",
                    "§7game and an overview of all.",
                    "",
                    "§7Players ranked §bMVP §7or higher",
                    "§7can use §f/stats (username) §7to view",
                    "§7other players' stats.",
                    "",
                    "§eClick to view your stats!"
                );
            }
        });
        set(new GUIItem(24) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aCoin Boosters",
                    Material.POTION,
                    1,
                    "§7Activate your personal and",
                    "§7network boosters for extra",
                    "§7coins.",
                    "",
                    "§eClick to activate boosters!"
                );
            }
        });
        set(new GUIItem(29) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aCustomize Appearances",
                    Material.LEATHER_CHESTPLATE,
                    1,
                    "",
                    "§7Customize the following visual options",
                    "§7for your player!",
                    "§f∙ MVP+ Rank Color",
                    "§f∙ Punch Messages",
                    "§f∙ Glow",
                    "§f∙ Status",
                    "",
                    "§eClick to view!"
                );
            }
        });

        set(new GUIClickableItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aAchievements",
                    Material.DIAMOND,
                    1,
                    "§7Track your progress as you unlock",
                    "§7Achievements and rack up points.",
                    "",
                    "§7Total Points: §e" + StringUtility.commaify(achievementPoints),
                    "",
                    "§eClick to view your achievements!"
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIAchievementsMenu().open(player);
            }
        });

        set(new GUIClickableItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String progressBar = createProgressBar(progress, 40);
                int progressPercent = (int) (progress * 100);

                return ItemStackCreator.getStack(
                    "§aHypixel Leveling",
                    Material.BREWING_STAND,
                    1,
                    "§7Playing games and completing quests",
                    "§7will reward you with §3Hypixel",
                    "§3Experience§7, which is required to",
                    "§7level up and acquire new perks and",
                    "§7rewards!",
                    "",
                    "§3Hypixel Level §a" + level + " §3" + progressBar + " §3" + progressPercent + "%",
                    "",
                    "§7Experience until next level: §3" + StringUtility.commaify(xpNeeded),
                    "",
                    "§eClick to see your rewards!"
                );
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

                return ItemStackCreator.getStack(
                    "§aQuests & Challenges",
                    Material.ENCHANTED_BOOK,
                    1,
                    "§7Completing quests and challenges",
                    "§7will reward you with §6Coins§7, §3Hypixel",
                    "§3Experience§7 and more!",
                    "",
                    "§7You can complete a maximum of §a15 ",
                    "§7challenges every day.",
                    "",
                    "§7Challenges completed today: §a" + challengesCompleted,
                    "",
                    "§eClick to view Quests & Challenges."
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIQuestsChallenges().open(player);
            }
        });

        set(new GUIItem(33) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aSettings & Visibility",
                    Material.COMPARATOR,
                    1,
                    "§7Allows you to edit and control",
                    "§7various personal settings.",
                    "",
                    "§eClick to edit your settings!"
                );
            }
        });

        set(new GUIClickableItem(39) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                ReplaysCommand.displaySendReplay(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aRecent Games",
                    Material.BOOK,
                    1,
                    "§7View your recently played games.",
                    "",
                    "§eClick to view!"
                );
            }
        });
        set(new GUIItem(40) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aAccount Status",
                    Material.ANVIL,
                    1,
                    "§7Check your punishment history and",
                    "§7see where you stand.",
                    "",
                    "§eClick to view!"
                );
            }
        });
        set(new GUIClickableItem(41) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.openView(new GUISelectLanguage(), new GUISelectLanguage.State());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStackHead(
                    "§aSelect Language",
                    "98daa1e3ed94ff3e33e1d4c6e43f024c47d78a57ba4d38e75e7c9264106",
                    1,
                    "§7Change your language.",
                    "",
                    "§7Currently available:",
                    "§7   ∙ §fEnglish",
                        "§7   ∙ §fFinnish",
                    "",
                    "§7More languages coming soon!",
                    "",
                    "§eClick to change your language!"
                );
            }
        });
        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aHypixel Store",
                    Material.GOLD_INGOT,
                    1,
                    "§7View the Hypixel Store from right",
                    "§7here in-game!",
                    "",
                    "§7Your Hypixel Gold: §60",
                    "",
                    "§eClick to view!"
                );
            }
        });
        set(new GUIItem(53) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStack(
                    "§aEvent Shop",
                    Material.EMERALD,
                    1,
                    "§7Level up during events by playing",
                    "§7games and completing quests.",
                    "",
                    "§7Earn §fEvent Silver §7when you gain an",
                    "§7Event Level. §fSilver §7can be used to",
                    "§7purchase event-themed cosmetics!",
                    "",
                    "§eClick to view shop!"
                );
            }
        });
        updateItemStacks(getInventory(), player);
    }

    private String createProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            bar.append(i < filled ? "|" : "§8|");
        }
        return bar.toString();
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
