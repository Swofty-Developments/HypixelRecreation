package net.swofty.commons.loot;

import net.kyori.adventure.key.Key;

public record LootRoll<T>(
        Key poolId,
        Key entryId,
        T value,
        double baseChance,
        double effectiveChance
) {
}
