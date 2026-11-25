package io.github.term4.polyp.mechanics.attribute.defense;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * The armor defense stage: reduces incoming damage by the victim's {@code ARMOR} (plus {@code ARMOR_TOUGHNESS} in 26).
 * Points come from the Minestom attribute and are identical across versions; the version is the {@link Formula}.
 * Per-type opt-out is the damage side's {@code bypassArmor} flag.
 */
public final class ArmorConfig {

    public enum Formula {
        /** 1.8 {@code applyArmorModifier}: {@code damage × (25 − armor) / 25} (no toughness). */
        LEGACY_LINEAR,
        /** 26 {@code CombatRules.getDamageAfterAbsorb}: {@code damage × (1 − clamp(armor − damage/(2+toughness/4), armor×0.2, 20)/25)}. */
        MODERN_TOUGHNESS
    }

    private final @Nullable Boolean enabled;
    private final @Nullable Formula formula;

    private ArmorConfig(Builder b) {
        this.enabled = b.enabled;
        this.formula = b.formula;
    }

    /** Default {@code true}. */
    public boolean enabled() { return enabled == null || enabled; }

    /** Default {@link Formula#LEGACY_LINEAR}. */
    public Formula formula() { return formula != null ? formula : Formula.LEGACY_LINEAR; }

    public float damageAfterArmor(LivingEntity victim, float damage) {
        if (damage <= 0) return damage;
        int armor = (int) victim.getAttributeValue(Attribute.ARMOR);
        return switch (formula()) {
            case LEGACY_LINEAR -> legacy(damage, armor);
            case MODERN_TOUGHNESS -> modern(damage, armor, (float) victim.getAttributeValue(Attribute.ARMOR_TOUGHNESS));
        };
    }

    /** 1.8 {@code applyArmorModifier}, float-exact. */
    public static float legacy(float damage, int armor) {
        return damage * (float) (25 - armor) / 25.0F;
    }

    /** 26 {@code CombatRules.getDamageAfterAbsorb}, float-exact; the weapon-side Breach effect is omitted. */
    public static float modern(float damage, int armor, float toughness) {
        float armorF = armor;
        float f = 2.0F + toughness / 4.0F;
        float g = clamp(armorF - damage / f, armorF * 0.2F, 20.0F);
        return damage * (1.0F - g / 25.0F);
    }

    /** Vanilla {@code Mth.clamp}. */
    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Merges this over {@code base}: set fields win. */
    public ArmorConfig fromBase(ArmorConfig base) {
        Builder b = new Builder();
        b.enabled = enabled != null ? enabled : base.enabled;
        b.formula = formula != null ? formula : base.formula;
        return b.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Boolean enabled;
        private Formula formula;

        public Builder enabled(Boolean v) { enabled = v; return this; }
        public Builder formula(Formula v) { formula = v; return this; }

        public ArmorConfig build() { return new ArmorConfig(this); }
    }
}
