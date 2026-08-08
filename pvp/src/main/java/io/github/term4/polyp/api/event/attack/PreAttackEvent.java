package io.github.term4.polyp.api.event.attack;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.attack.AttackConfig;
import io.github.term4.polyp.mechanics.attack.AttackSnapshot;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * The pre-attack gate: fired the moment a hit is detected, <em>before</em> config resolution, the {@link AttackEvent},
 * and the ruleset. Cancel to drop the hit before any processing, or redirect inputs (target / config) via
 * {@link #finalSnap}.
 */
public final class PreAttackEvent extends CancellableMechanicsEvent<AttackSnapshot> {

    public PreAttackEvent(AttackSnapshot snapshot, Services services) {
        super(snapshot, services);
    }

    /** Config carried so far, or {@code null} (resolution runs after this gate). */
    public @Nullable AttackConfig config() { return finalSnap().config(); }
    public void config(@Nullable AttackConfig config) { finalSnap(finalSnap().withConfig(config)); }

    public Entity attacker() { return finalSnap().attacker(); }

    /** Target, or {@code null} for a target-less detection (swing miss). */
    public @Nullable Entity target() { return finalSnap().target(); }
    /** The attacker's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(attacker()); }
}
