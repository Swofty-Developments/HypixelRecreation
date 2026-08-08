package io.github.term4.polyp.mechanics.attribute;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * A gameplay attribute a {@link io.github.term4.polyp.mechanics.attribute.source.Source Source} can modify:
 * {@link Vanilla} (Minestom-backed, client-facing, Minestom does the base+modifier math) or {@link Custom} (resolved here).
 *
 * <p>Inside this package the Minestom type is referenced fully-qualified to avoid the clash with this {@code Attribute}.
 */
public sealed interface Attribute permits Attribute.Vanilla, Attribute.Custom {

    Key key();

    record Vanilla(net.minestom.server.entity.attribute.Attribute handle) implements Attribute {
        @Override public Key key() { return handle.key(); }
    }

    record Custom(Key key) implements Attribute {}

    /** The wrapped Minestom attribute, or {@code null} for a {@link Custom} one. */
    default @Nullable net.minestom.server.entity.attribute.Attribute handle() {
        return this instanceof Vanilla v ? v.handle() : null;
    }

    static Attribute of(net.minestom.server.entity.attribute.Attribute handle) { return new Vanilla(handle); }

    static Attribute custom(String id) { return new Custom(Key.key(id)); }

    Attribute ATTACK_DAMAGE = of(net.minestom.server.entity.attribute.Attribute.ATTACK_DAMAGE);
    Attribute ATTACK_SPEED = of(net.minestom.server.entity.attribute.Attribute.ATTACK_SPEED);
    Attribute KNOCKBACK_RESISTANCE = of(net.minestom.server.entity.attribute.Attribute.KNOCKBACK_RESISTANCE);
    Attribute MAX_HEALTH = of(net.minestom.server.entity.attribute.Attribute.MAX_HEALTH);
    Attribute MOVEMENT_SPEED = of(net.minestom.server.entity.attribute.Attribute.MOVEMENT_SPEED);
    Attribute SAFE_FALL_DISTANCE = of(net.minestom.server.entity.attribute.Attribute.SAFE_FALL_DISTANCE);
    Attribute MINING_EFFICIENCY = of(net.minestom.server.entity.attribute.Attribute.MINING_EFFICIENCY);
    Attribute SUBMERGED_MINING_SPEED = of(net.minestom.server.entity.attribute.Attribute.SUBMERGED_MINING_SPEED);
    Attribute WATER_MOVEMENT_EFFICIENCY = of(net.minestom.server.entity.attribute.Attribute.WATER_MOVEMENT_EFFICIENCY);

    Attribute MELEE_FLAT_ADD = custom("polyp:melee_flat_add"); // added after crit
}
