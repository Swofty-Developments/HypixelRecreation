package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.fx.Fx;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;

/**
 * Egg throwable: launched on a {@link Material#EGG} use. Config-free, like the snowball - the generic
 * {@code ManagedProjectile} handles the hit. The {@code 1/8} baby-chicken easter egg isn't built in; attach a custom
 * {@link io.github.term4.polyp.mechanics.projectile.ProjectileBehavior} via the egg's {@code behavior} knob.
 */
public final class Egg extends ThrowableItemType {

    public static final Key KEY = Key.key("minecraft:egg");
    public static final Egg INSTANCE = new Egg();

    private Egg() {
        super(KEY, "Egg", EntityType.EGG, Material.EGG);
    }

    @Override protected Key throwSound() { return Fx.THROW_EGG; }
}
