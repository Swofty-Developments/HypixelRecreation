package io.github.term4.polyp.mechanics.knockback;

import io.github.term4.polyp.tracking.motion.MotionTracker;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The staged knockback pipeline: the calculator assembles the inputs ({@link State}) and runs the config's stage
 * list ({@code KnockbackConfig.stages}; {@code null} = {@link #vanilla()}). Users replace, insert, remove or reorder
 * stages freely; the built-in stages consult the per-stage rule knobs ({@code frictionRule} etc.), so overriding one
 * stage's MATH doesn't require touching the list. All vectors b/t.
 */
public final class KnockbackPipeline {

    private KnockbackPipeline() {}

    /** One pipeline stage; reads and mutates {@link State#kb}. */
    @FunctionalInterface
    public interface Stage {
        void apply(State state);
    }

    /** Mutable state threaded through the stages; {@link #kb} starts as {@link #base}. */
    public static final class State {
        public final KnockbackConfigResolver.KnockbackContext ctx;
        public final KnockbackConfigResolver.ResolvedKnockbackConfig cfg;
        /** The victim's reconstructed velocity (b/t) - what the friction stage folds. */
        public final Vec victimVelocity;
        /** The assembled base vector (direction x strength). */
        public final Vec base;
        /** The assembled extra vector (sprint/enchant, already level-scaled), or {@code null} when the level is 0. */
        public final @Nullable Vec extra;
        public final int extraLevel;
        public Vec kb;

        public State(KnockbackConfigResolver.KnockbackContext ctx, KnockbackConfigResolver.ResolvedKnockbackConfig cfg,
                     Vec victimVelocity, Vec base, @Nullable Vec extra, int extraLevel) {
            this.ctx = ctx;
            this.cfg = cfg;
            this.victimVelocity = victimVelocity;
            this.base = base;
            this.extra = extra;
            this.extraLevel = extraLevel;
            this.kb = base;
        }
    }

    /** Friction fold ({@code frictionRule} else {@link KnockbackCalculator#vanillaFriction}). */
    public static final Stage FRICTION = s -> s.kb = s.cfg.frictionRule() != null
            ? s.cfg.frictionRule().apply(s.ctx, s.victimVelocity, s.kb, s.cfg)
            : KnockbackCalculator.vanillaFriction(s.victimVelocity, s.kb, s.cfg);

    /** Base bounds pass ({@code boundsRule} else the configured {@code horizontal/verticalBounds} clamps). */
    public static final Stage BASE_BOUNDS = s -> s.kb = s.cfg.boundsRule() != null
            ? s.cfg.boundsRule().apply(s.ctx, s.kb, false, s.cfg)
            : KnockbackCalculator.vanillaBounds(s.kb, false, s.cfg);

    /** Folds the extra vector in ({@code combineRule} else {@link KnockbackCalculator#vanillaCombine}); no-op without an extra. */
    public static final Stage COMBINE_EXTRA = s -> {
        if (s.extra == null) return;
        s.kb = s.cfg.combineRule() != null
                ? s.cfg.combineRule().apply(s.ctx, s.kb, s.extra, s.cfg)
                : KnockbackCalculator.vanillaCombine(s.kb, s.extra, s.cfg);
    };

    /** Extra bounds pass; no-op without an extra. */
    public static final Stage EXTRA_BOUNDS = s -> {
        if (s.extra == null) return;
        s.kb = s.cfg.boundsRule() != null
                ? s.cfg.boundsRule().apply(s.ctx, s.kb, true, s.cfg)
                : KnockbackCalculator.vanillaBounds(s.kb, true, s.cfg);
    };

    /** Runs the config's {@link KnockbackComponent}s in order (append-only transforms within this stage). */
    public static final Stage COMPONENTS = s -> {
        if (s.cfg.customComponents() == null) return;
        for (KnockbackComponent comp : s.cfg.customComponents()) {
            Vec out = comp.apply(s.ctx, s.kb);
            if (out != null) s.kb = out;
        }
    };

    /** 26.1 anti-juggle: an airborne victim keeps its own motY ({@code airborneVertical} false); 1.8 always lifts. */
    public static final Stage AIRBORNE_GATE = s -> {
        if (!s.cfg.airborneVertical() && !MotionTracker.onGround(s.ctx.snap().target())) {
            s.kb = new Vec(s.kb.x(), s.victimVelocity.y(), s.kb.z());
        }
    };

    /** The vanilla stage order, as a fresh mutable list - edit it to insert/remove/reorder. */
    public static List<Stage> vanilla() {
        return new ArrayList<>(VANILLA);
    }

    /** Runs {@code stages} (or {@link #vanilla()} when {@code null}) over {@code state}; returns the final vector. */
    public static Vec run(@Nullable List<Stage> stages, State state) {
        for (Stage stage : stages != null ? stages : VANILLA) stage.apply(state);
        return state.kb;
    }

    private static final List<Stage> VANILLA = List.of(FRICTION, BASE_BOUNDS, COMBINE_EXTRA, EXTRA_BOUNDS, COMPONENTS, AIRBORNE_GATE);
}
