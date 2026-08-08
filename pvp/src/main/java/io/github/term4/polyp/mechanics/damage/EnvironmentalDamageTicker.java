package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.util.tick.TickContext;
import io.github.term4.polyp.util.tick.TickPhase;
import io.github.term4.polyp.util.tick.TickSystem;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dispatcher for self-driven environmental damage producers (cactus, burning): one per-instance {@link TickSystem} hook
 * (installed on first {@link #bind}, once for the JVM since {@link TickSystem} has no removal) runs every registered
 * {@link EnvironmentalTickProducer} against each living, non-exempt entity. Producers {@link #register}/{@link #unregister}
 * as their types enable/disable; inert when none remain.
 */
public final class EnvironmentalDamageTicker {

    private static final EnvironmentalDamageTicker INSTANCE = new EnvironmentalDamageTicker();

    private final Set<EnvironmentalTickProducer> producers = new CopyOnWriteArraySet<>();
    private final AtomicBoolean hooked = new AtomicBoolean();
    private volatile @Nullable DamageSystem system;

    private EnvironmentalDamageTicker() {}

    public static EnvironmentalDamageTicker instance() { return INSTANCE; }

    /** Binds the active damage system and installs the per-instance tick hook once for the JVM. */
    public void bind(DamageSystem system) {
        this.system = system;
        if (hooked.compareAndSet(false, true)) {
            TickSystem.register(TickPhase.DEFAULT, this::tick);
        }
    }

    public void register(EnvironmentalTickProducer producer) { producers.add(producer); }

    public void unregister(EnvironmentalTickProducer producer) { producers.remove(producer); }

    private void tick(TickContext ctx) {
        DamageSystem sys = this.system;
        if (sys == null || producers.isEmpty()) return;
        for (Entity entity : ctx.world().entities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!ctx.owns(living) || DamageProducers.exempt(living)) continue;
            for (EnvironmentalTickProducer producer : producers) producer.tick(living, sys);
        }
    }
}
