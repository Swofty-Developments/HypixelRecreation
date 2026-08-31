package net.swofty.dungeons.catacombs.loot;

import net.swofty.dungeons.catacombs.CatacombsFloor;

import java.util.Arrays;
import java.util.List;

public enum CatacombsRewardChest {
    WOOD(0),
    GOLD(0),
    DIAMOND(0),
    EMERALD(230),
    OBSIDIAN(270),
    BEDROCK(300);

    private final int requiredScore;

    CatacombsRewardChest(int requiredScore) {
        this.requiredScore = requiredScore;
    }

    public boolean available(CatacombsFloor floor, int score, int classMilestone) {
        if (classMilestone < 2 || score < requiredScore) return false;
        return this != BEDROCK || floor.ordinal() >= CatacombsFloor.FLOOR_FIVE.ordinal();
    }

    public int baseCost(CatacombsFloor floor) {
        int bracket = floor == CatacombsFloor.FLOOR_ONE ? 0
                : floor == CatacombsFloor.FLOOR_TWO ? 1 : 2;
        return switch (this) {
            case WOOD -> 0;
            case GOLD -> new int[]{25_000, 50_000, 100_000}[bracket];
            case DIAMOND -> new int[]{50_000, 100_000, 250_000}[bracket];
            case EMERALD -> new int[]{100_000, 250_000, 500_000}[bracket];
            case OBSIDIAN -> new int[]{250_000, 500_000, 1_000_000}[bracket];
            case BEDROCK -> 2_000_000;
        };
    }

    public static List<CatacombsRewardChest> availableChests(CatacombsFloor floor, int score, int classMilestone) {
        return Arrays.stream(values()).filter(chest -> chest.available(floor, score, classMilestone)).toList();
    }
}
