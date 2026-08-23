package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.AchievementDefinition;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.achievement.AchievementStatisticsService;
import net.swofty.type.generic.achievement.AchievementTier;
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

public class GUITieredAchievements extends HypixelInventoryGUI {
    private static final int ACHIEVEMENTS_PER_PAGE = 7;
    private static final int[][] COLUMN_SLOTS = {
            {1, 10, 19, 28, 37},
            {2, 11, 20, 29, 38},
            {3, 12, 21, 30, 39},
            {4, 13, 22, 31, 40},
            {5, 14, 23, 32, 41},
            {6, 15, 24, 33, 42},
            {7, 16, 25, 34, 43}
    };

    private static final String UNLOCKED_HEAD = "9631597dce4e4051e8d5a543641966ab54fbf25a0ed6047f11e6140d88bf48f";
    private static final String LOCKED_HEAD = "967a2f218a6e6e38f2b545f6c17733f4ef9bbb288e75402949c052189ee";

    private final AchievementCategory category;
    private int page = 0;
    private SortMode sortMode = SortMode.A_TO_Z;

    private enum SortMode {
        A_TO_Z("A to Z"),
        Z_TO_A("Z to A");

        private final String display;

        SortMode(String display) {
            this.display = display;
        }
    }

    public GUITieredAchievements(AchievementCategory category) {
        super(Text.literal(category.getDisplayName() + " Tiered Achievements"), InventoryType.CHEST_6_ROW);
        this.category = category;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        PlayerAchievementHandler handler = player.getAchievementHandler();

        List<AchievementDefinition> tieredAchievements = AchievementRegistry.getByCategory(category).stream()
                .filter(a -> a.getType() == AchievementType.TIERED)
                .toList();

        tieredAchievements = sortAchievements(tieredAchievements);

        int totalTiers = AchievementRegistry.getTierCount(category);
        int unlockedTiers = handler.getUnlockedCount(category, AchievementType.TIERED);
        int totalPoints = handler.getPoints(category, AchievementType.TIERED);
        int maxPoints = AchievementRegistry.getTotalPoints(category, AchievementType.TIERED);
        double unlockedPercent = totalTiers > 0 ? (unlockedTiers * 100.0 / totalTiers) : 0;
        double pointsPercent = maxPoints > 0 ? (totalPoints * 100.0 / maxPoints) : 0;

        int startIndex = page * ACHIEVEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ACHIEVEMENTS_PER_PAGE, tieredAchievements.size());

        for (int col = 0; col < ACHIEVEMENTS_PER_PAGE; col++) {
            int achievementIndex = startIndex + col;
            if (achievementIndex >= endIndex) break;

            AchievementDefinition achievement = tieredAchievements.get(achievementIndex);
            populateAchievementColumn(col, achievement, handler);
        }

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
                        <a>Tiered Achievements
                        <8>{}
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)""",
                        category.getDisplayName(),
                        unlockedTiers, totalTiers, (int) unlockedPercent,
                        totalPoints, maxPoints, (int) pointsPercent);
            }
        });

        set(new GUIClickableItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND, """
                        <a>Go to Challenge Achievements
                        <7>Click to view {} Challenge
                        <7>Achievements.""", category.getDisplayName());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIChallengeAchievements(category).open(player);
            }
        });

        set(new GUIClickableItem(51) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String nextSort = sortMode == SortMode.A_TO_Z ? "Z to A" : "A to Z";
                return ItemStacks.item(Material.HOPPER, """
                        <6>Sorted by: <a>{0}
                        <7>Sorts by name from {0}.

                        <7>Next sort: <a>{1}
                        <e>Left click to use!""", sortMode.display, nextSort);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                sortMode = sortMode == SortMode.A_TO_Z ? SortMode.Z_TO_A : SortMode.A_TO_Z;
                page = 0;
                open(player);
            }
        });

        int maxPages = getMaxPages(tieredAchievements.size());
        if (maxPages > 1) {
            if (page > 0) {
                set(new GUIClickableItem(45) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer player) {
                        return ItemStacks.item(Material.ARROW, "<a>Previous Page");
                    }

                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                        page--;
                        open(player);
                    }
                });
            }
            if (page < maxPages - 1) {
                set(new GUIClickableItem(53) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer player) {
                        return ItemStacks.item(Material.ARROW, "<a>Next Page");
                    }

                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                        page++;
                        open(player);
                    }
                });
            }
        }

        updateItemStacks(getInventory(), player);
    }

    private List<AchievementDefinition> sortAchievements(List<AchievementDefinition> achievements) {
        List<AchievementDefinition> sorted = new ArrayList<>(achievements);
        Comparator<AchievementDefinition> comparator = Comparator.comparing(AchievementDefinition::getName);
        if (sortMode == SortMode.Z_TO_A) {
            comparator = comparator.reversed();
        }
        sorted.sort(comparator);
        return sorted;
    }

    private void populateAchievementColumn(int column, AchievementDefinition achievement, PlayerAchievementHandler handler) {
        List<AchievementTier> tiers = achievement.getTiers();
        if (tiers == null || tiers.isEmpty()) return;

        int currentTier = handler.getAchievementTier(achievement.getId());
        int currentProgress = handler.getProgress(achievement.getId());
        int[] slots = COLUMN_SLOTS[column];

        boolean isTracked = handler.isTracked(achievement.getId());
        boolean isFullyCompleted = handler.hasFullyCompletedAchievement(achievement.getId());

        for (int row = 0; row < 5; row++) {
            int tierNum = 5 - row;
            int slot = slots[row];

            if (tierNum > tiers.size()) {
                continue;
            }

            AchievementTier tier = tiers.get(tierNum - 1);
            boolean unlocked = currentTier >= tierNum;
            boolean isCurrent = currentTier == tierNum - 1 && !unlocked;

            set(createTierItem(slot, achievement, tier, tierNum, unlocked, isCurrent, currentProgress, row == 0, isTracked, isFullyCompleted));
        }
    }

    private GUIClickableItem createTierItem(int slot, AchievementDefinition achievement, AchievementTier tier,
                                             int tierNum, boolean unlocked, boolean isCurrent, int currentProgress,
                                             boolean isTopRow, boolean isTracked, boolean isFullyCompleted) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                Text tierName = Text.of((unlocked ? "<a>" : "<c>") + "{} {}",
                        achievement.getName(), AchievementTier.toRomanNumeral(tierNum));

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<f>{}", achievement.getDescription()));
                lore.add(Text.empty());

                if (unlocked) {
                    lore.add(Text.of("<7>Progress: <a>DONE! <7>(<a>{:,}<7>)", currentProgress));
                } else {
                    lore.add(Text.of("<7>Progress: <a>{:,}<7>/<a>{:,}", currentProgress, tier.goal()));
                }

                lore.add(Text.of("<7>Reward:"));
                lore.add(Text.of("<8>+<e>{} <7>Achievement Points", tier.points()));
                lore.add(Text.empty());

                if (unlocked) {
                    lore.add(Text.of("<a>Tier unlocked!"));
                } else {
                    lore.add(Text.of("<c>Tier locked!"));
                }

                lore.add(Text.empty());
                String unlockPct = AchievementStatisticsService.getFormattedPercentage(achievement.getId(), tierNum);
                lore.add(Text.of("<7>Unlocked by <f>{}<7>% of players!", unlockPct));

                if (!isFullyCompleted) {
                    lore.add(Text.empty());
                    if (isTracked) {
                        lore.add(Text.of("<6>Currently tracking this achievement!"));
                        lore.add(Text.of("<e>Click to stop tracking."));
                    } else {
                        lore.add(Text.of("<7>Track this achievement to gain progress."));
                        lore.add(Text.of("<e>Click to track!"));
                    }
                }

                ItemStack.Builder builder;

                if (isTopRow) {
                    String texture = unlocked ? UNLOCKED_HEAD : LOCKED_HEAD;
                    builder = ItemStacks.head(texture, tierName, lore);
                } else {
                    Material mat;
                    if (unlocked) {
                        mat = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
                    } else if (isCurrent) {
                        mat = Material.YELLOW_STAINED_GLASS_PANE;
                    } else {
                        mat = Material.GRAY_STAINED_GLASS_PANE;
                    }

                    builder = ItemStacks.item(mat, 1, tierName, lore);
                }

                if (isTracked) {
                    builder = ItemStacks.enchanted(builder);
                }

                return builder;
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (isFullyCompleted) {
                    player.sendMessage("<c>This achievement is already fully completed!");
                    return;
                }

                player.getAchievementHandler().toggleTracking(achievement.getId());
                open(player);
            }
        };
    }

    private int getMaxPages(int totalAchievements) {
        return Math.max(1, (totalAchievements + ACHIEVEMENTS_PER_PAGE - 1) / ACHIEVEMENTS_PER_PAGE);
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
