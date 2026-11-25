package io.github.term4.polyp.mechanics.damage.silent;

import io.github.term4.polyp.tracking.ClientInfoTracker;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;

/**
 * Applies health changes without the client-side hurt effect (camera shake / red flash). Legacy clients: suppress the
 * outgoing health packets (via {@link HurtSuppression}) and deliver health via entity metadata. Modern clients: the
 * max-health trick (raise MAX_HEALTH so the bar reads full, setHealth, restore).
 */
public final class SilentDamage {

    private SilentDamage() {}

    public static void setHealthWithoutHurtEffect(Player player, float newHealth, ClientInfoTracker clientInfo) {
        boolean legacy = clientInfo != null && clientInfo.isLegacy(player);

        if (legacy) {
            HurtSuppression.setSuppressHealthPackets(player, true);
            try {
                player.setHealth(newHealth);
                player.sendPacket(player.getMetadataPacket());
            } finally {
                HurtSuppression.setSuppressHealthPackets(player, false);
            }
            return;
        }

        var maxAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double originalBase = maxAttr.getBaseValue();
        maxAttr.setBaseValue(Math.max(0.5, newHealth)); // min 0.5 for attribute validity
        player.setHealth(newHealth);
        maxAttr.setBaseValue(originalBase);
    }
}
