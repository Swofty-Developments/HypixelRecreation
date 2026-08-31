package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.api.event.attack.AttackEvent;
import io.github.term4.polyp.mechanics.attack.AttackConfig;
import io.github.term4.polyp.mechanics.attack.FakeHitConfig;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.projectile.ProjectileDamage;
import io.github.term4.polyp.util.tick.TickContext;
import io.github.term4.polyp.util.tick.TickPhase;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * mmc18 attack: the vanilla 1.8 attack ({@link io.github.term4.polyp.presets.vanilla18.Attack}) behind a
 * 1-tick hit queue against the damage-invul window, with no attacker self-slowdown and swing fake-hits.
 */
public final class Attack {

    private Attack() {}

    // 1.8 melee entity reach; the target box is already grown by the attacker's hit margin
    private static final double SWING_REACH = 3.0;

    public static AttackConfig config() {
        return AttackConfig.builder(Vanilla18.attack())
                .ruleset(ruleset())
                .fullHitScale(1.0) // a landed hit does NOT slow the attacker's tracked velocity
                .fakeHits(FakeHitConfig.ofReach(SWING_REACH))
                .build();
    }

    /**
     * A hit landing within the last {@link #HIT_QUEUE_BUFFER tick} of the target's invul window is buffered (last-wins
     * per target) and applied when the window expires instead of being eaten. Earlier hits fall through to vanilla.
     * Measured (sprintgate duo 2026-07-07): a queued hit flushes as a NON-sprint hit when the victim's most recent
     * damage is a PROJECTILE; an overdamage replacement in between overrides that, keeping sprint.
     */
    public static AttackEvent.AttackRule.Ruleset ruleset() {
        return services -> {
            ensureFlushListener();
            AttackEvent.AttackRule vanilla = Vanilla18.attackRuleset().create(services);
            return event -> {
                if (event.target() instanceof LivingEntity le) {
                    // drain an expired queued hit first: the older hit must land first
                    if (flushPending(le)) return;
                    // vanilla-tick window scaled to live TPS (identity at 20) to keep the same real-time slop
                    if (DamageSystem.isInvulnerableToDamage(le)
                            && DamageSystem.remainingDamageInvul(le) <= TickScaler.duration(HIT_QUEUE_BUFFER,
                            services.profiles().resolve(le, MechanicsKeys.TICK_SCALING), DamageSystem.KEY)) {
                        pendingHit.put(le, PendingHit.capture(event, vanilla, le));
                        return;
                    }
                }
                vanilla.processAttack(event);
            };
        };
    }

    private static final int HIT_QUEUE_BUFFER = 1;
    private static final Map<LivingEntity, PendingHit> pendingHit = new ConcurrentHashMap<>();
    private static final AtomicBoolean flushListenerStarted = new AtomicBoolean();

    /** {@code deadline} = the combat tick the ORIGINAL window ends. */
    private record PendingHit(AttackEvent event, AttackEvent.AttackRule rule, long deadline) {
        private static PendingHit capture(AttackEvent event, AttackEvent.AttackRule rule, LivingEntity target) {
            // crit/item read lazily; freeze them so the delayed hit is the queued swing
            event.overrideCritical(event.critical());
            event.overrideItem(event.item());
            return new PendingHit(event, rule,
                    TickSystem.tick(target) + DamageSystem.remainingDamageInvul(target));
        }
    }

    // per-tick order: advance clock -> flush expired -> attack packets
    private static void ensureFlushListener() {
        if (!flushListenerStarted.compareAndSet(false, true)) return;
        TickSystem.register(TickPhase.PRE_DISPATCH, Attack::flushExpired);
        // a deadline on the old clock fires once the new one counts up to it - a minutes-late phantom hit
        TickSystem.onClockChange(e -> { if (e instanceof LivingEntity le) pendingHit.remove(le); });
    }

    // victim's own pass only: shards share the base instance, so a foreign pass would run the hit on the wrong
    // thread against the owner's combat tick
    private static void flushExpired(TickContext ctx) {
        for (LivingEntity target : pendingHit.keySet()) {
            if (target.isRemoved()) pendingHit.remove(target);
            else if (ctx.owns(target)) flushPending(target);
        }
    }

    // read by Knockback's sprint gate on the same call stack; ThreadLocal because domains flush concurrently and
    // no entity/damage state can recompute "came through the queue"
    private static final ThreadLocal<LivingEntity> flushingNonSprintFor = new ThreadLocal<>();

    static boolean flushingNonSprint(Entity target) {
        return flushingNonSprintFor.get() == target;
    }

    // deadline-based: a window re-armed in between (fall damage, fire) must not hold the hit for whole extra windows
    private static boolean flushPending(LivingEntity le) {
        PendingHit p = pendingHit.get(le);
        if (p == null || TickSystem.tick(le) < p.deadline()) return false;
        if (!pendingHit.remove(le, p)) return false; // claimed elsewhere
        // non-sprint iff the most recent damage is still the projectile that opened the window
        if (DamageSystem.lastDamageType(le) instanceof ProjectileDamage) {
            flushingNonSprintFor.set(le);
            try { p.rule().processAttack(p.event()); } finally { flushingNonSprintFor.remove(); }
        } else {
            p.rule().processAttack(p.event());
        }
        return true;
    }
}
