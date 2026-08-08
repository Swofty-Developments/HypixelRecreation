package io.github.term4.polyp.mechanics.blocking;

import io.github.term4.polyp.mechanics.blocking.BlockingConfigResolver.BlockingContext;
import io.github.term4.polyp.mechanics.blocking.BlockingConfigResolver.ResolvedBlocking;

/**
 * <em>How</em> blocking works for an item - the blocking system's central abstraction (the {@code ProjectileBehavior} /
 * {@code ConsumableBehavior} idiom). An item maps to a behavior via the scope {@link BlockingConfig}. Sword vs shield are
 * two behaviors, not two item types; a server can map any material to {@link #SWORD}, {@link #SHIELD}, or a custom one.
 *
 * <p>Reduction math is shared ({@link #reduced}); behaviors differ in <em>applicability</em> - the 1.8 sword blocks any
 * non-armor-bypassing hit from any direction, the modern shield gates on a frontal arc, use-delay and per-type bypasses.
 */
@FunctionalInterface
public interface BlockingBehavior {

    /** The reduced damage for this hit (or {@code amount} unchanged when the block doesn't apply), given the resolved knobs. */
    float apply(BlockingContext ctx, ResolvedBlocking cfg, float amount);

    /**
     * Shared vanilla reduction: damage taken {@code = amount - clamp(base + factor*amount, 0, amount)}. The clamp keeps a
     * block from ever increasing damage. 1.8 sword = {@code base -0.5, factor 0.5} (the {@code (1+f)*0.5} curve); a
     * full-blocking shield = {@code base 0, factor 1.0}.
     */
    static float reduced(float amount, double base, double factor) {
        double removed = Math.max(0.0, Math.min(base + factor * amount, amount));
        return (float) (amount - removed);
    }

    /**
     * 1.8 sword block: omnidirectional, instant. Blocks any hit whose damage type doesn't bypass armor (vanilla
     * {@code !ignoresArmor}), reduced by {@link #reduced} (1.8 uses {@code base -0.5, factor 0.5}).
     */
    BlockingBehavior SWORD = (ctx, cfg, amount) ->
            ctx.bypassesArmor() ? amount : reduced(amount, cfg.reductionBase(), cfg.reductionFactor());

    /**
     * Modern shield block: active only after the use-delay, blocks within a frontal arc, and skips damage types the item
     * is bypassed by; otherwise reduced by {@link #reduced} (default full block).
     */
    BlockingBehavior SHIELD = (ctx, cfg, amount) -> {
        if (ctx.useTicks() < cfg.blockDelayTicks()) return amount;
        if (cfg.bypassedTypes().contains(ctx.type().key())) return amount;
        Double angle = cfg.blockingAngle();
        if (angle != null && ctx.attackerAngleDegrees() > angle) return amount;
        return reduced(amount, cfg.reductionBase(), cfg.reductionFactor());
    };
}
