package net.swofty.commons.loot;

import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public record LootPool<C, T>(
        Key id,
        Mode mode,
        int rolls,
        double emptyWeight,
        List<LootEntry<C, T>> entries
) {
    public LootPool {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(mode, "mode");
        if (rolls < 0) throw new IllegalArgumentException("Roll count cannot be negative");
        if (!Double.isFinite(emptyWeight) || emptyWeight < 0) {
            throw new IllegalArgumentException("Empty weight must be finite and non-negative");
        }
        entries = List.copyOf(entries);
    }

    public LootPool(Key id, Mode mode, List<LootEntry<C, T>> entries) {
        this(id, mode, 1, 0, entries);
    }

    List<LootRoll<T>> roll(C context, RandomGenerator random) {
        List<LootRoll<T>> results = new ArrayList<>();
        for (int roll = 0; roll < rolls; roll++) {
            switch (mode) {
                case INDEPENDENT -> rollIndependent(context, random, results);
                case FIRST_SUCCESS -> rollFirstSuccess(context, random, results);
                case WEIGHTED -> rollWeighted(context, random, results);
            }
        }
        return results;
    }

    private void rollIndependent(C context, RandomGenerator random, List<LootRoll<T>> results) {
        for (LootEntry<C, T> entry : entries) {
            if (!entry.eligible(context)) continue;
            double chance = Math.min(1, entry.effectiveChance(context));
            if (random.nextDouble() < chance) results.add(result(entry, chance));
        }
    }

    private void rollFirstSuccess(C context, RandomGenerator random, List<LootRoll<T>> results) {
        for (LootEntry<C, T> entry : entries) {
            if (!entry.eligible(context)) continue;
            double chance = Math.min(1, entry.effectiveChance(context));
            if (random.nextDouble() < chance) {
                results.add(result(entry, chance));
                return;
            }
        }
    }

    private void rollWeighted(C context, RandomGenerator random, List<LootRoll<T>> results) {
        double total = emptyWeight;
        List<WeightedEntry<C, T>> eligible = new ArrayList<>();
        for (LootEntry<C, T> entry : entries) {
            if (!entry.eligible(context)) continue;
            double weight = entry.effectiveChance(context);
            if (weight == 0) continue;
            eligible.add(new WeightedEntry<>(entry, weight));
            total += weight;
        }
        if (total == 0) return;

        double cursor = random.nextDouble(total);
        if (cursor < emptyWeight) return;
        cursor -= emptyWeight;
        for (WeightedEntry<C, T> weighted : eligible) {
            if (cursor < weighted.weight) {
                results.add(result(weighted.entry, weighted.weight / total));
                return;
            }
            cursor -= weighted.weight;
        }
    }

    private LootRoll<T> result(LootEntry<C, T> entry, double effectiveChance) {
        return new LootRoll<>(id, entry.id(), entry.value(), entry.chance(), effectiveChance);
    }

    public enum Mode {
        INDEPENDENT,
        FIRST_SUCCESS,
        WEIGHTED
    }

    private record WeightedEntry<C, T>(LootEntry<C, T> entry, double weight) {
    }
}
