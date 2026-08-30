package io.github.term4.polyp.util.tick;

/**
 * Ordering bucket for {@link TickSystem} work within one instance tick. {@code PRE_DISPATCH} runs before the dispatcher
 * ticks entities (motion poll, hit-queue flush - they must be current when combat reads them); {@code POST} runs after.
 */
public enum TickPhase { PRE_DISPATCH, DEFAULT, POST }
