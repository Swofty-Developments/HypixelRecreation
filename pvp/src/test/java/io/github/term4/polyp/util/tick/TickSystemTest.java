package io.github.term4.polyp.util.tick;

import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.projectile.ProjectileDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Dispatch contract: phase order ({@code PRE_DISPATCH → DEFAULT → POST}) and per-interval gating. Headless has no
 * tick loop, so the instance clock is positioned directly and {@code dispatch} is invoked by reflection.
 */
class TickSystemTest extends HeadlessServerTest {

    private static final List<String> SINK = new CopyOnWriteArrayList<>();

    @BeforeAll
    static void registerMarkers() {
        TickSystem.register(new Marker("A", TickPhase.PRE_DISPATCH, 1));
        TickSystem.register(new Marker("C", TickPhase.POST, 1));
        TickSystem.register(new Marker("B", TickPhase.DEFAULT, 1));   // registered after A/C but runs in DEFAULT order
        TickSystem.register(new Marker("D3", TickPhase.DEFAULT, 3));  // every 3rd tick only
    }

    @BeforeEach
    void clearSink() { SINK.clear(); }

    @Test
    void dispatchesInPhaseOrder() {
        dispatchAt(10); // 10 % 3 != 0 -> D3 skipped
        assertEquals(List.of("A", "B", "C"), SINK);
    }

    @Test
    void intervalGatesByTick() {
        dispatchAt(9); // 9 % 3 == 0 -> D3 fires, in DEFAULT after B
        assertEquals(List.of("A", "B", "D3", "C"), SINK);

        SINK.clear();
        dispatchAt(11); // not divisible by 3
        assertFalse(SINK.contains("D3"), "interval-3 tickable must skip non-multiple ticks");
    }

    @Test
    void worldPassRunsOnTheGivenClock() {
        TickSystem.tickWorld(MechanicsWorld.of(instance), 9); // 9 % 3 == 0 -> D3 fires, on the WORLD clock
        assertEquals(List.of("A", "B", "D3", "C"), SINK);
    }

    @Test
    void entityClockFollowsTheExternalOwner() {
        Entity owned = new Entity(EntityType.ARMOR_STAND);
        Entity plain = new Entity(EntityType.ARMOR_STAND);
        try {
            MechanicsWorld.resolver(new MechanicsWorld.Resolver() {
                @Override public MechanicsWorld resolve(Entity e) { return e.getTag(MechanicsWorld.ENTITY_TAG); }
                @Override public boolean externallyTicked(Entity e) { return e == owned; }
                @Override public long externalTick(Entity e) { return e == owned ? 123 : -1; }
            });
            assertEquals(123, TickSystem.tick(owned));
            TickContext main = new TickContext(MechanicsWorld.of(instance), 5, 20, false);
            TickContext world = new TickContext(MechanicsWorld.of(instance), 5, 20, true);
            assertFalse(main.owns(owned), "the main pass leaves externally ticked entities to their owner");
            assertTrue(world.owns(owned));
            assertTrue(main.owns(plain));
            assertFalse(world.owns(plain), "a world pass never touches server-ticked entities");
        } finally {
            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
        }
    }

    @Test
    void worldPassesOwnOnlyTheirOwnWorldsEntities() {
        Entity external = new Entity(EntityType.ARMOR_STAND);
        Entity boundOnly = new Entity(EntityType.ARMOR_STAND); // world-bound but server-ticked (plain-shard case)
        try {
            MechanicsWorld a = MechanicsWorld.of(instance);
            MechanicsWorld b = MechanicsWorld.of(net.minestom.server.MinecraftServer.getInstanceManager().createInstanceContainer());
            MechanicsWorld.resolver(new MechanicsWorld.Resolver() {
                @Override public MechanicsWorld resolve(Entity e) { return e.getTag(MechanicsWorld.ENTITY_TAG); }
                @Override public boolean externallyTicked(Entity e) { return e == external; }
                @Override public long externalTick(Entity e) { return e == external ? 7 : -1; }
            });
            external.setTag(MechanicsWorld.ENTITY_TAG, a);
            boundOnly.setTag(MechanicsWorld.ENTITY_TAG, a);
            TickContext passA = new TickContext(a, 5, 20, true);
            TickContext passB = new TickContext(b, 5, 20, true);
            TickContext main = new TickContext(a, 5, 20, false);
            assertTrue(passA.owns(external));
            assertFalse(passB.owns(external), "a foreign world's pass never owns another world's entity");
            assertTrue(main.owns(boundOnly), "the main pass owns a world-BOUND but server-ticked entity");
            assertFalse(passA.owns(boundOnly), "a world pass never takes server-ticked entities, bound or not");
        } finally {
            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
        }
    }

    @Test
    void clockChangeResetsTickStampedState() {
        var victim = zombie(new Pos(8, 65, 8));
        try {
            services.damage().apply(DamageSnapshot.of(victim, ProjectileDamage.INSTANCE).withAmount(0f));
            assertEquals(10, DamageSystem.remainingDamageInvul(victim));
            TickSystem.clockChanged(victim);
            assertEquals(0, DamageSystem.remainingDamageInvul(victim),
                    "an i-frame window never survives a clock change");
        } finally {
            victim.remove();
        }
    }

    private record Marker(String id, TickPhase phase, int interval) implements Tickable {
        @Override public void tick(TickContext ctx) { SINK.add(id); }
    }

    @SuppressWarnings("unchecked")
    private static void dispatchAt(long tick) {
        try {
            Field clocks = TickSystem.class.getDeclaredField("CLOCKS");
            clocks.setAccessible(true);
            ((Map<Instance, AtomicLong>) clocks.get(null)).computeIfAbsent(instance, k -> new AtomicLong()).set(tick);

            Method dispatch = TickSystem.class.getDeclaredMethod("dispatch", Instance.class);
            dispatch.setAccessible(true);
            dispatch.invoke(null, instance);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
