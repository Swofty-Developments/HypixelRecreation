package io.github.term4.polyp.mechanics.attribute;

/**
 * A typed key for an optional fact carried on an {@link AttributeConfigResolver.AttributeContext}. The calling domain
 * drops in whatever facts it has (combat -> the attack target, mining -> the block); conditional sources read the ones
 * they need. Keeps the context domain-agnostic: no field per domain, and facts are absent for ambient reads.
 *
 * @param <T> use a neutral type ({@code Entity}, {@code Block}) to avoid cross-package coupling
 */
public record FactKey<T>(String name) {
    @Override public String toString() { return "FactKey[" + name + "]"; }
}
