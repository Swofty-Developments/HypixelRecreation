package io.github.term4.polyp.mechanics.attack;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.presets.vanilla18.Attack;
import io.github.term4.polyp.api.event.attack.AttackEvent;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.attack.AttackConfigResolver.AttackContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Immutable attack config. Use {@link #builder()}, {@link #toBuilder()}. Minimal by design - the {@link #ruleset}
 * processor owns the attack behavior; this only selects it.
 */
@GenerateBuilder
public final class AttackConfig extends Config<AttackContext, AttackConfig> {

    /** Vanilla 1.8 attacker self-slowdown on a landed sprint/enchant hit ({@code motX/motZ *= 0.6}). */
    public static final double VANILLA_FULL_HIT_SCALE = 0.6;

    public final FieldValue<AttackContext, Boolean> enabled;
    public final FieldValue<AttackContext, AttackEvent.AttackRule.Ruleset> ruleset;
    public final AttackEvent.CriticalRule criticalRule;
    /** Arms the {@link FakeHits} swing fill for attackers in this scope; {@code null} = off (the compat layer may still fill bare fists). */
    public final @Nullable FakeHitConfig fakeHits;
    /**
     * Attacker self-slowdown on their own tracked horizontal velocity; affects only their next knockback's friction
     * fold, never the damage/KB dealt. {@code 1.0} = none; vanilla {@link #VANILLA_FULL_HIT_SCALE}.
     */
    public final FieldValue<AttackContext, Double> fullHitScale;

    private AttackConfig(Builder b) {
        super(b.subConfig);
        enabled = b.enabled;
        ruleset = b.ruleset;
        criticalRule = b.criticalRule;
        fakeHits = b.fakeHits;
        fullHitScale = b.fullHitScale;
    }

    /** Merges this config over base. */
    public AttackConfig fromBase(AttackConfig base) {
        Builder b = new Builder();
        b.mergeKnobs(this, base);
        return b
                .subConfig(subConfig != null ? subConfig : base.subConfig)
                .criticalRule(criticalRule != null ? criticalRule : base.criticalRule)
                .fakeHits(fakeHits != null ? fakeHits : base.fakeHits)
                .build();
    }

    public Builder toBuilder() { return new Builder(this); }

    public static Builder builder() { return builder(null); }
    public static Builder builder(@Nullable AttackConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static AttackConfig defaultConfig() { return builder().build(); }

    public static final class Builder extends AttackConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private Function<AttackContext, AttackConfig> subConfig;
        private AttackEvent.CriticalRule criticalRule;
        private FakeHitConfig fakeHits;

        Builder() {
            enabled = FieldValue.constant(true);
            ruleset = FieldValue.constant(Attack.ruleset());
            criticalRule = null;
            fullHitScale = FieldValue.constant(VANILLA_FULL_HIT_SCALE);
        }

        Builder(AttackConfig c) {
            super(c);
            subConfig = c.subConfig;
            criticalRule = c.criticalRule;
            fakeHits = c.fakeHits;
        }

        public Builder subConfig(Function<AttackContext, AttackConfig> fn) { subConfig = fn; return this; }

        public Builder criticalRule(AttackEvent.CriticalRule v) { criticalRule = v; return this; }

        public Builder fakeHits(@Nullable FakeHitConfig v) { fakeHits = v; return this; }

        public AttackConfig build() { return new AttackConfig(this); }
    }
}
