package io.github.term4.polyp.world;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.fx.FxContext;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Place and footstep sounds Minestom doesn't emit, played through the {@link Fx} registry
 * ({@link Fx#STEP} / {@link Fx#BLOCK_PLACE}, the block as the context detail). Owns only the cadence and
 * block resolution; look and audience live in the registered handlers. Breaks are emitted by the world
 * layer (the app's shard bridge routes them into {@link Fx#BLOCK_BREAK}).
 */
public final class WorldSounds {

    private WorldSounds() {}

    // vanilla step cadence (Entity.moveDist/nextStep): accumulate horizontalDistance*0.6, step past nextStep = floor(moveDist)+1
    private static final Tag<Float> MOVE_DIST = Tag.Transient("polyp:step-move-dist");
    private static final Tag<Float> NEXT_STEP = Tag.Transient("polyp:step-next");

    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:world-sounds", EventFilter.PLAYER);
        node.addListener(PlayerBlockPlaceEvent.class, e -> onPlace(polyp, e));
        node.addListener(PlayerMoveEvent.class, e -> onMove(polyp, e));
        polyp.install(node);
    }

    private static void onPlace(Polyp polyp, PlayerBlockPlaceEvent e) {
        if (e.isCancelled()) return; // a compat rule (reach / air) may cancel the placement
        Fx.play(polyp.services(), Fx.BLOCK_PLACE,
                FxContext.at(MechanicsWorld.of(e.getPlayer()), e.getBlockPosition(), e.getPlayer())
                        .withDetail(e.getBlock()));
    }

    private static void onMove(Polyp polyp, PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!p.isOnGround()) return; // steps only while grounded
        Pos from = p.getPosition(); // pre-move; getNewPosition is the target
        Point to = e.getNewPosition();
        double dx = to.x() - from.x(), dz = to.z() - from.z();
        float moved = (float) Math.sqrt(dx * dx + dz * dz) * 0.6f;
        if (moved <= 0) return;
        Float prev = p.getTag(MOVE_DIST);
        float moveDist = (prev != null ? prev : 0f) + moved;
        Float next = p.getTag(NEXT_STEP);
        float nextStep = next != null ? next : 1.0f;
        if (moveDist > nextStep) {
            step(polyp, p, to);
            nextStep = (int) moveDist + 1;
        }
        p.setTag(MOVE_DIST, moveDist);
        p.setTag(NEXT_STEP, nextStep);
    }

    private static void step(Polyp polyp, Player p, Point at) {
        if (p.getInstance() == null || !p.getInstance().isChunkLoaded(at)) return; // mid-load moves: getBlock throws
        // viewed world: on a virtual world the feet rest on the OVERLAY block, not the base map's
        Block below = MechanicsWorld.viewed(p).getBlock(at.withY(at.y() - 0.2));
        if (below.air()) return;
        Fx.play(polyp.services(), Fx.STEP, FxContext.at(MechanicsWorld.of(p), at, p).withDetail(below));
    }
}
