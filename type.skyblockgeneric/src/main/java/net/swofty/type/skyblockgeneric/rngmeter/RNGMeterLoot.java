package net.swofty.type.skyblockgeneric.rngmeter;

import net.kyori.adventure.key.Key;
import net.swofty.type.skyblockgeneric.loottable.BossDropRarity;
import net.swofty.type.skyblockgeneric.loottable.LootAnnouncement;

import java.util.Objects;

public record RNGMeterLoot(Key key, BossDropRarity rarity, double baseChancePercent,
                           LootAnnouncement announcement) {
    public RNGMeterLoot {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(rarity, "rarity");
        Objects.requireNonNull(announcement, "announcement");
        if (!Double.isFinite(baseChancePercent) || baseChancePercent < 0 || baseChancePercent > 100) {
            throw new IllegalArgumentException("Base chance must be between 0 and 100");
        }
    }
}
