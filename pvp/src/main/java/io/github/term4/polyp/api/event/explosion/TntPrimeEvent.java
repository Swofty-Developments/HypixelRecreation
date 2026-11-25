package io.github.term4.polyp.api.event.explosion;

import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fired before primed TNT enters the world ({@link PrimedTnt#ignite}/{@link PrimedTnt#spawn}). The entity is
 * live - adjust its velocity here. Cancel to prime nothing; an ignited block then stays in place.
 */
public final class TntPrimeEvent implements CancellableEvent {

    public enum Cause { FLINT_AND_STEEL, FIRE_CHARGE, EXPLOSION, PLACEMENT, API }

    private final PrimedTnt tnt;
    private final MechanicsWorld world;
    private final @Nullable Point blockPos;
    private final @Nullable Entity igniter;
    private final Cause cause;
    private boolean cancelled;

    public TntPrimeEvent(PrimedTnt tnt, MechanicsWorld world, @Nullable Point blockPos,
                         @Nullable Entity igniter, Cause cause) {
        this.tnt = tnt;
        this.world = world;
        this.blockPos = blockPos;
        this.igniter = igniter;
        this.cause = cause;
    }

    public PrimedTnt tnt() { return tnt; }
    public MechanicsWorld world() { return world; }
    /** The TNT block being converted, or {@code null} for a blockless prime. */
    public @Nullable Point blockPos() { return blockPos; }
    public @Nullable Entity igniter() { return igniter; }
    public Cause cause() { return cause; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
