package net.swofty.type.skyblockgeneric.bank;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum BankAccountTier {
    STARTER(NamedTextColor.GREEN, "Starter", 50_000_000D, 250_000D, 0D, 0, 0, 0),
    GOLD(NamedTextColor.GOLD, "Gold", 100_000_000D, 300_000D, 5_000_000D, 1, 100_000, 0),
    DELUXE(NamedTextColor.LIGHT_PURPLE, "Deluxe", 250_000_000D, 350_000D, 10_000_000D, 5, 250_000, 3),
    SUPER_DELUXE(NamedTextColor.DARK_PURPLE, "Super Deluxe", 500_000_000D, 390_000D, 25_000_000D, 20, 500_000, 6),
    PREMIER(NamedTextColor.RED, "Premier", 1_000_000_000D, 500_000D, 50_000_000D, 50, 1_000_000, 10),
    LUXURIOUS(NamedTextColor.DARK_AQUA, "Luxurious", 6_000_000_000D, 1_000_000D, 100_000_000D, 100, 2_000_000, 14),
    PALATIAL(NamedTextColor.DARK_RED, "Palatial", 60_000_000_000D, 1_500_000D, 200_000_000D, 200, 3_000_000, 18);

    private final TextColor color;
    private final String displayName;
    private final double capacity;
    private final double baseMaximumInterest;
    private final double coinCost;
    private final int enchantedGoldBlocks;
    private final int goldCollection;
    private final int museumMilestone;

    BankAccountTier(TextColor color, String displayName, double capacity, double baseMaximumInterest, double coinCost,
                    int enchantedGoldBlocks, int goldCollection, int museumMilestone) {
        this.color = color;
        this.displayName = displayName;
        this.capacity = capacity;
        this.baseMaximumInterest = baseMaximumInterest;
        this.coinCost = coinCost;
        this.enchantedGoldBlocks = enchantedGoldBlocks;
        this.goldCollection = goldCollection;
        this.museumMilestone = museumMilestone;
    }

    public BankAccountTier next() {
        return ordinal() == values().length - 1 ? null : values()[ordinal() + 1];
    }

    public static BankAccountTier fromLegacyLimit(double limit) {
        BankAccountTier closest = STARTER;
        for (BankAccountTier tier : values()) {
            if (limit >= tier.capacity) closest = tier;
        }
        return closest;
    }
}
