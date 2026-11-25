package io.github.term4.polyp.mechanics.attribute.defense;

import io.github.term4.polyp.item.Enchants;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.ProtectionEnchant;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.Set;

/**
 * The EPF / Protection defense stage: reduces incoming damage by the {@link ProtectionEnchant} factor summed off the
 * victim's four armor pieces. General Protection always counts; the specialized enchants only when the damage type
 * declares their {@link ProtectionCategory category}.
 *
 * <p>The versions diverge: {@link Formula#LEGACY_RANDOMIZED 1.8} floors {@code (6+lvl²)/3 × typeMult} per enchant, clamps
 * the sum to {@code [0,25]}, then <em>randomizes</em> it ({@code EnchantmentManager.a}) before the {@code ×(25−i)/25}
 * reduction; {@link Formula#MODERN_LINEAR 26} sums a flat {@code lvl × perLevel}, clamps to {@code [0,20]}, and applies
 * {@code ×(1−epf/25)} deterministically.
 */
public final class ProtectionConfig {

    public enum Formula {
        /** 1.8 {@code EnchantmentManager.a} + {@code applyMagicModifier}: floored per-piece terms, randomized roll. */
        LEGACY_RANDOMIZED,
        /** 26 {@code EnchantmentHelper.getDamageProtection} + {@code CombatRules.getDamageAfterMagicAbsorb}: linear, deterministic. */
        MODERN_LINEAR
    }

    private final @Nullable Boolean enabled;
    private final @Nullable Formula formula;

    private ProtectionConfig(Builder b) {
        this.enabled = b.enabled;
        this.formula = b.formula;
    }

    /** Default {@code true}. */
    public boolean enabled() { return enabled == null || enabled; }

    /** Default {@link Formula#LEGACY_RANDOMIZED}. */
    public Formula formula() { return formula != null ? formula : Formula.LEGACY_RANDOMIZED; }

    /** {@code random} drives the LEGACY roll; MODERN ignores it. */
    public float damageAfterProtection(LivingEntity victim, Set<ProtectionCategory> categories, float damage, Random random, Bypass bypass) {
        if (damage <= 0) return damage;
        Bypass b = bypass != null ? bypass : Bypass.NONE;
        return switch (formula()) {
            case LEGACY_RANDOMIZED -> legacy(victim, categories, damage, random, b);
            case MODERN_LINEAR -> modern(victim, categories, damage, b);
        };
    }

    /** 1.8: per-piece {@code floor((6+lvl²)/3 × mult)} summed, clamped [0,25], randomized, clamped 20, then {@code ×(25−i)/25}. */
    private static float legacy(LivingEntity victim, Set<ProtectionCategory> categories, float damage, Random random, Bypass bypass) {
        int raw = sumEpf(victim, categories, bypass, ProtectionEnchant::legacyPerPiece);
        if (raw <= 0) return damage;
        return applyLegacy(damage, legacyRoll(Math.min(raw, 25), random));
    }

    /** 26: per-piece {@code lvl × perLevel} summed, clamped [0,20], then {@code ×(1−epf/25)}. */
    private static float modern(LivingEntity victim, Set<ProtectionCategory> categories, float damage, Bypass bypass) {
        return applyModern(damage, sumEpf(victim, categories, bypass, ProtectionEnchant::modernPerPiece));
    }

    private static int sumEpf(LivingEntity victim, Set<ProtectionCategory> categories, Bypass bypass, PerPiece perPiece) {
        int sum = 0;
        for (EquipmentSlot slot : EquipmentSlot.armors()) {
            ItemStack piece = victim.getEquipment(slot);
            for (ProtectionEnchant p : ProtectionEnchant.values()) {
                if (!p.applies(categories) || bypass.enchant(p.key())) continue;
                sum += perPiece.of(p, Enchants.level(piece, p.key()));
            }
        }
        return sum;
    }

    @FunctionalInterface
    private interface PerPiece {
        int of(ProtectionEnchant enchant, int level);
    }

    /** 1.8 {@code EnchantmentManager.a} roll over the (already [0,25]-clamped) raw EPF: {@code (raw+1>>1) + rand[0, raw>>1]}. */
    public static int legacyRoll(int rawClamped, Random random) {
        return (rawClamped + 1 >> 1) + random.nextInt((rawClamped >> 1) + 1);
    }

    /** 1.8 {@code applyMagicModifier} tail: clamp the rolled EPF to 20, then {@code damage × (25 − i)/25}. */
    public static float applyLegacy(float damage, int rolledEpf) {
        int i = Math.min(rolledEpf, 20);
        return i > 0 ? damage * (float) (25 - i) / 25.0F : damage;
    }

    /** 26 {@code CombatRules.getDamageAfterMagicAbsorb}: {@code damage × (1 − clamp(epf,0,20)/25)}. */
    public static float applyModern(float damage, float epfSum) {
        float real = Math.max(0.0F, Math.min(20.0F, epfSum));
        return damage * (1.0F - real / 25.0F);
    }

    /** Merges this over {@code base}: set fields win. */
    public ProtectionConfig fromBase(ProtectionConfig base) {
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

        public ProtectionConfig build() { return new ProtectionConfig(this); }
    }
}
