package net.swofty.type.skyblockgeneric.loottable;

import net.kyori.adventure.key.Key;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LootRarityRegistry {
    private static final Map<Key, LootRarity> RARITIES = createRegistry();

    private LootRarityRegistry() {
    }

    public static Optional<LootRarity> find(Key key) {
        return Optional.ofNullable(RARITIES.get(key));
    }

    public static LootRarity get(Key key) {
        LootRarity rarity = RARITIES.get(key);
        if (rarity == null) throw new IllegalArgumentException("Unknown loot rarity: " + key.asString());
        return rarity;
    }

    public static List<LootRarity> values() {
        return List.copyOf(RARITIES.values());
    }

    private static Map<Key, LootRarity> createRegistry() {
        Map<Key, LootRarity> rarities = new LinkedHashMap<>();
        register(rarities, BestiaryDropRarity.values());
        register(rarities, BossDropRarity.values());
        return Map.copyOf(rarities);
    }

    private static void register(Map<Key, LootRarity> registry, LootRarity[] rarities) {
        for (LootRarity rarity : rarities) {
            if (registry.putIfAbsent(rarity.key(), rarity) != null) {
                throw new IllegalStateException("Duplicate loot rarity: " + rarity.key().asString());
            }
        }
    }
}
