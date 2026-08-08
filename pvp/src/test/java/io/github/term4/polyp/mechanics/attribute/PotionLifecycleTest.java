package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.catalog.effect.Invisibility;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Speed;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** An effect with a registered source applies + removes its contribution on the Minestom add/remove events. */
class PotionLifecycleTest extends HeadlessServerTest {

    @Test
    void speedPushesMovementSpeedModifierThenRemovesIt() {
        LivingEntity e = zombie(new Pos(0, 64, 60));
        double base = e.getAttributeValue(Attribute.MOVEMENT_SPEED);
        PotionEffect speed = PotionEffect.fromKey(Speed.KEY);
        assertNotNull(speed, "speed effect");

        e.addEffect(new Potion(speed, 0, 600));                 // Speed I = ×1.2
        assertEquals(base * 1.2, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);

        e.removeEffect(speed);
        assertEquals(base, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);
    }

    @Test
    void speedScalesWithAmplifier() {
        LivingEntity e = zombie(new Pos(0, 64, 61));
        double base = e.getAttributeValue(Attribute.MOVEMENT_SPEED);
        PotionEffect speed = PotionEffect.fromKey(Speed.KEY);
        assertNotNull(speed, "speed effect");
        e.addEffect(new Potion(speed, 1, 600));                 // Speed II = ×1.4
        try {
            assertEquals(base * 1.4, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);
        } finally {
            e.removeEffect(speed);
        }
    }

    @Test
    void replacingALowerEffectWithAHigherOneKeepsTheHigherModifier() {
        // regression: Minestom replaces via add(new)+remove(old); a per-level modifier id keeps the old effect's
        // removal from stripping the new one's push (the "speed 2 then speed 10 does nothing" bug).
        LivingEntity e = zombie(new Pos(0, 64, 63));
        double base = e.getAttributeValue(Attribute.MOVEMENT_SPEED);
        PotionEffect speed = PotionEffect.fromKey(Speed.KEY);
        assertNotNull(speed, "speed effect");

        e.addEffect(new Potion(speed, 1, 600));                 // Speed II = ×1.4
        assertEquals(base * 1.4, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);
        e.addEffect(new Potion(speed, 9, 600));                 // Speed X = ×3.0, replaces Speed II
        try {
            assertEquals(base * 3.0, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);
        } finally {
            e.removeEffect(speed);
        }
        assertEquals(base, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);
    }

    @Test
    void deathClearsEffectsAndTheirModifiers() {
        // Minestom's kill() leaves effects intact; the death path clears them (DeathConfig.clearEffects, default on)
        LivingEntity e = zombie(new Pos(0, 64, 66));
        double base = e.getAttributeValue(Attribute.MOVEMENT_SPEED);
        PotionEffect speed = PotionEffect.fromKey(Speed.KEY);
        assertNotNull(speed, "speed effect");
        e.addEffect(new Potion(speed, 0, 600));
        assertEquals(base * 1.2, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);

        e.kill();
        assertTrue(e.getActiveEffects().isEmpty(), "death clears active effects");
        assertEquals(base, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);
    }

    /**
     * Refreshing an effect at the SAME level used to leave it modifier-less: Minestom fires the add event before
     * the removeEffect it does internally, so the removal stripped the push the add had just made and a speed II
     * player on a speed II splash ran at base speed with the effect still showing.
     */
    @Test
    void refreshingAtTheSameLevelKeepsTheModifier() {
        LivingEntity e = zombie(new Pos(0, 64, 67));
        double base = e.getAttributeValue(Attribute.MOVEMENT_SPEED);
        PotionEffect speed = PotionEffect.fromKey(Speed.KEY);
        assertNotNull(speed, "speed effect");
        try {
            e.addEffect(new Potion(speed, 1, 600));                  // Speed II
            assertEquals(base * 1.4, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9);

            e.addEffect(new Potion(speed, 1, 1800));                 // the splash: same level, longer
            assertEquals(base * 1.4, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9,
                    "a same-level refresh keeps the speed it already had");

            e.addEffect(new Potion(speed, 0, 1800));                 // down a level: the II push must go
            assertEquals(base * 1.2, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9,
                    "a level change still swaps the modifier");
        } finally {
            e.removeEffect(speed);
        }
        assertEquals(base, e.getAttributeValue(Attribute.MOVEMENT_SPEED), 1e-9,
                "and the refreshed effect still cleans up on removal");
    }

    @Test
    void invisibilityTogglesTheFlag() {
        LivingEntity e = zombie(new Pos(0, 64, 62));
        PotionEffect invis = PotionEffect.fromKey(Invisibility.KEY);
        assertNotNull(invis, "invisibility effect");
        assertFalse(e.isInvisible());

        e.addEffect(new Potion(invis, 0, 600));
        assertTrue(e.isInvisible());

        e.removeEffect(invis);
        assertFalse(e.isInvisible());
    }
}
