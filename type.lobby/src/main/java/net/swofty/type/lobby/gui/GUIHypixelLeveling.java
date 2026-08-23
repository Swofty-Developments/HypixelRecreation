package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.experience.LevelReward;
import net.swofty.type.generic.experience.LevelRewardRegistry;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIHypixelLeveling extends HypixelInventoryGUI {
    private static final int LEVELS_PER_PAGE = 25;
    private static final int[] REWARD_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            19, 20, 21, 22, 23, 24, 25
    };

    private static final double[] MULTIPLIERS = {1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};
    private static final int[] MULTIPLIER_LEVELS = {10, 25, 50, 75, 100, 125, 150, 250};

    private int page = 0;

    public GUIHypixelLeveling() {
        super("Hypixel Leveling", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        PlayerExperienceHandler xpHandler = player.getExperienceHandler();

        int currentLevel = xpHandler.getLevel();
        double progress = xpHandler.getProgressToNextLevel();
        long xpNeeded = xpHandler.getXPForNextLevel() - xpHandler.getXPInCurrentLevel();

        populateLevelRewards(currentLevel);

        if (page > 0) {
            set(new GUIClickableItem(18) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, "<a>Page {}", page);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    page--;
                    open(player);
                }
            });
        }

        if (page < getMaxPages() - 1) {
            set(new GUIClickableItem(26) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, "<a>Page {}", page + 2);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    page++;
                    open(player);
                }
            });
        }

        for (int i = 0; i < MULTIPLIERS.length; i++) {
            int slot = 36 + i;
            double mult = MULTIPLIERS[i];
            int reqLevel = MULTIPLIER_LEVELS[i];
            boolean unlocked = currentLevel >= reqLevel;

            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.GOLD_BLOCK, 1,
                            Text.of("<6>{}x <a>Coin Multiplier", mult),
                            List.of(
                                    Text.empty(),
                                    Text.of("<7>Increases the amount of coins you"),
                                    Text.of("<7>earn when playing games."),
                                    Text.empty(),
                                    Text.of("<8><o>Automatically unlocks upon reaching"),
                                    Text.of("<8><o>the required level."),
                                    Text.empty(),
                                    unlocked ? Text.of("<a>Unlocked!") : Text.of("<c>Requires Level {}", reqLevel)));
                }
            });
        }

        set(new GUIClickableItem(44) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                boolean canAccess = currentLevel >= 100;
                return ItemStacks.item(Material.BEACON, 1,
                        Text.of("<6>Veteran Rewards"),
                        List.of(
                                Text.of("<7>Rewards for the most dedicated"),
                                Text.of("<7>players!"),
                                Text.empty(),
                                canAccess ? Text.of("<e>Click to view!") : Text.of("<c>You must be Hypixel Level 100 or"),
                                canAccess ? Text.empty() : Text.of("<c>higher to access this menu!")));
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (currentLevel >= 100) {
                    player.sendMessage("<6>Veteran Rewards coming soon!");
                } else {
                    player.sendMessage("<c>You must be Hypixel Level 100 or higher!");
                }
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

                        <7>Experience until next level: <3>{:,}""",
                        currentLevel, progressBar, progressPercent, xpNeeded);
            }
        });

        set(new GUIClickableItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ENCHANTED_BOOK, """
                        <a>Quest Log
                        <7>Completing quests will reward you
                        <7>with <6>Coins<7>, <3>Hypixel Experience <7>and
                        <7>more!

                        <7>Talk to <b>Quest Masters <7>located in
                        <7>game lobbies to accept quests.

                        <e>Click to view quest progress!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIQuestsChallenges().open(player);
            }
        });

        updateItemStacks(getInventory(), player);
    }

    private void populateLevelRewards(int playerLevel) {
        int startLevel = page * LEVELS_PER_PAGE + 1;

        for (int i = 0; i < REWARD_SLOTS.length; i++) {
            int level = startLevel + i;
            int slot = REWARD_SLOTS[i];

            set(createLevelRewardItem(slot, level, playerLevel));
        }
    }

    private GUIItem createLevelRewardItem(int slot, int level, int playerLevel) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                boolean claimed = playerLevel >= level;
                LevelReward reward = LevelRewardRegistry.get(level);

                List<Text> lore = new ArrayList<>();
                lore.add(Text.empty());

                if (reward != null && reward.hasAnyReward()) {
                    if (reward.hasCoins()) {
                        lore.add(Text.of("<8>+<6>{:,} <7>Arcade Coins", reward.getCoins()));
                        lore.add(Text.empty());
                        lore.add(Text.of("<8><o>Arcade Coins can be exchanged for"));
                        lore.add(Text.of("<8><o>other game coins inside the Arcade"));
                        lore.add(Text.of("<8><o>Lobby."));
                    }
                    if (reward.hasDust()) {
                        if (reward.getMysteryDust() > 0) {
                            lore.add(Text.of("<8>+<b>{} Mystery Dust", reward.getMysteryDust()));
                        }
                        if (reward.getDust() > 0) {
                            lore.add(Text.of("<8>+<b>{} Dust", reward.getDust()));
                        }
                    }
                    if (reward.hasBooster()) {
                        lore.add(Text.of("<8>+<7>2x <6>2.0x <7>Personal Coin Booster"));
                        lore.add(Text.of("<7>(<b>{}<7>)", reward.getBoosterDurationDisplay()));
                        lore.add(Text.empty());
                        lore.add(Text.of("<8><o>To activate a coin booster, go to"));
                        lore.add(Text.of("<a>My Profile > Coin Boosters <8><o>or type"));
                        lore.add(Text.of("<b><o>/booster"));
                    }
                    if (reward.hasSpecialRewards()) {
                        for (String special : reward.getSpecialRewards()) {
                            lore.add(Text.of("<8>+<7>{}", formatSpecialReward(special)));
                            lore.add(Text.empty());
                            lore.add(Text.of("<8><o>Type <b>/rankcolor <8>to change the"));
                            lore.add(Text.of("<8><o>color and view all options."));
                        }
                    }
                } else {
                    int coins = 40000 + (level * 1000);
                    lore.add(Text.of("<8>+<6>{:,} <7>Arcade Coins", coins));
                    lore.add(Text.empty());
                    lore.add(Text.of("<8><o>Arcade Coins can be exchanged for"));
                    lore.add(Text.of("<8><o>other game coins inside the Arcade"));
                    lore.add(Text.of("<8><o>Lobby."));
                }

                lore.add(Text.empty());
                if (claimed) {
                    lore.add(Text.of("<a>You have already claimed this reward!"));
                } else {
                    lore.add(Text.of("<e>Reach Level {} to claim!", level));
                }

                return ItemStacks.item(
                        claimed ? Material.MINECART : Material.CHEST_MINECART,
                        1,
                        Text.of((claimed ? "<c>" : "<a>") + "Hypixel Level Reward {}", level),
                        lore
                );
            }
        };
    }

    private Text formatSpecialReward(String reward) {
        return switch (reward) {
            case "rankcolor_yellow" -> Text.of("Yellow <e>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_light_purple" -> Text.of("Light Purple <d>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_white" -> Text.of("White <f>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_aqua" -> Text.of("Aqua <b>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_dark_green" -> Text.of("Dark Green <2>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_dark_aqua" -> Text.of("Dark Aqua <3>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_dark_red" -> Text.of("Dark Red <4>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_dark_purple" -> Text.of("Dark Purple <5>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_gold" -> Text.of("Gold <6>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_gray" -> Text.of("Gray <7>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_dark_gray" -> Text.of("Dark Gray <8>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_blue" -> Text.of("Blue <9>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_green" -> Text.of("Green <a>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_red" -> Text.of("Red <c>+ <7>option for <b>[MVP<c>+<b>]");
            case "rankcolor_black" -> Text.of("Black <0>+ <7>option for <b>[MVP<c>+<b>]");
            case "multiplier_1.5x" -> Text.of("1.5x Coin Multiplier");
            case "multiplier_2.0x" -> Text.of("2.0x Coin Multiplier");
            case "multiplier_2.5x" -> Text.of("2.5x Coin Multiplier");
            case "multiplier_3.0x" -> Text.of("3.0x Coin Multiplier");
            case "multiplier_3.5x" -> Text.of("3.5x Coin Multiplier");
            case "multiplier_4.0x" -> Text.of("4.0x Coin Multiplier");
            case "multiplier_4.5x" -> Text.of("4.5x Coin Multiplier");
            case "multiplier_5.0x" -> Text.of("5.0x Coin Multiplier");
            case "veteran_status" -> Text.of("Veteran Status");
            default -> Text.literal(reward);
        };
    }

    private Text createProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        return Text.of("<3>{}<8>{}", "|".repeat(filled), "|".repeat(length - filled));
    }

    private int getMaxPages() {
        int maxLevel = Math.max(LevelRewardRegistry.getMaxRewardLevel(), 250);
        return (maxLevel + LEVELS_PER_PAGE - 1) / LEVELS_PER_PAGE;
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
