package io.github.term4.polyp.mechanics.projectile.entities.arrow;

import io.github.term4.polyp.mechanics.attribute.catalog.VanillaPotions;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.CustomPotionEffect;

import java.util.List;

/**
 * Resolves an arrow item's {@code potion_contents} into the on-hit effect payload and stamps it on the
 * {@link ArrowEntity}. Called by a launcher with the actual ammo - a bow's {@code snap.item()} is the bow, not the
 * arrow. Scaled on hit by the item's {@code potion_duration_scale} (a crafted vanilla tipped arrow bakes 0.125 = 1/8).
 */
public final class TippedArrows {
    private TippedArrows() {}

    /** Stamps {@code arrowItem}'s tipped payload onto {@code arrow}; no-op if the item carries no {@code potion_contents}. */
    public static void apply(ArrowEntity arrow, ItemStack arrowItem) {
        List<CustomPotionEffect> effects = VanillaPotions.payload(arrowItem);
        if (effects.isEmpty()) return;
        Float scale = arrowItem.get(DataComponents.POTION_DURATION_SCALE);
        arrow.setOnHitEffects(effects, scale != null ? scale : 1.0f);
    }
}
