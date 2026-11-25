package io.github.term4.polyp.mechanics.damage.types.fall;

import org.jetbrains.annotations.Nullable;

/** Snapshot {@code detail} for {@link FallDamage}: distance plus an optional per-landing block modifier. */
public record FallDetail(float distance, @Nullable Float damageModifier) {

    public static FallDetail of(float distance) {
        return new FallDetail(distance, null);
    }
}
