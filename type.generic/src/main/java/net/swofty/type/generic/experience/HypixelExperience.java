package net.swofty.type.generic.experience;

import net.swofty.commons.text.Text;

public class HypixelExperience {
    private static final int[] MULTIPLIER_LEVELS = {10, 25, 50, 75, 100, 150, 200, 250};
    private static final double[] MULTIPLIER_VALUES = {1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};

    public static long levelToTotalXP(int level) {
        if (level <= 1) return 0;
        long x = level - 1;
        return 1250L * x * x + 8750L * x;
    }

    public static int xpToLevel(long xp) {
        if (xp <= 0) return 1;
        double level = Math.floor(Math.sqrt(xp / 1250.0 + 12.25) - 3.5) + 1;
        return Math.max(1, (int) level);
    }

    public static long xpForNextLevel(long currentXP) {
        int currentLevel = xpToLevel(currentXP);
        return levelToTotalXP(currentLevel + 1) - currentXP;
    }

    public static long xpBetweenLevels(int level) {
        return levelToTotalXP(level + 1) - levelToTotalXP(level);
    }

    public static double progressToNextLevel(long currentXP) {
        int currentLevel = xpToLevel(currentXP);
        long levelStart = levelToTotalXP(currentLevel);
        long levelEnd = levelToTotalXP(currentLevel + 1);
        if (levelEnd == levelStart) return 0.0;
        return (double) (currentXP - levelStart) / (levelEnd - levelStart);
    }

    public static long getXPInCurrentLevel(long currentXP) {
        int currentLevel = xpToLevel(currentXP);
        return currentXP - levelToTotalXP(currentLevel);
    }

    public static double getCoinMultiplier(int level) {
        double multiplier = 1.0;
        for (int i = 0; i < MULTIPLIER_LEVELS.length; i++) {
            if (level >= MULTIPLIER_LEVELS[i]) {
                multiplier = MULTIPLIER_VALUES[i];
            } else {
                break;
            }
        }
        return multiplier;
    }

    public static int getNextMultiplierLevel(int level) {
        for (int multiplierLevel : MULTIPLIER_LEVELS) {
            if (level < multiplierLevel) {
                return multiplierLevel;
            }
        }
        return -1;
    }

    public static boolean isVeteran(int level) {
        return level >= 100;
    }

    public static Text formatLevel(int level) {
        String color;
        if (level >= 250) {
            color = "<6>";
        } else if (level >= 200) {
            color = "<d>";
        } else if (level >= 150) {
            color = "<b>";
        } else if (level >= 100) {
            color = "<a>";
        } else if (level >= 50) {
            color = "<e>";
        } else if (level >= 25) {
            color = "<2>";
        } else {
            color = "<7>";
        }
        return Text.of(color + "{}", level);
    }

    public static String formatXP(long xp) {
        return String.format("%,d", xp);
    }
}
