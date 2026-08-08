package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base for a projectile type: identifies a projectile, maps to a Minestom {@link EntityType}, optionally carries
 * intrinsic {@link #defaultConfig() defaults}, and - for self-driven types - wires its launch trigger in
 * {@link #enable}. {@link #createEntity} produces the flying entity (default a generic {@link ManagedProjectile}).
 */
public abstract class ProjectileType {

    /** A config-free type carries no tuning - it all comes from the active {@code ProjectileConfig}. */
    private static final ProjectileTypeConfig NO_DEFAULTS = ProjectileTypeConfig.builder().build();

    private final Key key;
    private final String name;
    private final EntityType entityType;
    private final ProjectileTypeConfig defaultConfig;

    /** Config-free type: identity + behavior only, all tuning lives in the preset's {@code ProjectileConfig}. */
    protected ProjectileType(Key key, String name, EntityType entityType) {
        this(key, name, entityType, NO_DEFAULTS);
    }

    /** Type with intrinsic defaults baked in; a preset's defaults + per-type override still layer on top. */
    protected ProjectileType(Key key, String name, EntityType entityType, ProjectileTypeConfig defaultConfig) {
        this.key = key;
        this.name = name;
        this.entityType = entityType;
        this.defaultConfig = defaultConfig;
        ProjectileType previous = REGISTRY.put(key, this);
        // last-in wins for saves revived through byKey - surfaced so a key collision isn't a silent identity swap
        if (previous != null) LoggerFactory.getLogger(ProjectileType.class)
                .warn("projectile type {} re-registered ({} replaces {})", key, getClass().getName(), previous.getClass().getName());
    }

    private static final Map<Key, ProjectileType> REGISTRY = new ConcurrentHashMap<>();

    /** The constructed type registered under {@code key} (types self-register on construction), or {@code null}. */
    public static @Nullable ProjectileType byKey(@NotNull Key key) { return REGISTRY.get(key); }

    public Key key() { return key; }
    public String name() { return name; }
    public EntityType entityType() { return entityType; }
    public @NotNull ProjectileTypeConfig defaultConfig() { return defaultConfig; }

    /** Creates the flying entity for a launch; {@code effectiveConfig} is the merged per-type config. */
    public ProjectileEntity createEntity(@Nullable Entity shooter, ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        return new ManagedProjectile(shooter, entityType, snap, effectiveConfig);
    }

    /** Wires this type's launch trigger (item use, etc.) and emits snapshots through {@code system}. No-op by default. */
    public void enable(ProjectileSystem system, Polyp polyp) {}

    /** Tears down anything registered in {@link #enable}. No-op by default. */
    public void disable() {}

    @Override public String toString() { return "ProjectileType(" + key.asString() + ")"; }
}
