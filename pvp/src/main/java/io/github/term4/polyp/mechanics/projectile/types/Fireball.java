package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.FireballEntity;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

/**
 * Fireball throwable: a {@link Material#FIRE_CHARGE} use launches a self-propelled {@link FireballEntity} that detonates
 * on contact. Config-free: tuning (accel speed, explosion power) lives in the preset's {@code ProjectileConfig}.
 */
public final class Fireball extends ThrowableItemType {

    public static final Key KEY = Key.key("minecraft:fireball");
    public static final Fireball INSTANCE = new Fireball();

    private Fireball() {
        super(KEY, "Fireball", EntityType.FIREBALL, Material.FIRE_CHARGE, true); // blockAction: lights fire client-side
    }

    @Override protected Key throwSound() { return Fx.THROW_FIREBALL; }

    @Override
    public ProjectileEntity createEntity(@Nullable Entity shooter, ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        return new FireballEntity(shooter, entityType(), snap, effectiveConfig);
    }
}
