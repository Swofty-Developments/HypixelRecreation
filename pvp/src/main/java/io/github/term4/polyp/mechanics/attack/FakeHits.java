package io.github.term4.polyp.mechanics.attack;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.damage.DamageAppliedEvent;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.world.WorldPolicy;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerCancelDiggingEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerFinishDiggingEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Swing fake-hits: an arm-swing whose attack packet missed still lands on the LAST player the attacker hit, if the
 * look ray (the swing tick's own, then {@code lookWindow} ticks of move/look packets) falls on the victim's
 * margin-padded box within reach. A granted hit runs the normal attack pipeline carrying the intersecting ray as its
 * {@code aim}, so knockback follows it and effects route to the fake-hit endpoints. Only a pure left-click swing arms.
 *
 * <p>Two independent rule layers per attacker - a swing fills if either grants it: {@link AttackConfig#fakeHits} from
 * the attack scope chain (a preset's MineMen-style combo fill, {@link FakeHitConfig#windowed}), and a windowless
 * bare-fist fill for {@code CompatConfig.fistRayHits}-gated clients ({@code attack_range} rides items, so an empty
 * hand still picks with margin 0). Inert for players neither arms.
 */
public final class FakeHits {

    private static final Logger LOG = LoggerFactory.getLogger(FakeHits.class);

    // volatile: expiry is stamped on the victim's thread, read on the attacker's
    private static final class Swing {
        volatile Player lastVictim;
        volatile long victimExpiryTick = Long.MIN_VALUE; // combat tick the last-hit victim leaves i-frames (victim clock)
        volatile long swingTick = Long.MIN_VALUE;        // MIN_VALUE = no armed window (attacker clock)
        volatile FakeHitConfig armedRule;                // the rule that armed swingTick (read by the move-look rays)
        volatile long lastPacketTick = Long.MIN_VALUE;    // the client's own attack packet - a real hit, no fill needed
        volatile long lastNonAttackTick = Long.MIN_VALUE;
    }
    /** An entry exists only for armed attackers with recorded combat, so the listeners are inert for everyone else. */
    private static final Map<Player, Swing> swings = new ConcurrentHashMap<>();
    private static final AtomicBoolean CLOCK_RESET = new AtomicBoolean();

    private FakeHits() {}

    public static void install(Polyp polyp) {
        AttackLog.installDigTracking(polyp);
        if (CLOCK_RESET.compareAndSet(false, true)) {
            // a window stamped on the old clock never expires, so it re-rays on every move packet
            TickSystem.onClockChange(e -> { if (e instanceof Player p) swings.remove(p); });
        }
        EventNode<@NotNull Event> node = EventNode.all("polyp:fake-hits");
        // off the damage pipeline, not the attack events, so hits landing outside AttackSystem.apply (a preset's
        // queued-hit flush) still re-arm
        node.addListener(DamageAppliedEvent.class, e -> {
            if (e.outcome() != DamageSystem.DamageOutcome.FRESH_DAMAGE || !(e.snapshot().type() instanceof MeleeDamage)) return;
            if (!(e.snapshot().source() instanceof Player atk) || !(e.snapshot().target() instanceof Player victim) || victim == atk) return;
            if (rulesFor(polyp, atk).isEmpty()) return;
            Swing s = swings.computeIfAbsent(atk, k -> new Swing());
            s.lastVictim = victim;
            s.victimExpiryTick = TickSystem.tick(victim) + DamageSystem.remainingDamageInvul(victim);
        });
        // the client's own attack packet this tick is the real hit; the fill only covers a miss
        node.addListener(EntityAttackEvent.class, e -> {
            if (e.getEntity() instanceof Player atk) {
                Swing s = swings.get(atk);
                if (s != null) s.lastPacketTick = TickSystem.tick(atk);
            }
        });
        // a drop's or right-click use's swing is never an attack; also disarms a same-tick window (either packet order)
        node.addListener(ItemDropEvent.class, e -> markNonAttack(e.getPlayer()));
        node.addListener(PlayerUseItemEvent.class, e -> markNonAttack(e.getPlayer()));
        node.addListener(PlayerUseItemOnBlockEvent.class, e -> markNonAttack(e.getPlayer()));
        // dig ACTION ticks too, modern only (a 1.8 client's dig swings may be attacks): the live digging tag misses the
        // finish tick (STOP clears it before that tick's swing) and instant breaks entirely (creative fires no dig events)
        node.addListener(PlayerStartDiggingEvent.class, e -> markDigTick(polyp, e.getPlayer()));
        node.addListener(PlayerCancelDiggingEvent.class, e -> markDigTick(polyp, e.getPlayer()));
        node.addListener(PlayerFinishDiggingEvent.class, e -> markDigTick(polyp, e.getPlayer()));
        node.addListener(PlayerBlockBreakEvent.class, e -> markDigTick(polyp, e.getPlayer()));
        node.addListener(PlayerHandAnimationEvent.class, e -> arm(polyp, e));
        // fires PRE-move: getNewPosition() carries the incoming look for the ray while the hit reads the attacker's normal state
        node.addListener(PlayerMoveEvent.class, e -> {
            Player atk = e.getPlayer();
            Swing s = swings.get(atk);
            if (s == null || s.swingTick == Long.MIN_VALUE) return;
            FakeHitConfig rule = s.armedRule;
            // tracks the client's real-time packet cadence, so it scales to live TPS like the arm windows
            long look = rule == null ? 0 : TickScaler.duration(rule.lookWindow(), polyp.profiles().resolve(atk, MechanicsKeys.TICK_SCALING), DamageSystem.KEY);
            if (rule == null || TickSystem.tick(atk) - s.swingTick > look) s.swingTick = Long.MIN_VALUE;
            else if (tryFill(polyp, atk, s, rule, e.getNewPosition())) s.swingTick = Long.MIN_VALUE;
        });
        node.addListener(PlayerDisconnectEvent.class, e -> swings.remove(e.getPlayer()));
        polyp.install(node);
    }

    private static void markNonAttack(Player p) {
        Swing s = swings.get(p);
        if (s != null) { s.lastNonAttackTick = TickSystem.tick(p); s.swingTick = Long.MIN_VALUE; }
    }

    private static void markDigTick(Polyp polyp, Player p) {
        if (!polyp.clientInfo().isLegacy(p)) markNonAttack(p);
    }

    /** Independent layers: a preset's windowed rule must not shadow the windowless compat fist fill. */
    private static List<FakeHitConfig> rulesFor(Polyp polyp, Player atk) {
        AttackConfig cfg = polyp.profiles().resolve(atk, MechanicsKeys.ATTACK);
        if (cfg == null) {
            AttackSystem sys = polyp.module(AttackSystem.class);
            cfg = sys != null ? sys.config() : null;
        }
        FakeHitConfig preset = cfg != null ? cfg.fakeHits : null;
        // windowless: the bare hand should behave like a stamped item - EVERY swing picks the padded box, no combo timing
        FakeHitConfig fist = atk instanceof OptimizedPlayer op && op.compat().fistRayHits()
                ? new FakeHitConfig(true, false, op.compat().attackReach(), 1, 1, 1, true) : null;
        if (preset != null && fist != null) return List.of(preset, fist);
        if (preset != null) return List.of(preset);
        return fist != null ? List.of(fist) : List.of();
    }

    private static void arm(Polyp polyp, PlayerHandAnimationEvent e) {
        if (e.getHand() != PlayerHand.MAIN) return;
        Player atk = e.getPlayer();
        Swing s = swings.get(atk);
        Player victim = s != null ? s.lastVictim : null;
        if (victim == null || s.lastNonAttackTick == TickSystem.tick(atk)) return;
        // a modern client's break swings aren't attacks (the left-click drives the break); a 1.8 client's may be
        if (!polyp.clientInfo().isLegacy(atk) && AttackLog.digging(atk)) return;
        FakeHitConfig armed = null;
        for (FakeHitConfig rule : rulesFor(polyp, atk)) {
            if (rule.emptyHandOnly() && !atk.getItemInMainHand().isAir()) continue;
            if (rule.windowed() && !inWindow(polyp, s, victim, rule)) continue;
            armed = rule;
            break;
        }
        if (LOG.isDebugEnabled()) LOG.debug("swing {}: victim={} expiry={} now={} armed={}",
                atk.getUsername(), victim.getUsername(), s.victimExpiryTick, TickSystem.tick(victim), armed);
        if (armed == null) return;
        s.armedRule = armed;
        s.swingTick = TickSystem.tick(atk);
        // the swing tick's own aim counts too: a stationary attacker sends no follow-up move/look to read the ray from
        if (tryFill(polyp, atk, s, armed, atk.getPosition())) s.swingTick = Long.MIN_VALUE;
    }

    /** Whether the victim sits inside {@code rule}'s swing window around its i-frame expiry (compared on the VICTIM's clock, where the expiry was stamped). */
    private static boolean inWindow(Polyp polyp, Swing s, Player victim, FakeHitConfig rule) {
        var scaling = polyp.profiles().resolve(victim, MechanicsKeys.TICK_SCALING);
        long before = TickScaler.duration(rule.windowBefore(), scaling, DamageSystem.KEY);
        long after = TickScaler.duration(rule.windowAfter(), scaling, DamageSystem.KEY);
        long now = TickSystem.tick(victim);
        return now >= s.victimExpiryTick - before && now < s.victimExpiryTick + after;
    }

    /** Feeds the fill through the pipeline iff still eligible and the ray (eye at {@code from}) is on the victim's padded box within reach. */
    private static boolean tryFill(Polyp polyp, Player atk, Swing s, FakeHitConfig rule, Pos from) {
        Player victim = s.lastVictim;
        if (victim == null || victim.isRemoved() || !WorldPolicy.canAffect(atk, victim)) return false;
        if (s.lastPacketTick == TickSystem.tick(atk)) return false;
        if (rule.emptyHandOnly() && !atk.getItemInMainHand().isAir()) return false;
        if (!polyp.clientInfo().isLegacy(atk) && AttackLog.digging(atk)) return false;
        double dist = AttackLog.rayReach(atk, victim, polyp.clientInfo(), rule.reach(), from);
        if (dist < 0) return false;
        boolean stale = rule.fullBoxWhileInvul() && TickSystem.tick(victim) > s.victimExpiryTick;
        if (stale && !entersAboveBoxTop(atk, victim, from, dist)) return false;
        AttackSystem attack = polyp.module(AttackSystem.class);
        if (attack == null) return false;
        // no invul gate: a fill inside the last i-frame tick buffers/queues like a real hit would
        attack.apply(new AttackSnapshot(atk, victim, null, from));
        return true;
    }

    /** Whether the ray's entry point sits above the victim's REAL box top - i.e. in the head band of the margin expansion. */
    private static boolean entersAboveBoxTop(Player atk, Player victim, Pos from, double dist) {
        double entryY = from.y() + atk.getEyeHeight() + from.direction().y() * dist;
        return entryY >= victim.getPosition().y() + victim.getBoundingBox().height();
    }
}
