package test.presets;

import io.github.term4.polyp.api.event.attack.AttackEvent;
import io.github.term4.polyp.mechanics.attack.AttackSnapshot;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;
import io.github.term4.polyp.presets.mmc18.Attack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * mmc18 parity across tick modes: the hit queue stamps, judges and flushes through {@link TickSystem#tick}, so a
 * victim on an external clock (a domain player through a world bridge) must queue and flush exactly like an
 * instance-clocked one - and only its OWN world's pass may flush it.
 */
class Mmc18ExternalClockParityTest extends HeadlessServerTest {

    private record Run(float afterFresh, float afterQueue, float afterFlush) {}

    @Test
    void queuedHitsFlushIdenticallyOnAnExternalClock() throws Exception {
        Run baseline = instanceClockRun();
        Run external = externalClockRun();
        assertEquals(baseline.afterFresh(), external.afterFresh(), 1e-6f, "fresh hit");
        assertEquals(baseline.afterQueue(), external.afterQueue(), 1e-6f, "queued hit must not damage");
        assertEquals(baseline.afterFlush(), external.afterFlush(), 1e-6f, "the flushed hit lands the same");
    }

    private Run instanceClockRun() throws Exception {
        LivingEntity attacker = zombie(new Pos(0, 64, 700));
        LivingEntity victim = zombie(new Pos(0, 64, 701));
        try {
            AttackEvent.AttackRule rule = Attack.ruleset().create(services);
            setInstanceTick(0);
            return run(rule, attacker, victim, Mmc18ExternalClockParityTest::setInstanceTick,
                    () -> dispatchMainPass(instance));
        } finally {
            attacker.remove();
            victim.remove();
        }
    }

    private Run externalClockRun() throws Exception {
        LivingEntity attacker = zombie(new Pos(0, 64, 704));
        LivingEntity victim = zombie(new Pos(0, 64, 705));
        AtomicLong clock = new AtomicLong();
        try {
            MechanicsWorld.resolver(new MechanicsWorld.Resolver() {
                @Override public MechanicsWorld resolve(Entity e) { return e.getTag(MechanicsWorld.ENTITY_TAG); }
                @Override public boolean externallyTicked(Entity e) { return e == victim; }
                @Override public long externalTick(Entity e) { return e == victim ? clock.get() : -1; }
            });
            AttackEvent.AttackRule rule = Attack.ruleset().create(services);
            return run(rule, attacker, victim, clock::set, () -> {
                // a FOREIGN world's pass must leave the queued hit alone before the victim's own pass flushes it
                Instance elsewhere = MinecraftServer.getInstanceManager().createInstanceContainer();
                elsewhere.setGenerator(unit -> unit.modifier().fillHeight(0, 64, Block.STONE));
                TickSystem.tickWorld(MechanicsWorld.of(elsewhere), 10);
                try {
                    assertEquals(1, pendingHits().size(), "a foreign pass never flushes another world's victim");
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                TickSystem.tickWorld(MechanicsWorld.of(instance), 10);
            });
        } finally {
            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
            attacker.remove();
            victim.remove();
        }
    }

    private interface TickSetter { void set(long tick); }

    private Run run(AttackEvent.AttackRule rule, LivingEntity attacker, LivingEntity victim,
                    TickSetter clock, Runnable flushPass) throws Exception {
        attacker.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD));
        victim.setHealth(20f);
        pendingHits().clear();

        clock.set(0);
        rule.processAttack(attack(attacker, victim));
        float afterFresh = victim.getHealth();

        clock.set(9);
        rule.processAttack(attack(attacker, victim)); // last window tick: queues
        float afterQueue = victim.getHealth();
        assertEquals(1, pendingHits().size(), "the in-window hit queued");

        clock.set(10);
        flushPass.run();
        assertEquals(0, pendingHits().size(), "the victim's own pass flushed it");
        return new Run(afterFresh, afterQueue, victim.getHealth());
    }

    private static AttackEvent attack(LivingEntity attacker, LivingEntity victim) {
        return new AttackEvent(new AttackSnapshot(attacker, victim, Attack.config()), services);
    }

    private static void setInstanceTick(long value) {
        try {
            Field f = TickSystem.class.getDeclaredField("CLOCKS");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Instance, AtomicLong> clocks = (Map<Instance, AtomicLong>) f.get(null);
            clocks.computeIfAbsent(instance, k -> new AtomicLong()).set(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void dispatchMainPass(Instance in) {
        try {
            Method dispatch = TickSystem.class.getDeclaredMethod("dispatch", Instance.class);
            dispatch.setAccessible(true);
            dispatch.invoke(null, in);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<LivingEntity, ?> pendingHits() throws Exception {
        Field f = Attack.class.getDeclaredField("pendingHit");
        f.setAccessible(true);
        return (Map<LivingEntity, ?>) f.get(null);
    }
}
