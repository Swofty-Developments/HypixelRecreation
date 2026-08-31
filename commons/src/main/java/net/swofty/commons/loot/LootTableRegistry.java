package net.swofty.commons.loot;

import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class LootTableRegistry<C, T> {
    private final Map<Key, LootTable<C, T>> tables = new LinkedHashMap<>();

    public LootTable<C, T> register(LootTable<C, T> table) {
        Objects.requireNonNull(table, "table");
        LootTable<C, T> existing = tables.putIfAbsent(table.id(), table);
        if (existing != null) throw new IllegalArgumentException("Duplicate loot table: " + table.id().asString());
        return table;
    }

    public Optional<LootTable<C, T>> find(Key key) {
        return Optional.ofNullable(tables.get(key));
    }

    public LootTable<C, T> get(Key key) {
        LootTable<C, T> table = tables.get(key);
        if (table == null) throw new IllegalArgumentException("Unknown loot table: " + key.asString());
        return table;
    }

    public Collection<LootTable<C, T>> values() {
        return java.util.List.copyOf(tables.values());
    }
}
