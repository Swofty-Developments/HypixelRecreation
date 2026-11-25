package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.PearlEntity;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

/**
 * Ender pearl throwable: spawns a {@link PearlEntity} that teleports its shooter to the impact (+5 fall damage for a
 * player). Config-free; the self-pass-through comes from {@code Vanilla18.pearl()}.
 */
public final class Pearl extends ThrowableItemType {

    public static final Key KEY = Key.key("minecraft:ender_pearl");
    public static final Pearl INSTANCE = new Pearl();

    private Pearl() {
        super(KEY, "Ender Pearl", EntityType.ENDER_PEARL, Material.ENDER_PEARL);
    }

    @Override protected Key throwSound() { return Fx.THROW_PEARL; }

    @Override
    public ProjectileEntity createEntity(@Nullable Entity shooter, ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        return new PearlEntity(shooter, entityType(), snap, effectiveConfig);
    }
}
