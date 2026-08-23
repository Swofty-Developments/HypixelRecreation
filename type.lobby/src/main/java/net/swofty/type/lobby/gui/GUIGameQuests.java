package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.quest.QuestData;
import net.swofty.type.generic.quest.QuestDefinition;
import net.swofty.type.generic.quest.QuestRegistry;
import net.swofty.type.generic.quest.QuestType;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIGameQuests extends HypixelInventoryGUI {
    private final AchievementCategory category;

    public GUIGameQuests(AchievementCategory category) {
        super(Text.literal(category.getDisplayName() + " Quests"), InventoryType.CHEST_6_ROW);
        this.category = category;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        QuestData questData = player.getQuestHandler().getQuestData();

        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.of(category.getMaterial(), 1, """
                        <a>{0} Quests & Challenges
                        <7>View all available quests and challenges
                        <7>that you can complete by playing {0}.""", category.getDisplayName());
            }
        });

        populateDailyQuests(player, questData);
        populateWeeklyQuests(player, questData);
        populateSpecialDailyQuests(player, questData);
        populateChallengeQuests(player, questData);

        set(new GUIClickableItem(45) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int unlocked = player.getAchievementHandler().getUnlockedCount(category);
                int total = AchievementRegistry.getByCategory(category).size();
                int points = player.getAchievementHandler().getTotalPoints(category);
                int maxPoints = AchievementRegistry.getTotalPoints(category);
                int unlockedPercent = total > 0 ? (int) (unlocked * 100.0 / total) : 0;
                int pointsPercent = maxPoints > 0 ? (int) (points * 100.0 / maxPoints) : 0;

                return ItemStacks.item(Material.DIAMOND, """
                        <a>{} Achievements
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)

                        <e>Click to view achievements!""",
                        category.getDisplayName(),
                        unlocked, total, unlockedPercent,
                        points, maxPoints, pointsPercent);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameAchievements(category).open(player);
            }
        });

        set(new GUIClickableItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To Quests & Challenges""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIQuestsChallenges().open(player);
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

    private void populateDailyQuests(HypixelPlayer player, QuestData questData) {
        List<QuestDefinition> dailyQuests = QuestRegistry.getByCategory(category, QuestType.DAILY);
        int[] dailySlots = {9, 10, 11, 12};

        for (int i = 0; i < dailySlots.length; i++) {
            int slot = dailySlots[i];

            if (i < dailyQuests.size()) {
                QuestDefinition quest = dailyQuests.get(i);
                int progress = questData.getProgress(quest.getId());
                int goal = quest.getGoal();
                boolean completed = questData.isCompleted(quest.getId());
                boolean active = questData.isActive(quest.getId());

                set(new GUIClickableItem(slot) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        List<Text> lore = new ArrayList<>();
                        lore.add(Text.of("<7>{}<b> (<6>{}<b>/<6>{}<b>)", quest.getDescription(), progress, goal));
                        lore.add(Text.empty());
                        lore.add(Text.of("<7>Rewards:"));

                        if (quest.getReward() != null) {
                            if (quest.getReward().getHypixelExperience() > 0) {
                                lore.add(Text.of("<8>+<3>{}<7> Hypixel Experience", quest.getReward().getHypixelExperience()));
                            }
                            if (quest.getReward().getGameExperience() > 0) {
                                lore.add(Text.of("<8>+<b>{}<7> {} Experience", quest.getReward().getGameExperience(), category.getDisplayName()));
                            }
                            if (quest.getReward().getCoins() > 0) {
                                lore.add(Text.of("<8>+<6>{}<7> Coins", quest.getReward().getCoins()));
                            }
                        }

                        lore.add(Text.empty());
                        lore.add(Text.of("<8><o>Daily Quests can be completed once every"));
                        lore.add(Text.of("<8><o>day."));
                        lore.add(Text.empty());

                        if (completed) {
                            lore.add(Text.of("<a><l>COMPLETED!"));
                        } else if (active) {
                            lore.add(Text.of("<a>You've already started this quest!"));
                        } else {
                            lore.add(Text.of("<e>Click to start this quest!"));
                        }

                        return ItemStacks.item(Material.PAPER, 1,
                                Text.of("<a>Daily Quest: {}", quest.getName()), lore);
                    }

                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                        if (!questData.isActive(quest.getId()) && !questData.isCompleted(quest.getId())) {
                            p.getQuestHandler().startQuest(quest.getId());
                            p.sendMessage("<a>Quest started: <e>{}", quest.getName());
                            open(p);
                        }
                    }
                });
            }
        }
    }

    private void populateWeeklyQuests(HypixelPlayer player, QuestData questData) {
        List<QuestDefinition> weeklyQuests = QuestRegistry.getByCategory(category, QuestType.WEEKLY);
        int[] weeklySlots = {14, 15, 16, 17};

        for (int i = 0; i < weeklySlots.length; i++) {
            int slot = weeklySlots[i];

            if (i < weeklyQuests.size()) {
                QuestDefinition quest = weeklyQuests.get(i);
                int progress = questData.getProgress(quest.getId());
                int goal = quest.getGoal();
                boolean completed = questData.isCompleted(quest.getId());
                boolean active = questData.isActive(quest.getId());

                set(new GUIClickableItem(slot) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        List<Text> lore = new ArrayList<>();
                        lore.add(Text.of("<7>{}<b> (<6>{}<b>/<6>{}<b>)", quest.getDescription(), progress, goal));
                        lore.add(Text.empty());
                        lore.add(Text.of("<7>Rewards:"));

                        if (quest.getReward() != null) {
                            if (quest.getReward().getHypixelExperience() > 0) {
                                lore.add(Text.of("<8>+<3>{}<7> Hypixel Experience", quest.getReward().getHypixelExperience()));
                            }
                            if (quest.getReward().getGameExperience() > 0) {
                                lore.add(Text.of("<8>+<b>{}<7> {} Experience", quest.getReward().getGameExperience(), category.getDisplayName()));
                            }
                            if (quest.getReward().getCoins() > 0) {
                                lore.add(Text.of("<8>+<6>{}<7> Coins", quest.getReward().getCoins()));
                            }
                        }

                        lore.add(Text.empty());
                        lore.add(Text.of("<8><o>Weekly Quests can be completed once every"));
                        lore.add(Text.of("<8><o>week. Resets Thursday night."));
                        lore.add(Text.empty());

                        if (completed) {
                            lore.add(Text.of("<a><l>COMPLETED!"));
                        } else if (active) {
                            lore.add(Text.of("<a>You've already started this quest!"));
                        } else {
                            lore.add(Text.of("<e>Click to start this quest!"));
                        }

                        return ItemStacks.item(Material.PAPER, 1,
                                Text.of("<a>Weekly Quest: {}", quest.getName()), lore);
                    }

                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                        if (!questData.isActive(quest.getId()) && !questData.isCompleted(quest.getId())) {
                            p.getQuestHandler().startQuest(quest.getId());
                            p.sendMessage("<a>Quest started: <e>{}", quest.getName());
                            open(p);
                        }
                    }
                });
            }
        }
    }

    private void populateSpecialDailyQuests(HypixelPlayer player, QuestData questData) {
        List<QuestDefinition> specialDailyQuests = QuestRegistry.getByCategory(category, QuestType.SPECIAL_DAILY);
        int[] specialSlots = {22};

        for (int i = 0; i < specialSlots.length && i < specialDailyQuests.size(); i++) {
            int slot = specialSlots[i];
            QuestDefinition quest = specialDailyQuests.get(i);
            int progress = questData.getProgress(quest.getId());
            int goal = quest.getGoal();
            boolean completed = questData.isCompleted(quest.getId());
            boolean active = questData.isActive(quest.getId());

            set(new GUIClickableItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.of("<7>{}<b> (<6>{}<b>/<6>{}<b>)", quest.getDescription(), progress, goal));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>Rewards:"));

                    if (quest.getReward() != null) {
                        if (quest.getReward().getHypixelExperience() > 0) {
                            lore.add(Text.of("<8>+<3>{:,}<7> Hypixel Experience", quest.getReward().getHypixelExperience()));
                        }
                        if (quest.getReward().getGameExperience() > 0) {
                            lore.add(Text.of("<8>+<b>{}<7> {} Experience", quest.getReward().getGameExperience(), category.getDisplayName()));
                        }
                        if (quest.getReward().getCoins() > 0) {
                            lore.add(Text.of("<8>+<6>{}<7> Coins", quest.getReward().getCoins()));
                        }
                    }

                    lore.add(Text.empty());
                    lore.add(Text.of("<8><o>Daily Quests can be completed once every"));
                    lore.add(Text.of("<8><o>day."));
                    lore.add(Text.empty());

                    if (completed) {
                        lore.add(Text.of("<a><l>COMPLETED!"));
                    } else if (active) {
                        lore.add(Text.of("<a>You've already started this quest!"));
                    } else {
                        lore.add(Text.of("<e>Click to start this quest!"));
                    }

                    Text name = Text.of("<a>Special Daily: {}", quest.getName());
                    if (quest.getHeadTexture() != null) {
                        return ItemStacks.head(quest.getHeadTexture(), 1, name, lore);
                    } else {
                        return ItemStacks.item(Material.PAPER, 1, name, lore);
                    }
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    if (!questData.isActive(quest.getId()) && !questData.isCompleted(quest.getId())) {
                        p.getQuestHandler().startQuest(quest.getId());
                        p.sendMessage("<a>Quest started: <e>{}", quest.getName());
                        open(p);
                    }
                }
            });
        }
    }

    private void populateChallengeQuests(HypixelPlayer player, QuestData questData) {
        List<QuestDefinition> challengeQuests = QuestRegistry.getByCategory(category, QuestType.CHALLENGE);
        int[] challengeSlots = {38, 39, 41, 42};
        int challengesRemaining = questData.getRemainingChallenges();

        for (int i = 0; i < challengeSlots.length && i < challengeQuests.size(); i++) {
            int slot = challengeSlots[i];
            QuestDefinition quest = challengeQuests.get(i);

            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.of("<7>{}", quest.getDescription()));
                    lore.add(Text.empty());

                    if (quest.getReward() != null && quest.getReward().getHypixelExperience() > 0) {
                        lore.add(Text.of("<7>Reward: <8>+<3>{}<7> Hypixel Experience", quest.getReward().getHypixelExperience()));
                    }

                    lore.add(Text.empty());
                    lore.add(Text.of("<8><o>You can complete the same challenge"));
                    lore.add(Text.of("<8><o>multiple times per day, but only"));
                    lore.add(Text.of("<8><o>once per game."));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>Challenges remaining today: <a>{}", challengesRemaining));

                    return ItemStacks.item(Material.MAP, 1,
                            Text.of("<a>{}", quest.getName()), lore);
                }
            });
        }
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
