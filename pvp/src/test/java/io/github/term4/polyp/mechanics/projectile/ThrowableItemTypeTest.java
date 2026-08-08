package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.projectile.ProjectileLaunchEvent;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * One click = one throw: a block-aimed click arrives as use_item_on_block FOLLOWED by use_item (a plain throwable has
 * no block action, so the client sends both) and vanilla launches from use_item alone. The fire charge is the inverse -
 * its client-side block use consumes the click, so use_item_on_block is the only signal when aimed at a block.
 */
class ThrowableItemTypeTest extends HeadlessServerTest {

    private interface Throwing {
        void run(FakePlayer thrower, AtomicInteger launches);
    }

    private static void withThrowables(String playerName, Throwing test) {
        var system = new ProjectileSystem(Polyp.getInstance(), Vanilla18.projectiles());
        system.registerVanillaDefaults();
        system.enable(Snowball.KEY);
        system.enable(Fireball.KEY);
        var handler = MinecraftServer.getGlobalEventHandler();
        handler.addChild(system.node());
        AtomicInteger launches = new AtomicInteger();
        EventNode<Event> counter = EventNode.all("test:launch-counter");
        counter.addListener(ProjectileLaunchEvent.class, e -> launches.incrementAndGet());
        handler.addChild(counter);
        try {
            FakePlayer thrower = FakePlayer.connect(instance, new Pos(8.5, 65, 8.5), playerName);
            test.run(thrower, launches);
            thrower.player.remove();
        } finally {
            system.disable(Snowball.KEY);
            system.disable(Fireball.KEY);
            handler.removeChild(counter);
            handler.removeChild(system.node());
        }
    }

    @Test
    void snowballBlockAimedClickThrowsExactlyOnce() {
        withThrowables("SnowThrower", (thrower, launches) -> {
            var p = thrower.player;
            ItemStack snowballs = ItemStack.of(Material.SNOWBALL, 16);
            p.setItemInMainHand(snowballs);
            EventDispatcher.call(new PlayerUseItemOnBlockEvent(p, PlayerHand.MAIN, snowballs,
                    new Vec(8, 64, 9), new Vec(0.5, 1, 0.5), BlockFace.TOP));
            EventDispatcher.call(new PlayerUseItemEvent(p, PlayerHand.MAIN, snowballs, 0));
            assertEquals(1, launches.get(), "the pair is one click: one snowball");
            assertEquals(15, p.getItemInMainHand().amount(), "consumed once");
        });
    }

    @Test
    void fireChargeThrowsOnceFromEitherPacket() {
        withThrowables("ChargeThrower", (thrower, launches) -> {
            var p = thrower.player;
            ItemStack charge = ItemStack.of(Material.FIRE_CHARGE, 16);
            p.setItemInMainHand(charge);
            // block-aimed: the client consumed the click (lights fire), use_item_on_block is the sole signal
            EventDispatcher.call(new PlayerUseItemOnBlockEvent(p, PlayerHand.MAIN, charge,
                    new Vec(8, 64, 9), new Vec(0.5, 1, 0.5), BlockFace.TOP));
            assertEquals(1, launches.get(), "the fire charge throws from use_item_on_block");
            // its client-side use FAILED (1.8 adventure edge): use_item follows in the same tick - the same click
            EventDispatcher.call(new PlayerUseItemEvent(p, PlayerHand.MAIN, p.getItemInMainHand(), 0));
            assertEquals(1, launches.get(), "the same-tick use_item is not a second throw");
            assertEquals(15, p.getItemInMainHand().amount(), "consumed once");
        });
    }
}
