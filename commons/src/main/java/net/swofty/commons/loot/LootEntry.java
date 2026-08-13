package net.swofty.commons.loot;

import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public record LootEntry<C, T>(
        Key id,
        T value,
        double chance,
        Predicate<C> condition,
        List<LootModifier<C, T>> modifiers
) {
    public LootEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(value, "value");
        if (!Double.isFinite(chance) || chance < 0)
            throw new IllegalArgumentException("Chance must be finite and non-negative");
        condition = condition == null ? ignored -> true : condition;
        modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }

    public LootEntry(Key id, T value, double chance) {
        this(id, value, chance, null, List.of());
    }

    boolean eligible(C context) {
        return condition.test(context);
    }

    double effectiveChance(C context) {
        double effective = chance;
        for (LootModifier<C, T> modifier : modifiers) {
            effective = modifier.apply(context, this, effective);
            if (!Double.isFinite(effective) || effective < 0) {
                throw new IllegalStateException("Loot modifier produced an invalid chance for " + id);
            }
        }
        return effective;
    }

    public interface LootModifier<C, T> {
        double apply(C context, LootEntry<C, T> entry, double currentChance);
    }
}
