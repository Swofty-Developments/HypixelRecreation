package io.github.term4.polyp.platform.fixes.visuals.legacy_1_8;

import io.github.term4.polyp.platform.SharedTeam;
import io.github.term4.polyp.platform.fixes.FixesSystem;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;

/**
 * Keeps enabled players on the shared friendly-fire-off lib team ({@link SharedTeam}) so a 1.8 client stops hiding
 * deflected / passed-through arrows. The glitch is a native 1.8 client bug ({@code EntityArrow} bounces+hides a hit it
 * can't damage); the only client lever is the {@code canAttackPlayer} null-out - same team + FF off - which survives
 * the Via chain. Server-side damage ignores teams. Per PLAYER, not per arrow (teams are pairwise - both shooter and
 * target must be members); membership resolves from {@link FixesSystem#legacyArrowVisibilityEnabled} on (re)spawn.
 */
public final class LegacyArrowVisibility {

    private final FixesSystem system;
    private volatile boolean enabled = true; // runtime master switch; the per-player config still decides who

    public LegacyArrowVisibility(FixesSystem system) {
        this.system = system;
    }

    /** Re-evaluates membership on every spawn (first / respawn / instance change, so an instance-scoped config is
     *  picked up too); {@code SharedTeam} cleans up on disconnect. */
    public void install(EventNode<Event> node) {
        node.addListener(PlayerSpawnEvent.class, e -> apply(e.getPlayer()));
    }

    /**
     * Runtime master switch: {@code false} removes everyone from the team (the glitch returns), {@code true} re-applies
     * the per-player {@link LegacyArrowVisibilityConfig#enabled} knob. Re-evaluates all online players immediately.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        refreshAll();
    }

    public boolean isEnabled() { return enabled; }

    /** Re-evaluates every online player's membership - call after changing profile configs at runtime. */
    public void refreshAll() {
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) apply(p);
    }

    private void apply(Player player) {
        SharedTeam.set(player, SharedTeam.Reason.ARROW_VISIBILITY,
                enabled && system.legacyArrowVisibilityEnabled(player));
    }
}
