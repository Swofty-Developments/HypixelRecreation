package io.github.term4.polyp.fx;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.fx.FxEvent;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventListener;
import net.kyori.adventure.key.Key;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.junit.jupiter.api.AfterEach;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxTest extends HeadlessServerTest {

    @AfterEach
    void clearScope() { Polyp.getInstance().profiles().setGlobal(null); }

    private void useRegistry(FxRegistry reg) {
        Polyp.getInstance().profiles().setGlobal(
                MechanicsProfile.builder().set(MechanicsKeys.FX, reg).build());
    }

    // ARROW_CRIT is a positional particle, so the source itself receives it
    private static int arrowCrits(FakePlayer p) {
        return (int) p.sent(ParticlePacket.class).stream().filter(e -> e.particle() == Particle.CRIT).count();
    }

    private static int critAnims(FakePlayer p) {
        return (int) p.sent(EntityAnimationPacket.class).stream()
                .filter(e -> e.animation() == EntityAnimationPacket.Animation.CRITICAL_EFFECT).count();
    }

    private static int sounds(FakePlayer p, SoundEvent ev) {
        return (int) p.sent(SoundEffectPacket.class).stream().filter(e -> e.soundEvent() == ev).count();
    }

    private static int eatSounds(FakePlayer p) { return sounds(p, SoundEvent.ENTITY_GENERIC_EAT); }

    @Test
    void registryIsCopyOnWriteAndOverrides() {
        FxHandler a = ctx -> {};
        FxHandler b = ctx -> {};
        FxRegistry reg = FxRegistry.empty().register(Fx.CRIT, a);
        assertSame(a, reg.get(Fx.CRIT));
        assertNull(reg.get(Fx.BURP), "an unregistered key resolves to null");
        FxRegistry reg2 = reg.register(Fx.CRIT, b);
        assertSame(a, reg.get(Fx.CRIT), "the original registry is unchanged (copy-on-write)");
        assertSame(b, reg2.get(Fx.CRIT), "the copy carries the override");
    }

    @Test
    void playResolvesTheScopeRegistryAndReachesTheAudience() {
        useRegistry(Fx.vanilla18());
        FakePlayer p = FakePlayer.connect(instance, new Pos(5.5, 65, 5.5), "FxP");
        try {
            Fx.play(services, Fx.ARROW_CRIT, FxContext.of(p.player));
            assertEquals(1, arrowCrits(p), "the registered particle reaches the scope audience");
        } finally {
            p.player.remove();
        }
    }

    @Test
    void noRegistryOrUnregisteredKeyIsANoOp() {
        FakePlayer p = FakePlayer.connect(instance, new Pos(6.5, 65, 6.5), "FxP2");
        try {
            Fx.play(services, Fx.ARROW_CRIT, FxContext.of(p.player));
            assertEquals(0, arrowCrits(p), "no scope registry -> nothing plays");
            useRegistry(FxRegistry.empty());
            Fx.play(services, Fx.ARROW_CRIT, FxContext.of(p.player));
            assertEquals(0, arrowCrits(p), "an unregistered key -> nothing plays");
        } finally {
            p.player.remove();
        }
    }

    @Test
    void perKeyNoneSilencesOneEffect() {
        useRegistry(Fx.vanilla18().register(Fx.ARROW_CRIT, FxHandler.NONE));
        FakePlayer p = FakePlayer.connect(instance, new Pos(7.5, 65, 7.5), "FxP3");
        try {
            Fx.play(services, Fx.ARROW_CRIT, FxContext.of(p.player));
            assertEquals(0, arrowCrits(p), "a key registered as NONE plays nothing");
        } finally {
            p.player.remove();
        }
    }

    @Test
    void tntPrimeSoundPlaysOnIgnite() {
        useRegistry(Fx.vanilla18());
        FakePlayer p = FakePlayer.connect(instance, new Pos(900.5, 65, 900.5), "FxTnt");
        try {
            var explosion = new io.github.term4.polyp.mechanics.explosion.ExplosionSystem(Polyp.getInstance(), null);
            var tnt = io.github.term4.polyp.presets.mmc18.Tnt.spawn(explosion, instance, new Pos(902, 64, 902));
            assertEquals(1, sounds(p, SoundEvent.ENTITY_TNT_PRIMED), "ignite plays the primed sound");
            tnt.remove();
        } finally {
            p.player.remove();
        }
    }

    /** The big flash rides the fx layer (the wire explosion packet has no radius through Via): power >= 2 only. */
    @Test
    void explosionEmitterPlaysForBigBlastsOnly() {
        useRegistry(Fx.vanilla18());
        FakePlayer p = FakePlayer.connect(instance, new Pos(910.5, 65, 910.5), "FxBoom");
        try {
            var explosion = new io.github.term4.polyp.mechanics.explosion.ExplosionSystem(Polyp.getInstance(), null);
            explosion.explode(instance, new Pos(915.5, 66, 915.5), 4.0f, null);
            long emitters = p.sent(ParticlePacket.class).stream().filter(e -> e.particle() == Particle.EXPLOSION_EMITTER).count();
            assertEquals(1, emitters, "TNT-power blast flashes");
            explosion.explode(instance, new Pos(915.5, 66, 915.5), 1.0f, null);
            emitters = p.sent(ParticlePacket.class).stream().filter(e -> e.particle() == Particle.EXPLOSION_EMITTER).count();
            assertEquals(1, emitters, "a power-1 blast (vanilla ghast) stays small");
        } finally {
            p.player.remove();
        }
    }

    @Test
    void eventCancelsAndSwaps() {
        useRegistry(Fx.vanilla18());
        FakePlayer a = FakePlayer.connect(instance, new Pos(8.5, 65, 8.5), "FxP4");
        EventListener<FxEvent> canceller = EventListener.of(FxEvent.class, FxEvent::cancel);
        MinecraftServer.getGlobalEventHandler().addListener(canceller);
        try {
            Fx.play(services, Fx.ARROW_CRIT, FxContext.of(a.player));
            assertEquals(0, arrowCrits(a), "a listener cancelled the effect");
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(canceller);
            a.player.remove();
        }

        useRegistry(Fx.vanilla18().register(Fx.ARROW_CRIT, FxHandler.NONE));
        FakePlayer b = FakePlayer.connect(instance, new Pos(9.5, 65, 9.5), "FxP5");
        EventListener<FxEvent> swapper = EventListener.of(FxEvent.class,
                e -> e.fx(FxHandler.particle(Particle.CRIT, 1, 0.0, 0f)));
        MinecraftServer.getGlobalEventHandler().addListener(swapper);
        try {
            Fx.play(services, Fx.ARROW_CRIT, FxContext.of(b.player));
            assertTrue(arrowCrits(b) >= 1, "...but a listener swapped a real effect back in");
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(swapper);
            b.player.remove();
        }
    }

    @Test
    void critAnimationExcludesTheAttacker() {
        useRegistry(Fx.vanilla18());
        FakePlayer attacker = FakePlayer.connect(instance, new Pos(10.5, 65, 10.5), "Critter");
        LivingEntity victim = zombie(new Pos(11.5, 65, 10.5));
        try {
            Fx.play(services, Fx.CRIT, FxContext.of(attacker.player, victim));
            // the 1.8 client predicts its own crit locally
            assertEquals(0, critAnims(attacker), "the crit is not sent to the attacker itself");
        } finally {
            attacker.player.remove();
            victim.remove();
        }
    }

    @Test
    void eatChewIsViewersOnlyOn18() {
        useRegistry(Fx.vanilla18());
        FakePlayer eater = FakePlayer.connect(instance, new Pos(5.5, 65, 5.5), "Eater");
        FakePlayer viewer = FakePlayer.connect(instance, new Pos(7.5, 65, 5.5), "Viewer");
        try {
            assertTrue(eater.player.getViewers().contains(viewer.player), "a nearby player tracks the eater");
            Fx.play(services, Fx.EAT, FxContext.of(eater.player));
            // the client self-predicts its chew from the eating metadata
            assertEquals(0, eatSounds(eater), "the eater self-predicts, so gets no server chew");
            assertEquals(1, eatSounds(viewer), "a nearby viewer hears the chew");
        } finally {
            eater.player.remove();
            viewer.player.remove();
        }
    }

    @Test
    void eatChewIsViewersOnlyOnModern() {
        useRegistry(Fx.modern());
        FakePlayer eater = FakePlayer.connect(instance, new Pos(5.5, 65, 5.5), "MEater");
        FakePlayer viewer = FakePlayer.connect(instance, new Pos(7.5, 65, 5.5), "MViewer");
        try {
            assertTrue(eater.player.getViewers().contains(viewer.player), "a nearby player tracks the eater");
            Fx.play(services, Fx.EAT, FxContext.of(eater.player));
            assertEquals(0, eatSounds(eater), "the modern eater gets no server chew (no double)");
            assertEquals(1, eatSounds(viewer), "a nearby viewer hears the chew");
        } finally {
            eater.player.remove();
            viewer.player.remove();
        }
    }

    @Test
    void everyLaunchAndHitSoundIsRegisteredInBothPresets() {
        List<Key> keys = List.of(Fx.THROW_SNOWBALL, Fx.THROW_EGG, Fx.THROW_PEARL, Fx.THROW_FIREBALL,
                Fx.BOW_SHOOT, Fx.ROD_CAST, Fx.ROD_RETRIEVE, Fx.ARROW_HIT);
        for (FxRegistry reg : List.of(Fx.vanilla18(), Fx.modern()))
            for (Key k : keys) assertNotNull(reg.get(k), k + " is registered");
    }

    @Test
    void throwSoundReachesEveryoneIncludingTheThrower() {
        useRegistry(Fx.vanilla18());
        FakePlayer thrower = FakePlayer.connect(instance, new Pos(5.5, 65, 5.5), "Thrower");
        FakePlayer viewer = FakePlayer.connect(instance, new Pos(7.5, 65, 5.5), "TViewer");
        try {
            assertTrue(thrower.player.getViewers().contains(viewer.player), "a nearby player tracks the thrower");
            Fx.play(services, Fx.THROW_SNOWBALL, FxContext.of(thrower.player));
            // 1.8 does NOT self-predict the throw (unlike the chew)
            assertEquals(1, sounds(thrower, SoundEvent.ENTITY_SNOWBALL_THROW), "the thrower hears its own throw");
            assertEquals(1, sounds(viewer, SoundEvent.ENTITY_SNOWBALL_THROW), "a nearby viewer hears the throw");
        } finally {
            thrower.player.remove();
            viewer.player.remove();
        }
    }

    @Test
    void arrowHitMarkerDingsOnlyTheShooterAndOnlyWhenRegistered() {
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(5.5, 65, 5.5), "Shooter");
        FakePlayer bystander = FakePlayer.connect(instance, new Pos(7.5, 65, 5.5), "Bystander");
        try {
            useRegistry(Fx.vanilla18());
            Fx.play(services, Fx.ARROW_HIT_PLAYER, FxContext.of(shooter.player, bystander.player));
            assertEquals(0, sounds(shooter, SoundEvent.ENTITY_ARROW_HIT_PLAYER), "vanilla does not ding");

            useRegistry(Fx.vanilla18().register(Fx.ARROW_HIT_PLAYER, Fx.arrowHitMarker()));
            Fx.play(services, Fx.ARROW_HIT_PLAYER, FxContext.of(shooter.player, bystander.player));
            assertEquals(1, sounds(shooter, SoundEvent.ENTITY_ARROW_HIT_PLAYER), "the shooter hears the ding");
            assertEquals(0, sounds(bystander, SoundEvent.ENTITY_ARROW_HIT_PLAYER), "only the shooter, not a bystander");
        } finally {
            shooter.player.remove();
            bystander.player.remove();
        }
    }
}
