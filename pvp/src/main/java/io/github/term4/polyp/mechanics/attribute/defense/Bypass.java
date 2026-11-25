package io.github.term4.polyp.mechanics.attribute.defense;

import net.kyori.adventure.key.Key;

import java.util.HashSet;
import java.util.Set;

/**
 * A hit's mitigation-bypass spec: which defense stages (or specific attribute/effect/enchant keys within them) the
 * {@link io.github.term4.polyp.mechanics.attribute.AttributeSystem#mitigate pipeline} skips. Two layers
 * that {@link #merge merge}: broad stage flags from the damage TYPE (void skips everything, fall skips armor), and
 * targeted keys from the attacking ITEM (a "god killer" ignoring only {@code minecraft:resistance}).
 * Deliberately not "true damage" - that reads as armor-only.
 */
public final class Bypass {

    public static final Bypass NONE = builder().build();

    private final boolean all;
    private final boolean armor;
    private final boolean effects;
    private final boolean enchants;
    private final Set<Key> attributeKeys;
    private final Set<Key> effectKeys;
    private final Set<Key> enchantKeys;

    private Bypass(Builder b) {
        this.all = b.all;
        this.armor = b.armor;
        this.effects = b.effects;
        this.enchants = b.enchants;
        this.attributeKeys = Set.copyOf(b.attributeKeys);
        this.effectKeys = Set.copyOf(b.effectKeys);
        this.enchantKeys = Set.copyOf(b.enchantKeys);
    }

    /** The caller also checks {@link #attribute} for the armor attribute. */
    public boolean armorStage() { return all || armor; }

    public boolean effectStage() { return all || effects; }

    public boolean enchantStage() { return all || enchants; }

    public boolean attribute(Key key) { return attributeKeys.contains(key); }

    public boolean effect(Key key) { return effectKeys.contains(key); }

    public boolean enchant(Key key) { return enchantKeys.contains(key); }

    /** OR the broad flags, union the targeted sets. */
    public Bypass merge(Bypass o) {
        if (o == null || o == NONE) return this;
        if (this == NONE) return o;
        Builder b = new Builder();
        b.all = all || o.all;
        b.armor = armor || o.armor;
        b.effects = effects || o.effects;
        b.enchants = enchants || o.enchants;
        b.attributeKeys.addAll(attributeKeys); b.attributeKeys.addAll(o.attributeKeys);
        b.effectKeys.addAll(effectKeys); b.effectKeys.addAll(o.effectKeys);
        b.enchantKeys.addAll(enchantKeys); b.enchantKeys.addAll(o.enchantKeys);
        return b.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private boolean all;
        private boolean armor;
        private boolean effects;
        private boolean enchants;
        private final Set<Key> attributeKeys = new HashSet<>();
        private final Set<Key> effectKeys = new HashSet<>();
        private final Set<Key> enchantKeys = new HashSet<>();

        /** Skip every mitigation stage. */
        public Builder all() { return all(true); }
        public Builder all(boolean v) { this.all = v; return this; }
        public Builder armor(boolean v) { this.armor = v; return this; }
        /** Skip the resistance/effect stage. */
        public Builder effects(boolean v) { this.effects = v; return this; }
        /** Skip the EPF/enchant stage. */
        public Builder enchants(boolean v) { this.enchants = v; return this; }
        public Builder attribute(Key... keys) { for (Key k : keys) attributeKeys.add(k); return this; }
        public Builder effect(Key... keys) { for (Key k : keys) effectKeys.add(k); return this; }
        public Builder enchant(Key... keys) { for (Key k : keys) enchantKeys.add(k); return this; }

        public Bypass build() { return new Bypass(this); }
    }
}
