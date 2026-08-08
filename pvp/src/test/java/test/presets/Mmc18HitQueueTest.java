package test.presets;

import io.github.term4.polyp.api.event.attack.AttackEvent;
import io.github.term4.polyp.mechanics.attack.AttackSnapshot;
import io.github.term4.polyp.presets.mmc18.Attack;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.projectile.ProjectileDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.tick.TickSystem;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract + regression for the mmc18 1-tick hit queue against the damage-invulnerability window. Combat reads the
 * instance-local {@link TickSystem}; headless tests have no tick loop, so the clock is positioned directly (via
 * reflection, mirroring how the rest of the suite drives the global clock). The queue's expiry phase is exercised by
 * the inline drain on the next attack, which is what these tests assert.
 */
class Mmc18HitQueueTest extends HeadlessServerTest {

    @BeforeEach
    void resetClockAndQueue() throws Exception {
        pendingHits().clear();
        setCombatTick(0);
    }

    @AfterEach
    void clearQueue() throws Exception {
        pendingHits().clear();
    }

    @Test
    void queuedHitFlushConsumesSameTickLiveAttack() {
        LivingEntity attacker = zombie(new Pos(0, 64, 600));
        LivingEntity victim = zombie(new Pos(0, 64, 601));
        attacker.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD));
        victim.setHealth(20f);

        AttackEvent.AttackRule rule = Attack.ruleset().create(services);

        setCombatTick(0);
        rule.processAttack(attack(attacker, victim, false));
        float afterFirst = (float) victim.getHealth();
        float firstDamage = 20f - afterFirst;
        assertEquals(10, DamageSystem.remainingDamageInvul(victim));

        setCombatTick(9);
        rule.processAttack(attack(attacker, victim, false));
        assertEquals(afterFirst, victim.getHealth(), 1e-4f, "queueing must not damage");
        assertEquals(1, DamageSystem.remainingDamageInvul(victim));

        setCombatTick(10);
        rule.processAttack(attack(attacker, victim, true));
        assertEquals(afterFirst - firstDamage, victim.getHealth(), 1e-4f,
                "the queued hit lands, but the same-tick live hit must not overdamage");
        assertEquals(10, DamageSystem.remainingDamageInvul(victim));
    }

    /** Regression: a window re-armed between queue and flush (fall damage on landing, fire) must not hold the
     *  queued hit past its ORIGINAL deadline - that deferred KB for whole extra windows. */
    @Test
    void rearmedWindowDoesNotHoldQueuedHit() throws Exception {
        LivingEntity attacker = zombie(new Pos(0, 64, 604));
        LivingEntity victim = zombie(new Pos(0, 64, 605));
        attacker.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD));
        victim.setHealth(20f);

        AttackEvent.AttackRule rule = Attack.ruleset().create(services);

        setCombatTick(0);
        rule.processAttack(attack(attacker, victim, false)); // fresh hit: window 0..10
        setCombatTick(9);
        rule.processAttack(attack(attacker, victim, false)); // queued, deadline 10
        assertFalse(pendingHits().isEmpty());

        DamageSystem.setDamageInvulnerable(victim, 10); // the landing's fall damage re-arms a fresh window

        setCombatTick(10);
        rule.processAttack(attack(attacker, victim, false)); // drain path
        assertEquals(0, pendingHits().size(), "the original-window deadline releases the hit despite the re-arm");
    }

    /** Regression for the phase bug: under per-tick spam, fresh hits are gated to exactly 10 combat ticks apart. */
    @Test
    void freshHitsAreExactly10CombatTicksApart_underSpam() {
        LivingEntity attacker = zombie(new Pos(0, 64, 602));
        LivingEntity victim = zombie(new Pos(0, 64, 603));
        attacker.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD));

        AttackEvent.AttackRule rule = Attack.ruleset().create(services);

        List<Long> freshTicks = new ArrayList<>();
        for (long t = 0; t <= 40; t++) {
            setCombatTick(t);
            victim.setHealth(20f); // keep the victim alive; fresh detection is via the i-frame window, not health
            rule.processAttack(attack(attacker, victim, false));
            // only a FRESH hit (re)opens a full window; overdamage/queued never reset it to its full duration
            if (DamageSystem.remainingDamageInvul(victim) == 10) freshTicks.add(t);
        }

        assertFalse(freshTicks.isEmpty(), "spam should land fresh hits");
        for (int i = 1; i < freshTicks.size(); i++) {
            assertEquals(10L, freshTicks.get(i) - freshTicks.get(i - 1),
                    "consecutive fresh hits must be exactly 10 combat ticks apart, got " + freshTicks);
        }
    }

    /** Measured (sprintgate duo 2026-07-07): a queued hit flushes sprint-stripped iff the victim's MOST RECENT
     *  landed damage is a projectile; any other landed damage (overdamage included) overrides the condition. */
    @Test
    void queuedHitStripsSprintOnlyAfterAProjectile() throws Exception {
        LivingEntity attacker = zombie(new Pos(0, 64, 606));
        LivingEntity victim = zombie(new Pos(0, 64, 607));
        attacker.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD));
        victim.setHealth(20f);
        AttackEvent.AttackRule rule = Attack.ruleset().create(services);

        setCombatTick(0);
        services.damage().apply(DamageSnapshot.of(victim, ProjectileDamage.INSTANCE).withAmount(0f)); // the 0-dmg snowball
        assertEquals(10, DamageSystem.remainingDamageInvul(victim), "0-damage projectile opens the window");
        assertTrue(DamageSystem.lastDamageType(victim) instanceof ProjectileDamage, "projectile recorded as the most recent damage");

        setCombatTick(5); // an overdamage replacement mid-window overrides the strip condition
        rule.processAttack(attack(attacker, victim, true)); // crit > snowball's 0 -> lands as overdamage
        assertFalse(DamageSystem.lastDamageType(victim) instanceof ProjectileDamage, "overdamage overrides: a queued hit keeps sprint");

        setCombatTick(9); // last tick: still queues as usual; the flush reads the (now non-projectile) strip condition
        rule.processAttack(attack(attacker, victim, false));
        assertFalse(pendingHits().isEmpty(), "in-window hit queues");

        pendingHits().clear();
        victim.remove();
        attacker.remove();
    }

    /** Vanilla parity (EntityLiving.damageEntity + EntityHuman.attack, source-verified): an overdamage hit deals the
     *  difference AND consumes the attacker's sprint - the next fresh hit is non-sprint until a w-tap. */
    @Test
    void overdamageHitLandsThroughTheQueueAndConsumesSprint() {
        LivingEntity attacker = zombie(new Pos(0, 64, 608));
        LivingEntity victim = zombie(new Pos(0, 64, 609));
        attacker.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD));
        victim.setHealth(20f);
        AttackEvent.AttackRule rule = Attack.ruleset().create(services);

        setCombatTick(0);
        rule.processAttack(attack(attacker, victim, false)); // fresh hit opens the window
        float afterFirst = (float) victim.getHealth();

        attacker.setSprinting(true);
        setCombatTick(5); // mid-window, outside the queue buffer: falls through to vanilla overdamage
        rule.processAttack(attack(attacker, victim, true)); // crit = higher damage -> the difference lands
        assertTrue(victim.getHealth() < afterFirst, "overdamage (crit > fresh) deals the difference mid-window");
        assertFalse(attacker.isSprinting(), "a landed overdamage hit consumes sprint (vanilla setSprinting(false))");
        victim.remove();
        attacker.remove();
    }

    private static AttackEvent attack(LivingEntity attacker, LivingEntity victim, boolean critical) {
        AttackEvent event = new AttackEvent(new AttackSnapshot(attacker, victim, Attack.config()), services);
        event.overrideCritical(critical);
        return event;
    }

    /** Positions the test instance's combat clock (no public setter by design; mirrors the suite's clock reflection). */
    @SuppressWarnings("unchecked")
    private static void setCombatTick(long value) {
        try {
            Field f = TickSystem.class.getDeclaredField("CLOCKS");
            f.setAccessible(true);
            Map<Instance, AtomicLong> clocks = (Map<Instance, AtomicLong>) f.get(null);
            clocks.computeIfAbsent(instance, k -> new AtomicLong()).set(value);
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
