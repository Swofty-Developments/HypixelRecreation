package io.github.term4.polyp.mechanics.knockback;

import io.github.term4.polyp.MechanicsProfiles;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.knockback.KnockbackEvent;
import io.github.term4.polyp.api.event.knockback.PreKnockbackEvent;
import io.github.term4.polyp.api.event.knockback.KnockbackAppliedEvent;
import io.github.term4.polyp.presets.vanilla18.Knockback;
import io.github.term4.polyp.platform.compatibility.LegacyVelocityBridge;
import io.github.term4.polyp.tracking.motion.LegacyVelocity;
import io.github.term4.polyp.tracking.motion.MotionTracker;
import net.kyori.adventure.key.Key;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.ListenerHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Knockback system, configured via {@link KnockbackConfig} or the {@link KnockbackEvent} API. No invul window of its
 * own (neither does vanilla); every {@link #apply} deals knockback and gating lives in the attack processor.
 */
public final class KnockbackSystem implements MechanicsModule {

    /** This system's identity for per-module TPS scaling (its {@code referenceTps} feel-baseline). */
    public static final Key KEY = Key.key("polyp:knockback");

    private final KnockbackConfig config;
    private final KnockbackCalculator calc;
    private final MechanicsProfiles profiles;
    private final Services services;
    private final EventNode<@NotNull Event> node;

    // Pre/Applied fire only when listened to; main always fires
    private static final ListenerHandle<PreKnockbackEvent> PRE_KNOCKBACK = EventDispatcher.getHandle(PreKnockbackEvent.class);
    private static final ListenerHandle<KnockbackAppliedEvent> KNOCKBACK_APPLIED = EventDispatcher.getHandle(KnockbackAppliedEvent.class);

    public KnockbackSystem(Polyp polyp, KnockbackConfig config) {
        this.config = config;
        this.profiles = polyp.profiles();
        this.node = EventNode.all("polyp:knockback");
        this.services = polyp.services();
        KnockbackConfig defaults = Knockback.melee();
        this.calc = new KnockbackCalculator(this.services, defaults);
    }

    /** Resolves the effective knockback values for {@code snap} (config chain + calculator defaults); the {@link KnockbackEvent} preview. */
    public KnockbackConfigResolver.ResolvedKnockbackConfig resolveConfig(KnockbackSnapshot snap) {
        return calc.resolveConfig(snap.config() != null ? snap : snap.withConfig(configFor(snap.target())));
    }

    /** Effective config for a snapshot carrying none: the victim's scoped profile, else the install config. */
    private KnockbackConfig configFor(@Nullable Entity target) {
        KnockbackConfig scoped = profiles.resolve(target, MechanicsKeys.KNOCKBACK);
        return scoped != null ? scoped : config;
    }

    public void apply(KnockbackSnapshot snap) {
        apply(snap, null);
    }

    /**
     * Applies knockback plus an optional {@code impulse} stacked onto the computed velocity right before delivery -
     * un-folded (past the friction term), so it adds on top rather than being halved. Explosions use it to put the
     * falloff push on top of the base knockback; the impulse rides the same quantize + wire + applied event as the base.
     */
    public void apply(KnockbackSnapshot snap, @Nullable Vec impulse) {
        // none = inert, empty = vanilla floor; an impulse is delivered regardless
        if (impulse == null && snap.config() == null && configFor(snap.target()) == null) return;

        if (PRE_KNOCKBACK.hasListener()) {
            PreKnockbackEvent pre = new PreKnockbackEvent(snap, services);
            EventDispatcher.call(pre);
            if (pre.isCancelled()) return;
            snap = pre.finalSnap();
        }

        var event = new KnockbackEvent(snap, services);
        EventDispatcher.call(event);
        if (event.isCancelled()) return;
        KnockbackSnapshot finalSnap = event.finalSnap();

        Entity target = finalSnap.target();
        if (target == null) return;
        KnockbackSnapshot cfgSnap = finalSnap.config() != null ? finalSnap : finalSnap.withConfig(configFor(target));

        // an override skips compute, so the quantize knobs are resolved lazily below
        @Nullable Vec velocity;
        @Nullable KnockbackConfigResolver.ResolvedKnockbackConfig resolved;
        if (event.velocity() != null) {
            velocity = event.velocity();
            resolved = null;
        } else {
            KnockbackCalculator.KnockbackResult result = calc.computeResult(cfgSnap);
            velocity = result != null ? result.velocity() : null;
            resolved = result != null ? result.config() : null;
            if (velocity != null && event.direction() != null) {
                double mag = Math.sqrt(velocity.x() * velocity.x() + velocity.z() * velocity.z());
                Vec dir = event.direction().normalize();
                velocity = new Vec(dir.x() * mag, velocity.y(), dir.z() * mag);
            }
        }

        if (impulse != null) velocity = velocity != null ? velocity.add(impulse) : impulse;

        if (velocity != null) {
            if (resolved == null) resolved = calc.resolveConfig(cfgSnap);
            broadcast(target, velocity, resolved, finalSnap);
        }
    }

    /**
     * Puts a pre-computed velocity straight on the wire (quantize + the 1.8-exact ViaBridge) with NO calculation or
     * velocity fold - the explosion uses it for the radial (Hypixel) knockback, which SETS the victim's velocity to
     * {@code push + base} rather than folding the pre-hit velocity like the vanilla {@code a()} path.
     */
    public void deliver(@NotNull Entity target, @NotNull Vec velocity) {
        KnockbackSnapshot snap = new KnockbackSnapshot(target, false, null, null, null, null);
        broadcast(target, velocity, resolveConfig(snap), snap);
    }

    private void broadcast(Entity target, Vec velocity, KnockbackConfigResolver.ResolvedKnockbackConfig resolved, KnockbackSnapshot snap) {
        double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        KnockbackConfig.WireRule wire = resolved.wireRule();
        // before the fold: the tracker must see what the client will receive
        if (wire != null) velocity = wire.apply(velocity.div(tps)).mul(tps);
        boolean quantize = resolved.quantizeVelocity();
        double cap = resolved.velocityCap();
        Vec applied = quantize ? LegacyVelocity.snap(velocity, cap) : velocity;
        // non-melee stays in server mot and folds into later hits; melee restores (measured, see foldDelivered)
        if (!snap.melee()) MotionTracker.foldDelivered(target, velocity.div(tps));
        // exact 1.8 wire for a knocked legacy client above the LP-exact band (via ViaBridge); else the normal LP broadcast
        if (!(quantize && LegacyVelocityBridge.applyExact(target, velocity, applied, cap))) target.setVelocity(applied);
        if (KNOCKBACK_APPLIED.hasListener()) EventDispatcher.call(new KnockbackAppliedEvent(snap, applied, services));
    }

    public KnockbackConfig config() { return config; }

    public EventNode<@NotNull Event> node() { return node; }

    /** Installs inert (no install-level config): an {@link #apply} with no scoped or snapshot config applies nothing. Pass an empty config to apply at the vanilla floor. */
    public static KnockbackSystem install(Polyp polyp) {
        return install(polyp, (KnockbackConfig) null);
    }

    public static KnockbackSystem install(Polyp polyp, KnockbackConfig config) {
        var system = new KnockbackSystem(polyp, config);
        polyp.register(system);
        polyp.install(system.node);
        return system;
    }

}