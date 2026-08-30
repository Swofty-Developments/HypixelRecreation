package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.attribute.catalog.VanillaPotions;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.mechanics.projectile.types.SplashPotion;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.Directions;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.potion.TimedPotion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The vanilla splash model (1.8 {@code EntityPotion.a()}). Each case gets its own far-apart impact point in a
 * dedicated chunk so the shared-instance zombies of other tests can't fall in range.
 */
class SplashPotionTest extends HeadlessServerTest {

    private static final Pos BASE = new Pos(1000.5, 64, 1000.5);

    @BeforeAll
    static void loadArena() {
        instance.loadChunk(62, 62).join();
        instance.loadChunk(63, 63).join();
    }

    private static SplashPotionEntity potionAt(Pos at, PotionType potion) {
        return potionAt(at, potion, ProjectileTypeConfig.builder().build());
    }

    private static SplashPotionEntity potionAt(Pos at, PotionType potion, ProjectileTypeConfig config) {
        ItemStack item = ItemStack.of(Material.SPLASH_POTION)
                .with(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        var snap = ProjectileSnapshot.of(looseZombie(), SplashPotion.INSTANCE).withItem(item);
        var entity = (SplashPotionEntity) SplashPotion.INSTANCE.createEntity(snap.shooter(), snap, config);
        entity.setInstance(instance, at).join();
        return entity;
    }

    private static Optional<TimedPotion> effect(LivingEntity e, PotionEffect id) {
        return e.getActiveEffects().stream().filter(t -> t.potion().effect() == id).findFirst();
    }

    @Test
    void timedEffectScalesWithDistance() {
        Pos at = BASE;
        LivingEntity near = zombie(at.add(2, 0, 0));    // intensity 1 - 2/4 = 0.5
        SplashPotionEntity potion = potionAt(at, PotionType.SWIFTNESS);
        potion.onImpact(null);
        assertEquals(1800, effect(near, PotionEffect.SPEED).orElseThrow().potion().duration()); // (0.5 * 3600 + 0.5)
        near.remove();
        potion.remove();
    }

    @Test
    void directHitGetsFullIntensity() {
        Pos at = BASE.add(32, 0, 0);
        LivingEntity hit = zombie(at);
        SplashPotionEntity potion = potionAt(at, PotionType.SWIFTNESS);
        potion.onImpact(hit);
        assertEquals(3600, effect(hit, PotionEffect.SPEED).orElseThrow().potion().duration());
        hit.remove();
        potion.remove();
    }

    @Test
    void tinyIntensityFallsUnderTheTickFloor() {
        Pos at = BASE.add(0, 0, 32);
        LivingEntity far = zombie(at.add(3.99, 0, 0));  // intensity ~0.0025 -> 9 ticks, under the 20 floor
        SplashPotionEntity potion = potionAt(at, PotionType.SWIFTNESS);
        potion.onImpact(null);
        assertTrue(effect(far, PotionEffect.SPEED).isEmpty());
        far.remove();
        potion.remove();
    }

    @Test
    void harmingDealsScaledMagicDamage() {
        Pos at = BASE.add(32, 0, 32);
        LivingEntity victim = zombie(at.add(2, 0, 0)); // intensity 0.5 -> (int)(0.5 * 6 + 0.5) = 3
        victim.setHealth(20); // a fresh Minestom LivingEntity spawns at 1 health
        SplashPotionEntity potion = potionAt(at, PotionType.HARMING);
        potion.onImpact(null);
        assertEquals(17, victim.getHealth(), 1e-6);
        victim.remove();
        potion.remove();
    }

    @Test
    void healingRestoresScaledHealth() {
        Pos at = BASE.add(64, 0, 0);
        LivingEntity hurt = zombie(at.add(2, 0, 0));
        hurt.setHealth(10);
        SplashPotionEntity potion = potionAt(at, PotionType.HEALING); // intensity 0.5 -> (int)(0.5 * 4 + 0.5) = 2
        potion.onImpact(null);
        assertEquals(12, hurt.getHealth(), 1e-6);
        hurt.remove();
        potion.remove();
    }

    @Test
    void splashEventSplitsPerViewerProtocol() {
        Pos at = BASE.add(64, 0, 64);
        var polyp = Polyp.getInstance();
        var legacyViewer = FakePlayer.connect(instance, at.add(3, 0, 0), "LegacyView");
        var modernViewer = FakePlayer.connect(instance, at.add(-3, 0, 0), "ModernView");
        polyp.clientInfo().setProxyDetails(legacyViewer.player, "{\"version\": 47}");   // 1.8
        polyp.clientInfo().setProxyDetails(modernViewer.player, "{\"version\": 774}");  // modern

        var potion = potionAt(at, PotionType.SWIFTNESS,
                ProjectileTypeConfig.builder().legacyPotionColors(true).build());
        potion.addViewer(legacyViewer.player);
        potion.addViewer(modernViewer.player);
        legacyViewer.sent.clear();
        modernViewer.sent.clear();
        potion.onImpact(null);

        var legacyEvent = legacyViewer.sent(net.minestom.server.network.packet.server.play.WorldEventPacket.class).getFirst();
        var modernEvent = modernViewer.sent(net.minestom.server.network.packet.server.play.WorldEventPacket.class).getFirst();
        assertEquals(16386, legacyEvent.data(), "legacy viewer gets the raw 1.8 splash swiftness value");
        assertEquals(8171462, modernEvent.data(), "modern viewer gets the 1.8-palette speed color (vanilla18 sets legacyPotionColors)");
        legacyViewer.player.remove();
        modernViewer.player.remove();
        potion.remove();
    }

    @Test
    void weakerOrShorterEffectNeverOverridesVanillaCombine() {
        Pos at = BASE.add(96, 0, 0);
        LivingEntity target = zombie(at.add(1, 0, 0));
        // strong swiftness (amp 1, 1800t) first, then a plain splash (amp 0, scaled 3600t): must NOT downgrade
        VanillaPotions.addEffect(target,
                new net.minestom.server.potion.Potion(PotionEffect.SPEED, (byte) 1, 1800));
        SplashPotionEntity potion = potionAt(at, PotionType.SWIFTNESS);
        potion.onImpact(target);
        var kept = effect(target, PotionEffect.SPEED).orElseThrow().potion();
        assertEquals(1, kept.amplifier(), "amp II must survive a splash of amp I");
        assertEquals(1800, kept.duration());
        // same amplifier, longer duration: extends
        VanillaPotions.addEffect(target,
                new net.minestom.server.potion.Potion(PotionEffect.SPEED, (byte) 1, 6000));
        assertEquals(6000, effect(target, PotionEffect.SPEED).orElseThrow().potion().duration());
        target.remove();
        potion.remove();
    }

    @Test
    void modernSplashScalesByBoxDistance() {
        Pos at = BASE.add(96, 0, 96);
        // gap = 2.125 - 0.3 (zombie half-width) - 0.125 (potion half-width) = 1.7; intensity 1 - 1.7/4 = 0.575
        // (the 1.8 center-distance model would give 0.46875 -> 1688)
        LivingEntity target = zombie(at.add(2.125, 0, 0));
        var potion = potionAt(at, PotionType.SWIFTNESS, ProjectileTypeConfig.builder().modernSplash(true).build());
        potion.onImpact(null);
        assertEquals(2070, effect(target, PotionEffect.SPEED).orElseThrow().potion().duration()); // (int)(0.575 * 3600 + 0.5)
        target.remove();
        potion.remove();
    }

    @Test
    void modernInstantSplashUses2007ForModernViewers() {
        Pos at = BASE.add(96, 0, 0);
        var polyp = Polyp.getInstance();
        var modernViewer = FakePlayer.connect(instance, at.add(-3, 0, 0), "ModernV2007");
        polyp.clientInfo().setProxyDetails(modernViewer.player, "{\"version\": 774}");

        var potion = potionAt(at, PotionType.HARMING, ProjectileTypeConfig.builder().modernSplash(true).build());
        potion.addViewer(modernViewer.player);
        modernViewer.sent.clear();
        potion.onImpact(null);

        var event = modernViewer.sent(net.minestom.server.network.packet.server.play.WorldEventPacket.class).getFirst();
        assertEquals(2007, event.effectId(), "instant potion splash is 2007 in the 26.1 model");
        modernViewer.player.remove();
        potion.remove();
    }

    @Test
    void vanillaHeadingKeepsHorizontalFromUnOffsetPitch() {
        // yaw 0, pitch 0, offset -20: horizontal stays full (cos 0 = 1), vertical = -sin(-20°); then normalized
        Vec v = Directions.headingWithPitchOffset(0, 0, -20);
        double len = Math.sqrt(1 + Math.pow(Math.sin(Math.toRadians(20)), 2));
        assertEquals(Math.sin(Math.toRadians(20)) / len, v.y(), 1e-9);
        assertEquals(1.0 / len, v.z(), 1e-9);
        assertEquals(0.0, v.x(), 1e-9);
        assertEquals(1.0, v.length(), 1e-9);
    }
}
