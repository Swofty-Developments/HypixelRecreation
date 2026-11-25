package io.github.term4.polyp.api.event.consume;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.consumable.Consumable;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;

/**
 * The pre-consume gate: fired the moment an enabled consumable use starts, before the {@code canConsume} gate and the
 * use timer. Cancel and the use never begins (no animation, no timer).
 */
public final class PreConsumeEvent extends CancellableMechanicsEvent<ConsumableContext> {

    public PreConsumeEvent(ConsumableContext ctx, Services services) {
        super(ctx, services);
    }

    public Player user() { return finalSnap().user(); }
    public ItemStack item() { return finalSnap().item(); }
    public PlayerHand hand() { return finalSnap().hand(); }
    /** A registered {@link Consumable} or the component-food floor. */
    public Consumable consumable() { return finalSnap().consumable(); }
}
