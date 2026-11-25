package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.presets.vanilla18.Attributes;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.ProtectionEnchant;
import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.attribute.defense.MitigationRequest;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionConfig;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionConfig.Formula;
import io.github.term4.polyp.mechanics.damage.types.burning.BurningDamage;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDamage;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The EPF / Protection defense stage and the bypass model, pinned vs vanilla 1.8 ({@code EnchantmentProtection.a} +
 * {@code EnchantmentManager.a} + {@code applyMagicModifier}) and 26 ({@code damage_protection} data +
 * {@code CombatRules.getDamageAfterMagicAbsorb}).
 */
class ProtectionStageTest extends HeadlessServerTest {

    private static final Key PROTECTION = ProtectionEnchant.PROTECTION.key();
    private static final Key FEATHER_FALLING = ProtectionEnchant.FEATHER_FALLING.key();
    private static final Key FIRE_PROTECTION = ProtectionEnchant.FIRE_PROTECTION.key();
    private static final Key ARMOR_ATTR = Key.key("minecraft:armor");
    private static final Key RESISTANCE = Key.key("minecraft:resistance");

    private static final Random MAX_ROLL = new Random() {
        @Override public int nextInt(int bound) { return bound - 1; }
    };
    private static final Random MIN_ROLL = new Random() {
        @Override public int nextInt(int bound) { return 0; }
    };

    @Test
    void legacyPerPieceIsFlooredVanilla() {
        // floor((6 + lvl²)/3 × typeMult); MathHelper.d = floor of float
        assertEquals(5, ProtectionEnchant.PROTECTION.legacyPerPiece(4));            // floor(7.333×0.75=5.5)
        assertEquals(1, ProtectionEnchant.PROTECTION.legacyPerPiece(1));            // floor(2.333×0.75=1.75)
        assertEquals(18, ProtectionEnchant.FEATHER_FALLING.legacyPerPiece(4));      // floor(7.333×2.5=18.33)
        assertEquals(9, ProtectionEnchant.FIRE_PROTECTION.legacyPerPiece(4));       // floor(7.333×1.25=9.16)
        assertEquals(11, ProtectionEnchant.BLAST_PROTECTION.legacyPerPiece(4));     // floor(7.333×1.5=11.0)
        assertEquals(11, ProtectionEnchant.PROJECTILE_PROTECTION.legacyPerPiece(4));
    }

    @Test
    void modernPerPieceIsLinear() {
        assertEquals(4, ProtectionEnchant.PROTECTION.modernPerPiece(4));
        assertEquals(12, ProtectionEnchant.FEATHER_FALLING.modernPerPiece(4));
        assertEquals(8, ProtectionEnchant.FIRE_PROTECTION.modernPerPiece(4));
    }

    @Test
    void legacyRollSpansHalfToFull() {
        // (raw+1>>1) + rand[0, raw>>1]: raw 20 -> [10,20]; raw 18 -> [9,18]
        assertEquals(10, ProtectionConfig.legacyRoll(20, MIN_ROLL));
        assertEquals(20, ProtectionConfig.legacyRoll(20, MAX_ROLL));
        assertEquals(9, ProtectionConfig.legacyRoll(18, MIN_ROLL));
        assertEquals(18, ProtectionConfig.legacyRoll(18, MAX_ROLL));
    }

    @Test
    void applyLegacyClampsRolledEpfAt20() {
        assertEquals(2.0f, ProtectionConfig.applyLegacy(10f, 20), 1e-5f);  // ×(25−20)/25
        assertEquals(2.0f, ProtectionConfig.applyLegacy(10f, 25), 1e-5f);  // clamped to 20
        assertEquals(4.8f, ProtectionConfig.applyLegacy(8f, 10), 1e-5f);   // 8×(25−10)/25
        assertEquals(10.0f, ProtectionConfig.applyLegacy(10f, 0), 1e-5f);  // no protection
    }

    @Test
    void applyModernClampsSumAt20() {
        assertEquals(3.6f, ProtectionConfig.applyModern(10f, 16f), 1e-5f); // Prot IV full set ×(1−16/25)
        assertEquals(2.0f, ProtectionConfig.applyModern(10f, 30f), 1e-5f); // clamped to 20
        assertEquals(10.0f, ProtectionConfig.applyModern(10f, 0f), 1e-5f); // no protection
    }

    @Test
    void bypassModelQueriesAndMerge() {
        Bypass b = Bypass.builder().armor(true).effect(RESISTANCE).enchant(PROTECTION).build();
        assertTrue(b.armorStage());
        assertFalse(b.effectStage());           // broad effects not set
        assertTrue(b.effect(RESISTANCE));       // but targeted
        assertFalse(b.enchant(FIRE_PROTECTION));
        assertTrue(b.enchant(PROTECTION));

        Bypass merged = b.merge(Bypass.builder().all().build());
        assertTrue(merged.armorStage());
        assertTrue(merged.effectStage());       // all subsumes effects
        assertTrue(merged.enchantStage());
        assertEquals(Bypass.NONE, Bypass.NONE.merge(null));
    }

    @Test
    void modernProtectionReducesAllDamage() {
        LivingEntity victim = zombie(new Pos(0, 64, 400));
        wearAll(victim, PROTECTION, 4); // 4 pieces × (4×1) = 16 EPF
        ProtectionConfig modern = ProtectionConfig.builder().formula(Formula.MODERN_LINEAR).build();
        assertEquals(3.6f, modern.damageAfterProtection(
                victim, MeleeDamage.INSTANCE.protectionCategories(), 10f, MAX_ROLL, Bypass.NONE), 1e-4f); // 10 × 0.36
    }

    @Test
    void modernFeatherFallingGatedToFall() {
        LivingEntity victim = zombie(new Pos(0, 64, 401));
        victim.setEquipment(EquipmentSlot.BOOTS, enchanted(Material.LEATHER_BOOTS, FEATHER_FALLING, 4)); // 4×3 = 12 EPF
        ProtectionConfig modern = ProtectionConfig.builder().formula(Formula.MODERN_LINEAR).build();
        // fall: Feather Falling counts -> 10 × (1 − 12/25)
        assertEquals(5.2f, modern.damageAfterProtection(
                victim, FallDamage.INSTANCE.protectionCategories(), 10f, MAX_ROLL, Bypass.NONE), 1e-4f);
        // melee: not the FALL category -> unchanged
        assertEquals(10.0f, modern.damageAfterProtection(
                victim, MeleeDamage.INSTANCE.protectionCategories(), 10f, MAX_ROLL, Bypass.NONE), 1e-4f);
    }

    @Test
    void modernFireProtectionGatedToFireAndSurvivesArmorBypass() {
        LivingEntity victim = zombie(new Pos(0, 64, 402));
        victim.setEquipment(EquipmentSlot.BOOTS, enchanted(Material.LEATHER_BOOTS, FIRE_PROTECTION, 4)); // 4×2 = 8 EPF
        ProtectionConfig modern = ProtectionConfig.builder().formula(Formula.MODERN_LINEAR).build();
        // on_fire bypasses armor but is still the FIRE category, so Fire Protection reduces it: 10 × (1 − 8/25)
        assertEquals(6.8f, modern.damageAfterProtection(
                victim, BurningDamage.INSTANCE.protectionCategories(), 10f, MAX_ROLL, Bypass.NONE), 1e-4f);
        // melee: not the FIRE category -> unchanged
        assertEquals(10.0f, modern.damageAfterProtection(
                victim, MeleeDamage.INSTANCE.protectionCategories(), 10f, MAX_ROLL, Bypass.NONE), 1e-4f);
    }

    @Test
    void legacyProtectionRollSpansVanillaRange() {
        LivingEntity victim = zombie(new Pos(0, 64, 403));
        wearAll(victim, PROTECTION, 4); // raw 4×5 = 20, clamp 25 -> 20
        ProtectionConfig legacy = ProtectionConfig.builder().formula(Formula.LEGACY_RANDOMIZED).build();
        var cats = MeleeDamage.INSTANCE.protectionCategories();
        // roll [10,20] -> i [10,20] -> 10×(25−i)/25 -> [2.0, 6.0]
        assertEquals(2.0f, legacy.damageAfterProtection(victim, cats, 10f, MAX_ROLL, Bypass.NONE), 1e-4f);
        assertEquals(6.0f, legacy.damageAfterProtection(victim, cats, 10f, MIN_ROLL, Bypass.NONE), 1e-4f);
    }

    @Test
    void targetedEnchantBypassSkipsOnlyThatEnchant() {
        LivingEntity victim = zombie(new Pos(0, 64, 408));
        wearAll(victim, PROTECTION, 4); // 16 EPF modern
        ProtectionConfig modern = ProtectionConfig.builder().formula(Formula.MODERN_LINEAR).build();
        var cats = MeleeDamage.INSTANCE.protectionCategories();
        // bypassing minecraft:protection drops all the EPF -> no reduction
        Bypass bypassProtection = Bypass.builder().enchant(PROTECTION).build();
        assertEquals(10.0f, modern.damageAfterProtection(victim, cats, 10f, MAX_ROLL, bypassProtection), 1e-4f);
        // bypassing a different enchant leaves Protection's EPF intact
        Bypass bypassOther = Bypass.builder().enchant(FIRE_PROTECTION).build();
        assertEquals(3.6f, modern.damageAfterProtection(victim, cats, 10f, MAX_ROLL, bypassOther), 1e-4f);
    }

    @Test
    void mitigateHonoursTargetedArmorAttributeAndBypassAll() {
        LivingEntity victim = zombie(new Pos(0, 64, 409));
        victim.getAttribute(Attribute.ARMOR).setBaseValue(20); // LEGACY harness: armor reduction is deterministic
        // no bypass: 10 × (25−20)/25 = 2.0
        assertEquals(2.0f, mitigate(victim, 10f, Bypass.NONE), 1e-4f);
        // targeted bypass of the armor attribute -> armor stage skipped
        assertEquals(10.0f, mitigate(victim, 10f, Bypass.builder().attribute(ARMOR_ATTR).build()), 1e-4f);
        // blanket bypassAll -> everything skipped
        assertEquals(10.0f, mitigate(victim, 10f, Bypass.builder().all().build()), 1e-4f);
    }

    @Test
    void presetsWireTheFormula() {
        assertEquals(Formula.LEGACY_RANDOMIZED, Attributes.config().protection.formula());
        assertEquals(Formula.MODERN_LINEAR, io.github.term4.polyp.presets.vanilla.Attributes.config().protection.formula());
    }

    @Test
    void protectionArmorReducesMoreThanPlainArmorInLivePipeline() {
        LivingEntity attacker = zombie(new Pos(0, 64, 405));
        LivingEntity plain = zombie(new Pos(0, 64, 406));
        LivingEntity protected_ = zombie(new Pos(0, 64, 407));
        wearAll(plain, PROTECTION, 0);       // leather, no enchant
        wearAll(protected_, PROTECTION, 4);  // leather + Protection IV
        plain.setHealth(20f);
        protected_.setHealth(20f);

        ItemStack sword = ItemStack.of(Material.DIAMOND_SWORD);
        services.damage().apply(MeleeDamage.INSTANCE.snapshot(attacker, plain, false, sword, services));
        services.damage().apply(MeleeDamage.INSTANCE.snapshot(attacker, protected_, false, sword, services));
        // protection only reduces (min legacy roll > 0), so the protected victim always keeps more health
        assertTrue(protected_.getHealth() > plain.getHealth(),
                "protected=" + protected_.getHealth() + " plain=" + plain.getHealth());
    }

    private float mitigate(LivingEntity victim, float damage, Bypass bypass) {
        return services.attributes().mitigate(victim, damage,
                MitigationRequest.of(Set.of(), bypass, MAX_ROLL));
    }

    private static void wearAll(LivingEntity le, Key enchant, int level) {
        le.setEquipment(EquipmentSlot.HELMET, enchanted(Material.LEATHER_HELMET, enchant, level));
        le.setEquipment(EquipmentSlot.CHESTPLATE, enchanted(Material.LEATHER_CHESTPLATE, enchant, level));
        le.setEquipment(EquipmentSlot.LEGGINGS, enchanted(Material.LEATHER_LEGGINGS, enchant, level));
        le.setEquipment(EquipmentSlot.BOOTS, enchanted(Material.LEATHER_BOOTS, enchant, level));
    }
}
