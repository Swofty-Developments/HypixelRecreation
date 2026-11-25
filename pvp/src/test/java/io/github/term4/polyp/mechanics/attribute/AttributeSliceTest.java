package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Sharpness;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Strength;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Weakness;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.RegistryKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * LEGACY variants: {@code attackDamage = table × (1 + 1.3·strength)}, then {@code × crit}, then
 * {@code + 1.25·sharpness}.
 */
class AttributeSliceTest extends HeadlessServerTest {

    private static final float EPS = 1e-3f;

    private static ItemStack sword(int sharpnessLevel) {
        ItemStack s = ItemStack.of(Material.DIAMOND_SWORD);
        if (sharpnessLevel <= 0) return s;
        RegistryKey<Enchantment> sharp = RegistryKey.unsafeOf(Sharpness.KEY);
        return s.with(DataComponents.ENCHANTMENTS, new EnchantmentList(sharp, sharpnessLevel));
    }

    private float melee(LivingEntity attacker, boolean crit, ItemStack item) {
        DamageSnapshot snap = MeleeDamage.INSTANCE.snapshot(attacker, target(), crit, item, services);
        return snap.amount();
    }

    private static LivingEntity target;
    private static LivingEntity target() {
        if (target == null) target = zombie(new Pos(0, 64, 51));
        return target;
    }

    @Test
    void sharpnessAddsAfterCrit() {
        LivingEntity atk = zombie(new Pos(0, 64, 50));
        assertEquals(13.25f, melee(atk, false, sword(5)), EPS); // 7 + 1.25×5
        assertEquals(16.75f, melee(atk, true, sword(5)), EPS);  // 7×1.5 + 6.25
    }

    @Test
    void weaknessSubtractsFromAttackDamage() {
        LivingEntity atk = zombie(new Pos(0, 64, 53));
        PotionEffect weakness = PotionEffect.fromKey(Weakness.KEY);
        assertNotNull(weakness, "weakness potion effect");
        atk.addEffect(new Potion(weakness, 0, 600)); // Weakness I
        try {
            assertEquals(6.5f, melee(atk, false, ItemStack.of(Material.DIAMOND_SWORD)), EPS); // 7 - 0.5
        } finally {
            atk.clearEffects();
        }
    }

    @Test
    void strengthMultipliesLegacyAttackDamage() {
        LivingEntity atk = zombie(new Pos(0, 64, 52));
        PotionEffect strength = PotionEffect.fromKey(Strength.KEY);
        assertNotNull(strength, "strength potion effect");
        atk.addEffect(new Potion(strength, 0, 600)); // amplifier 0 = Strength I
        try {
            assertEquals(16.1f, melee(atk, false, ItemStack.of(Material.DIAMOND_SWORD)), EPS); // 7 × 2.3
            assertEquals(30.4f, melee(atk, true, sword(5)), EPS);                              // 7×2.3×1.5 + 6.25
        } finally {
            atk.clearEffects();
        }
    }
}
