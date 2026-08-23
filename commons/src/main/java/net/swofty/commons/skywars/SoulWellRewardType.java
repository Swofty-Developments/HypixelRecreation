package net.swofty.commons.skywars;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum SoulWellRewardType {
    KIT_UNLOCK(20, "Kit Unlock", NamedTextColor.GREEN),
    PERK_UNLOCK(15, "Perk Unlock", NamedTextColor.AQUA),
    SOUL_RETURN(25, "Soul Return", NamedTextColor.YELLOW),
    COSMETIC_UNLOCK(10, "Cosmetic", NamedTextColor.LIGHT_PURPLE),
    COINS_SMALL(15, "Coins", NamedTextColor.GOLD),
    COINS_LARGE(10, "Jackpot!", NamedTextColor.GOLD),
    EXPERIENCE_BOOST(5, "XP Boost", NamedTextColor.GREEN);

    private final int weight;
    private final String displayName;
    private final TextColor color;

    SoulWellRewardType(int weight, String displayName, TextColor color) {
        this.weight = weight;
        this.displayName = displayName;
        this.color = color;
    }

    public static SoulWellRewardType rollRandom() {
        int totalWeight = 0;
        for (SoulWellRewardType type : values()) {
            totalWeight += type.weight;
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;

        for (SoulWellRewardType type : values()) {
            cumulative += type.weight;
            if (roll < cumulative) {
                return type;
            }
        }

        return COINS_SMALL;
    }

    public int getRandomCoinsAmount() {
        return switch (this) {
            case COINS_SMALL -> ThreadLocalRandom.current().nextInt(500, 1001);
            case COINS_LARGE -> ThreadLocalRandom.current().nextInt(2000, 5001);
            default -> 0;
        };
    }

    public int getRandomSoulReturn() {
        if (this == SOUL_RETURN) {
            return ThreadLocalRandom.current().nextInt(5, 16);
        }
        return 0;
    }
}
