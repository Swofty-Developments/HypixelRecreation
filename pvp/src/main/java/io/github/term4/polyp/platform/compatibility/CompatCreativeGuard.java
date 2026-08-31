package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.CreativeInventoryActionEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Seals the client-view item rewrites ({@link CompatState#rewriteItems}) against the one path that leaks them into
 * server state: creative slots are client-authoritative, so an affected client echoes the rewritten item back
 * ({@link CreativeInventoryActionEvent}) and the server would store the phantom, from where it spreads to drops and
 * other viewers. Inert for unaffected clients; needs the {@link OptimizedPlayer} provider.
 */
public final class CompatCreativeGuard {

    private CompatCreativeGuard() {}

    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:compat-creative-guard", EventFilter.PLAYER);
        node.addListener(CreativeInventoryActionEvent.class, e -> {
            if (e.getPlayer() instanceof OptimizedPlayer op) e.setClickedItem(op.compat().sanitizeInboundItem(e.getClickedItem()));
        });
        polyp.install(node);
    }
}
