package io.github.term4.polyp.api.event;

import io.github.term4.polyp.Services;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Base for the action-domain events (attack / damage / knockback / consume): an immutable {@link #snapshot()} plus a
 * mutable {@link #finalSnap()} for processing. These four domains get Pre/main/Applied triads; projectiles and
 * explosions fire single events (their damage + KB route through the triads).
 */
public abstract class MechanicsEvent<S> implements Event {

    private final S snapshot;
    private final Services services;
    private @Nullable S finalSnap;

    protected MechanicsEvent(S snapshot, Services services) {
        this.snapshot = snapshot;
        this.services = services;
    }

    public S snapshot() { return snapshot; }

    /** The {@link #finalSnap(Object) override} when set, else the original snapshot. */
    public S finalSnap() { return finalSnap != null ? finalSnap : snapshot; }
    public void finalSnap(S snap) { this.finalSnap = snap; }

    public Services services() { return services; }
}
