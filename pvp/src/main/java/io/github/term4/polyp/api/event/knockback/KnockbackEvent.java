package io.github.term4.polyp.api.event.knockback;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfigResolver;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * The main knockback phase: inspect and modify a knockback instance before it is applied. Bracketed by
 * {@link PreKnockbackEvent} (before computation) and {@link KnockbackAppliedEvent} (after the velocity is set).
 */
public final class KnockbackEvent extends CancellableMechanicsEvent<KnockbackSnapshot> {

    private @Nullable Vec velocity;
    private @Nullable Vec direction;

    public KnockbackEvent(KnockbackSnapshot snap, Services services) {
        super(snap, services);
    }

    /** {@code null} = the system config. */
    public @Nullable KnockbackConfig config() { return finalSnap().config(); }
    public void config(@Nullable KnockbackConfig config) { finalSnap(finalSnap().withConfig(config)); }

    /** Re-resolved from {@link #finalSnap()} each call, so it tracks listener changes. */
    public KnockbackConfigResolver.ResolvedKnockbackConfig resolvedConfig() {
        return services().knockback().resolveConfig(finalSnap());
    }

    /** Melee hit (gates sprint extra / melee-only components). */
    public boolean melee() { return finalSnap().melee(); }

    /** If set, used instead of running the calculator. */
    public @Nullable Vec velocity() { return velocity; }
    public void velocity(@Nullable Vec velocity) { this.velocity = velocity; }

    /** Overrides the computed horizontal direction (keeps the magnitude). */
    public @Nullable Vec direction() { return direction; }
    public void direction(@Nullable Vec direction) { this.direction = direction; }

    public @Nullable Entity source() { return finalSnap().source(); }
    public @Nullable Entity target() { return finalSnap().target(); }
    /** The victim's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(target()); }
}
