package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.catalog.effect.InstantHealth;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Instant Health heals {@code 4<<amp}; Resistance scales damage {@code ×(25-5·level)/25}, level 5 = immune
 * (1.8 + 26).
 */
class EffectApplicationTest extends HeadlessServerTest {

    private static final Key RESISTANCE = Key.key("minecraft:resistance");

    @Test
    void instantHealthHealsFourShiftedByAmplifier() {
        LivingEntity e = zombie(new Pos(0, 64, 80));
        PotionEffect effect = PotionEffect.fromKey(InstantHealth.KEY);
        assertNotNull(effect, "instant health effect");

        e.setHealth(5f);
        e.addEffect(new Potion(effect, (byte) 0, 1)); // Instant Health I -> +4
        assertEquals(9f, e.getHealth(), 1e-4);

        e.setHealth(19f);
        e.addEffect(new Potion(effect, (byte) 0, 1)); // capped at max (20)
        assertEquals(20f, e.getHealth(), 1e-4);
    }

    @Test
    void resistanceScalesIncomingDamage() {
        LivingEntity attacker = zombie(new Pos(0, 64, 81));
        LivingEntity victim = zombie(new Pos(0, 64, 82)); // not a Player: no absorption buffer in the way
        victim.getAttribute(Attribute.ARMOR).setBaseValue(0); // isolate resistance from the armor stage
        PotionEffect resistance = PotionEffect.fromKey(RESISTANCE);
        assertNotNull(resistance, "resistance effect");

        victim.setHealth(20f);
        victim.addEffect(new Potion(resistance, (byte) 1, 600)); // Resistance II (level 2) -> ×(25-10)/25 = 0.6
        DamageSnapshot snap = MeleeDamage.INSTANCE.snapshot(attacker, victim, false, ItemStack.of(Material.DIAMOND_SWORD), services);
        services.damage().apply(snap); // 7 × 0.6 = 4.2
        assertEquals(15.8f, victim.getHealth(), 1e-2);
    }

    @Test
    void resistanceFiveIsImmune() {
        LivingEntity attacker = zombie(new Pos(0, 64, 83));
        LivingEntity victim = zombie(new Pos(0, 64, 84));
        PotionEffect resistance = PotionEffect.fromKey(RESISTANCE);
        assertNotNull(resistance, "resistance effect");

        victim.setHealth(20f);
        victim.addEffect(new Potion(resistance, (byte) 4, 600)); // Resistance V (level 5) -> ×0, immune
        DamageSnapshot snap = MeleeDamage.INSTANCE.snapshot(attacker, victim, false, ItemStack.of(Material.DIAMOND_SWORD), services);
        services.damage().apply(snap);
        assertEquals(20f, victim.getHealth(), 1e-4);
    }
}
