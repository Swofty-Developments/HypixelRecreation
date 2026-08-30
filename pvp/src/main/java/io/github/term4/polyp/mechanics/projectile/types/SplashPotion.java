package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.entities.SplashPotionEntity;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

/**
 * Splash potion throwable: launched on a {@link Material#SPLASH_POTION} use; the item's {@code potion_contents} rides
 * to the impact AoE in {@link SplashPotionEntity}. Presets configure it with {@code entityHit(DESTROY)} - vanilla
 * potions never deal a contact hit, the splash IS the effect - plus {@code speed(0.5)}/{@code launchPitchOffset(-20)}.
 */
public final class SplashPotion extends ThrowableItemType {

    public static final Key KEY = Key.key("minecraft:splash_potion");
    public static final SplashPotion INSTANCE = new SplashPotion();

    private SplashPotion() {
        super(KEY, "Splash Potion", EntityType.SPLASH_POTION, Material.SPLASH_POTION);
    }

    @Override
    public ProjectileEntity createEntity(@Nullable Entity shooter, ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        return new SplashPotionEntity(shooter, entityType(), snap, effectiveConfig);
    }
}
