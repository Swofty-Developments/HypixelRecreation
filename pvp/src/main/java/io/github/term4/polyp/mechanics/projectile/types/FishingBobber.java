package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.FishingBobberEntity;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Fishing bobber projectile (entity = {@link FishingBobberEntity}). Pure identity - tuning lives in the preset config.
 * The rod item that casts/retrieves it is the {@code FishingRod} {@code Shootable} launcher.
 */
public final class FishingBobber extends ProjectileType {

    public static final Key KEY = Key.key("minecraft:fishing_bobber");
    public static final FishingBobber INSTANCE = new FishingBobber();

    private FishingBobber() { super(KEY, "Fishing Bobber", EntityType.FISHING_BOBBER); }

    @Override
    public ProjectileEntity createEntity(@Nullable Entity shooter, ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        return new FishingBobberEntity(shooter, entityType(), snap, effectiveConfig);
    }
}
