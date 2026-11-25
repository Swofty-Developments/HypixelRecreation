package io.github.term4.polyp.mechanics.damage.silent;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.play.EntityAttributesPacket;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Suppresses outgoing health packets while {@link SilentDamage} applies a silent health change to a legacy client:
 * the player's {@code UpdateHealth}/{@code EntityAttributes} packets are cancelled so health rides entity metadata
 * instead (no 1.8 hurt-cam tilt).
 */
public final class HurtSuppression {

    private static final Tag<Boolean> SUPPRESS = Tag.Transient("polyp:suppress-health-packets");

    private HurtSuppression() {}

    /** Installs the packet-out listener under the damage system's node (so it lives and dies with the install). */
    public static void install(EventNode<@NotNull Event> parent) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:hurt-suppression", EventFilter.PLAYER);
        node.addListener(PlayerPacketOutEvent.class, e -> {
            Player player = e.getPlayer();
            if (!Boolean.TRUE.equals(player.getTag(SUPPRESS))) return;
            var packet = e.getPacket();
            if (packet instanceof UpdateHealthPacket) {
                e.setCancelled(true);
            } else if (packet instanceof EntityAttributesPacket attr && attr.entityId() == player.getEntityId()) {
                e.setCancelled(true);
            }
        });
        node.addListener(PlayerDisconnectEvent.class, e -> e.getPlayer().removeTag(SUPPRESS));
        parent.addChild(node);
    }

    /** Toggle suppression of health packets for a player (set before setHealth, clear after). */
    public static void setSuppressHealthPackets(Player player, boolean suppress) {
        if (suppress) {
            player.setTag(SUPPRESS, true);
        } else {
            player.removeTag(SUPPRESS);
        }
    }
}
