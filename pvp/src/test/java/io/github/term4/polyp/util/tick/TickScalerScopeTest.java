package io.github.term4.polyp.util.tick;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.InstanceContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Scope-aware physics scaling: {@code clientTps} resolved from the SUBJECT's profile chain, so one world can run
 * dilated (simulated TPS) while everything else stays native. Velocity re-rates linearly, gravity quadratically.
 */
class TickScalerScopeTest extends HeadlessServerTest {

    private static final int SERVER_TPS = ServerFlag.SERVER_TICKS_PER_SECOND;

    private static Entity spawned(InstanceContainer where) {
        Entity e = new Entity(EntityType.ARMOR_STAND);
        e.setInstance(where, new Pos(0, 65, 0)).join();
        return e;
    }

    private static InstanceContainer dilatedTo(int simulatedTps) {
        return flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.TICK_SCALING, TickScalingConfig.builder().clientTps(simulatedTps).build())
                .build());
    }

    @Test
    void unscopedSubjectsRunNative() {
        Entity plain = spawned(instance);
        assertEquals(0.08, TickScaler.gravityPerTick(plain, 0.08), 1.0e-12);
        assertEquals(0.99, TickScaler.dragPerTick(plain, 0.99), 1.0e-12);
        assertEquals(1.5, TickScaler.impulse(plain, 1.5), 1.0e-12);
    }

    @Test
    void worldScopedClientTpsDilatesOnlyThatWorld() {
        Entity slow = spawned(dilatedTo(SERVER_TPS / 2)); // s = 0.5
        Entity plain = spawned(instance);

        assertEquals(0.75, TickScaler.impulse(slow, 1.5), 1.0e-12);           // velocity x s
        assertEquals(0.02, TickScaler.gravityPerTick(slow, 0.08), 1.0e-12);   // gravity x s²
        assertEquals(Math.sqrt(0.99), TickScaler.dragPerTick(slow, 0.99), 1.0e-12); // drag^s

        // a neighbouring world is untouched
        assertEquals(1.5, TickScaler.impulse(plain, 1.5), 1.0e-12);
        assertEquals(0.08, TickScaler.gravityPerTick(plain, 0.08), 1.0e-12);
    }

    @Test
    void clientTpsAboveServerTpsRunsFastMotion() {
        Entity fast = spawned(dilatedTo(SERVER_TPS * 2)); // s = 2
        assertEquals(3.0, TickScaler.impulse(fast, 1.5), 1.0e-12);
        assertEquals(0.32, TickScaler.gravityPerTick(fast, 0.08), 1.0e-12);
    }

    /** One resolve must dilate gravity and both drags. */
    @Test
    void scaledAerodynamicsDilatesGravityAndBothDrags() {
        Aerodynamics base = new Aerodynamics(0.08, 0.91, 0.98);
        Entity slow = spawned(dilatedTo(SERVER_TPS / 2)); // s = 0.5
        Aerodynamics scaled = TickScaler.aerodynamics(slow, base);
        assertEquals(0.02, scaled.gravity(), 1.0e-12);                      // x s²
        assertEquals(Math.sqrt(0.91), scaled.horizontalAirResistance(), 1.0e-12); // ^s
        assertEquals(Math.sqrt(0.98), scaled.verticalAirResistance(), 1.0e-12);
    }

    /** Native scope must hand back the SAME instance - no rebuild on the default path. */
    @Test
    void unscopedAerodynamicsIsUntouched() {
        Aerodynamics base = new Aerodynamics(0.08, 0.91, 0.98);
        assertSame(base, TickScaler.aerodynamics(spawned(instance), base));
    }

    /** {@code simulated(N)} pins BOTH baselines, so durations stretch with the physics rather than staying literal. */
    @Test
    void simulatedPinsBothBaselines() {
        TickScalingConfig half = TickScalingConfig.simulated(SERVER_TPS / 2);
        assertEquals(SERVER_TPS / 2, half.clientTps());
        assertEquals(SERVER_TPS / 2, half.referenceTps(io.github.term4.polyp.mechanics.damage.DamageSystem.KEY));
        // a 60-tick cooldown takes twice the server ticks at half rate
        assertEquals(120, TickScaler.scaleDuration(60, SERVER_TPS, SERVER_TPS / 2));
    }

    @Test
    void simulatedRejectsRatesOutsideTheSupportedBand() {
        assertThrows(IllegalArgumentException.class, () -> TickScalingConfig.simulated(0)); // 0 is the sentinel, not a rate
        assertThrows(IllegalArgumentException.class, () -> TickScalingConfig.simulated(-5));
        assertThrows(IllegalArgumentException.class,
                () -> TickScalingConfig.simulated(TickScalingConfig.MAX_SIMULATED_TPS + 1));
        TickScalingConfig.simulated(1);                                    // both ends inclusive
        TickScalingConfig.simulated(TickScalingConfig.MAX_SIMULATED_TPS);
    }

    /** Single-member assignment must MERGE - dilating a scope cannot wipe the configs already on it. */
    @Test
    void perKeyScopeSetKeepsTheScopesOtherMembers() {
        InstanceContainer world = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.PLAYER, io.github.term4.polyp.platform.player.PlayerConfig.builder().build())
                .build());
        polyp.profiles().setInstance(world, MechanicsKeys.TICK_SCALING, TickScalingConfig.simulated(SERVER_TPS / 2));

        MechanicsProfile merged = polyp.profiles().instance(world);
        assertNotNull(merged.get(MechanicsKeys.PLAYER), "the pre-existing member survived");
        assertEquals(SERVER_TPS / 2, merged.get(MechanicsKeys.TICK_SCALING).clientTps());

        // and clearing just that member leaves the rest in place
        polyp.profiles().setInstance(world, MechanicsKeys.TICK_SCALING, null);
        assertNull(polyp.profiles().instance(world).get(MechanicsKeys.TICK_SCALING));
        assertNotNull(polyp.profiles().instance(world).get(MechanicsKeys.PLAYER));
    }

    /**
     * The in-game regression: an entity built but not yet world-spawned resolves NO instance scope, so everything
     * armed at launch silently fell back to global. Whoever arms it must supply a subject that IS in the world.
     */
    @Test
    void preSpawnEntityResolvesNothingSoTheArmerMustSupplyTheScope() {
        InstanceContainer slowWorld = dilatedTo(SERVER_TPS / 2);
        Entity shooter = spawned(slowWorld);
        Entity unspawned = new Entity(EntityType.ARROW);

        // the trap: scoped off itself it reads native, though it is about to enter a dilated world
        assertEquals(1.5, TickScaler.impulse(unspawned, 1.5), 1.0e-12);
        // scoped off the shooter, who IS in the world
        assertEquals(0.75, TickScaler.impulse(shooter, 1.5), 1.0e-12);
    }

    /**
     * Resync cadences ride the PREDICTION baseline, not the duration one - they bound the client's own integration
     * drift. Stretching durations without dilating physics must leave them alone.
     */
    @Test
    void resyncCadencesFollowPredictionNotDurations() {
        InstanceContainer stretchedDurationsOnly = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.TICK_SCALING, TickScalingConfig.builder()
                        .referenceTps(SERVER_TPS / 2)   // cooldowns stretch...
                        .clientTps(SERVER_TPS)          // ...but the client still predicts at the server rate
                        .build())
                .build());
        Entity e = spawned(stretchedDurationsOnly);
        assertEquals(4, TickScaler.clientTicks(e, 4), "prediction is untouched, so the resync cadence is too");
        assertEquals(8, TickScaler.duration(e, 4, io.github.term4.polyp.mechanics.damage.DamageSystem.KEY),
                "durations still stretch on their own baseline");

        // and under real dilation the cadence stretches with the client's slower integration
        Entity slow = spawned(dilatedTo(SERVER_TPS / 2));
        assertEquals(8, TickScaler.clientTicks(slow, 4));
    }

    /** {@code 0} is a wire sentinel (silent / no per-tick velocity), never a rate - it must survive scaling. */
    @Test
    void clientTicksPreservesTheSilentWireSentinel() {
        Entity slow = spawned(dilatedTo(SERVER_TPS / 2));
        assertEquals(0, TickScaler.clientTicks(slow, 0));
    }

    /**
     * The in-game "slow, jump, slow, jump": scaled against itself a projectile's launch velocity was halved but its
     * gravity ran full speed, so the server arc split from the prediction and every resync teleported it forward.
     * Every term must come from ONE subject - for a projectile, its shooter.
     */
    @Test
    void aThrownEntityMustReadOneScopeForItsWholeArc() {
        Entity thrower = spawned(dilatedTo(SERVER_TPS / 2));
        Entity thrown = spawned(instance); // the projectile sits in a NATIVE world

        assertEquals(0.08, TickScaler.gravityPerTick(thrown, 0.08), 1.0e-12);
        // scoped to its thrower every term agrees
        assertEquals(0.75, TickScaler.impulse(thrower, 1.5), 1.0e-12);
        assertEquals(0.02, TickScaler.gravityPerTick(thrower, 0.08), 1.0e-12);
    }

    /**
     * Sub-step count is {@code simTps / SERVER tps}, never a fixed 20: a server already ticking at 40 is doing the
     * work for us and must take ONE step, not two.
     */
    @Test
    void subStepCountCancelsAgainstAFasterServer() {
        assertEquals(1.0, TickScaler.stepsPerTick(spawned(instance)), 1.0e-12);
        assertEquals(0.5, TickScaler.stepsPerTick(spawned(dilatedTo(SERVER_TPS / 2))), 1.0e-12);
        assertEquals(2.0, TickScaler.stepsPerTick(spawned(dilatedTo(SERVER_TPS * 2))), 1.0e-12);

        // the case that must NOT double up: simulated rate == the server's own rate
        assertEquals(1.0, TickScaler.stepsPerTick(spawned(dilatedTo(SERVER_TPS))), 1.0e-12);
    }

    /**
     * A vanilla client floors its tick period at 50ms ({@code max(50, msPerTick)} over a 20-tps DeltaTracker), so it
     * follows a rate DOWNWARD only - told 40 it still ticks 20. Above 20 the wire must carry a LONGER step, or the
     * client covers half the ground and every resync yanks it forward (as vanilla does at {@code /tick rate 40}).
     */
    @Test
    void aboveTwentyTheClientIsCappedSoTheWireStepGrows() {
        Entity fast = spawned(dilatedTo(SERVER_TPS * 2));   // sim 40
        assertEquals(SERVER_TPS, TickScaler.effectiveClientTps(fast), "the client cannot tick past 20");

        // sub-stepping covers 2 native steps of v per server tick -> the capped client needs 2v per ITS tick
        Vec perServerTick = new Vec(0.5, 0, 0).mul(TickScaler.stepsPerTick(fast));
        assertEquals(1.0, TickScaler.wireVelocity(fast, perServerTick).x(), 1.0e-12);

        // at or below 20 the client really does follow, so the step is unchanged
        Entity slow = spawned(dilatedTo(SERVER_TPS / 2));   // sim 10
        assertEquals(SERVER_TPS / 2, TickScaler.effectiveClientTps(slow));
        assertEquals(1.0, TickScaler.wireVelocity(slow, new Vec(0.5, 0, 0)).x(), 1.0e-12);
    }

    @Test
    void wireVelocityRoundTripsThroughTheScopedRate() {
        Entity slow = spawned(dilatedTo(SERVER_TPS / 2));
        Vec serverBt = new Vec(0.4, 0.1, 0);
        assertEquals(0.8, TickScaler.wireVelocity(slow, serverBt).x(), 1.0e-12);
        assertEquals(0.4, TickScaler.fromClientVelocity(slow, TickScaler.wireVelocity(slow, serverBt)).x(), 1.0e-12);
    }
}
