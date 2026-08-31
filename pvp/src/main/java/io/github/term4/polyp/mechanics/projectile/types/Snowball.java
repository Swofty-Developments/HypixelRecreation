package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.fx.Fx;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;

/**
 * Snowball throwable: launched on a {@link Material#SNOWBALL} use. Config-free (tuning lives in the preset's
 * {@code ProjectileConfig}); the generic {@code ManagedProjectile} handles the hit.
 */
public final class Snowball extends ThrowableItemType {

    public static final Key KEY = Key.key("minecraft:snowball");
    public static final Snowball INSTANCE = new Snowball();

    private Snowball() {
        super(KEY, "Snowball", EntityType.SNOWBALL, Material.SNOWBALL);
    }

    @Override protected Key throwSound() { return Fx.THROW_SNOWBALL; }
}
