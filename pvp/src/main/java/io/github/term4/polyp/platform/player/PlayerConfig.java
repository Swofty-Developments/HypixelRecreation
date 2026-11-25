package io.github.term4.polyp.platform.player;

import org.jetbrains.annotations.Nullable;

/**
 * Immutable player platform config (per-player server behavior, not combat mechanics). Scoped via
 * {@code MechanicsProfile.player} and applied at spawn (join / instance change) by {@link PlayerConfigApplier}.
 * Plain values only - no {@code FieldValue}/subconfig machinery; these are rarely-changing platform knobs, not
 * per-hit values. Unset ({@code null}) fields are left unmanaged.
 */
public final class PlayerConfig {

    /** Position broadcast interval in ticks (1 = every tick, the Minestom default). */
    public final @Nullable Integer positionBroadcastInterval;

    private PlayerConfig(Builder b) {
        positionBroadcastInterval = b.positionBroadcastInterval;
    }

    public Builder toBuilder() { return new Builder(this); }

    public static Builder builder() { return builder(null); }
    public static Builder builder(@Nullable PlayerConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static final class Builder {
        private @Nullable Integer positionBroadcastInterval;

        Builder() {}

        Builder(PlayerConfig c) {
            positionBroadcastInterval = c.positionBroadcastInterval;
        }

        public Builder positionBroadcastInterval(@Nullable Integer v) { positionBroadcastInterval = v; return this; }

        public PlayerConfig build() { return new PlayerConfig(this); }
    }
}
