package io.github.term4.polyp.api.event.damage;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The main damage phase: fired after mitigation with the resolved {@link #amount()}, before it is applied. Bracketed by
 * {@link PreDamageEvent} and {@link DamageAppliedEvent}.
 */
public final class DamageEvent extends CancellableMechanicsEvent<DamageSnapshot> {

    private float amount;
    private final boolean invulnerable;
    private final int remainingInvul;
    private final float stored;
    private boolean bypassInvul;
    private boolean bypassImmune;

    public DamageEvent(DamageSnapshot snap, float amount, Services services) {
        super(snap, services);
        this.amount = amount;
        this.invulnerable = DamageSystem.isInvulnerableToDamage(snap.target());
        this.remainingInvul = snap.target() instanceof LivingEntity le
                ? DamageSystem.remainingDamageInvul(le) : 0;
        this.stored = snap.target() instanceof LivingEntity le
                ? DamageSystem.lastDamage(le) : 0f;
    }

    public float amount() { return amount; }
    public void amount(float amount) { this.amount = amount; }

    /** {@code null} = the system's install config. */
    public @Nullable DamageConfig config() { return finalSnap().config(); }
    public void config(@Nullable DamageConfig config) { finalSnap(finalSnap().withConfig(config)); }

    /** In its i-frame window when the hit arrived (not creative/spectator immunity). */
    public boolean invulnerable() { return invulnerable; }
    public int remainingInvul() { return remainingInvul; }

    /** Highwater of the current i-frame window; overdamage applies only {@code amount - stored}. {@code 0} when fresh. */
    public float stored() { return stored; }

    /** Ignore the target's i-frame window. */
    public boolean bypassInvul() { return bypassInvul; }
    public void bypassInvul(boolean bypass) { this.bypassInvul = bypass; }
    /** Ignore creative/spectator immunity (void / admin kill). */
    public boolean bypassImmune() { return bypassImmune; }
    public void bypassImmune(boolean bypass) { this.bypassImmune = bypass; }

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
