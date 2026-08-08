package io.github.term4.polyp.api.event.consume;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.consumable.Consumable;
import io.github.term4.polyp.mechanics.consumable.ConsumableBehavior;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The consume main phase: fired when a use completes, before the behavior runs and the item is consumed. Cancel and
 * nothing applies (no effects, no restore, no item decrement).
 */
public final class ConsumeEvent extends CancellableMechanicsEvent<ConsumableContext> {

    private @Nullable ConsumableBehavior behavior;

    public ConsumeEvent(ConsumableContext ctx, Services services) {
        super(ctx, services);
    }

    public Player user() { return finalSnap().user(); }
    public ItemStack item() { return finalSnap().item(); }
    public PlayerHand hand() { return finalSnap().hand(); }
    public Consumable consumable() { return finalSnap().consumable(); }

    /** Per-consume override ({@code null} = the resolved behavior). */
    public @Nullable ConsumableBehavior behavior() { return behavior; }
    public void behavior(@Nullable ConsumableBehavior behavior) { this.behavior = behavior; }
}
