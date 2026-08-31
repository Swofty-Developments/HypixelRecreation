package io.github.term4.polyp.mechanics.attribute.defense;

/**
 * The specialized armor-enchant protection categories the EPF stage gates on. A {@link io.github.term4.polyp.mechanics.damage.types.DamageType}
 * declares which apply to it; general Protection always applies. Mirrors the vanilla damage-source flags (1.8
 * {@code DamageSource}) / damage-type tags (26 {@code is_fire}/{@code is_fall}/{@code is_explosion}/{@code is_projectile}).
 */
public enum ProtectionCategory {
    /** Fire Protection. */
    FIRE,
    /** Feather Falling. */
    FALL,
    /** Blast Protection. */
    EXPLOSION,
    /** Projectile Protection. */
    PROJECTILE
}
