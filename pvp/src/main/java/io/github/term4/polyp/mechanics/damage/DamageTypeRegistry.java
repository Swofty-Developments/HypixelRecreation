package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.burning.BurningDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.InFireDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.LavaDamage;
import io.github.term4.polyp.mechanics.damage.types.breathing.DrowningDamage;
import io.github.term4.polyp.mechanics.damage.types.breathing.SuffocationDamage;
import io.github.term4.polyp.mechanics.damage.types.cactus.CactusDamage;
import io.github.term4.polyp.mechanics.damage.types.explosion.ExplosionDamage;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDamage;
import io.github.term4.polyp.mechanics.damage.types.generic.GenericDamage;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.mechanics.damage.types.projectile.ProjectileDamage;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry of {@link DamageType}s feeding the {@link DamageSystem}: register a type, enable it, done. One-off types
 * carry data and are driven externally; self-driven types override {@link DamageType#enable}/{@link DamageType#disable}.
 */
public final class DamageTypeRegistry {

    private static final class Entry {
        final DamageType type;
        boolean enabled;
        Entry(DamageType type) {
            this.type = type;
        }
    }

    private final DamageSystem system;
    private final Polyp polyp;
    private final Map<Key, Entry> entries = new ConcurrentHashMap<>();

    public DamageTypeRegistry(DamageSystem system, Polyp polyp) {
        this.system = system;
        this.polyp = polyp;
    }

    /** Register a damage type, keyed by its {@link DamageType#key()}. */
    public DamageTypeRegistry register(DamageType type) {
        entries.put(type.key(), new Entry(type));
        return this;
    }

    /** Enable a registered type, starting its self-driven behavior (if any). No-op if already enabled. */
    public void enable(Key key) {
        Entry e = entries.get(key);
        if (e == null) throw new IllegalArgumentException("No damage type registered for " + key.asString());
        if (e.enabled) return;
        e.enabled = true;
        e.type.enable(system, polyp);
    }

    /** Disable a registered type, tearing down its self-driven behavior (if any). No-op if not enabled. */
    public void disable(Key key) {
        Entry e = entries.get(key);
        if (e == null) return;
        if (!e.enabled) return;
        e.enabled = false;
        e.type.disable();
    }

    public boolean isEnabled(Key key) {
        Entry e = entries.get(key);
        return e != null && e.enabled;
    }

    public @Nullable DamageType get(Key key) {
        Entry e = entries.get(key);
        return e != null ? e.type : null;
    }

    /** Default config for a registered type, or {@code null} if not registered. */
    public @Nullable DamageTypeConfig config(Key key) {
        Entry e = entries.get(key);
        return e != null ? e.type.defaultConfig() : null;
    }

    public boolean contains(Key key) {
        return entries.containsKey(key);
    }

    /** All registered damage types. */
    public Collection<DamageType> all() {
        return entries.values().stream().map(e -> e.type).collect(Collectors.toUnmodifiableList());
    }

    /** Registers the built-in vanilla type definitions (data only, no producers enabled). Producers start via {@link #enable}. */
    public DamageTypeRegistry registerVanillaDefaults() {
        register(MeleeDamage.INSTANCE);
        register(GenericDamage.INSTANCE);
        register(ProjectileDamage.INSTANCE);
        register(FallDamage.INSTANCE);
        register(InFireDamage.INSTANCE);
        register(BurningDamage.INSTANCE);
        register(LavaDamage.INSTANCE);
        register(CactusDamage.INSTANCE);
        register(DrowningDamage.INSTANCE);
        register(SuffocationDamage.INSTANCE);
        register(ExplosionDamage.INSTANCE);
        return this;
    }
}
