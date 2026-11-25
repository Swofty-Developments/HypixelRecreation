package io.github.term4.polyp.mechanics.attribute.source;
import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.AttributeConfigResolver;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * A source of attribute modifiers - an enchant, potion effect, or custom registry entry. Version differences are
 * <em>different instances</em> ({@code Strength.LEGACY} / {@code MODERN}), not a flag the source branches on. Keyed by the
 * vanilla effect/enchant key it answers to. Consumers read an attribute's value, never a specific source.
 */
public abstract class Source {

    private final Key key;

    protected Source(Key key) { this.key = key; }

    public Key key() { return key; }

    public List<Mod> modifiers(int level) { return List.of(); }

    /**
     * Context-aware modifiers: a domain-conditional source reads facts off {@code ctx} (e.g. the attack target via
     * {@code CombatFacts.TARGET}) and contributes only when its predicate holds. Falls back to the ambient
     * {@link #modifiers(int)}.
     */
    public List<Mod> modifiers(int level, AttributeConfigResolver.AttributeContext ctx) { return modifiers(level); }

    /** Lifecycle hooks while active. */
    public Behavior behavior() { return Behavior.NONE; }

    /** Minestom's shared 3-operation model. */
    public record Mod(Attribute attribute, AttributeOperation operation, double amount) {}
}
