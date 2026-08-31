package io.github.term4.polyp.platform.player;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfiles;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.SharedTeam;
import io.github.term4.polyp.platform.compatibility.CompatAnimatium;
import io.github.term4.polyp.platform.compatibility.CompatConfig;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventFilter;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import io.github.term4.polyp.util.tick.TickScalingConfig;
import net.minestom.server.network.packet.server.play.SetTickStatePacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * Applies the scoped player-platform configs - {@link PlayerConfig} and {@link CompatConfig} - at spawn (join and
 * instance change) and on every profile assignment change, so swaps are live without polling. Each member is applied
 * only when a scope sets it; otherwise the player is left untouched, so the manual {@link OptimizedPlayer} setters
 * stay authoritative.
 */
public final class PlayerConfigApplier {

    /** Huge, so the modern attack cooldown is always full (no indicator, 1.8-style hits). */
    private static final double REMOVED_ATTACK_COOLDOWN_SPEED = 1024.0;

    private PlayerConfigApplier() {}

    /** Called by {@code Polyp.init()} when installPlayerProvider is on. */
    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:player-config", EventFilter.PLAYER);
        node.addListener(PlayerSpawnEvent.class, e -> apply(polyp, e.getPlayer()));
        polyp.install(node);
    }

    /** Applies the scoped config to every online player (run when profile assignments change). */
    public static void applyAll(Polyp polyp) {
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) apply(polyp, p);
    }

    /** Applies the player's scoped platform configs; each member is a no-op when no scope sets it. */
    public static void apply(Polyp polyp, Player player) {
        if (!(player instanceof OptimizedPlayer op)) return;
        // Reads two members of the same player's profile - resolve its scope chain once.
        MechanicsProfiles.Resolved profile = polyp.profiles().resolved(player);
        PlayerConfig cfg = profile.get(MechanicsKeys.PLAYER);
        if (cfg != null && cfg.positionBroadcastInterval != null) {
            op.setPositionBroadcastInterval(Math.max(1, cfg.positionBroadcastInterval));
        }
        // one pass from the resolved config (all-off when no scope sets it), so a profile swap is a clean mode SWITCH -
        // the previous mode's state never sticks
        CompatConfig compat = profile.get(MechanicsKeys.COMPAT);
        var state = op.compat();
        var prevView = state.itemViewKey();
        boolean prevCooldownRemoved = state.attackCooldownRemoved();
        state.apply(compat);
        // the join inventory is sent before this applies, so a changed client-view rewrite wouldn't reach the client
        // until the next packet; visible entities too - the blocking-pose stamp rides their equipment
        if (!prevView.equals(state.itemViewKey())) {
            op.getInventory().update();
            resendViewedEquipment(op);
        }
        // touched only on a real change, so a non-compat server's attack speed is never clobbered
        boolean nowCooldownRemoved = compat != null && Boolean.TRUE.equals(compat.removeAttackCooldown);
        if (nowCooldownRemoved != prevCooldownRemoved) {
            var attackSpeed = op.getAttribute(Attribute.ATTACK_SPEED);
            if (attackSpeed != null) {
                // restore the base the app had set, not a hardcoded vanilla 4.0
                if (nowCooldownRemoved) {
                    state.setSavedAttackSpeedBase(attackSpeed.getBaseValue());
                    attackSpeed.setBaseValue(REMOVED_ATTACK_COOLDOWN_SPEED);
                } else {
                    attackSpeed.setBaseValue(state.savedAttackSpeedBase());
                }
            }
            state.setAttackCooldownRemoved(nowCooldownRemoved);
        }
        SharedTeam.set(op, SharedTeam.Reason.NO_PUSH, state.noEntityPush());
        applyTickRate(polyp, op, profile.get(MechanicsKeys.TICK_SCALING));
        // re-pushed on every apply: the client handshakes only once on join, so a kit/profile or instance swap must
        // re-send it. Runs even when compat is null, so a profile that drops compat clears the client's features.
        CompatAnimatium.applyFeatures(polyp, player);
    }

    /** Re-sends every visible entity's equipment through {@code player}'s (now changed) item view. */
    private static void resendViewedEquipment(Player player) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        for (Entity entity : instance.getEntities()) {
            if (entity != player && entity instanceof EquipmentHandler handler && entity.getViewers().contains(player)) {
                player.sendPacket(handler.getEquipmentsPacket());
            }
        }
    }

    /** Last rate pushed to this client, so a re-apply (spawn, any profile change) is silent when nothing moved. */
    private static final Tag<Float> SENT_TICK_RATE = Tag.Transient("polyp:sent-tick-rate");

    /** Puts the client's own sim on the scope's {@code clientTps}. Only an EXPLICIT baseline pushes, so the
     *  default stays a no-op at any server TPS; legacy clients have no such packet and can only spectate. */
    private static void applyTickRate(Polyp polyp, Player player, @Nullable TickScalingConfig cfg) {
        if (polyp.clientInfo().isLegacy(player)) return;
        int baseline = cfg != null ? cfg.clientTps() : TickScalingConfig.SERVER_TPS;
        float want = baseline == TickScalingConfig.SERVER_TPS
                ? TickScalingConfig.VANILLA_CLIENT_TPS : baseline;
        Float sent = player.getTag(SENT_TICK_RATE);
        if (sent != null && sent == want) return;
        // nothing sent yet AND already native: leave the connection untouched
        if (sent == null && want == TickScalingConfig.VANILLA_CLIENT_TPS) return;
        player.sendPacket(new SetTickStatePacket(want, false));
        player.setTag(SENT_TICK_RATE, want);
    }
}
