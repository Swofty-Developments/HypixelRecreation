package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.ProtectionEnchant;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Fire Aspect ignites for {@code level × 4} seconds (vanilla {@code EntityHuman.attack}). */
class FireAspectTest extends HeadlessServerTest {

    @Test
    void ignitesVictimOnMeleeHit() {
        LivingEntity attacker = zombie(new Pos(0, 64, 500));
        LivingEntity victim = zombie(new Pos(0, 64, 501));
        victim.setHealth(20f);
        services.damage().apply(MeleeDamage.INSTANCE.snapshot(attacker, victim, false, fireAspectSword(2), services));
        assertEquals(2 * 4 * 20, victim.getFireTicks());
    }

    @Test
    void plainWeaponDoesNotIgnite() {
        LivingEntity attacker = zombie(new Pos(0, 64, 502));
        LivingEntity victim = zombie(new Pos(0, 64, 503));
        victim.setHealth(20f);
        services.damage().apply(MeleeDamage.INSTANCE.snapshot(
                attacker, victim, false, ItemStack.of(Material.DIAMOND_SWORD), services));
        assertEquals(0, victim.getFireTicks());
    }

    /** Vanilla setOnFire only extends: a fresh weaker ignition must not cut a longer burn short. */
    @Test
    void neverShortensALongerBurn() {
        LivingEntity attacker = zombie(new Pos(0, 64, 504));
        LivingEntity victim = zombie(new Pos(0, 64, 505));
        victim.setHealth(20f);
        victim.setFireTicks(300);
        services.damage().apply(MeleeDamage.INSTANCE.snapshot(attacker, victim, false, fireAspectSword(1), services));
        assertEquals(300, victim.getFireTicks(), "a 4s ignition must not overwrite a 15s burn");
    }

    /** Vanilla EnchantmentProtection.a: -15% duration per Fire Protection level, highest armor piece. */
    @Test
    void fireProtectionCutsTheBurnDuration() {
        LivingEntity attacker = zombie(new Pos(0, 64, 506));
        LivingEntity victim = zombie(new Pos(0, 64, 507));
        victim.setHealth(20f);
        victim.setBoots(enchanted(Material.DIAMOND_BOOTS, ProtectionEnchant.FIRE_PROTECTION.key(), 4));
        services.damage().apply(MeleeDamage.INSTANCE.snapshot(attacker, victim, false, fireAspectSword(2), services));
        assertEquals(160 - (int) Math.floor(160 * 4 * 0.15f), victim.getFireTicks());
    }

    /** Vanilla applies fire aspect on ANY landed hit - an overdamage replacement included. */
    @Test
    void overdamageStillIgnites() {
        LivingEntity attacker = zombie(new Pos(0, 64, 508));
        LivingEntity victim = zombie(new Pos(0, 64, 509));
        victim.setHealth(20f);
        services.damage().apply(MeleeDamage.INSTANCE.snapshot(
                attacker, victim, false, ItemStack.of(Material.WOODEN_SWORD), services));
        assertEquals(0, victim.getFireTicks(), "the opener carries no fire");
        var outcome = services.damage().apply(MeleeDamage.INSTANCE.snapshot(
                attacker, victim, false, fireAspectSword(2), services));
        assertEquals(io.github.term4.polyp.mechanics.damage.DamageSystem.DamageOutcome.OVERDAMAGE, outcome,
                "the stronger hit inside the window must land as overdamage");
        assertEquals(2 * 4 * 20, victim.getFireTicks(), "and it ignites like any landed hit");
    }

    private static ItemStack fireAspectSword(int level) {
        return enchanted(Material.DIAMOND_SWORD, FireAspect.KEY, level);
    }
}
