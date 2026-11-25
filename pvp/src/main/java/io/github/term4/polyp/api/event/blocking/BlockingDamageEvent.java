package io.github.term4.polyp.api.event.blocking;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player blocks a hit (sword/shield), after the reduction is computed but before armor mitigation. Cancel
 * to veto the block - the hit takes its full {@link #originalAmount()}.
 */
public final class BlockingDamageEvent implements CancellableEvent {

    private final Player defender;
    private final @Nullable Entity attacker;
    private final float originalAmount;
    private final float blockedAmount;
    private final RegistryKey<DamageType> damageType;
    private boolean cancelled;

    public BlockingDamageEvent(Player defender, @Nullable Entity attacker, float originalAmount, float blockedAmount,
                               RegistryKey<DamageType> damageType) {
        this.defender = defender;
        this.attacker = attacker;
        this.originalAmount = originalAmount;
        this.blockedAmount = blockedAmount;
        this.damageType = damageType;
    }

    public Player defender() { return defender; }
    /** The defender's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(defender()); }
    public @Nullable Entity attacker() { return attacker; }
    /** Incoming damage before the block. */
    public float originalAmount() { return originalAmount; }
    /** Damage after the block (passed on to armor). */
    public float blockedAmount() { return blockedAmount; }
    public RegistryKey<DamageType> damageType() { return damageType; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
