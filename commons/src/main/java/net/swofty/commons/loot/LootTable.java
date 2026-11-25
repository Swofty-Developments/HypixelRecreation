package net.swofty.commons.loot;

import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public record LootTable<C, T>(Key id, List<LootPool<C, T>> pools) {
    public LootTable {
        if (id == null) throw new IllegalArgumentException("Loot table id cannot be null");
        pools = List.copyOf(pools);
    }

    public List<LootRoll<T>> roll(C context) {
        return roll(context, RandomGenerator.getDefault());
    }

    public List<LootRoll<T>> roll(C context, RandomGenerator random) {
        List<LootRoll<T>> results = new ArrayList<>();
        for (LootPool<C, T> pool : pools) results.addAll(pool.roll(context, random));
        return List.copyOf(results);
    }

    public static <T> Optional<LootRoll<T>> rollSingle(Key id, T value, double chance) {
        LootTable<Void, T> table = new LootTable<>(id, List.of(new LootPool<>(Key.key(id.namespace(), "chance"),
                LootPool.Mode.INDEPENDENT, List.of(new LootEntry<>(id, value, chance)))));
        return table.roll(null).stream().findFirst();
    }
}
