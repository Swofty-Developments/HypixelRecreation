package io.github.term4.polyp.api.event.consume;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.consumable.Consumable;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

/** Fired after the behavior ran and the item was consumed - informational. */
public final class ConsumeAppliedEvent implements Event {

    private final ConsumableContext snapshot;
    private final Services services;

    public ConsumeAppliedEvent(ConsumableContext snapshot, Services services) {
        this.snapshot = snapshot;
        this.services = services;
    }

    public ConsumableContext snapshot() { return snapshot; }
    public Services services() { return services; }

    public Player user() { return snapshot.user(); }
    public ItemStack item() { return snapshot.item(); }
    public Consumable consumable() { return snapshot.consumable(); }
}
