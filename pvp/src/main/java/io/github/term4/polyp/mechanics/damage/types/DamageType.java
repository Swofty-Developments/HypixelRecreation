package io.github.term4.polyp.mechanics.damage.types;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionCategory;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Base for a damage type in the {@code DamageTypeRegistry}: identifies a source of damage and maps to a Minestom
 * {@link net.minestom.server.entity.damage.DamageType} for application + death messages. Tunables live on the global
 * {@code DamageConfig} ({@code typeConfigs}), keyed by {@link #key()}; each type carries an immutable
 * {@link #defaultConfig()}. A self-driven type (fire, fall) overrides {@link #enable}/{@link #disable} to wire its own triggers.
 */
public abstract class DamageType {

    private final Key key;
    private final String name;
    private final RegistryKey<net.minestom.server.entity.damage.DamageType> minecraftType;
    private final DamageTypeConfig defaultConfig;

    protected DamageType(Key key, String name,
                         RegistryKey<net.minestom.server.entity.damage.@NotNull DamageType> minecraftType,
                         DamageTypeConfig defaultConfig) {
        this.key = key;
        this.name = name;
        this.minecraftType = minecraftType;
        this.defaultConfig = defaultConfig;
    }

    public Key key() { return key; }

    /** Human-readable label for this damage type (debug, death messages). */
    public String name() { return name; }

    /** Minestom damage type used when applying the damage (cosmetic, death messages). */
    public RegistryKey<net.minestom.server.entity.damage.@NotNull DamageType> minecraftType() { return minecraftType; }

    /** Immutable per-type defaults, used when the active {@code DamageConfig} has no override for this type. */
    public @NotNull DamageTypeConfig defaultConfig() { return defaultConfig; }

    /**
     * The EPF protection categories this damage type belongs to (the specialized armor enchants that reduce it). General
     * Protection always applies; these gate Fire/Feather Falling/Blast/Projectile. Empty by default - override per type.
     */
    public @NotNull Set<ProtectionCategory> protectionCategories() { return Set.of(); }

    /** Called when the type is enabled. Self-driven types wire triggers here and emit snapshots through {@code system}. No-op by default. */
    public void enable(DamageSystem system, Polyp polyp) {}

    /** Called when this type is disabled. Tear down anything registered in {@link #enable}. No-op by default. */
    public void disable() {}

    @Override public String toString() { return "DamageType(" + key.asString() + ")"; }
}
