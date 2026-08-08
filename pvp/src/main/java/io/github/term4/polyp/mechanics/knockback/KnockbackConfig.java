package io.github.term4.polyp.mechanics.knockback;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfigResolver.KnockbackContext;
import io.github.term4.polyp.tracking.motion.VelocityRule;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Immutable knockback config. Use {@link #builder()}, {@link #toBuilder()}. */
@GenerateBuilder
public final class KnockbackConfig extends Config<KnockbackContext, KnockbackConfig> {

    /** How the position-based and look-based direction inputs combine. */
    public enum DirectionMode {
        /** Weighted blend of the unit directions, re-normalized; the strength stays the configured magnitude. */
        SCALAR,
        /** Weighted vector sum; the resulting length IS the strength (vanilla 1.8 base+extra addition). */
        VECTOR_ADDITION
    }
    /** How a friction value is applied to the reconstructed victim velocity in the friction term. */
    public enum FrictionMode {
        /** {@code mot / value} - the value is a divisor (default; vanilla-style). */
        DIVISOR,
        /** {@code mot * value} - the value is a multiplier, so coefficients (incl. negatives) read directly. */
        FACTOR
    }

    /** Replaces the friction stage (default {@link KnockbackCalculator#vanillaFriction}: {@code kb + victimVel * coeff} per axis). All vectors b/t. */
    @FunctionalInterface
    public interface FrictionRule {
        Vec apply(KnockbackConfigResolver.KnockbackContext ctx, Vec victimVelocity, Vec kb,
                  KnockbackConfigResolver.ResolvedKnockbackConfig cfg);
    }

    /** Replaces the base+extra combination stage (default {@link KnockbackCalculator#vanillaCombine}: per-{@link DirectionMode} addition). */
    @FunctionalInterface
    public interface CombineRule {
        Vec apply(KnockbackConfigResolver.KnockbackContext ctx, Vec base, Vec extra,
                  KnockbackConfigResolver.ResolvedKnockbackConfig cfg);
    }

    /** Replaces the bounds stage (default {@link KnockbackCalculator#vanillaBounds}). Runs once after the base fold
     *  ({@code afterExtra} false) and, when an extra was added, once more after it ({@code afterExtra} true). */
    @FunctionalInterface
    public interface BoundsRule {
        Vec apply(KnockbackConfigResolver.KnockbackContext ctx, Vec kb, boolean afterExtra,
                  KnockbackConfigResolver.ResolvedKnockbackConfig cfg);
    }

    /** Lower and upper bound for knockback components. Null means no bound. */
    public record Bounds(@Nullable Double lower, @Nullable Double upper) {
        public static Bounds of(@Nullable Double lower, @Nullable Double upper) {
            return new Bounds(lower, upper);
        }
    }

    /** Recent-sprint window (ticks) for the melee sprint {@code +1} extra level; {@code 0} = live sprint state only. */
    public final FieldValue<KnockbackContext, Integer> sprintBuffer;
    /** Base horizontal strength (b/t); vanilla 1.8 hurt-KB {@code 0.4}. */
    public final FieldValue<KnockbackContext, Double> horizontal;
    /** Base vertical strength (b/t); vanilla 1.8 {@code 0.4}. */
    public final FieldValue<KnockbackContext, Double> vertical;
    /** Extra (sprint/enchant) horizontal strength per level; vanilla 1.8 {@code 0.5}. */
    public final FieldValue<KnockbackContext, Double> extraHorizontal;
    /** Extra vertical strength (level-independent); vanilla 1.8 {@code 0.1}. */
    public final FieldValue<KnockbackContext, Double> extraVertical;
    /** Clamp on the folded base horizontal magnitude; {@code null} = none. */
    public final FieldValue<KnockbackContext, Bounds> horizontalBounds;
    /** Clamp on the folded base vertical; vanilla 1.8 caps at {@code 0.4f}. */
    public final FieldValue<KnockbackContext, Bounds> verticalBounds;
    /** Clamp applied after the extra is added (horizontal magnitude); {@code null} = none. */
    public final FieldValue<KnockbackContext, Bounds> extraHorizontalBounds;
    /** Clamp applied after the extra is added (vertical); {@code null} = none. */
    public final FieldValue<KnockbackContext, Bounds> extraVerticalBounds;
    /** Horizontal direction weight: {@code 0} = away from the attacker (position), {@code 1} = along the attacker's look. */
    public final FieldValue<KnockbackContext, Double> yawWeight;
    /** {@link #yawWeight} for the extra component; vanilla 1.8 extra is pure look ({@code 1.0}). */
    public final FieldValue<KnockbackContext, Double> extraYawWeight;
    /** Vertical direction weight from the attacker's pitch; vanilla {@code 0} (vertical is a flat add). */
    public final FieldValue<KnockbackContext, Double> pitchWeight;
    /** {@link #pitchWeight} for the extra component. */
    public final FieldValue<KnockbackContext, Double> extraPitchWeight;
    /** Vertical direction weight from the attacker-victim height difference; vanilla {@code 0}. */
    public final FieldValue<KnockbackContext, Double> heightDelta;
    /** {@link #heightDelta} for the extra component. */
    public final FieldValue<KnockbackContext, Double> extraHeightDelta;
    /** How the horizontal direction inputs combine; vanilla 1.8 = {@link DirectionMode#VECTOR_ADDITION}. */
    public final FieldValue<KnockbackContext, DirectionMode> horizontalCombine;
    /** How the vertical direction inputs combine; vanilla 1.8 = {@link DirectionMode#SCALAR}. */
    public final FieldValue<KnockbackContext, DirectionMode> verticalCombine;
    /** Horizontal friction term on the victim's reconstructed velocity (per {@link #frictionModeH}); vanilla 1.8 halves ({@code DIVISOR 2}). */
    public final FieldValue<KnockbackContext, Double> frictionH;
    /** Vertical friction term (per {@link #frictionModeV}); vanilla 1.8 halves ({@code DIVISOR 2}). */
    public final FieldValue<KnockbackContext, Double> frictionV;
    public final FieldValue<KnockbackContext, FrictionMode> frictionModeH;
    public final FieldValue<KnockbackContext, FrictionMode> frictionModeV;
    /**
     * How the friction term estimates the victim's velocity (see {@link VelocityRule}). Resolution is config override
     * -&gt; the victim's scoped profile velocity -&gt; {@link VelocityRule#DEFAULT}; prefer setting it once on a profile
     * scope. Custom {@link KnockbackComponent}s read the resolved rule via {@code ctx.victimVelocity()}.
     */
    public final FieldValue<KnockbackContext, VelocityRule> velocity;
    /**
     * Quantize the outgoing velocity to 1.8's wire grid (see {@link io.github.term4.polyp.tracking.motion.LegacyVelocity}).
     * A server-emulation knob: emulating 1.8 sends what a 1.8 server would, to all clients. Unset = enabled; disable for modern presets.
     */
    public final FieldValue<KnockbackContext, Boolean> quantizeVelocity;
    /**
     * Per-axis cap (b/t) for the quantized 1.8 wire velocity (see {@link io.github.term4.polyp.tracking.motion.LegacyVelocity}).
     * Only applies while {@link #quantizeVelocity} is on; unset = {@link io.github.term4.polyp.tracking.motion.LegacyVelocity#DEFAULT_CAP}
     * (vanilla 1.8's {@code +-3.9}). The 1.8 wire saturates near {@code +-4.0} b/t.
     */
    public final FieldValue<KnockbackContext, Double> velocityCap;
    /**
     * Whether an airborne victim gets vertical knockback. {@code true} (1.8): always lifts. {@code false} (26.1): an
     * off-ground victim keeps its {@code motY} (the anti-juggle rule), gated on the reconstructed onGround; horizontal is unaffected.
     */
    public final FieldValue<KnockbackContext, Boolean> airborneVertical;
    /**
     * Pluggable transforms applied in order on the final knockback vector (b/t), after all base/extra/friction/bounds
     * logic. Each {@link KnockbackComponent} self-gates and may apply non-linear logic the base pipeline can't.
     * TODO(stages): components are post-stages only; replacing a built-in stage's formula will become per-stage knobs (see the KnockbackCalculator stages TODO).
     */
    @Nullable public final List<KnockbackComponent> customComponents;
    /** Stage replacement: the friction fold; {@code null} = vanilla math. */
    @Nullable public final FrictionRule frictionRule;
    /** Stage replacement: the base+extra combination; {@code null} = vanilla math. */
    @Nullable public final CombineRule combineRule;
    /** Stage replacement: the bounds application; {@code null} = the configured {@link Bounds} clamps. */
    @Nullable public final BoundsRule boundsRule;
    /** The full pipeline stage list ({@link KnockbackPipeline#vanilla()} to seed edits); {@code null} = vanilla order. */
    @Nullable public final List<KnockbackPipeline.Stage> stages;
    /**
     * <b>Experimental.</b> Source the look-weighted knockback direction ({@link #yawWeight} / {@link #extraYawWeight} -
     * e.g. the vanilla sprint / Knockback-enchant extra) from the attacker's CLICK look instead of the one-packet-stale
     * server look. The attack packet carries no rotation and lands a beat before the same-tick flying packet on EVERY
     * client version (modern included), so that component normally fires along the previous tick's look; enabling this
     * applies only that tick's look before the attack, leaving the position-delta base untouched. NOT vanilla (real
     * servers keep the lag), so no preset sets it. {@code null}/false = off. Read context-free at the packet layer
     * (implemented by {@code AttackAimSync}).
     */
    @Nullable public final Boolean experimentalAimSync;

    private KnockbackConfig(Builder b) {
        super(b.subConfig);
        sprintBuffer = b.sprintBuffer;
        horizontal = b.horizontal;
        vertical = b.vertical;
        extraHorizontal = b.extraHorizontal;
        extraVertical = b.extraVertical;
        horizontalBounds = b.horizontalBounds;
        verticalBounds = b.verticalBounds;
        extraHorizontalBounds = b.extraHorizontalBounds;
        extraVerticalBounds = b.extraVerticalBounds;
        yawWeight = b.yawWeight;
        extraYawWeight = b.extraYawWeight;
        pitchWeight = b.pitchWeight;
        extraPitchWeight = b.extraPitchWeight;
        heightDelta = b.heightDelta;
        extraHeightDelta = b.extraHeightDelta;
        horizontalCombine = b.horizontalCombine;
        verticalCombine = b.verticalCombine;
        frictionH = b.frictionH;
        frictionV = b.frictionV;
        frictionModeH = b.frictionModeH;
        frictionModeV = b.frictionModeV;
        velocity = b.velocity;
        quantizeVelocity = b.quantizeVelocity;
        velocityCap = b.velocityCap;
        airborneVertical = b.airborneVertical;
        customComponents = b.customComponents;
        frictionRule = b.frictionRule;
        combineRule = b.combineRule;
        boundsRule = b.boundsRule;
        stages = b.stages;
        experimentalAimSync = b.experimentalAimSync;
    }

    /** Merges this config over base. */
    public KnockbackConfig fromBase(KnockbackConfig base) {
        Builder b = new Builder();
        b.subConfig(subConfig != null ? subConfig : base.subConfig);
        b.mergeKnobs(this, base);
        b.customComponents(customComponents != null ? customComponents : base.customComponents);
        b.frictionRule(frictionRule != null ? frictionRule : base.frictionRule);
        b.combineRule(combineRule != null ? combineRule : base.combineRule);
        b.boundsRule(boundsRule != null ? boundsRule : base.boundsRule);
        b.stages(stages != null ? stages : base.stages);
        b.experimentalAimSync = experimentalAimSync != null ? experimentalAimSync : base.experimentalAimSync;
        return b.build();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(@Nullable KnockbackConfig base) {
        return base != null ? new Builder(base) : new Builder();
    }

    public static final class Builder extends KnockbackConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }

        private Function<KnockbackContext, KnockbackConfig> subConfig;
        private List<KnockbackComponent> customComponents;
        private FrictionRule frictionRule;
        private CombineRule combineRule;
        private BoundsRule boundsRule;
        private List<KnockbackPipeline.Stage> stages;
        private @Nullable Boolean experimentalAimSync;

        Builder() {}

        Builder(KnockbackConfig c) {
            super(c);
            subConfig = c.subConfig;
            customComponents = c.customComponents;
            frictionRule = c.frictionRule;
            combineRule = c.combineRule;
            boundsRule = c.boundsRule;
            stages = c.stages;
            experimentalAimSync = c.experimentalAimSync;
        }

        public Builder subConfig(Function<KnockbackContext, KnockbackConfig> fn) { subConfig = fn; return this; }
        public Builder horizontalBounds(@Nullable Double lower, @Nullable Double upper) { horizontalBounds = FieldValue.constant(Bounds.of(lower, upper)); return this; }
        public Builder verticalBounds(@Nullable Double lower, @Nullable Double upper) { verticalBounds = FieldValue.constant(Bounds.of(lower, upper)); return this; }
        public Builder extraHorizontalBounds(@Nullable Double lower, @Nullable Double upper) { extraHorizontalBounds = FieldValue.constant(Bounds.of(lower, upper)); return this; }
        public Builder extraVerticalBounds(@Nullable Double lower, @Nullable Double upper) { extraVerticalBounds = FieldValue.constant(Bounds.of(lower, upper)); return this; }
        /** Appends to the current/inherited components (copying first, so shared lists aren't mutated). */
        public Builder addCustomComponent(KnockbackComponent component) {
            List<KnockbackComponent> list = customComponents == null ? new ArrayList<>() : new ArrayList<>(customComponents);
            list.add(component);
            customComponents = list;
            return this;
        }
        public Builder frictionRule(FrictionRule v) { frictionRule = v; return this; }
        public Builder combineRule(CombineRule v) { combineRule = v; return this; }
        public Builder boundsRule(BoundsRule v) { boundsRule = v; return this; }
        /** Replaces the whole stage list (order included); seed from {@link KnockbackPipeline#vanilla()}. */
        public Builder stages(List<KnockbackPipeline.Stage> v) { stages = v; return this; }
        /** <b>Experimental</b>, off by default: source the look-weighted knockback from the click look (see the field doc). */
        public Builder experimentalAimSync(boolean v) { experimentalAimSync = v; return this; }
        Builder customComponents(List<KnockbackComponent> v) { customComponents = v; return this; }

        public KnockbackConfig build() {
            return new KnockbackConfig(this);
        }
    }
}
