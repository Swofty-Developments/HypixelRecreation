package io.github.term4.polyp.util.tick;

/** A unit of per-tick work installed in the {@link TickSystem}: runs once per instance tick, in {@link #phase()} order, every {@link #interval()} ticks. */
@FunctionalInterface
public interface Tickable {

    void tick(TickContext ctx);

    /** Run every Nth instance tick ({@code 1} = every tick). */
    default int interval() { return 1; }

    /** Ordering bucket within a tick. */
    default TickPhase phase() { return TickPhase.DEFAULT; }
}
