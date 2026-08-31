package net.swofty.dungeons.catacombs.loot;

import net.kyori.adventure.key.Key;
import net.swofty.commons.loot.LootEntry;
import net.swofty.commons.loot.LootPool;
import net.swofty.commons.loot.LootRoll;
import net.swofty.commons.loot.LootTable;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

public record CatacombsRewardTable<T>(Key id, int rolls, List<CatacombsRewardEntry<T>> entries) {
    public CatacombsRewardTable {
        if (id == null) throw new IllegalArgumentException("Reward table id cannot be null");
        if (rolls < 0) throw new IllegalArgumentException("Roll count cannot be negative");
        entries = List.copyOf(entries);
    }

    public List<CatacombsRewardEntry<T>> roll(CatacombsRewardChest chest, int quality,
                                              RandomGenerator random) {
        return roll(chest, quality, random, CatacombsRewardEntry::weight);
    }

    public List<CatacombsRewardEntry<T>> roll(CatacombsRewardChest chest, int quality,
                                              RandomGenerator random,
                                              ToDoubleFunction<CatacombsRewardEntry<T>> weight) {
        Context context = new Context(chest, quality);
        List<LootEntry<Context, CatacombsRewardEntry<T>>> pool = entries.stream()
                .map(entry -> new LootEntry<Context, CatacombsRewardEntry<T>>(
                        entry.id(), entry, weight.applyAsDouble(entry),
                        ctx -> entry.chests().contains(ctx.chest()) && entry.requiredQuality() <= ctx.quality(),
                        List.of()))
                .toList();
        return new LootTable<Context, CatacombsRewardEntry<T>>(id, List.of(
                new LootPool<>(Key.key(id.namespace(), "chest"), LootPool.Mode.WEIGHTED, rolls, 0, pool)
        )).roll(context, random).stream().map(LootRoll::value).toList();
    }

    public int price(CatacombsRewardChest chest, net.swofty.dungeons.catacombs.CatacombsFloor floor,
                     List<CatacombsRewardEntry<T>> rewards) {
        return chest.baseCost(floor) + rewards.stream().mapToInt(CatacombsRewardEntry::addedChestCost).sum();
    }

    private record Context(CatacombsRewardChest chest, int quality) {
    }
}
