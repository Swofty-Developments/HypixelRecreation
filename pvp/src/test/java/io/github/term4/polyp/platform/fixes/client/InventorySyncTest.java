package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.entity.GameMode;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket.ClickType;
import net.minestom.server.network.packet.server.play.RespawnPacket;
import net.minestom.server.network.packet.server.play.SetCursorItemPacket;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.network.packet.server.play.data.PlayerSpawnInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The remote-slot mirror: a slot echo the client already predicts is dropped, a genuine change is sent. Headline case
 * is the captured high-lag flicker - a number-key swap out and back nets zero, every transition echo suppressed.
 */
class InventorySyncTest extends HeadlessServerTest {

    private static final ItemStack APPLES = ItemStack.of(Material.GOLDEN_APPLE, 16);

    /** The click packet carries a hash the sync never reads; any valid one does. */
    private static ItemStack.Hash airHash() {
        return ItemStack.Hash.of(ItemStack.AIR, net.minestom.server.MinecraftServer.process());
    }

    private static ClientClickWindowPacket click(ClickType type, int wireSlot, int button) {
        return new ClientClickWindowPacket(0, 0, (short) wireSlot, (byte) button, type, Map.of(), airHash());
    }

    /** Window slot 36 = hotbar 0. */
    private static List<ItemStack> windowWithApples() {
        List<ItemStack> items = new ArrayList<>(Collections.nCopies(46, ItemStack.AIR));
        items.set(36, APPLES);
        return items;
    }

    @Test
    void netZeroSwapSuppressesEveryEcho() { // the captured bug: tap "1" over an empty slot, then again
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "anchor: hotbar 0 holds the apples");

        sync.onClick(click(ClickType.SWAP, 35, 0), false); // main slot 35 <-> hotbar 0
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(35, APPLES)), "predicted -> dropped");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, ItemStack.AIR)), "predicted -> dropped");

        sync.onClick(click(ClickType.SWAP, 35, 0), false); // nets zero
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(35, ItemStack.AIR)), "predicted -> dropped");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "predicted -> dropped");
    }

    @Test
    void genuineChangeIsSentRedundantIsDropped() {
        InventorySync sync = new InventorySync();
        ItemStack five = ItemStack.of(Material.DIAMOND, 5);
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(10, five)), "AIR -> diamonds is a real change");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(10, five)), "same value again -> redundant, dropped");
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(10, five.withAmount(3))), "count change -> sent");
    }

    @Test
    void pickupTracksCursorRoundTrip() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(9, APPLES)), "anchor slot 9");

        sync.onClick(click(ClickType.PICKUP, 9, 0), false); // left-click
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(9, ItemStack.AIR)), "slot emptied - predicted");
        assertNull(sync.filter(new SetCursorItemPacket(APPLES)), "cursor holds the stack - predicted");

        sync.onClick(click(ClickType.PICKUP, 9, 0), false); // left-click again
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(9, APPLES)), "slot refilled - predicted");
        assertNull(sync.filter(new SetCursorItemPacket(ItemStack.AIR)), "cursor cleared - predicted");
    }

    @Test
    void respawnPacketForgetsTheMirrorSoTheResendPasses() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "anchor: hotbar 0 holds the apples");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "match -> dropped");

        // any client-rebuild packet (respawn, dimension change, reconfig) makes the client forget its inventory
        assertNotNull(sync.filter(new RespawnPacket(
                new PlayerSpawnInfo(0, "minestom:world", 0, GameMode.SURVIVAL, GameMode.SURVIVAL,
                        false, false, null, 0, 63), (byte) RespawnPacket.COPY_ALL)), "respawn passes through");
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)),
                "identical contents must be SENT after a respawn - the rebuilt client shows nothing");
    }

    @Test
    void throwDecrementsPredictedSlot() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(9, APPLES)), "anchor slot 9");
        sync.onClick(click(ClickType.THROW, 9, 0), false); // Q
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(9, APPLES.withAmount(15))), "one dropped - predicted");
    }

    @Test
    void legacyThrowIsNeverPredictedSoTheRepaintPasses() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(9, APPLES)), "anchor slot 9");
        // the ViaBackwards drop replay: a throw-click the 1.8 client never made, so it predicted nothing
        sync.onClick(click(ClickType.THROW, 9, 0), true);
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(9, APPLES.withAmount(15))),
                "the corrective echo must reach the 1.8 client");
    }

    @Test
    void windowItemsReBaselines() {
        InventorySync sync = new InventorySync();
        List<ItemStack> items = windowWithApples();
        assertNotNull(sync.filter(new WindowItemsPacket(0, 0, items, ItemStack.AIR)), "full window always passes");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "matches the new baseline - dropped");
    }

    @Test
    void leftDragDistributesAndSuppressesResync() {
        InventorySync sync = new InventorySync();
        ItemStack stone = ItemStack.of(Material.STONE, 64);
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(9, stone)), "anchor slot 9 = 64 stone");
        sync.onClick(click(ClickType.PICKUP, 9, 0), false);
        sync.onClick(click(ClickType.QUICK_CRAFT, -999, 0), false); // drag start (left)
        sync.onClick(click(ClickType.QUICK_CRAFT, 10, 1), false);   // add slot 10
        sync.onClick(click(ClickType.QUICK_CRAFT, 11, 1), false);   // add slot 11
        sync.onClick(click(ClickType.QUICK_CRAFT, -999, 2), false); // end

        assertNull(sync.filter(new SetPlayerInventorySlotPacket(10, stone.withAmount(32))), "slot 10 got 32 - predicted");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(11, stone.withAmount(32))), "slot 11 got 32 - predicted");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(9, ItemStack.AIR)), "source slot emptied - predicted");
        assertNull(sync.filter(new SetCursorItemPacket(ItemStack.AIR)), "cursor drained - predicted");
    }

    @Test
    void redundantWindowItemsIsDroppedChangedIsSent() {
        InventorySync sync = new InventorySync();
        List<ItemStack> items = windowWithApples();
        assertNotNull(sync.filter(new WindowItemsPacket(0, 0, items, ItemStack.AIR)), "first full window sent + baselines");
        assertNull(sync.filter(new WindowItemsPacket(0, 0, items, ItemStack.AIR)), "identical resync is redundant - dropped");

        List<ItemStack> changed = new ArrayList<>(items);
        changed.set(36, APPLES.withAmount(15));
        assertNotNull(sync.filter(new WindowItemsPacket(0, 0, changed, ItemStack.AIR)), "a differing resync is sent");
    }

    /**
     * A refused placement is the one case the mirror cannot vouch for: the client already showed the stack shrink,
     * the refusal carries the ORIGINAL stack, and a stale mirror matches it. Dropping there is a stuck desync.
     */
    @Test
    void aRefusedPlacementResyncSurvivesTheMirror() {
        InventorySync sync = new InventorySync();
        List<ItemStack> items = windowWithApples();
        assertNotNull(sync.filter(new WindowItemsPacket(0, 0, items, ItemStack.AIR)), "baseline");

        sync.onPredictedUse(0); // the client predicted 16 -> 15
        assertNotNull(sync.filter(new WindowItemsPacket(0, 0, items, ItemStack.AIR)),
                "the refund carries the pre-placement stack - it must reach the client even though the mirror matches");
        assertNull(sync.filter(new WindowItemsPacket(0, 0, items, ItemStack.AIR)), "and re-anchors: the next one is redundant again");
    }

    @Test
    void aRefusedPlacementSlotRefundSurvivesButOtherSlotsStaySuppressed() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "baseline hotbar 0");
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(1, APPLES)), "baseline hotbar 1");

        sync.onPredictedUse(0);
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(1, APPLES)), "an unrelated slot is still suppressed");
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "the refund for the predicted slot is sent");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "re-anchored - suppressed again");
    }

    @Test
    void shiftClickHotbarToMainMovesAndSuppresses() {
        InventorySync sync = new InventorySync();
        ItemStack stick = ItemStack.of(Material.STICK, 1);
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, stick)), "anchor hotbar 0 = stick");
        sync.onClick(click(ClickType.QUICK_MOVE, 36, 0), false); // shift-click hotbar 0 (window slot 36)
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(9, stick)), "moved to first main slot 9 - predicted");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, ItemStack.AIR)), "hotbar 0 emptied - predicted");
    }

    @Test
    void shiftClickMergesThenOverflows() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, ItemStack.of(Material.STONE, 10))), "hotbar 0 = 10 stone");
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(9, ItemStack.of(Material.STONE, 60))), "main 9 = 60 stone");
        sync.onClick(click(ClickType.QUICK_MOVE, 36, 0), false); // shift-click the 10
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(9, ItemStack.of(Material.STONE, 64))), "topped 60->64 - predicted");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(10, ItemStack.of(Material.STONE, 6))), "overflow 6 to next slot - predicted");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, ItemStack.AIR)), "source emptied - predicted");
    }

    @Test
    void doubleClickGathersMatchingStacks() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(10, ItemStack.of(Material.STONE, 20))), "slot 10 = 20 stone");
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(11, ItemStack.of(Material.STONE, 30))), "slot 11 = 30 stone");
        sync.onClick(click(ClickType.PICKUP, 10, 0), false);
        sync.onClick(click(ClickType.PICKUP_ALL, 10, 0), false); // double-click
        assertNull(sync.filter(new SetCursorItemPacket(ItemStack.of(Material.STONE, 50))), "cursor gathered 20+30=50 - predicted");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(11, ItemStack.AIR)), "slot 11 drained - predicted");
    }

    @Test
    void cachedPacketsPassThroughUnframed() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "anchor: hotbar 0");
        // a shared CachedPacket must NOT be unwrapped: framing it for a hardcoded state poisoned Minestom's
        // config-phase tags cache (play id 134 -> every modern join crashed)
        var cached = new net.minestom.server.network.packet.server.CachedPacket(
                () -> new SetPlayerInventorySlotPacket(0, APPLES.withAmount(1)));
        assertNotNull(sync.filter(cached), "passes through");
        assertNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)),
                "mirror untouched by the cached packet's contents");
    }

    @Test
    void containerWindowClickLeavesMirrorUntouched() {
        InventorySync sync = new InventorySync();
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, APPLES)), "anchor hotbar 0");
        sync.onClick(new ClientClickWindowPacket(3, 0, (short) 35, (byte) 0, ClickType.SWAP, Map.of(), airHash()), false);
        assertNotNull(sync.filter(new SetPlayerInventorySlotPacket(0, ItemStack.AIR)), "no window-0 prediction, so the echo is a real change");
    }
}
