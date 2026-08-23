package net.swofty.type.skywarslobby.level;

import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the SkyWars level progression system.
 * Contains data structures for levels, rewards, and progression tracking.
 */
public interface SkywarsLevelCategory {

    /**
     * Get all level data
     */
    SkywarsLevel[] getLevels();

    /**
     * Calculate the player's current level based on their XP
     */
    default int calculateLevel(long xp) {
        SkywarsLevel[] levels = getLevels();
        int currentLevel = 1;
        long cumulativeXP = 0;

        for (SkywarsLevel level : levels) {
            cumulativeXP += level.requirement();
            if (xp >= cumulativeXP) {
                currentLevel = level.level();
            } else {
                break;
            }
        }
        return currentLevel;
    }

    /**
     * Get the cumulative XP required for a specific level
     */
    default long getCumulativeXPForLevel(int targetLevel) {
        SkywarsLevel[] levels = getLevels();
        long cumulativeXP = 0;

        for (SkywarsLevel level : levels) {
            if (level.level() <= targetLevel) {
                cumulativeXP += level.requirement();
            } else {
                break;
            }
        }
        return cumulativeXP;
    }

    /**
     * Get progress towards the next level (0.0 to 1.0)
     */
    default double getProgressToNextLevel(long xp) {
        int currentLevel = calculateLevel(xp);
        SkywarsLevel[] levels = getLevels();

        if (currentLevel >= levels.length) {
            return 1.0; // Max level
        }

        long xpForCurrentLevel = getCumulativeXPForLevel(currentLevel);
        long xpForNextLevel = getCumulativeXPForLevel(currentLevel + 1);
        long xpIntoCurrentLevel = xp - xpForCurrentLevel;
        long xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;

        if (xpNeededForNextLevel <= 0) return 1.0;

        return Math.min(1.0, Math.max(0.0, (double) xpIntoCurrentLevel / xpNeededForNextLevel));
    }

    /**
     * Get XP progress into the current level
     */
    default long getXPIntoCurrentLevel(long xp) {
        int currentLevel = calculateLevel(xp);
        long xpForCurrentLevel = getCumulativeXPForLevel(currentLevel);
        return xp - xpForCurrentLevel;
    }

    /**
     * Get XP needed for the next level
     */
    default long getXPForNextLevel(long xp) {
        int currentLevel = calculateLevel(xp);
        SkywarsLevel[] levels = getLevels();

        if (currentLevel >= levels.length) {
            return 0; // Max level
        }

        for (SkywarsLevel level : levels) {
            if (level.level() == currentLevel + 1) {
                return level.requirement();
            }
        }
        return 0;
    }

    /**
     * Represents a single SkyWars level with its requirements and rewards
     */
    record SkywarsLevel(
            int level,
            long requirement,
            boolean isPrestige,
            @Nullable String prestigeName,
            @Nullable String prestigeColor,
            Material material,
            @Nullable String headTexture,
            List<Reward> rewards
    ) {
        /**
         * Get the level type description
         */
        public String getLevelType() {
            return isPrestige ? "Prestige Level" : "Normal Level";
        }

        /**
         * Format the level emblem for display
         */
        public Text getEmblem() {
            if (isPrestige && prestigeColor != null) {
                return Text.of("<color:{}>[{}\u272F]", prestigeColor, level);
            }
            return Text.of("<7>[{}\u272F]", level);
        }
    }

    /**
     * Base interface for all reward types
     */
    sealed interface Reward permits CoinReward, HypixelXPReward, TokenReward, OpalReward, PrestigeSchemeReward, FeatureUnlockReward {
        Text getDisplayLine();
    }

    /**
     * Coin reward
     */
    record CoinReward(int amount) implements Reward {
        @Override
        public Text getDisplayLine() {
            return Text.of(" <8>+<6>{} <7>SkyWars Coins", formatNumber(amount));
        }
    }

    /**
     * Hypixel network XP reward
     */
    record HypixelXPReward(int amount) implements Reward {
        @Override
        public Text getDisplayLine() {
            return Text.of(" <8>+<3>{}<7> Hypixel Experience", formatNumber(amount));
        }
    }

    /**
     * Token reward
     */
    record TokenReward(int amount) implements Reward {
        @Override
        public Text getDisplayLine() {
            return Text.of(" <8>+<2>{} <7>SkyWars Tokens", formatNumber(amount));
        }
    }

    /**
     * Opal reward
     */
    record OpalReward(int amount) implements Reward {
        @Override
        public Text getDisplayLine() {
            return Text.of(" <8>+<9>{} <7>Opal", amount);
        }
    }

    /**
     * Prestige scheme unlock reward
     */
    record PrestigeSchemeReward(String name, String colorCode, int level) implements Reward {
        @Override
        public Text getDisplayLine() {
            return Text.of(" <8>+<color:{}>[{}\u272F] <a>{} <7>Prestige Scheme", colorCode, level, name);
        }
    }

    /**
     * Feature unlock reward (e.g., Angel's Descent, Angel's Brewery)
     */
    record FeatureUnlockReward(String feature) implements Reward {
        @Override
        public Text getDisplayLine() {
            return switch (feature) {
                case "ANGELS_DESCENT" -> Text.of(" <8>+<b>Access to the Angel's Descent");
                case "ANGELS_BREWERY" -> Text.of(" <8>+<c>Access to Angel's Brewery");
                default -> Text.of(" <8>+<7>{}", feature);
            };
        }
    }

    /**
     * Format a number with commas for display
     */
    static String formatNumber(int number) {
        if (number >= 1000) {
            return String.format("%,d", number);
        }
        return String.valueOf(number);
    }

    /**
     * Format XP requirement for display (e.g., 1000 -> "1k", 2500 -> "2.5k")
     */
    static String formatXPRequirement(long xp) {
        if (xp >= 1000) {
            double kValue = xp / 1000.0;
            if (kValue == (int) kValue) {
                return (int) kValue + "k";
            }
            return String.format("%.1fk", kValue).replace(".0k", "k");
        }
        return String.valueOf(xp);
    }
}
