package net.swofty.dungeons.catacombs.loot;

import net.kyori.adventure.key.Key;

import java.util.Set;

public record CatacombsRewardEntry<T>(
        Key id,
        T reward,
        double weight,
        Set<CatacombsRewardChest> chests,
        int requiredQuality,
        int addedChestCost
) {
    public CatacombsRewardEntry {
        if (id == null) throw new IllegalArgumentException("Reward id cannot be null");
        if (reward == null) throw new IllegalArgumentException("Reward cannot be null");
        if (!Double.isFinite(weight) || weight < 0) throw new IllegalArgumentException("Weight must be non-negative");
        chests = Set.copyOf(chests);
        if (requiredQuality < 0 || addedChestCost < 0)
            throw new IllegalArgumentException("Quality and cost cannot be negative");
    }
}
