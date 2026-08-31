package io.github.term4.polyp.mechanics.damage.types.fall;

import io.github.term4.polyp.presets.vanilla18.Damage;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Jump Boost reduces fall damage in both presets, via different vehicles. MODERN: the safe distance is the victim's
 * SAFE_FALL_DISTANCE attribute, onto which {@code JumpBoost.MODERN} pushes {@code +1/level} (push pinned in {@code CatalogTest}).
 * LEGACY: 1.8 has no such attribute, so the threshold subtracts {@code (amp+1)} blocks straight off the live effect
 * (vanilla {@code EntityLiving.e}). MODERN_FLOOR is {@code floor(distance + ε - safe)}; LEGACY_CEIL is {@code ceil(distance - safe)}.
 */
class SafeFallDistanceTest extends HeadlessServerTest {

    /** The preset's own config, so this guards the wiring rather than a copy of the formula. */
    private static FallDamageConfig modernFall() {
        return (FallDamageConfig) io.github.term4.polyp.presets.vanilla.Damage.config().typeConfig(FallDamage.KEY);
    }

    private static FallDamageConfig legacyFall() {
        return (FallDamageConfig) Damage.config().typeConfig(FallDamage.KEY);
    }

    private static DamageContext fallCtx(LivingEntity victim, float distance) {
        DamageSnapshot snap = DamageSnapshot.of(victim, FallDamage.INSTANCE).withDetail(FallDetail.of(distance));
        return DamageContext.of(snap, services);
    }

    @Test
    void modernDefaultSafeDistanceIsThreeBlocks() {
        LivingEntity z = zombie(new Pos(0, 64, 200));
        // floor(8 + ε - 3) = 5
        assertEquals(5.0, modernFall().baseAmount(fallCtx(z, 8f)), 1e-9);
    }

    @Test
    void modernJumpBoostSafeFallModifierReducesDamage() {
        LivingEntity z = zombie(new Pos(0, 64, 201));
        // simulate JumpBoost.MODERN level 2: SAFE_FALL_DISTANCE += 2 -> safe distance = 5
        z.getAttribute(Attribute.SAFE_FALL_DISTANCE).addModifier(
                new AttributeModifier(Key.key("polyp:test/jump-boost"), 2.0, AttributeOperation.ADD_VALUE));
        // floor(8 + ε - 5) = 3
        assertEquals(3.0, modernFall().baseAmount(fallCtx(z, 8f)), 1e-9);
    }

    @Test
    void legacyJumpBoostSubtractsBlocksFromFallDistance() {
        LivingEntity z = zombie(new Pos(0, 64, 202));
        // baseline: ceil(8 - 3) = 5
        assertEquals(5.0, legacyFall().baseAmount(fallCtx(z, 8f)), 1e-9);
        // Jump Boost II (amplifier 1) -> safe = 3 + (1+1) = 5 -> ceil(8 - 5) = 3
        z.addEffect(new Potion(PotionEffect.JUMP_BOOST, (byte) 1, 600));
        assertEquals(3.0, legacyFall().baseAmount(fallCtx(z, 8f)), 1e-9);
    }
}
