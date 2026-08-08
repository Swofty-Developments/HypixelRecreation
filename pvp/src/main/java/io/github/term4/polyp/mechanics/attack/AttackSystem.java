package io.github.term4.polyp.mechanics.attack;

import io.github.term4.polyp.MechanicsProfiles;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.attack.AttackEvent;
import io.github.term4.polyp.api.event.attack.AttackAppliedEvent;
import io.github.term4.polyp.api.event.attack.PreAttackEvent;
import net.kyori.adventure.key.Key;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.ListenerHandle;
import org.jetbrains.annotations.NotNull;

/**
 * Attack pipeline: {@link HitDetection} turns raw input into snapshots, then the system fires the {@link AttackEvent}
 * API and runs the configured ruleset. No invul window or hit buffering of its own - every hit is processed and the
 * damage/knockback systems gate themselves. Preset behaviors (e.g. Mmc18's hit queue) live in custom rulesets/detections.
 */
public final class AttackSystem implements MechanicsModule {

    /** This system's identity for per-module TPS scaling (its {@code referenceTps} feel-baseline). */
    public static final Key KEY = Key.key("polyp:attack");

    private final AttackConfig config;
    private final MechanicsProfiles profiles;
    private final Services services;
    private final EventNode<@NotNull Event> node;

    // Pre/Applied fire only when listened to; main always fires
    private static final ListenerHandle<PreAttackEvent> PRE_ATTACK = EventDispatcher.getHandle(PreAttackEvent.class);
    private static final ListenerHandle<AttackAppliedEvent> ATTACK_APPLIED = EventDispatcher.getHandle(AttackAppliedEvent.class);

    /**
     * @param detection how hits are detected; none given installs {@link HitDetection#PACKET}. Always live - {@code enabled}
     *                  gates per hit, so a disabled install config can be switched live by assigning an enabled profile.
     */
    public AttackSystem(Polyp polyp, AttackConfig config, HitDetection... detection) {
        this.config = config;
        this.profiles = polyp.profiles();
        this.services = polyp.services();
        this.node = EventNode.all("polyp:attack");

        if (detection.length == 0) detection = new HitDetection[]{HitDetection.PACKET};
        for (HitDetection d : detection) d.install(node, this::apply);
    }

    /**
     * Feeds a detected hit through the pipeline: config chain (snapshot -> attacker scope -> install) -> {@link AttackEvent}
     * -> ruleset. Public so custom hit detection submits hits exactly like the built-in packet detection.
     */
    public void apply(AttackSnapshot snap) {
        if (PRE_ATTACK.hasListener()) {
            PreAttackEvent pre = new PreAttackEvent(snap, services);
            EventDispatcher.call(pre);
            if (pre.isCancelled()) return;
            snap = pre.finalSnap();
        }
        // none = inert, empty = vanilla floor
        AttackConfig effective = snap.config();
        if (effective == null) {
            AttackConfig scoped = profiles.resolve(snap.attacker(), MechanicsKeys.ATTACK);
            effective = scoped != null ? scoped : config;
        }
        if (effective == null) return;
        if (snap.config() == null) snap = snap.withConfig(effective);

        AttackEvent api = new AttackEvent(snap, services);
        EventDispatcher.call(api);
        // enabled read live: a listener can swap in an enabled config to let the hit through
        if (api.isCancelled() || !api.process() || !api.resolvedConfig().enabled()) return;

        AttackEvent.AttackRule proc = api.processor() != null
                ? api.processor().create(services)
                : api.resolvedConfig().ruleset().create(services);
        proc.processAttack(api);

        if (ATTACK_APPLIED.hasListener()) EventDispatcher.call(new AttackAppliedEvent(api.finalSnap(), services));
    }

    /** Installs inert (no install-level config): a detected hit with no scoped or snapshot config is dropped. Pass an empty config to process at the vanilla floor. */
    public static AttackSystem install(Polyp polyp, HitDetection... detection) {
        return install(polyp, (AttackConfig) null, detection);
    }

    /** Installs the system. {@code detection} as in {@link #AttackSystem(Polyp, AttackConfig, HitDetection...)}. */
    public static AttackSystem install(Polyp polyp, AttackConfig config, HitDetection... detection) {
        var system = new AttackSystem(polyp, config, detection);
        polyp.register(system);
        polyp.install(system.node);
        return system;
    }

    public AttackConfig config() { return config; }

    /** This system's listener node ({@code polyp:attack}); detection listeners mount here. */
    public EventNode<@NotNull Event> node() { return node; }
}
