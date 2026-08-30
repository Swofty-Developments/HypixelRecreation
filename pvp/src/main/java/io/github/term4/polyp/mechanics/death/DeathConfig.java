package io.github.term4.polyp.mechanics.death;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Config for the death/respawn cleanup. Assigned per scope via the
 * {@link io.github.term4.polyp.MechanicsProfile} {@code DEATH} member and resolved per-victim by
 * {@code DamageSystem} on the death path. An unset knob reads as its vanilla default (on / 20-tick animation).
 */
@GenerateBuilder
public final class DeathConfig extends Config<DeathConfig.DeathContext, DeathConfig> {

    public record DeathContext(LivingEntity victim) {}

    public final @Nullable FieldValue<DeathContext, Boolean> clearEffects;
    public final @Nullable FieldValue<DeathContext, Boolean> resetMechanicsState;
    public final @Nullable FieldValue<DeathContext, Boolean> hideCorpse;
    public final @Nullable FieldValue<DeathContext, Integer> deathAnimationTicks;

    private DeathConfig(Builder b) {
        super(b.subConfig);
        this.clearEffects = b.clearEffects;
        this.resetMechanicsState = b.resetMechanicsState;
        this.hideCorpse = b.hideCorpse;
        this.deathAnimationTicks = b.deathAnimationTicks;
    }

    /** Clear active potion effects on death (Minestom's {@code kill()} doesn't). Unset = on. */
    public @Nullable Boolean clearEffects(DeathContext ctx) { return resolve(clearEffects, ctx); }

    /** Reset fire, velocity, drowning air, and stuck arrows on death. Unset = on. */
    public @Nullable Boolean resetMechanicsState(DeathContext ctx) { return resolve(resetMechanicsState, ctx); }

    /** Hide the health-0 body from viewers until respawn (Minestom keeps broadcasting it). Unset = on. */
    public @Nullable Boolean hideCorpse(DeathContext ctx) { return resolve(hideCorpse, ctx); }

    /** Ticks the death animation plays before {@link #hideCorpse} removes the body. Unset = 20. */
    public @Nullable Integer deathAnimationTicks(DeathContext ctx) { return resolve(deathAnimationTicks, ctx); }

    /** Merges this config over {@code base}. */
    public DeathConfig fromBase(DeathConfig base) {
        Builder b = new Builder();
        b.mergeKnobs(this, base);
        b.subConfig = subConfig != null ? subConfig : base.subConfig;
        return b.build();
    }

    public Builder toBuilder() { return new Builder(this); }
    public static Builder builder() { return new Builder(); }
    public static Builder builder(@Nullable DeathConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static final class Builder extends DeathConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private Function<DeathContext, DeathConfig> subConfig;

        Builder() {}

        Builder(DeathConfig c) {
            super(c);
            subConfig = c.subConfig;
        }

        public Builder subConfig(Function<DeathContext, DeathConfig> fn) { subConfig = fn; return this; }

        public DeathConfig build() { return new DeathConfig(this); }
    }
}
