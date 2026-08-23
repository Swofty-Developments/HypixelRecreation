package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.AchievementDefinition;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.achievement.AchievementStatisticsService;
import net.swofty.type.generic.achievement.AchievementType;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIChallengeAchievements extends HypixelInventoryGUI {
    private static final int ACHIEVEMENTS_PER_PAGE = 21;
    private static final int[] ACHIEVEMENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final AchievementCategory category;
    private int page = 0;
    private SortMode sortMode = SortMode.A_TO_Z;
    private CompletionSort completionSort = CompletionSort.NO_SORT;

    private enum SortMode {
        A_TO_Z("A to Z", "Sorts by name from A to Z.", "Z to A"),
        Z_TO_A("Z to A", "Sorts by name from Z to A.", "A to Z");

        private final String display;
        private final String description;
        private final String next;

        SortMode(String display, String description, String next) {
            this.display = display;
            this.description = description;
            this.next = next;
        }
    }

    private enum CompletionSort {
        NO_SORT("No Sort"),
        UNLOCKED_FIRST("Unlocked First"),
        LOCKED_FIRST("Locked First");

        private final String display;

        CompletionSort(String display) {
            this.display = display;
        }
    }

    public GUIChallengeAchievements(AchievementCategory category) {
        super(Text.literal(category.getDisplayName() + " Challenge Achievements"), InventoryType.CHEST_6_ROW);
        this.category = category;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        PlayerAchievementHandler handler = player.getAchievementHandler();

        List<AchievementDefinition> achievements = AchievementRegistry.getByCategory(category).stream()
                .filter(a -> a.getType() == AchievementType.CHALLENGE)
                .toList();

        achievements = sortAchievements(achievements, handler);

        int totalCount = achievements.size();
        int unlockedCount = handler.getUnlockedCount(category, AchievementType.CHALLENGE);
        int totalPoints = handler.getPoints(category, AchievementType.CHALLENGE);
        int maxPoints = AchievementRegistry.getTotalPoints(category, AchievementType.CHALLENGE);
        double unlockedPercent = totalCount > 0 ? (unlockedCount * 100.0 / totalCount) : 0;
        double pointsPercent = maxPoints > 0 ? (totalPoints * 100.0 / maxPoints) : 0;

        populateAchievements(achievements, handler);

        set(new GUIClickableItem(47) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.HOPPER, """
                        <6>Sorted by: <a>{}
                        <7>{}

                        <7>Next sort: <a>{}
                        <e>Left click to use!

                        <7>Completion sort: <8>{}
                        <e>Right click to cycle!""",
                        sortMode.display, sortMode.description, sortMode.next, completionSort.display);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                boolean isRightClick = e.getClick() instanceof Click.Right;
                if (isRightClick) {
                    completionSort = CompletionSort.values()[(completionSort.ordinal() + 1) % CompletionSort.values().length];
                } else {
                    sortMode = sortMode == SortMode.A_TO_Z ? SortMode.Z_TO_A : SortMode.A_TO_Z;
                }
                page = 0;
                open(player);
            }
        });

        set(new GUIClickableItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To {} Achievements""", category.getDisplayName());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameAchievements(category).open(player);
            }
        });

        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.of(category.getMaterial(), 1, """
                        <a>Challenge Achievements
                        <8>{}
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)""",
                        category.getDisplayName(),
                        unlockedCount, totalCount, (int) unlockedPercent,
                        totalPoints, maxPoints, (int) pointsPercent);
            }
        });

        set(new GUIClickableItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND_BLOCK, """
                        <a>Go to Tiered Achievements
                        <7>Click to view {} Tiered
                        <7>Achievements.""", category.getDisplayName());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUITieredAchievements(category).open(player);
            }
        });

        int maxPages = getMaxPages(totalCount);
        if (maxPages > 1) {
            set(new GUIClickableItem(53) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    if (page == 0 && maxPages > 1) {
                        return ItemStacks.item(Material.ARROW, """
                                <e>Left-click for next page!
                                <b>Right-click for last page!""");
                    } else if (page == maxPages - 1) {
                        return ItemStacks.item(Material.ARROW, """
                                <e>Left-click for first page!
                                <b>Right-click for previous page!""");
                    } else {
                        return ItemStacks.item(Material.ARROW, """
                                <e>Left-click for next page!
                                <b>Right-click for previous page!""");
                    }
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    boolean isRightClick = e.getClick() instanceof Click.Right;
                    if (isRightClick) {
                        page = page > 0 ? page - 1 : maxPages - 1;
                    } else {
                        page = page < maxPages - 1 ? page + 1 : 0;
                    }
                    open(player);
                }
            });
        }

        updateItemStacks(getInventory(), player);
    }

    private List<AchievementDefinition> sortAchievements(List<AchievementDefinition> achievements, PlayerAchievementHandler handler) {
        List<AchievementDefinition> sorted = new ArrayList<>(achievements);

        Comparator<AchievementDefinition> comparator = Comparator.comparing(AchievementDefinition::getName);
        if (sortMode == SortMode.Z_TO_A) {
            comparator = comparator.reversed();
        }

        if (completionSort != CompletionSort.NO_SORT) {
            Comparator<AchievementDefinition> completionComparator = (a, b) -> {
                boolean aUnlocked = handler.hasAchievement(a.getId());
                boolean bUnlocked = handler.hasAchievement(b.getId());
                if (aUnlocked == bUnlocked) return 0;
                if (completionSort == CompletionSort.UNLOCKED_FIRST) {
                    return aUnlocked ? -1 : 1;
                } else {
                    return aUnlocked ? 1 : -1;
                }
            };
            comparator = completionComparator.thenComparing(comparator);
        }

        sorted.sort(comparator);
        return sorted;
    }

    private void populateAchievements(List<AchievementDefinition> achievements, PlayerAchievementHandler handler) {
        int startIndex = page * ACHIEVEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ACHIEVEMENTS_PER_PAGE, achievements.size());

        for (int i = 0; i < ACHIEVEMENT_SLOTS.length; i++) {
            int achievementIndex = startIndex + i;
            if (achievementIndex >= endIndex) break;

            AchievementDefinition achievement = achievements.get(achievementIndex);
            int slot = ACHIEVEMENT_SLOTS[i];

            set(createAchievementItem(slot, achievement, handler));
        }
    }

    private GUIItem createAchievementItem(int slot, AchievementDefinition achievement, PlayerAchievementHandler handler) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                boolean unlocked = handler.hasAchievement(achievement.getId());

                Material mat = unlocked ? Material.DIAMOND : Material.COAL;

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>{}", achievement.getDescription()));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Reward:"));
                lore.add(Text.of("<8>+<e>{} <7>Achievement Points", achievement.getPoints()));
                lore.add(Text.empty());

                String unlockPct = AchievementStatisticsService.getFormattedPercentage(achievement.getId());
                lore.add(Text.of("<7>Unlocked by <f>{}<7>% of players!", unlockPct));
                lore.add(Text.empty());

                if (unlocked) {
                    lore.add(Text.of("<a>Achievement unlocked!"));
                } else {
                    lore.add(Text.of("<c>Achievement locked!"));
                }

                return ItemStacks.item(
                        mat,
                        achievement.getPoints(),
                        Text.of((unlocked ? "<a>" : "<c>") + "{}", achievement.getName()),
                        lore
                );
            }
        };
    }

    private int getMaxPages(int totalCount) {
        return Math.max(1, (totalCount + ACHIEVEMENTS_PER_PAGE - 1) / ACHIEVEMENTS_PER_PAGE);
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
