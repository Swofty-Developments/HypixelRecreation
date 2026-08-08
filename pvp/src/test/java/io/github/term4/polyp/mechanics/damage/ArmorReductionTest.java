package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.mechanics.attribute.defense.ArmorConfig;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDamage;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Armor stage pinned vs vanilla 1.8 ({@code EntityLiving.applyArmorModifier}) and 26
 * ({@code CombatRules.getDamageAfterAbsorb}).
 */
class ArmorReductionTest extends HeadlessServerTest {

    @Test
    void legacyIsLinear() {
        // 1.8: damage × (25 − armor) / 25
        assertEquals(2.0f, ArmorConfig.legacy(10f, 20), 1e-5f);  // full diamond
        assertEquals(6.0f, ArmorConfig.legacy(10f, 10), 1e-5f);
        assertEquals(10.0f, ArmorConfig.legacy(10f, 0), 1e-5f);  // no armor
    }

    @Test
    void modernUsesToughness() {
        // 26: real = clamp(armor − damage/(2+tough/4), armor×0.2, 20), then damage × (1 − real/25)
        assertEquals(4.0f, ArmorConfig.modern(10f, 20, 0f), 1e-5f); // t=2, real=15  -> ×0.4
        assertEquals(3.0f, ArmorConfig.modern(10f, 20, 8f), 1e-5f); // diamond t=4, real=17.5 -> ×0.3
        assertEquals(10.0f, ArmorConfig.modern(10f, 0, 0f), 1e-5f); // no armor
    }

    @Test
    void meleeHitIsReducedByArmor() {
        LivingEntity attacker = zombie(new Pos(0, 64, 300));
        LivingEntity victim = zombie(new Pos(0, 64, 301));
        victim.getAttribute(Attribute.ARMOR).setBaseValue(20); // a fully-armored player
        victim.setHealth(20f);
        DamageSnapshot snap = MeleeDamage.INSTANCE.snapshot(attacker, victim, false, ItemStack.of(Material.DIAMOND_SWORD), services);
        services.damage().apply(snap); // 7 melee × (25−20)/25 = 7 × 0.2 = 1.4
        assertEquals(18.6f, victim.getHealth(), 1e-2f);
    }

    @Test
    void fallTypeBypassesArmor() {
        LivingEntity z = zombie(new Pos(0, 64, 302));
        DamageContext ctx = services.damage().contextFor(DamageSnapshot.of(z, FallDamage.INSTANCE));
        assertTrue(ctx.typeConfig().bypassArmor(ctx));
    }
}
