package io.github.term4.polyp.mechanics.damage.types.burning;

import io.github.term4.polyp.item.Enchants;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.ProtectionEnchant;
import io.github.term4.polyp.util.tick.TickScaler;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;

/**
 * Vanilla {@code Entity.setOnFire}: Fire Protection cuts the duration 15% per level (highest armor piece),
 * and a shorter burn never overwrites a longer one - Minestom's raw {@code setFireTicks} does both wrong.
 */
public final class Ignite {

    private Ignite() {}

    /** {@code vanillaTicks} pre-scale; {@code scaleKey} is the calling system's TickScaler key. */
    public static void ignite(LivingEntity victim, int vanillaTicks, Key scaleKey) {
        int prot = maxFireProtection(victim);
        if (prot > 0) vanillaTicks -= (int) Math.floor(vanillaTicks * prot * 0.15f);
        int scaled = TickScaler.duration(victim, vanillaTicks, scaleKey);
        if (victim.getFireTicks() < scaled) victim.setFireTicks(scaled);
    }

    // 1.8 EnchantmentManager.a: the HIGHEST level across equipment, not the sum
    private static int maxFireProtection(LivingEntity victim) {
        int max = 0;
        for (ItemStack armor : new ItemStack[]{
                victim.getHelmet(), victim.getChestplate(), victim.getLeggings(), victim.getBoots()}) {
            max = Math.max(max, Enchants.level(armor, ProtectionEnchant.FIRE_PROTECTION.key()));
        }
        return max;
    }
}
