package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.platform.fixes.FixToggleConfig;
import io.github.term4.polyp.platform.fixes.FixesSystem;
import io.github.term4.polyp.world.FireSupport;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import org.jetbrains.annotations.NotNull;

/**
 * 1.8 face douse: fire is not crosshair-targetable on a 1.8 client, so EVERY dig-start douses the cell on the
 * CLICKED FACE ({@code World.douseFire}, world event 1004); in CREATIVE a successful douse consumes the click
 * (the block survives). Adventure bails before the douse (1.8's held-item CanDestroy exception dropped).
 * Insta-breaks never fire StartDigging, so those ride the cancellable break event. Cancelled events never
 * douse - protection listeners must run before the polyp nodes.
 */
public final class LegacyFireDouseFix {

    private LegacyFireDouseFix() {}

    public static void install(EventNode<@NotNull Event> node, FixesSystem fixes) {
        // slow digs: vanilla douses at dig START (survival then keeps digging the clicked block)
        node.addListener(PlayerStartDiggingEvent.class, e -> {
            if (e.isCancelled() || !douses(fixes, e.getPlayer())) return;
            douse(e.getPlayer(), e.getBlockPosition().relative(e.getBlockFace()));
        });
        node.addListener(PlayerBlockBreakEvent.class, e -> {
            if (e.isCancelled() || !douses(fixes, e.getPlayer())) return;
            if (douse(e.getPlayer(), e.getBlockPosition().relative(e.getBlockFace()))
                    && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        });
    }

    private static boolean douses(FixesSystem fixes, Player miner) {
        GameMode mode = miner.getGameMode();
        if (mode == GameMode.ADVENTURE || mode == GameMode.SPECTATOR) return false;
        FixToggleConfig cfg = fixes.configFor(miner).legacyFireDouse();
        return cfg != null && cfg.enabled();
    }

    private static boolean douse(Player miner, Point at) {
        MechanicsWorld world = MechanicsWorld.of(miner);
        if (!world.isChunkLoaded(at)) return false; // a horizontal face can cross into an unloaded neighbor
        if (!FireSupport.isFire(world.getBlock(at, Block.Getter.Condition.TYPE))) return false;
        world.setBlock(at, Block.AIR);
        world.broadcast(new WorldEventPacket(FireSupport.FIZZ, at, 0, false));
        return true;
    }
}
