package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.entities.arrow.ArrowEntity;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Arrow projectile (entity = {@link ArrowEntity}). Pure identity - all tuning lives in {@code Vanilla18.arrow()}. The
 * bow item that fires it is a separate {@code Shootable} launcher.
 */
public final class Arrow extends ProjectileType {

    public static final Key KEY = Key.key("minecraft:arrow");
    public static final Arrow INSTANCE = new Arrow();

    private Arrow() { super(KEY, "Arrow", EntityType.ARROW); }

    @Override
    public ProjectileEntity createEntity(@Nullable Entity shooter, ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        return new ArrowEntity(shooter, entityType(), snap, effectiveConfig);
    }
}
