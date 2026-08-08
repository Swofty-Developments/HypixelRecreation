package io.github.term4.polyp;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.Nullable;

/**
 * Marker for an installable system in the {@link Polyp} registry: registered via
 * {@link Polyp#register} (typically from a static {@code install}) and looked up by concrete type via
 * {@link Polyp#module}.
 */
public interface MechanicsModule {

    /** The system's installed event node; {@code register} detaches the replaced module's, so a re-install never stacks listeners. */
    default @Nullable EventNode<? extends Event> node() { return null; }
}
