package io.github.term4.polyp.vri;

import io.github.term4.polyp.entity.DroppedItemEntity;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Spawns dropped items (Q / drag-out / cursor stack on inventory close) - Minestom fires {@link ItemDropEvent}
 * but spawns nothing. Vanilla 1.8 {@code EntityHuman.a}: eye - 0.3, thrown 0.3 b/t along the look, 40t pickup
 * delay. Container closes route here natively; the close listener adds the own-screen close Minestom misses.
 * No-take GUIs: cancel {@link ItemDropEvent} and reclaim the cursor in their own close listener.
 */
public final class ItemDrop {

    private static final int PICKUP_DELAY_TICKS = 40;
    private static final double THROW_SPEED = 0.3;

    private ItemDrop() {}

    public static void install(EventNode<@NotNull Event> node, Vri vri) {
        node.addListener(InventoryCloseEvent.class, e -> {
            // containers drop the cursor natively (removeViewer); only the player's own screen leaks it
            var inventory = e.getPlayer().getInventory();
            if (e.getInventory() != inventory || !vri.configFor(e.getPlayer()).itemDrop) return;
            ItemStack cursor = inventory.getCursorItem();
            if (cursor.isAir()) return;
            inventory.setCursorItem(ItemStack.AIR);
            ItemDropEvent drop = new ItemDropEvent(e.getPlayer(), cursor);
            EventDispatcher.call(drop); // spawned by the listener below
            if (drop.isCancelled()) inventory.setCursorItem(cursor);
        });
        node.addListener(ItemDropEvent.class, e -> {
            if (e.isCancelled()) return;
            Player player = e.getPlayer();
            Instance instance = player.getInstance();
            VriConfig cfg = vri.configFor(player);
            if (instance == null || !cfg.itemDrop) return;

            double yaw = Math.toRadians(player.getPosition().yaw()), pitch = Math.toRadians(player.getPosition().pitch());
            var rnd = ThreadLocalRandom.current();
            double vx = -Math.sin(yaw) * Math.cos(pitch) * THROW_SPEED;
            double vz = Math.cos(yaw) * Math.cos(pitch) * THROW_SPEED;
            double vy = -Math.sin(pitch) * THROW_SPEED + 0.1;
            double angle = rnd.nextFloat() * Math.PI * 2;
            double scatter = 0.02 * rnd.nextFloat();
            vx += Math.cos(angle) * scatter;
            vy += (rnd.nextFloat() - rnd.nextFloat()) * 0.1f;
            vz += Math.sin(angle) * scatter;

            DroppedItemEntity.spawn(instance,
                    player.getPosition().add(0, player.getEyeHeight() - 0.30000001192092896, 0),
                    new Vec(vx, vy, vz), e.getItemStack(), cfg.itemPhysics,
                    PICKUP_DELAY_TICKS, ItemSpawnEvent.Cause.PLAYER_DROP, player);
        });
    }
}
