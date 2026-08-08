package io.github.term4.polyp.mechanics.attribute.catalog;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Efficiency;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.FireAspect;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Sharpness;
import io.github.term4.polyp.mechanics.attribute.combat.OnHit;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Haste;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.JumpBoost;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.MiningFatigue;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Strength;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Weakness;
import net.minestom.server.entity.attribute.AttributeOperation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Each catalog variant's modifiers at exact vanilla constants, independent of any calculator. */
class CatalogTest {

    @Test
    void sharpnessLegacyIs1_25PerLevelOnMeleeFlatAdd() {
        for (int lvl = 1; lvl <= 5; lvl++) {
            Source.Mod m = only(Sharpness.LEGACY.modifiers(lvl));
            assertEquals(Attribute.MELEE_FLAT_ADD, m.attribute());
            assertEquals(AttributeOperation.ADD_VALUE, m.operation());
            assertEquals(1.25 * lvl, m.amount(), 1e-9);
        }
    }

    @Test
    void sharpnessModernIsHalfLevelPlusHalf() {
        assertEquals(1.0, only(Sharpness.MODERN.modifiers(1)).amount(), 1e-9);
        assertEquals(2.0, only(Sharpness.MODERN.modifiers(3)).amount(), 1e-9);
        assertEquals(3.0, only(Sharpness.MODERN.modifiers(5)).amount(), 1e-9);
    }

    @Test
    void strengthLegacyIsMultiplicativeAttackDamage() {
        Source.Mod m = only(Strength.LEGACY.modifiers(1));
        assertEquals(Attribute.ATTACK_DAMAGE, m.attribute());
        assertEquals(AttributeOperation.ADD_MULTIPLIED_TOTAL, m.operation());
        assertEquals(1.3, m.amount(), 1e-9);
        assertEquals(2.6, only(Strength.LEGACY.modifiers(2)).amount(), 1e-9);
    }

    @Test
    void strengthModernIsFlatAttackDamage() {
        Source.Mod m = only(Strength.MODERN.modifiers(2));
        assertEquals(Attribute.ATTACK_DAMAGE, m.attribute());
        assertEquals(AttributeOperation.ADD_VALUE, m.operation());
        assertEquals(6.0, m.amount(), 1e-9); // 3 × 2
    }

    @Test
    void weaknessIsFlatNegativeAttackDamage() {
        Source.Mod legacy = only(Weakness.LEGACY.modifiers(1));
        assertEquals(Attribute.ATTACK_DAMAGE, legacy.attribute());
        assertEquals(AttributeOperation.ADD_VALUE, legacy.operation());
        assertEquals(-0.5, legacy.amount(), 1e-9);          // 1.8: -0.5 × level
        assertEquals(-8.0, only(Weakness.MODERN.modifiers(2)).amount(), 1e-9); // 26: -4 × level
    }

    @Test
    void zeroLevelContributesNothing() {
        assertTrue(Sharpness.LEGACY.modifiers(0).isEmpty());
        assertTrue(Strength.LEGACY.modifiers(0).isEmpty());
        assertTrue(Weakness.LEGACY.modifiers(0).isEmpty());
    }

    @Test
    void variantsShareTheVanillaKey() {
        assertEquals(Sharpness.KEY, Sharpness.MODERN.key());
        assertEquals(Strength.KEY, Strength.LEGACY.key());
    }

    @Test
    void hasteAndMiningFatigueModernAreAttackSpeed() {
        Source.Mod haste = only(Haste.MODERN.modifiers(1));
        assertEquals(Attribute.ATTACK_SPEED, haste.attribute());
        assertEquals(AttributeOperation.ADD_MULTIPLIED_TOTAL, haste.operation());
        assertEquals(0.1, haste.amount(), 1e-9);
        assertEquals(-0.2, only(MiningFatigue.MODERN.modifiers(2)).amount(), 1e-9); // -0.1 × 2
    }

    @Test
    void efficiencyIsLevelSquaredPlusOneOnMiningEfficiency() {
        // identical in 1.8 (i²+1, EntityHuman) and 26 (mining_efficiency levels_squared+1)
        for (int lvl = 1; lvl <= 5; lvl++) {
            Source.Mod m = only(Efficiency.INSTANCE.modifiers(lvl));
            assertEquals(Attribute.MINING_EFFICIENCY, m.attribute());
            assertEquals(AttributeOperation.ADD_VALUE, m.operation());
            assertEquals(lvl * lvl + 1, m.amount(), 1e-9);
        }
        assertTrue(Efficiency.INSTANCE.modifiers(0).isEmpty());
    }

    @Test
    void jumpBoostModernIsSafeFallDistance() {
        Source.Mod m = only(JumpBoost.MODERN.modifiers(3));
        assertEquals(Attribute.SAFE_FALL_DISTANCE, m.attribute());
        assertEquals(AttributeOperation.ADD_VALUE, m.operation());
        assertEquals(3.0, m.amount(), 1e-9); // 1 × 3
    }

    @Test
    void fireAspectIsAnOnHitWithNoAttributeModifiers() {
        assertTrue(FireAspect.INSTANCE.modifiers(2).isEmpty());
        assertTrue(FireAspect.INSTANCE instanceof OnHit);
        assertEquals(FireAspect.KEY, FireAspect.INSTANCE.key());
    }

    private static Source.Mod only(List<Source.Mod> mods) {
        assertEquals(1, mods.size());
        return mods.get(0);
    }
}
