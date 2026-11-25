package io.github.term4.polyp.api.event;

import io.github.term4.polyp.Services;
import net.minestom.server.event.trait.CancellableEvent;

/** A {@link MechanicsEvent} that can be cancelled; cancelling drops the pending action, as documented per event. */
public abstract class CancellableMechanicsEvent<S> extends MechanicsEvent<S> implements CancellableEvent {

    private boolean cancelled;

    protected CancellableMechanicsEvent(S snapshot, Services services) {
        super(snapshot, services);
    }

    public void cancel() { setCancelled(true); }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
