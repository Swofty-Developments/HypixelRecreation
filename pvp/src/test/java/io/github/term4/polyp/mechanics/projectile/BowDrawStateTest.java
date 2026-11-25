package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.PlayerActionListener;
import net.minestom.server.listener.UseItemListener;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The bow trusts Minestom's use-duration wholesale, so tap-tap-tap draw spam must never accumulate: every
 * arriving use RESTAMPS the clock, and every release both fires the cancel with the CURRENT duration and
 * clears the state. Exercises the real packet listeners.
 */
class BowDrawStateTest extends HeadlessServerTest {

    private static void use(Player p) {
        UseItemListener.useItemListener(new ClientUseItemPacket(PlayerHand.MAIN, 0,
                p.getPosition().yaw(), p.getPosition().pitch()), p);
    }

    private static void release(Player p) {
        PlayerActionListener.playerActionListener(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.UPDATE_ITEM_STATE, Vec.ZERO, BlockFace.TOP, 0), p);
    }

    private static void ticks(Player p, int n) {
        for (int i = 0; i < n; i++) p.tick(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()));
    }

    @Test
    void tapSpamNeverAccumulatesDraw() {
        Player p = FakePlayer.connect(instance, new Pos(8.5, 42, 8.5), "BowTapper").player;
        p.setItemInMainHand(ItemStack.of(Material.BOW));
        List<Long> durations = new ArrayList<>();
        var node = net.minestom.server.MinecraftServer.getGlobalEventHandler();
        var listener = net.minestom.server.event.EventListener.of(PlayerCancelItemUseEvent.class,
                e -> durations.add(e.getUseDuration()));
        node.addListener(listener);
        try {
            // five quick taps: use, 2 ticks, release - then a real draw
            for (int i = 0; i < 5; i++) {
                use(p);
                ticks(p, 2);
                release(p);
                ticks(p, 1);
            }
            use(p);
            ticks(p, 3);
            release(p);
            assertEquals(List.of(2L, 2L, 2L, 2L, 2L, 3L), durations,
                    "durations must reflect each tap alone, never the span");

            // a use with NO release, then a re-use: the re-use must restamp
            durations.clear();
            use(p);
            ticks(p, 30);
            use(p);
            ticks(p, 2);
            release(p);
            assertEquals(List.of(2L), durations, "a repeated use must restart the draw clock");
        } finally {
            node.removeListener(listener);
            p.remove();
        }
    }
}
