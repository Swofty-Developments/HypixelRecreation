package io.github.term4.polyp.world;

/**
 * A self-driven polyp entity that is safe for an external clock (an Archipelago shard domain) to own: it overrides
 * {@code tick} to bail when {@link MechanicsWorld#ownsCurrentTick} is false, and {@code refreshCurrentChunk} to skip
 * the global dispatcher when {@link MechanicsWorld#externallyTicked}. Implementing this is a PROMISE those guards
 * exist - a Java interface can't override a superclass {@code tick}, so a bridge covers {@code instanceof
 * ExternallyTickable} in ONE rule and every future entity (end crystals, ...) is domain-tickable with no bridge edit.
 * Without the guards an attached entity double-ticks (both clocks drive it).
 */
public interface ExternallyTickable {
}
