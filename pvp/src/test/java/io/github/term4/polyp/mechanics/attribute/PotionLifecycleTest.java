package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.catalog.effect.Absorption;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Invisibility;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Speed;
import io.github.term4.polyp.mechanics.attribute.source.Behavior;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

    /** Re-eating a gapple refills absorption, never stacks it: vanilla's combine runs the behavior remove-then-add,
     *  and the remove clamps at 0 (1.8 {@code MobEffectAbsorption} + {@code setAbsorptionHearts}). */
    @Test
    void absorptionRefreshRefillsInsteadOfStacking() {
        FakePlayer p = FakePlayer.connect(instance, new Pos(0, 64, 68.5), "Gapple");
        PotionEffect absorption = PotionEffect.fromKey(Absorption.KEY);
        assertNotNull(absorption, "absorption effect");
        try {
            p.player.addEffect(new Potion(absorption, 0, 2400));
            assertEquals(4f, p.player.getAdditionalHearts(), 1e-6, "one gapple = 4 absorption");

            p.player.addEffect(new Potion(absorption, 0, 2400));
            assertEquals(4f, p.player.getAdditionalHearts(), 1e-6, "a second gapple refreshes, not stacks");

            p.player.setAdditionalHearts(1f); // absorbed some hits
            p.player.addEffect(new Potion(absorption, 0, 2400));
            assertEquals(4f, p.player.getAdditionalHearts(), 1e-6, "and refills damaged absorption");

            p.player.removeEffect(absorption);
            assertEquals(0f, p.player.getAdditionalHearts(), 1e-6, "expiry takes the hearts (1.8)");
        } finally {
            p.player.remove();
        }
    }

    /** Custom sources own their lifecycle: {@code sources(...)} registers (same key overrides the catalog), and
     *  {@code onRefresh} replaces the default remove-then-apply pair on a re-application. */
    @Test
    void customSourceOverridesRefreshSemantics() {
        List<String> calls = new ArrayList<>();
        EntitySource luck = new EntitySource(Key.key("minecraft:luck")) {
            @Override public Behavior behavior() {
                return new Behavior() {
                    @Override public void onApply(Entity entity, int level) { calls.add("apply:" + level); }
                    @Override public void onRemove(Entity entity, int level) { calls.add("remove:" + level); }
                    @Override public void onRefresh(Entity entity, int oldLevel, int newLevel) { calls.add("refresh:" + oldLevel + ">" + newLevel); }
                };
            }
        };
        var system = new AttributeSystem(polyp, AttributeConfig.builder().sources(luck).build());
        MinecraftServer.getGlobalEventHandler().addChild(system.node());
        LivingEntity e = zombie(new Pos(0, 64, 69));
        try {
            e.addEffect(new Potion(PotionEffect.LUCK, 0, 600));
            e.addEffect(new Potion(PotionEffect.LUCK, 1, 600));
            e.removeEffect(PotionEffect.LUCK);
            assertEquals(List.of("apply:1", "refresh:1>2", "remove:2"), calls);
        } finally {
            MinecraftServer.getGlobalEventHandler().removeChild(system.node());
            e.remove();
        }
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
