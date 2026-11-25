package io.github.term4.polyp.mechanics.attack;

import io.github.term4.polyp.world.WorldPolicy;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Pluggable hit detection: mounts listeners under the attack node that turn raw input into {@link AttackSnapshot}s and
 * feed them to the pipeline. None given installs {@link #PACKET}; combine several by passing them all, or call
 * {@link AttackSystem#apply} directly.
 */
@FunctionalInterface
public interface HitDetection {

    /** Mounts this detection's listeners on {@code node}, feeding detected hits to {@code sink}. */
    void install(EventNode<@NotNull Event> node, Consumer<AttackSnapshot> sink);

    /** The built-in default: vanilla attack (interact-entity) packets. */
    HitDetection PACKET = (node, sink) -> node.addListener(EntityAttackEvent.class, e -> {
        if (!(e.getEntity() instanceof Player attacker)) return;
        // an observer (or hacked client) clicking an entity in a world they're not playing in
        if (!WorldPolicy.canAffect(attacker, e.getTarget())) return;
        sink.accept(new AttackSnapshot(attacker, e.getTarget(), null));
    });

    /** No built-in detection: hits are submitted programmatically via {@link AttackSystem#apply}. */
    HitDetection NONE = (node, sink) -> {};
}
