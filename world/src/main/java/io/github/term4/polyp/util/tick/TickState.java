package io.github.term4.polyp.util.tick;

/**
 * Event at eventTick, effective for duration ticks. The no-arg methods read the server-wide clock
 * ({@link TickSystem#serverTick()}); the {@code (long now)} overloads take the clock explicitly, to evaluate a window
 * against a per-instance clock instead. Use {@link #isActiveWithin(int)} when the duration is passed at check time.
 */
public record TickState(long eventTick, int duration) {

    public boolean isActive() {
        return isActive(TickSystem.serverTick());
    }

    /**
     * True if {@code eventTick <= now < eventTick + duration} against the supplied clock. A "future" stamp
     * ({@code now < eventTick}) reads inactive - guards a stamp made under a different clock baseline (e.g. one carried
     * across instances by the per-instance {@link TickSystem} clock).
     */
    public boolean isActive(long now) {
        return now >= eventTick && now < eventTick + duration;
    }

    /** True if the event occurred within the last {@code ticks}. */
    public boolean isActiveWithin(int ticks) {
        return isActiveWithin(TickSystem.serverTick(), ticks);
    }

    /** Against the supplied clock; a "future" stamp reads not-recent (see {@link #isActive(long)}). */
    public boolean isActiveWithin(long now, int ticks) {
        long elapsed = now - eventTick;
        return elapsed >= 0 && elapsed <= ticks;
    }

    /** True if event was more than {@code ticks} ago (e.g. "on ground in past N" = last airborne was stale). */
    public boolean isStaleAfter(int ticks) {
        return isStaleAfter(TickSystem.serverTick(), ticks);
    }

    /** True if event was more than {@code ticks} ago against the supplied clock; a "future" stamp reads stale. */
    public boolean isStaleAfter(long now, int ticks) {
        long elapsed = now - eventTick;
        return elapsed < 0 || elapsed > ticks;
    }

    public int remainingTicks() {
        return remainingTicks(TickSystem.serverTick());
    }

    /** Ticks remaining against the supplied clock; a "future" stamp reads {@code 0}. */
    public int remainingTicks(long now) {
        return now < eventTick ? 0 : (int) Math.max(0, eventTick + duration - now);
    }
}
