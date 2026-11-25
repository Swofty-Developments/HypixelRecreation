package io.github.term4.polyp.mechanics.hunger;

/**
 * The cost rule for one exhaustion source: maps the source's reported quantity to the exhaustion charged. No config
 * entry = {@link #dynamic()}.
 */
@FunctionalInterface
public interface ExhaustionCost {

    float cost(float quantity);

    /** A fixed cost per event, ignoring the quantity. */
    static ExhaustionCost flat(float value) { return q -> value; }

    static ExhaustionCost scaled(float scale) { return q -> q * scale; }

    /** The quantity as-is (the default for unconfigured sources). */
    static ExhaustionCost dynamic() { return q -> q; }

    /** Never charges. */
    static ExhaustionCost free() { return q -> 0f; }
}
