package io.github.term4.polyp.api.event.damage;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The pre-damage gate: fired the moment a damage instance is proposed, <em>before</em> computation/mitigation and the
 * {@link DamageEvent}. Cancel to drop the hit entirely (it never consumes an i-frame window or runs mitigation), or
 * redirect the inputs (type / source / target / config) via {@link #finalSnap}. The amount isn't computed yet - adjust
 * it in {@link DamageEvent}.
 */
public final class PreDamageEvent extends CancellableMechanicsEvent<DamageSnapshot> {

    public PreDamageEvent(DamageSnapshot snap, Services services) {
        super(snap, services);
    }

    /** {@code null} = the system config. */
    public @Nullable DamageConfig config() { return finalSnap().config(); }
    public void config(@Nullable DamageConfig config) { finalSnap(finalSnap().withConfig(config)); }

    public DamageType type() { return finalSnap().type(); }
    public Entity target() { return finalSnap().target(); }
    /** The victim's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(target()); }
    public @Nullable Entity source() { return finalSnap().source(); }
    /** The attacker's weapon, or {@code null}. */
    public @Nullable ItemStack item() { return finalSnap().item(); }
    /** Type-specific payload from the producer (e.g. fall distance), or {@code null}. */
    public @Nullable Object detail() { return finalSnap().detail(); }
}
