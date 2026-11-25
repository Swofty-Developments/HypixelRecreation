package net.swofty.type.skyblockgeneric.experimentation;

import net.swofty.commons.skyblock.item.Rarity;

public record SuperPairTile(String pairId, ExperimentReward reward, SuperPairItem item, int amount,
                            Rarity rewardRarity) {
    public SuperPairTile(String pairId, ExperimentReward reward, SuperPairItem item, int amount) {
        this(pairId, reward, item, amount, null);
    }

    public SuperPairTile {
        if (pairId == null || pairId.isBlank()) throw new IllegalArgumentException("Pair id cannot be blank");
        if (reward == null && (item == null || !item.isPowerUp())) {
            throw new IllegalArgumentException("A tile must contain a reward or a power-up");
        }
        if (reward != null && item != null && item.isPowerUp()) {
            throw new IllegalArgumentException("Reward tiles must contain a reward item");
        }
        if (amount < 1) throw new IllegalArgumentException("Pair amount must be positive");
        if (isPowerUp(item) && amount != 1) throw new IllegalArgumentException("Power-ups cannot have quantities");
        if (rewardRarity != null && reward != ExperimentReward.GUARDIAN_PET) {
            throw new IllegalArgumentException("Only Guardian Pet rewards can have a rarity override");
        }
        if (isPowerUp(item) && rewardRarity != null) {
            throw new IllegalArgumentException("Power-ups cannot have a rarity override");
        }
    }

    public boolean isPowerUp() {
        return isPowerUp(item);
    }

    public net.swofty.type.skyblockgeneric.item.SkyBlockItem createItem() {
        if (reward == null) throw new IllegalStateException("Power-ups do not have reward items");
        return reward.createItem(rewardRarity);
    }

    private static boolean isPowerUp(SuperPairItem item) {
        return item != null && item.isPowerUp();
    }
}
