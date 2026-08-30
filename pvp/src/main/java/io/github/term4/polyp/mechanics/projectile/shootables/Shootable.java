package io.github.term4.polyp.mechanics.projectile.shootables;

import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

/**
 * A pluggable launcher: an item that fires a projectile (bow, crossbow, rod), as opposed to a thrown item that is the
 * projectile (snowball/egg/pearl). Mounts its item listeners under the projectile node and launches via
 * {@link ProjectileSystem#launch}.
 */
@FunctionalInterface
public interface Shootable {

    /** Mounts this launcher's listeners on the system's {@code node}, launching through {@code system}. */
    void install(@NotNull EventNode<@NotNull Event> node, @NotNull ProjectileSystem system);
}
