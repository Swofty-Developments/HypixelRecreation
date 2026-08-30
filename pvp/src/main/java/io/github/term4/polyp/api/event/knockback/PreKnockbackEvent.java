package io.github.term4.polyp.api.event.knockback;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * The pre-knockback gate: fired <em>before</em> the velocity is computed and the {@link KnockbackEvent}. Cancel to
 * suppress the knockback entirely, or redirect the inputs (melee flag / config) via {@link #finalSnap}. The velocity
 * isn't computed yet - override it in {@link KnockbackEvent}.
 */
public final class PreKnockbackEvent extends CancellableMechanicsEvent<KnockbackSnapshot> {

    public PreKnockbackEvent(KnockbackSnapshot snap, Services services) {
        super(snap, services);
    }

    /** {@code null} = the system config. */
    public @Nullable KnockbackConfig config() { return finalSnap().config(); }
    public void config(@Nullable KnockbackConfig config) { finalSnap(finalSnap().withConfig(config)); }

    /** Melee hit (gates sprint extra / melee-only components). */
    public boolean melee() { return finalSnap().melee(); }

    public @Nullable Entity source() { return finalSnap().source(); }
    public @Nullable Entity target() { return finalSnap().target(); }
    /** The victim's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(target()); }
}
