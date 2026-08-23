package net.swofty.type.skywarslobby.level;

import net.swofty.commons.text.Text;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Static registry for SkyWars level progression data.
 * Should be initialized during server startup.
 */
public class SkywarsLevelRegistry {
    private static SkywarsLevelCategory levelCategory;
    private static boolean initialized = false;

    /**
     * Initialize the level registry by loading from YAML
     */
    public static void initialize() {
        if (initialized) {
            Logger.warn("SkywarsLevelRegistry already initialized, skipping...");
            return;
        }

        levelCategory = SkywarsLevelLoader.loadFromFile();
        initialized = true;

        Logger.info("Initialized SkyWars Level Registry with " + levelCategory.getLevels().length + " levels");
    }

    /**
     * Check if the registry has been initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get all levels
     */
    public static List<SkywarsLevelCategory.SkywarsLevel> getAllLevels() {
        ensureInitialized();
        return Arrays.asList(levelCategory.getLevels());
    }

    /**
     * Get a specific level by level number
     */
    public static @Nullable SkywarsLevelCategory.SkywarsLevel getLevel(int level) {
        ensureInitialized();
        for (SkywarsLevelCategory.SkywarsLevel l : levelCategory.getLevels()) {
            if (l.level() == level) {
                return l;
            }
        }
        return null;
    }

    /**
     * Calculate the player's current level based on their XP
     */
    public static int calculateLevel(long xp) {
        ensureInitialized();
        return levelCategory.calculateLevel(xp);
    }

    /**
     * Get the cumulative XP required for a specific level
     */
    public static long getCumulativeXPForLevel(int level) {
        ensureInitialized();
        return levelCategory.getCumulativeXPForLevel(level);
    }

    /**
     * Get progress towards the next level (0.0 to 1.0)
     */
    public static double getProgressToNextLevel(long xp) {
        ensureInitialized();
        return levelCategory.getProgressToNextLevel(xp);
    }

    /**
     * Get XP progress into the current level
     */
    public static long getXPIntoCurrentLevel(long xp) {
        ensureInitialized();
        return levelCategory.getXPIntoCurrentLevel(xp);
    }

    /**
     * Get XP needed for the next level
     */
    public static long getXPForNextLevel(long xp) {
        ensureInitialized();
        return levelCategory.getXPForNextLevel(xp);
    }

    /**
     * Get the maximum level available
     */
    public static int getMaxLevel() {
        ensureInitialized();
        SkywarsLevelCategory.SkywarsLevel[] levels = levelCategory.getLevels();
        if (levels.length == 0) return 1;
        return levels[levels.length - 1].level();
    }

    /**
     * Get levels for a specific page (25 levels per page)
     * Page 1: levels 1-25
     * Page 2: levels 26-50
     * Page 3: levels 51-75
     */
    public static List<SkywarsLevelCategory.SkywarsLevel> getLevelsForPage(int page) {
        ensureInitialized();
        int startLevel = (page - 1) * 25 + 1;
        int endLevel = page * 25;

        return getAllLevels().stream()
                .filter(l -> l.level() >= startLevel && l.level() <= endLevel)
                .toList();
    }

    /**
     * Get the total number of pages (25 levels per page)
     */
    public static int getTotalPages() {
        ensureInitialized();
        int maxLevel = getMaxLevel();
        return (int) Math.ceil(maxLevel / 25.0);
    }

    /**
     * Check if a player has unlocked a specific level
     */
    public static boolean hasUnlockedLevel(long xp, int level) {
        return calculateLevel(xp) >= level;
    }

    /**
     * Get the prestige level data for a player's current level
     */
    public static @Nullable SkywarsLevelCategory.SkywarsLevel getCurrentPrestige(long xp) {
        ensureInitialized();
        int currentLevel = calculateLevel(xp);

        // Find the highest prestige level at or below current level
        SkywarsLevelCategory.SkywarsLevel prestige = null;
        for (SkywarsLevelCategory.SkywarsLevel level : levelCategory.getLevels()) {
            if (level.isPrestige() && level.level() <= currentLevel) {
                prestige = level;
            }
        }
        return prestige;
    }

    /**
     * Generate the progress bar string for display
     */
    public static String generateProgressBar(long xp) {
        double progress = getProgressToNextLevel(xp);
        int filledBars = (int) (progress * 10);
        int emptyBars = 10 - filledBars;

        return Text.of("<8>[<b>{}<7>{}<8>]",
                "\u25A0".repeat(filledBars),
                "\u25A0".repeat(emptyBars)).serialize();
    }

    /**
     * Format XP display (e.g., "550/1,000" or "5k/5k")
     */
    public static Text formatXPDisplay(long xp) {
        long currentProgress = getXPIntoCurrentLevel(xp);
        long xpNeeded = getXPForNextLevel(xp);

        return Text.of("{}<7>/<a>{}",
                SkywarsLevelCategory.formatXPRequirement(currentProgress),
                SkywarsLevelCategory.formatXPRequirement(xpNeeded));
    }

    /**
     * Ensure the registry is initialized before use
     */
    private static void ensureInitialized() {
        if (!initialized) {
            Logger.warn("SkywarsLevelRegistry not initialized, initializing now...");
            initialize();
        }
    }
}
