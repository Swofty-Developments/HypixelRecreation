package io.github.term4.polyp.mechanics.attribute.catalog;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 1.8 {@code MobEffect.a} combine rule: stronger replaces, equal extends-if-longer, weaker discarded. */
class VanillaPotionsCombineTest extends HeadlessServerTest {

    private static Potion active(LivingEntity e) {
        return e.getActiveEffects().stream()
                .filter(t -> t.potion().effect() == PotionEffect.SPEED)
                .findFirst().orElseThrow().potion();
    }

    @Test
    void strongerAmplifierReplacesEvenIfShorter() {
        LivingEntity e = zombie(new Pos(0, 64, 70));
        VanillaPotions.addEffect(e, new Potion(PotionEffect.SPEED, 0, 600));
        VanillaPotions.addEffect(e, new Potion(PotionEffect.SPEED, 1, 100));
        assertEquals(1, active(e).amplifier());
        assertEquals(100, active(e).duration());
    }

    @Test
    void equalAmplifierExtendsOnlyIfLonger() {
        LivingEntity e = zombie(new Pos(0, 64, 71));
        VanillaPotions.addEffect(e, new Potion(PotionEffect.SPEED, 0, 600));
        VanillaPotions.addEffect(e, new Potion(PotionEffect.SPEED, 0, 900));
        assertEquals(900, active(e).duration());
        VanillaPotions.addEffect(e, new Potion(PotionEffect.SPEED, 0, 200));
        assertEquals(900, active(e).duration());
    }

    @Test
    void weakerAmplifierDiscardedEvenIfLonger() {
        LivingEntity e = zombie(new Pos(0, 64, 72));
        VanillaPotions.addEffect(e, new Potion(PotionEffect.SPEED, 1, 100));
        VanillaPotions.addEffect(e, new Potion(PotionEffect.SPEED, 0, 9600));
        assertEquals(1, active(e).amplifier());
        assertEquals(100, active(e).duration());
    }
}
