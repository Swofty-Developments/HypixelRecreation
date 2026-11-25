package io.github.term4.polyp.mechanics.damage;

import net.minestom.server.entity.LivingEntity;

/**
 * A self-driven environmental damage scan, run by {@link EnvironmentalDamageTicker} once per instance tick against each
 * living, non-exempt {@code entity}. The dispatcher already filtered out dead/creative/spectator/flying entities, so a
 * producer only does its own contact test and emits.
 */
@FunctionalInterface
public interface EnvironmentalTickProducer {

    void tick(LivingEntity entity, DamageSystem system);
}
