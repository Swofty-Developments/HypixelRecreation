package io.github.term4.polyp.mechanics.attribute.catalog;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tipped-arrow logic: the {@link VanillaPotions} base-potion table and the harming payload's damage handler. */
class TippedArrowTest extends HeadlessServerTest {

    @Test
    void vanillaPotionsResolvesBaseType() {
        List<CustomPotionEffect> slow = VanillaPotions.effects(PotionType.SLOW_FALLING);
        assertEquals(1, slow.size());
        assertEquals(PotionEffect.SLOW_FALLING, slow.get(0).id());
        assertEquals(1800, slow.get(0).duration());

        List<CustomPotionEffect> strongHarming = VanillaPotions.effects(PotionType.STRONG_HARMING);
        assertEquals(PotionEffect.INSTANT_DAMAGE, strongHarming.get(0).id());
        assertEquals(1, strongHarming.get(0).amplifier());

        assertTrue(VanillaPotions.effects(PotionType.WATER).isEmpty(), "unmapped base potion → no rows (use customEffects)");
    }

    @Test
    void harmingPayloadDealsDamage() {
        LivingEntity e = zombie(new Pos(0, 64, 120));
        float before = e.getHealth();
        e.addEffect(new Potion(PotionEffect.INSTANT_DAMAGE, 0, 1, (byte) 0));
        assertTrue(e.getHealth() < before, "harming (instant damage) reduces health (" + e.getHealth() + " < " + before + ")");
    }
}
