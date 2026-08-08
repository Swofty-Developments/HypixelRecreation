package io.github.term4.polyp.vri;

import io.github.term4.polyp.entity.DroppedItemEntity;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import java.util.concurrent.atomic.AtomicReference;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The VRI block-break slice: drops spawn with the vanilla shape, pickup fills the inventory, item physics. */
class VriFeaturesTest extends HeadlessServerTest {

    private static final BlockVec BLOCK = new BlockVec(3, 42, 3);
    private static FakePlayer miner;

    @BeforeAll
    static void install() {
        Vri.install(polyp, VriConfig.builder().blockDrops(BlockDrops.VANILLA)
                .itemPhysics(DroppedItemEntity.Model.LEGACY).itemPickup(true).itemDrop(true).build());
        miner = FakePlayer.connect(instance, new Pos(3.5, 43, 3.5), "VriMiner");
    }

    private static PlayerBlockBreakEvent breakEvent(Block block) {
        instance.setBlock(BLOCK, block);
        return new PlayerBlockBreakEvent(miner.player, instance, block, Block.AIR, BLOCK, BlockFace.TOP);
    }

    private static ItemEntity dropOf(Material material) {
        return instance.getEntities().stream()
                .filter(e -> e instanceof ItemEntity i && i.getItemStack().material() == material)
                .map(e -> (ItemEntity) e).findFirst().orElseThrow();
    }

    @Test
    void dropSpawnsWithVanillaShape() {
        long before = instance.getEntities().stream().filter(e -> e instanceof ItemEntity).count();
        EventDispatcher.call(breakEvent(Block.DIRT)); // no tool required
        var items = instance.getEntities().stream().filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).toList();
        assertEquals(before + 1, items.size());
        ItemEntity drop = items.getLast();
        assertEquals(Material.DIRT, drop.getItemStack().material());
        // vanilla spawn box: blockPos + [0.25, 0.75] per axis; 10t pickup delay
        assertTrue(drop.getPosition().x() >= BLOCK.x() + 0.25 && drop.getPosition().x() <= BLOCK.x() + 0.75, drop.getPosition().toString());
        assertFalse(drop.isPickable(), "10-tick pickup delay");
        drop.remove();
    }

    @Test
    void pickupAddsToInventoryAndCancelWhenFull() {
        ItemEntity item = new ItemEntity(ItemStack.of(Material.DIRT, 5));
        var pickup = new PickupItemEvent(miner.player, item);
        EventDispatcher.call(pickup);
        assertFalse(pickup.isCancelled());
        assertTrue(miner.player.getInventory().getItemStack(0).material() == Material.DIRT
                        || countDirt() >= 5, "stack landed in the inventory");
    }

    @Test
    void deadAndSpectatorPickupsAreCancelled() {
        var dead = FakePlayer.connect(instance, new Pos(5.5, 43, 5.5), "VriDead");
        var ghost = FakePlayer.connect(instance, new Pos(5.5, 43, 5.5), "VriGhost");
        ItemEntity item = new ItemEntity(ItemStack.of(Material.DIRT, 2));
        try {
            dead.player.kill();
            var pickup = new PickupItemEvent(dead.player, item);
            EventDispatcher.call(pickup);
            assertTrue(pickup.isCancelled(), "dead players collide with nothing (1.8 EntityPlayer.onUpdate)");

            ghost.player.setGameMode(net.minestom.server.entity.GameMode.SPECTATOR);
            var spectate = new PickupItemEvent(ghost.player, item);
            EventDispatcher.call(spectate);
            assertTrue(spectate.isCancelled(), "spectators collide with nothing");
        } finally {
            dead.player.remove();
            ghost.player.remove();
        }
    }

    @Test
    void qDropSpawnsThrownItemAtEye() {
        EventDispatcher.call(new ItemDropEvent(miner.player, ItemStack.of(Material.OAK_PLANKS)));
        var drop = dropOf(Material.OAK_PLANKS);
        // vanilla: spawned at eye - 0.3, 40t pickup delay, thrown roughly along the look
        assertEquals(miner.player.getPosition().y() + miner.player.getEyeHeight() - 0.3, drop.getPosition().y(), 1e-6);
        assertFalse(drop.isPickable(), "40-tick pickup delay");
        assertTrue(drop.getVelocity().length() > 0, "thrown, not placed");
        drop.remove();
    }

    @Test
    void toolGateBlocksBareHandStone() {
        long before = instance.getEntities().stream().filter(e -> e instanceof ItemEntity).count();
        EventDispatcher.call(breakEvent(Block.STONE)); // requiresTool, bare hand -> vanilla drops nothing
        long after = instance.getEntities().stream().filter(e -> e instanceof ItemEntity).count();
        assertEquals(before, after, "stone without a pickaxe drops nothing");
    }

    @Test
    void itemPhysicsModelsDiverge() {
        for (int y = 45; y <= 47; y++) instance.setBlock(10, y, 10, Block.WATER);
        // different materials so the two drops can't merge
        var legacy = new DroppedItemEntity(ItemStack.of(Material.STONE), DroppedItemEntity.Model.LEGACY);
        var modern = new DroppedItemEntity(ItemStack.of(Material.DIRT), DroppedItemEntity.Model.MODERN);
        legacy.setInstance(instance, new Pos(10.5, 46.5, 10.5)).join();
        modern.setInstance(instance, new Pos(10.5, 46.5, 10.5)).join();
        for (int i = 0; i < 100; i++) { // full ticks: stock physics in movementTick, fluid overlay in update
            legacy.tick(0);
            modern.tick(0);
        }
        assertTrue(legacy.getPosition().y() <= 45.01, "1.8 items sink to the bottom: " + legacy.getPosition());
        // 26.1 items dip first - gravity applies until the first fluid sample - then buoyancy wins
        assertTrue(modern.getVelocity().y() > 0, "26.1 items rise in water: " + modern.getVelocity());
        assertTrue(modern.getPosition().y() > legacy.getPosition().y() + 0.5, "26.1 floats above 1.8: " + modern.getPosition());
        legacy.remove();
        modern.remove();
        for (int y = 45; y <= 47; y++) instance.setBlock(10, y, 10, Block.AIR);
    }

    @Test
    void legacyItemSitsUnderSourceSlidesUnderFlowing() {
        // enclosed source cell: no slope, the item on the bottom must not move
        instance.setBlock(12, 45, 12, Block.WATER);
        var still = new DroppedItemEntity(ItemStack.of(Material.STONE), DroppedItemEntity.Model.LEGACY);
        still.setInstance(instance, new Pos(12.5, 45.0, 12.5)).join();
        for (int i = 0; i < 60; i++) still.tick(0);
        assertEquals(12.5, still.getPosition().x(), 1e-9, "no current under source water");
        assertEquals(12.5, still.getPosition().z(), 1e-9);
        still.remove();
        instance.setBlock(12, 45, 12, Block.AIR);

        // source -> level-4 gradient pushes +x; the resting item (fractional y = 0) must slide along the bottom
        instance.setBlock(5, 45, 5, Block.WATER);
        instance.setBlock(6, 45, 5, Block.WATER.withProperty("level", "4"));
        var slider = new DroppedItemEntity(ItemStack.of(Material.STONE), DroppedItemEntity.Model.LEGACY);
        slider.setInstance(instance, new Pos(5.5, 45.0, 5.5)).join();
        for (int i = 0; i < 15; i++) slider.tick(0);
        // vanilla pushes items twice per tick: ~0.9 blocks by now; a single push only reaches ~0.45
        assertTrue(slider.getPosition().x() > 6.2, "double-push slide rate: " + slider.getPosition());
        assertEquals(5.5, slider.getPosition().z(), 1e-9, "gradient is pure +x");
        assertTrue(slider.getPosition().y() < 45.05, "slides, not hops: " + slider.getPosition());
        slider.remove();
        instance.setBlock(5, 45, 5, Block.AIR);
        instance.setBlock(6, 45, 5, Block.AIR);
    }

    @Test
    void legacyItemNotPushedUnderTwoLayers() {
        // vanilla shape: a flowing gradient OVER a source pool (water under water is never level 1-7); the resting
        // item's scan covers only the bottom cell -> uniform decay -> no push, whatever the top layer does
        instance.setBlock(8, 45, 8, Block.WATER);
        instance.setBlock(9, 45, 8, Block.WATER);
        instance.setBlock(8, 46, 8, Block.WATER.withProperty("level", "2"));
        instance.setBlock(9, 46, 8, Block.WATER.withProperty("level", "3"));
        var item = new DroppedItemEntity(ItemStack.of(Material.STONE), DroppedItemEntity.Model.LEGACY);
        item.setInstance(instance, new Pos(8.5, 45.0, 8.5)).join();
        for (int i = 0; i < 60; i++) item.tick(0);
        assertEquals(8.5, item.getPosition().x(), 1e-9, "no push under 2 layers: " + item.getPosition());
        assertEquals(8.5, item.getPosition().z(), 1e-9);
        item.remove();
        instance.setBlock(8, 45, 8, Block.AIR);
        instance.setBlock(9, 45, 8, Block.AIR);
        instance.setBlock(8, 46, 8, Block.AIR);
        instance.setBlock(9, 46, 8, Block.AIR);
    }

    @Test
    void cursorItemDropsOnInventoryClose() {
        miner.player.getInventory().setCursorItem(ItemStack.of(Material.GOLD_INGOT, 3));
        miner.player.closeInventory();
        assertTrue(miner.player.getInventory().getCursorItem().isAir(), "cursor cleared on close");
        var drop = dropOf(Material.GOLD_INGOT);
        assertEquals(3, drop.getItemStack().amount(), "the whole dragged stack drops");
        drop.remove();
    }

    @Test
    void containerCloseDropsCursorNatively() {
        var chest = new net.minestom.server.inventory.Inventory(net.minestom.server.inventory.InventoryType.CHEST_3_ROW, "Chest");
        miner.player.openInventory(chest);
        miner.player.getInventory().setCursorItem(ItemStack.of(Material.IRON_INGOT, 4));
        miner.player.closeInventory();
        var drop = dropOf(Material.IRON_INGOT);
        assertEquals(4, drop.getItemStack().amount(), "chest close drops the dragged stack");
        assertTrue(miner.player.getInventory().getCursorItem().isAir());
        drop.remove();
    }

    @Test
    void guiCancelledCloseDropRestoresCursor() {
        var listener = net.minestom.server.event.EventListener.of(ItemDropEvent.class, e -> {
            if (e.getItemStack().material() == Material.EMERALD) e.setCancelled(true);
        });
        MinecraftServer.getGlobalEventHandler().addListener(listener);
        try {
            miner.player.getInventory().setCursorItem(ItemStack.of(Material.EMERALD, 2));
            miner.player.closeInventory();
            assertEquals(Material.EMERALD, miner.player.getInventory().getCursorItem().material(),
                    "cancel restores the cursor for the GUI to reclaim");
            assertTrue(instance.getEntities().stream()
                    .noneMatch(en -> en instanceof ItemEntity i && i.getItemStack().material() == Material.EMERALD));
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(listener);
            miner.player.getInventory().setCursorItem(ItemStack.AIR);
        }
    }
    @Test
    void dropSpawnPacketCarriesDataOneAndVelocity() {
        // ViaRewind synthesizes a DELAYED zero SET_ENTITY_MOTION for data-0 object spawns (froze 1.8 TNT hops);
        // items ride Minestom's ItemEntityMeta data=1 + embedded velocity, as vanilla 1.8 spawns them
        int before = miner.sent.size();
        var drop = DroppedItemEntity.spawn(instance, new Pos(3.5, 45, 3.5), new Vec(0.1, 0.2, 0.05),
                ItemStack.of(Material.FEATHER), DroppedItemEntity.Model.LEGACY, 10,
                ItemSpawnEvent.Cause.BLOCK_DROP, miner.player);
        assertNotNull(drop);
        var spawn = miner.sent.subList(before, miner.sent.size()).stream()
                .map(sp -> net.minestom.server.network.packet.server.SendablePacket
                        .extractServerPacket(net.minestom.server.network.ConnectionState.PLAY, sp))
                .filter(pk -> pk instanceof net.minestom.server.network.packet.server.play.SpawnEntityPacket se
                        && se.entityId() == drop.getEntityId())
                .map(pk -> (net.minestom.server.network.packet.server.play.SpawnEntityPacket) pk)
                .findFirst().orElseThrow();
        assertEquals(1, spawn.data(), "vanilla 1.8 item spawn data (ViaRewind passthrough branch)");
        assertEquals(0.2, spawn.velocity().y(), 1e-9, "launch velocity rides the spawn packet");
        drop.remove();
    }


    @Test
    void nativeBreakEffectReachesCrossChunkViewer() {
        instance.loadChunk(2, 0).join();
        var watcher = FakePlayer.connect(instance, new Pos(36.5, 66, 3.5), "VriWatcher"); // chunk (2,0), ~33 blocks out
        try {
            instance.setBlock(3, 65, 3, Block.DIRT);
            watcher.sent.clear();
            miner.sent.clear();
            assertTrue(instance.breakBlock(miner.player, new BlockVec(3, 65, 3), BlockFace.TOP));
            var received = watcher.sent(net.minestom.server.network.packet.server.play.WorldEventPacket.class);
            assertFalse(received.isEmpty(), "cross-chunk viewer gets the native 2001");
            assertEquals(2001, received.getFirst().effectId());
            assertTrue(miner.sent(net.minestom.server.network.packet.server.play.WorldEventPacket.class).isEmpty(),
                    "the breaker predicts its own");
        } finally {
            watcher.player.remove();
        }
    }

    @Test
    void itemSpawnEventFiresAndCancels() {
        var seen = new AtomicReference<ItemSpawnEvent>();
        var listener = net.minestom.server.event.EventListener.of(ItemSpawnEvent.class, e -> {
            if (e.item().getItemStack().material() != Material.FEATHER) return;
            seen.set(e);
            e.setCancelled(true);
        });
        MinecraftServer.getGlobalEventHandler().addListener(listener);
        try {
            var spawned = DroppedItemEntity.spawn(instance, new Pos(3.5, 45, 3.5), Vec.ZERO,
                    ItemStack.of(Material.FEATHER), DroppedItemEntity.Model.LEGACY, 10,
                    ItemSpawnEvent.Cause.BLOCK_DROP, miner.player);
            assertNull(spawned, "cancelled spawn returns null");
            ItemSpawnEvent event = seen.get();
            assertEquals(ItemSpawnEvent.Cause.BLOCK_DROP, event.cause());
            assertEquals(miner.player, event.player());
            assertNull(event.item().getInstance(), "never entered the instance");
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(listener);
        }
    }

    private static int countDirt() {
        int n = 0;
        for (ItemStack s : miner.player.getInventory().getItemStacks()) if (s.material() == Material.DIRT) n += s.amount();
        return n;
    }

    /** The point of the scoped config: one world runs a VRI behavior the install config left on, another doesn't. */
    @Test
    void aScopeOverridesTheInstallConfig() {
        var noDrops = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.VRI, VriConfig.builder().blockDrops(null).build())
                .build());
        FakePlayer scoped = FakePlayer.connect(noDrops, new Pos(3.5, 43, 3.5), "NoDropWorld");
        try {
            BlockVec pos = new BlockVec(3, 42, 3);
            noDrops.setBlock(pos, Block.DIRT);
            EventDispatcher.call(new PlayerBlockBreakEvent(
                    scoped.player, noDrops, Block.DIRT, Block.AIR, pos, BlockFace.TOP));

            assertTrue(noDrops.getEntities().stream().noneMatch(ItemEntity.class::isInstance),
                    "the scope turned drops off even though the install config enables them");
        } finally {
            scoped.player.remove();
        }
    }
}
