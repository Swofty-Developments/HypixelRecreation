package io.github.term4.polyp.util.tick;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;

/**
 * One dispatch pass: the pass's world, its just-advanced clock, live server TPS, and whether an external world
 * ticker is driving it ({@link TickSystem#tickWorld}).
 */
public record TickContext(MechanicsWorld world, long tick, int serverTps, boolean external) {

    public Instance instance() {
        return world.instance();
    }

    /**
     * Whether this pass owns {@code entity}'s work: the main pass takes every server-ticked entity, a world pass takes
     * exactly its OWN world's externally ticked ones - co-located worlds' passes never cross.
     */
    public boolean owns(Entity entity) {
        if (external != MechanicsWorld.externallyTicked(entity)) return false;
        if (!external) return true;
        Instance in = entity.getInstance();
        return MechanicsWorld.of(entity, in != null ? in : world.instance()) == world;
    }
}
