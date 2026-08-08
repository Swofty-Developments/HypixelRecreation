package io.github.term4.polyp.mechanics.attribute.source;

import io.github.term4.polyp.Services;
import net.minestom.server.entity.Entity;

/**
 * Generic lifecycle hooks a {@link Source} runs while active on an entity. Domain-agnostic - domain dispatch (e.g. combat
 * on-hit) layers on top in the owning system.
 */
public interface Behavior {

    Behavior NONE = new Behavior() {};

    default void onApply(Entity entity, int level) {}

    default void onRemove(Entity entity, int level) {}

    /** Ticks between {@link #onTick} calls; {@code 0} = never. */
    default int tickInterval(int level) { return 0; }

    default void onTick(Entity entity, int level) {}

    /** {@link #onTick} with the service hub, for behaviors that consume other systems. */
    default void onTick(Services services, Entity entity, int level) { onTick(entity, level); }
}
