package net.swofty.type.lobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.achievement.AchievementType;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIGameAchievements extends HypixelInventoryGUI {
    private final AchievementCategory category;

    public GUIGameAchievements(AchievementCategory category) {
        super(Text.literal(category.getDisplayName() + " Achievements"), InventoryType.CHEST_4_ROW);
        this.category = category;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        PlayerAchievementHandler handler = player.getAchievementHandler();

        int totalUnlocked = handler.getUnlockedCount(category);
        int totalCount = AchievementRegistry.getByCategory(category).size();
        int totalPoints = handler.getTotalPoints(category);
        int maxPoints = AchievementRegistry.getTotalPoints(category);
        double unlockedPercent = totalCount > 0 ? (totalUnlocked * 100.0 / totalCount) : 0;
        double pointsPercent = maxPoints > 0 ? (totalPoints * 100.0 / maxPoints) : 0;

        set(new GUIClickableItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int unlocked = handler.getUnlockedCount(category, AchievementType.CHALLENGE);
                int total = AchievementRegistry.getCount(category, AchievementType.CHALLENGE);
                int points = handler.getPoints(category, AchievementType.CHALLENGE);
                int maxPts = AchievementRegistry.getTotalPoints(category, AchievementType.CHALLENGE);
                double uPercent = total > 0 ? (unlocked * 100.0 / total) : 0;
                double pPercent = maxPts > 0 ? (points * 100.0 / maxPts) : 0;

                return ItemStacks.item(Material.DIAMOND, """
                        <a>Challenge Achievements
                        <8>{}
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)

                        <7>Challenge achievements may be
                        <7>completed a single time.

                        <e>Click to view achievements!""",
                        category.getDisplayName(),
                        unlocked, total, (int) uPercent,
                        points, maxPts, (int) pPercent);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIChallengeAchievements(category).open(player);
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int unlocked = handler.getUnlockedCount(category, AchievementType.TIERED);
                int total = AchievementRegistry.getTierCount(category);
                int points = handler.getPoints(category, AchievementType.TIERED);
                int maxPts = AchievementRegistry.getTotalPoints(category, AchievementType.TIERED);
                double uPercent = total > 0 ? (unlocked * 100.0 / total) : 0;
                double pPercent = maxPts > 0 ? (points * 100.0 / maxPts) : 0;

                return ItemStacks.item(Material.DIAMOND_BLOCK, """
                        <a>Tiered Achievements
                        <8>{}
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)

                        <7>Tiered achievements are completed
                        <7>over multiple tiers.

                        <e>Click to view achievements!""",
                        category.getDisplayName(),
                        unlocked, total, (int) uPercent,
                        points, maxPts, (int) pPercent);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUITieredAchievements(category).open(player);
            }
        });

        set(new GUIClickableItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To Achievements""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIAchievementsMenu().open(player);
            }
        });

        set(new GUIItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.of(category.getMaterial(), 1, """
                        <a>Total Completion
                        <8>{}
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)""",
                        category.getDisplayName(),
                        totalUnlocked, totalCount, (int) unlockedPercent,
                        totalPoints, maxPoints, (int) pointsPercent);
            }
        });

        set(new GUIClickableItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.MAGMA_CREAM, """
                        <a>Seasonal Challenge Achievements
                        <7>View challenge achievements for {}
                        <7>that are exclusive to seasonal
                        <7>events.

                        <8>These achievements do not count
                        <8>towards total game completion.

                        <e>Click to view achievements!""", category.getDisplayName());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUISeasonalAchievements(category).open(player);
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
