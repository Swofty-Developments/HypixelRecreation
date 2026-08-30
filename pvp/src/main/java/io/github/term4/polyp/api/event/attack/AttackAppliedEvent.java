package io.github.term4.polyp.api.event.attack;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.attack.AttackSnapshot;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired after a detected attack has been processed by its ruleset (damage / knockback applied) - informational. The
 * post-attack counterpart to {@link PreAttackEvent} / {@link AttackEvent}.
 */
public final class AttackAppliedEvent implements Event {

    private final AttackSnapshot snapshot;
    private final Services services;

    public AttackAppliedEvent(AttackSnapshot snapshot, Services services) {
        this.snapshot = snapshot;
        this.services = services;
    }

    /** The finalized snapshot the attack was processed from. */
    public AttackSnapshot snapshot() { return snapshot; }

    public Services services() { return services; }

    public Entity attacker() { return snapshot.attacker(); }
    public @Nullable Entity target() { return snapshot.target(); }
    /** The attacker's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(attacker()); }
}
