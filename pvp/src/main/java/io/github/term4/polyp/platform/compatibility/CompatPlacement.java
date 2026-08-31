package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.util.BlockContact;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.tracking.ClientInfoTracker;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Server-side 1.8 block-placement rules, each gated by its own {@code CompatConfig} knob.
 *
 * <p><b>Reach ({@code blockPlaceReach}):</b> cancels a placement whose clicked point is farther than the reach from the
 * player's <em>server</em> eye (the 1.8 preset under {@code legacyHitbox}), closing the modern sneak-bridge over-reach
 * (the lower crouch eye out-reaches 1.8). An honest Animatium client aims from the 1.8 eye, so the check is a no-op for
 * it - kept live to cover a spoofed handshake.
 *
 * <p><b>Air placement ({@code oldPlacement}):</b> refuses a placement whose clicked cell is air - the server half of the
 * 1.8 "don't place against air" rule (Animatium enforces the client half via {@code OLD_PLACEMENT}).
 *
 * <p>Installed once; each rule is inert unless the player's config enables it.
 */
public final class CompatPlacement {

    private CompatPlacement() {}

    /**
     * The per-body placement entity check for mixed-version play (shaped for a shard router's body-check hook).
     * A LEGACY placer gets the 1.8 reference-server semantics, source-verified against Paper 1.8.8 + the 1.8.9
     * client: the placer's own body NEVER blocks their placement (Paper passes the placer into
     * {@code checkNoEntityCollision}, which excludes it - and the 1.8 client sends every attempt before its own
     * prediction runs, so the server's accept is what the player sees; stairs into your own face land), and
     * no-collision-box blocks check nobody (1.8's null-AABB skip - the ladder clutch). Other bodies stay on the
     * precise check; everyone else (Animatium included, for now) is precise throughout, matching their own
     * client's prediction.
     *
     * <p>Server POLICY on top of the vanilla mechanic (a Hypixel-style anticheat refusing self-overlap) belongs
     * to the app: cancel {@code PlayerBlockPlaceEvent} - both placement paths fire it with the resolved target
     * and resync on cancel - with {@link BlockContact#overlapsBody} as the condition, composed with
     * {@link BlockContact#isFullCube}/{@link BlockContact#isPassable} to scope the fill level. Wholesale
     * replacements go through the body-check hook itself.
     */
    public static boolean placementBodyCheck(@NotNull Player placer, @NotNull Entity body, @NotNull Block placing,
                                             @NotNull Point cellRelativeBody, @NotNull BoundingBox bodyBox) {
        if (placer instanceof OptimizedPlayer op && op.compat().legacyClient()) {
            if (body == placer) return false;
            if (BlockContact.isPassable(placing)) return false;
        }
        return placing.collisionShape().intersectBox(cellRelativeBody, bodyBox);
    }

    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:compat-placement", EventFilter.PLAYER);
        // resolve the client-info tracker lazily - install runs before it's created in init()
        node.addListener(PlayerBlockPlaceEvent.class, e -> onPlace(e, polyp.clientInfo()));
        node.addListener(PlayerBlockInteractEvent.class, CompatPlacement::onInteract);
        polyp.install(node);
    }

    /** A live raycast never block-hits air, so an air clicked-block = the client aimed at a cell it just broke (creative quick-replace). */
    private static void onInteract(PlayerBlockInteractEvent event) {
        if (!(event.getPlayer() instanceof OptimizedPlayer op)) return;
        // the event's block is the base-map read; a virtual-world member's clicked block may exist only in their world
        if (op.compat().oldPlacement() && MechanicsWorld.viewed(op).getBlock(event.getBlockPosition()).air()) {
            event.setBlockingItemUse(true);
        }
    }

    private static void onPlace(PlayerBlockPlaceEvent event, ClientInfoTracker clientInfo) {
        Player player = event.getPlayer();
        if (!(player instanceof OptimizedPlayer op)) return;
        Double reach = op.compat().blockPlaceReach();
        if (reach == null) return;
        // only modern survival clients can sneak-bridge past 1.8 reach; legacy/creative/spectator already aim correctly
        if (clientInfo.isLegacy(player)
                || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        // exact clicked point = the support block (one step back along the clicked face) + the cursor offset on its face
        Point hit = event.getBlockPosition().relative(event.getBlockFace().getOppositeFace()).add(event.getCursorPosition());
        Point eye = player.getPosition().add(0, player.getEyeHeight(), 0); // value (b) server eye
        if (eye.distanceSquared(hit) > reach * reach) event.setCancelled(true);
    }
}
