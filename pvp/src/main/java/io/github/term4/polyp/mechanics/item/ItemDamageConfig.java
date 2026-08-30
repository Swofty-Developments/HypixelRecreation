package io.github.term4.polyp.mechanics.item;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * What destroys a dropped item, per scope ({@code itemDamage} profile member). Vanilla (both eras) gives the
 * entity {@link #health} 5 and subtracts each hit; fire also IGNITES it, and the burn keeps charging after it
 * leaves the flame.
 *
 * <p>Everything is overridable and removable: {@link Builder#health(Material, Integer) per-item health},
 * {@link Builder#damage per-source price}, and immunity in both directions -
 * {@link Builder#immune add} or {@link Builder#vulnerable strip} one, including the vanilla defaults.
 */
public final class ItemDamageConfig {

    private final @Nullable Boolean enabled;
    private final @Nullable Integer health;
    private final @Nullable Boolean voidDestroys;
    private final @Nullable Boolean itemResistance;
    private final @Nullable Integer fireIgniteTicks;
    private final @Nullable Integer lavaIgniteTicks;
    private final @Nullable Integer burnInterval;
    /** Per-item health, overriding {@link #health()}. */
    public final Map<Material, Integer> healthPerItem;
    /** Per-source damage, keyed by the {@link ItemDamageSystem} source keys; unset = the source's own amount. */
    public final Map<Key, Float> damage;
    /** Per-item added immunity: material -&gt; the sources it shrugs off. */
    public final Map<Material, Set<Key>> immune;
    /** Per-item REMOVED immunity; beats {@link #immune}, the resistance component and the vanilla defaults. */
    public final Map<Material, Set<Key>> vulnerable;

    private ItemDamageConfig(Builder b) {
        this.enabled = b.enabled;
        this.health = b.health;
        this.voidDestroys = b.voidDestroys;
        this.itemResistance = b.itemResistance;
        this.fireIgniteTicks = b.fireIgniteTicks;
        this.lavaIgniteTicks = b.lavaIgniteTicks;
        this.burnInterval = b.burnInterval;
        this.healthPerItem = Map.copyOf(b.healthPerItem);
        this.damage = Map.copyOf(b.damage);
        this.immune = copy(b.immune);
        this.vulnerable = copy(b.vulnerable);
    }

    private static Map<Material, Set<Key>> copy(Map<Material, Set<Key>> in) {
        Map<Material, Set<Key>> out = new LinkedHashMap<>();
        in.forEach((m, keys) -> out.put(m, Set.copyOf(keys)));
        return Map.copyOf(out);
    }

    /** Unset = active. */
    public @Nullable Boolean enabled() { return enabled; }

    /** Starting health of a dropped item (vanilla 5, both eras). */
    public @Nullable Integer health() { return health; }

    /** Starting health for {@code material}, or null = {@link #health()}. */
    public @Nullable Integer health(Material material) { return healthPerItem.get(material); }

    /** Below the world floor an item is removed outright, never damaged (vanilla {@code outOfWorld}); unset = on. */
    public @Nullable Boolean voidDestroys() { return voidDestroys; }

    /** Honour the stack's own {@code damage_resistant} component (26.1's rule: netherite shrugs off fire); unset = on. */
    public @Nullable Boolean itemResistance() { return itemResistance; }

    /** Fire-ticks an item standing in flame is lit for (vanilla {@code setOnFire(8)} = 160). */
    public @Nullable Integer fireIgniteTicks() { return fireIgniteTicks; }

    /** Fire-ticks lava lights it for (vanilla {@code setOnFire(15)} = 300). */
    public @Nullable Integer lavaIgniteTicks() { return lavaIgniteTicks; }

    /** Fire-ticks between lingering burn charges (vanilla {@code fireTicks % 20}). */
    public @Nullable Integer burnInterval() { return burnInterval; }

    /** Damage for {@code source}, or null = use the amount the source itself supplies. */
    public @Nullable Float damage(Key source) { return damage.get(source); }

    /** Whether the scope adds immunity to {@code source} for {@code material}. */
    public boolean immune(Material material, Key source) {
        Set<Key> keys = immune.get(material);
        return keys != null && keys.contains(source);
    }

    /** Whether the scope STRIPS immunity to {@code source} for {@code material} (beats every other rule). */
    public boolean vulnerable(Material material, Key source) {
        Set<Key> keys = vulnerable.get(material);
        return keys != null && keys.contains(source);
    }

    /** Merges this config over {@code base}; the maps overlay entry-wise. */
    public ItemDamageConfig fromBase(ItemDamageConfig base) {
        Builder b = new Builder()
                .enabled(enabled != null ? enabled : base.enabled)
                .health(health != null ? health : base.health)
                .voidDestroys(voidDestroys != null ? voidDestroys : base.voidDestroys)
                .itemResistance(itemResistance != null ? itemResistance : base.itemResistance)
                .fireIgniteTicks(fireIgniteTicks != null ? fireIgniteTicks : base.fireIgniteTicks)
                .lavaIgniteTicks(lavaIgniteTicks != null ? lavaIgniteTicks : base.lavaIgniteTicks)
                .burnInterval(burnInterval != null ? burnInterval : base.burnInterval);
        b.healthPerItem.putAll(base.healthPerItem);
        b.healthPerItem.putAll(healthPerItem);
        b.damage.putAll(base.damage);
        b.damage.putAll(damage);
        merge(b.immune, base.immune); merge(b.immune, immune);
        merge(b.vulnerable, base.vulnerable); merge(b.vulnerable, vulnerable);
        return b.build();
    }

    private static void merge(Map<Material, Set<Key>> into, Map<Material, Set<Key>> from) {
        from.forEach((m, keys) -> into.computeIfAbsent(m, k -> new LinkedHashSet<>()).addAll(keys));
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private @Nullable Boolean enabled;
        private @Nullable Integer health;
        private @Nullable Boolean voidDestroys;
        private @Nullable Boolean itemResistance;
        private @Nullable Integer fireIgniteTicks;
        private @Nullable Integer lavaIgniteTicks;
        private @Nullable Integer burnInterval;
        private final Map<Material, Integer> healthPerItem = new LinkedHashMap<>();
        private final Map<Key, Float> damage = new LinkedHashMap<>();
        private final Map<Material, Set<Key>> immune = new LinkedHashMap<>();
        private final Map<Material, Set<Key>> vulnerable = new LinkedHashMap<>();

        public Builder enabled(@Nullable Boolean v) { enabled = v; return this; }
        public Builder health(@Nullable Integer v) { health = v; return this; }
        public Builder voidDestroys(@Nullable Boolean v) { voidDestroys = v; return this; }
        public Builder itemResistance(@Nullable Boolean v) { itemResistance = v; return this; }
        public Builder fireIgniteTicks(@Nullable Integer v) { fireIgniteTicks = v; return this; }
        public Builder lavaIgniteTicks(@Nullable Integer v) { lavaIgniteTicks = v; return this; }
        public Builder burnInterval(@Nullable Integer v) { burnInterval = v; return this; }

        /** Health for one item; {@code null} drops the override back to the global {@link #health(Integer)}. */
        public Builder health(Material material, @Nullable Integer v) {
            if (v == null) healthPerItem.remove(material); else healthPerItem.put(material, v);
            return this;
        }

        /** Prices one source; {@code null} clears it back to the source's own amount. */
        public Builder damage(Key source, @Nullable Float amount) {
            if (amount == null) damage.remove(source); else damage.put(source, amount);
            return this;
        }

        /** {@code material} shrugs off these sources. */
        public Builder immune(Material material, Key... sources) {
            immune.computeIfAbsent(material, k -> new LinkedHashSet<>()).addAll(Set.of(sources));
            Set<Key> stripped = vulnerable.get(material);
            if (stripped != null) stripped.removeAll(Set.of(sources));
            return this;
        }

        /** {@code material} takes these sources even if vanilla, its component, or {@link #immune} exempts it. */
        public Builder vulnerable(Material material, Key... sources) {
            vulnerable.computeIfAbsent(material, k -> new LinkedHashSet<>()).addAll(Set.of(sources));
            Set<Key> granted = immune.get(material);
            if (granted != null) granted.removeAll(Set.of(sources));
            return this;
        }

        public ItemDamageConfig build() { return new ItemDamageConfig(this); }
    }
}
