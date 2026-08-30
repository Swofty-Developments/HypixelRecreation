package io.github.term4.polyp.mechanics.durability;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Item durability (damage-on-use). Per-scope config via {@code DURABILITY}.
 *
 * <p><b>Stub:</b> the API surface is in place, but no durability is consumed yet (TODO).
 */
public final class DurabilitySystem implements MechanicsModule {

    private final Polyp polyp;
    private final DurabilityConfig config;
    private final EventNode<@NotNull Event> node;

    public DurabilitySystem(Polyp polyp, DurabilityConfig config) {
        this.polyp = polyp;
        this.config = config;
        this.node = EventNode.all("polyp:durability");
    }

    public EventNode<@NotNull Event> node() { return node; }
    public DurabilityConfig config() { return config; }

    /** Effective config for {@code subject}: the scoped profile (player -&gt; instance -&gt; global), else the install config. */
    public DurabilityConfig configFor(@Nullable Entity subject) {
        return polyp.profiles().resolveOr(subject, MechanicsKeys.DURABILITY, config);
    }

    /** Active by default; only an explicit {@code enabled(false)} disables. */
    public boolean enabled(@Nullable Entity subject) {
        return !Boolean.FALSE.equals(configFor(subject).enabled());
    }

    /**
     * The combat/mining/Thorns entry point. <b>Stub:</b> a no-op until the durability logic lands.
     */
    public void damage(LivingEntity holder, EquipmentSlot slot, int amount) {
        if (!enabled(holder)) return;
        // TODO(durability): consume Unbreaking, decrement the stack's damage component, break + emit the item on overflow.
    }

    /** Installs the system active (a per-scope {@code MechanicsProfile.durability} config can disable it). */
    public static DurabilitySystem install(Polyp polyp) {
        return install(polyp, DurabilityConfig.builder().build());
    }

    public static DurabilitySystem install(Polyp polyp, DurabilityConfig cfg) {
        DurabilitySystem system = new DurabilitySystem(polyp, cfg);
        polyp.register(system);
        polyp.install(system.node);
        return system;
    }
}
