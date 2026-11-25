package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.attribute.defense.ProtectionCategory;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The five vanilla armor protection enchants - the EPF contributors. Each carries its key, the
 * {@link ProtectionCategory category} that gates it ({@code null} = general "all"), and the per-version per-piece term.
 * Definitions only; the aggregation lives in {@link io.github.term4.polyp.mechanics.attribute.defense.ProtectionConfig}.
 */
public enum ProtectionEnchant {
    PROTECTION(Key.key("minecraft:protection"), null, 0.75F, 1),
    FIRE_PROTECTION(Key.key("minecraft:fire_protection"), ProtectionCategory.FIRE, 1.25F, 2),
    FEATHER_FALLING(Key.key("minecraft:feather_falling"), ProtectionCategory.FALL, 2.5F, 3),
    BLAST_PROTECTION(Key.key("minecraft:blast_protection"), ProtectionCategory.EXPLOSION, 1.5F, 2),
    PROJECTILE_PROTECTION(Key.key("minecraft:projectile_protection"), ProtectionCategory.PROJECTILE, 1.5F, 2);

    private final Key key;
    private final @Nullable ProtectionCategory category;
    private final float legacyMult;   // 1.8 EnchantmentProtection per-type multiplier
    private final int modernPerLevel; // 26 damage_protection linear add per level

    ProtectionEnchant(Key key, @Nullable ProtectionCategory category, float legacyMult, int modernPerLevel) {
        this.key = key;
        this.category = category;
        this.legacyMult = legacyMult;
        this.modernPerLevel = modernPerLevel;
    }

    public Key key() { return key; }

    /** {@code null} for general Protection. */
    public @Nullable ProtectionCategory category() { return category; }

    /** General always applies; specialized only when its category is present. */
    public boolean applies(Set<ProtectionCategory> categories) {
        return category == null || categories.contains(category);
    }

    /** 1.8 {@code EnchantmentProtection.a}: {@code floor((6 + lvl²)/3 × typeMult)}, float math. */
    public int legacyPerPiece(int level) {
        if (level <= 0) return 0;
        float f = (float) (6 + level * level) / 3.0F * legacyMult;
        return (int) Math.floor(f);
    }

    /** 26 {@code damage_protection}: flat {@code lvl × perLevel}. */
    public int modernPerPiece(int level) {
        return level <= 0 ? 0 : level * modernPerLevel;
    }
}
