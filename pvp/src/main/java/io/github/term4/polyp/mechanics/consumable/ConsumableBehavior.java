package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;

/**
 * The pre/during/post hooks of a {@link Consumable} - the open seam for custom consume behavior. No-op by default;
 * resolved per consume as a {@link ConsumableTypeConfig} knob.
 */
public interface ConsumableBehavior {

    /** The default. */
    ConsumableBehavior NONE = new ConsumableBehavior() {};

    default void onStart(ConsumableContext ctx) {}

    /** Each tick while consuming, with the ticks left before completion. */
    default void onUsing(ConsumableContext ctx, int ticksRemaining) {}

    /** The system consumes the item afterward. */
    default void onFinish(ConsumableContext ctx) {}

    /** Released before completion. Vanilla = nothing happens. */
    default void onCancel(ConsumableContext ctx) {}
}
