package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.PacketShapes;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.JoinGamePacket;
import net.minestom.server.network.packet.server.play.RespawnPacket;
import net.minestom.server.network.packet.server.play.SetCursorItemPacket;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.StartConfigurationPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Per-player inventory echo suppressor: the vanilla remote-slot synchronizer Minestom lacks. Minestom re-sends every
 * slot a click touches, so under lag a rapid move-and-revert flickers (the late echoes fight the client's own
 * prediction) - and a stray held-slot echo resets an in-progress eat. Vanilla instead mirrors what the client believes
 * each slot holds and sends only genuine corrections.
 *
 * <p>We rebuild that mirror by replaying the client's deterministic prediction per click mode. An outgoing slot echo
 * that matches the mirror is dropped; a mismatch (a rejected click, a server-side change) is sent and re-anchors the
 * mirror, so a wrong guess costs a redundant echo, never a stuck desync. The full {@link WindowItemsPacket}
 * re-baselines everything.
 *
 * <p><b>Experimental</b> ({@code FixesConfig#inventorySync}): a port of Minestom's click model, so a click-logic change
 * upstream can drift it.
 */
public final class InventorySync {

    private static volatile boolean enabled;

    public static boolean enabled() { return enabled; }

    /** Arms the fix and registers the click-prediction listener; call once from {@code FixesSystem.install}. */
    public static void install(EventNode<? super Event> node) {
        enabled = true;
        node.addListener(PlayerPacketEvent.class, e -> {
            if (!enabled || !(e.getPlayer() instanceof OptimizedPlayer op)) return;
            switch (e.getPacket()) {
                case ClientClickWindowPacket click ->
                        op.inventorySync().onClick(click, Polyp.getInstance().clientInfo().isLegacy(op));
                case ClientPlayerBlockPlacementPacket p -> op.inventorySync().onPredictedUse(held(op, p.hand()));
                case ClientUseItemPacket p -> op.inventorySync().onPredictedUse(held(op, p.hand()));
                default -> {}
            }
        });
        // Minestom resends the inventory after every client-rebuild flow EXCEPT the death respawn - fill that gap
        // (the preceding RespawnPacket already forgot the mirror, so this update passes)
        node.addListener(PlayerRespawnEvent.class, e -> e.getPlayer().getInventory().update(e.getPlayer()));
    }

    /** The client's believed slot contents, Minestom slot indexing (0-8 hotbar, 9-35 main, 36-40 craft, 41-44 armor, 45 offhand). */
    private final ItemStack[] believed = new ItemStack[PlayerInventory.INVENTORY_SIZE];

    /** Slot the client predicted outside the click model ({@link #onPredictedUse}), or -1. */
    private int unverifiedSlot = -1;
    private ItemStack believedCursor = ItemStack.AIR;
    /** Slots accumulated across a drag's start/add/end packets, mirroring Minestom's {@code ClickPreprocessor}. */
    private final Set<Integer> leftDrag = new LinkedHashSet<>();
    private final Set<Integer> rightDrag = new LinkedHashSet<>();
    private final Object lock = new Object();

    public InventorySync() { Arrays.fill(believed, ItemStack.AIR); }

    /** Forgets the mirror; call under {@link #lock}. */
    private void forget() {
        Arrays.fill(believed, ItemStack.AIR);
        believedCursor = ItemStack.AIR;
        unverifiedSlot = -1;
        leftDrag.clear();
        rightDrag.clear();
    }

    /**
     * The client predicted a change we do not model (a placement or use shrinks the held stack), so drop nothing for
     * {@code slot} until a server packet re-anchors it. Without this a refused placement is a STUCK desync: the refusal
     * carries the original stack, which the stale mirror matches and eats.
     */
    void onPredictedUse(int slot) {
        synchronized (lock) { unverifiedSlot = slot; }
    }

    // ------------------------------------------------------------------ incoming: replay the client's prediction

    /** Advances the mirror by the client's predicted result of {@code click} (player inventory only, supported modes). */
    void onClick(ClientClickWindowPacket click, boolean legacyClient) {
        if (click.windowId() != 0) return; // container-window clicks use a different slot space; left to fall through
        synchronized (lock) { predict(click, legacyClient); }
    }

    private void predict(ClientClickWindowPacket click, boolean legacyClient) {
        final short wireSlot = click.slot();
        final byte button = click.button();
        switch (click.clickType()) {
            case PICKUP -> {
                if (wireSlot == -999) { dropCursor(button); return; } // click outside the window = drop the cursor
                final int slot = slot(wireSlot);
                if (slot >= 0) pickup(slot, button);
            }
            case SWAP -> {
                final int slot = slot(wireSlot);
                final int hotbar = button == 40 ? PlayerInventoryUtils.OFFHAND_SLOT : (button >= 0 && button < 9 ? button : -1);
                if (slot >= 0 && hotbar >= 0) { ItemStack t = believed[slot]; believed[slot] = believed[hotbar]; believed[hotbar] = t; }
            }
            case THROW -> {
                // never predicted for a legacy client: ViaBackwards replays 1.8 hotbar drops as throw-clicks the client
                // never made, so the echo must pass to repaint the count (a real 1.8 GUI throw costs one redundant echo)
                if (legacyClient) return;
                final int slot = slot(wireSlot);
                if (slot < 0 || believed[slot].isAir()) return;
                believed[slot] = button == 1 ? ItemStack.AIR : decrement(believed[slot]); // ctrl-Q drops the whole stack
            }
            case QUICK_CRAFT -> { // drag: accumulate across start(0/4)/add(1/5), distribute on end(2/6); middle(8/9/10) skipped
                switch (button) {
                    case 0 -> leftDrag.clear();
                    case 4 -> rightDrag.clear();
                    case 1 -> { int s = slot(wireSlot); if (s >= 0) leftDrag.add(s); }
                    case 5 -> { int s = slot(wireSlot); if (s >= 0) rightDrag.add(s); }
                    case 2 -> { applyDrag(leftDrag, false); leftDrag.clear(); }
                    case 6 -> { applyDrag(rightDrag, true); rightDrag.clear(); }
                    default -> {}
                }
            }
            case QUICK_MOVE -> { int s = slot(wireSlot); if (s >= 0) shiftMove(s); } // shift-click
            case PICKUP_ALL -> { int s = slot(wireSlot); if (s >= 0) doubleClick(s); }
            case CLONE -> {} // creative middle-click clone: creative-only, left to fall through
        }
    }

    /** Shift-click: equip if armour, else merge/fill into the target section, matching Minestom's {@code PlayerInventory.shiftClick} + {@code TransactionType.ADD}. */
    private void shiftMove(int slot) {
        final ItemStack clicked = believed[slot];
        if (clicked.isAir()) return;
        final boolean craftingGrid = slot >= PlayerInventoryUtils.CRAFT_RESULT && slot <= PlayerInventoryUtils.CRAFT_SLOT_4;
        final Equippable eq = clicked.get(DataComponents.EQUIPPABLE);
        if (eq != null && !craftingGrid) {
            final int equip = equipmentSlot(eq.slot()); // ignores the exotic allowed-entities gate: a wrong guess only costs one resync
            if (equip >= 0 && believed[equip].isAir()) { believed[equip] = clicked; believed[slot] = ItemStack.AIR; return; }
        }
        final boolean[] moved = {false};
        ItemStack remaining = clicked;
        for (int[] range : shiftRanges(slot)) { // second range is a fallback Minestom only tries when the first moved nothing
            remaining = add(remaining, range[0], range[1], range[2], slot, moved);
            if (moved[0] || remaining.isAir()) break;
        }
        if (moved[0]) believed[slot] = remaining; // nothing moved -> Minestom cancels + full-updates; leave the mirror, that resync is sent
    }

    /** Double-click gather: pull similar stacks onto the cursor up to a full stack, matching {@code TransactionType.TAKE}. */
    private void doubleClick(int slot) {
        final ItemStack cursor = believedCursor;
        if (cursor.isAir()) return;
        final int max = cursor.maxStackSize();
        int need = max - cursor.amount();
        if (need <= 0) return;
        for (int i = 0; i < believed.length && need > 0; i++) {
            if (i == slot) continue; // never gathers from the double-clicked slot itself
            final ItemStack in = believed[i];
            if (in.isAir() || !cursor.isSimilar(in)) continue;
            final int take = Math.min(in.amount(), need);
            believed[i] = in.amount() <= need ? ItemStack.AIR : in.withAmount(in.amount() - take);
            need -= take;
        }
        believedCursor = cursor.withAmount(max - need);
    }

    /** Merges then fills {@code moving} across {@code [start,end)} step {@code step} (skipping {@code exclude}); returns the leftover, sets {@code moved}. */
    private ItemStack add(ItemStack moving, int start, int end, int step, int exclude, boolean[] moved) {
        for (int i = start; step > 0 ? i < end : i > end; i += step) { // top off existing similar stacks
            if (i == exclude || i < 0 || i >= believed.length) continue;
            final ItemStack in = believed[i];
            if (in.isAir() || !moving.isSimilar(in) || in.amount() >= in.maxStackSize()) continue;
            final int total = moving.amount() + in.amount();
            if (total > moving.maxStackSize()) {
                believed[i] = in.withAmount(in.maxStackSize());
                moving = moving.withAmount(total - in.maxStackSize());
                moved[0] = true;
            } else {
                believed[i] = in.withAmount(total);
                moved[0] = true;
                return ItemStack.AIR;
            }
        }
        for (int i = start; step > 0 ? i < end : i > end; i += step) { // then drop into empty slots
            if (i == exclude || i < 0 || i >= believed.length || !believed[i].isAir()) continue;
            if (moving.amount() > moving.maxStackSize()) {
                believed[i] = moving.withAmount(moving.maxStackSize());
                moving = moving.withAmount(moving.amount() - moving.maxStackSize());
                moved[0] = true;
            } else {
                believed[i] = moving;
                moved[0] = true;
                return ItemStack.AIR;
            }
        }
        return moving;
    }

    /** The target section(s) a shift-click scans, in Minestom's order (hotbar<->main, crafting/armour into inventory then hotbar). */
    private static int[][] shiftRanges(int slot) {
        if (slot < 9) return new int[][]{{9, PlayerInventory.INNER_INVENTORY_SIZE, 1}};       // hotbar -> main
        if (slot < PlayerInventory.INNER_INVENTORY_SIZE) return new int[][]{{0, 9, 1}};       // main -> hotbar
        if (slot == PlayerInventoryUtils.CRAFT_RESULT) return new int[][]{{8, -1, -1}, {PlayerInventory.INNER_INVENTORY_SIZE - 1, 8, -1}};
        return new int[][]{{9, PlayerInventory.INNER_INVENTORY_SIZE, 1}, {0, 9, 1}};          // craft grid / armour / offhand
    }

    private static int equipmentSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HELMET -> PlayerInventoryUtils.HELMET_SLOT;
            case CHESTPLATE -> PlayerInventoryUtils.CHESTPLATE_SLOT;
            case LEGGINGS -> PlayerInventoryUtils.LEGGINGS_SLOT;
            case BOOTS -> PlayerInventoryUtils.BOOTS_SLOT;
            case OFF_HAND -> PlayerInventoryUtils.OFFHAND_SLOT;
            default -> -1;
        };
    }

    /** Distributes the cursor over {@code slots} - one each ({@code single}, right-drag) or evenly (left-drag), matching Minestom's {@code dragging}. */
    private void applyDrag(Set<Integer> slots, boolean single) {
        final ItemStack cursor = believedCursor;
        final int slotCount = slots.size();
        final int cursorAmount = cursor.amount();
        if (cursor.isAir() || slotCount == 0 || slotCount > cursorAmount) return; // Minestom returns null here -> full update, left to fall through
        final int slotSize = single ? 1 : cursorAmount / slotCount;
        int remaining = cursorAmount;
        for (int slot : slots) {
            if (slot < 0 || slot >= believed.length) continue;
            final ItemStack in = believed[slot];
            if (in.isSimilar(cursor)) {
                final int merged = in.amount() + slotSize;
                if (merged <= in.maxStackSize()) {
                    believed[slot] = in.withAmount(merged);
                    remaining -= slotSize;
                } else if (!single) { // left-drag overflow: top the slot off, keep the rest on the cursor
                    believed[slot] = in.withAmount(cursor.maxStackSize());
                    remaining -= cursor.maxStackSize() - in.amount();
                }
            } else if (in.isAir()) {
                believed[slot] = cursor.withAmount(slotSize);
                remaining -= slotSize;
            } // different item: unchanged, cursor keeps its share
        }
        believedCursor = remaining <= 0 ? ItemStack.AIR : cursor.withAmount(remaining);
    }

    /** Left ({@code button 0}) / right ({@code button 1}) click onto {@code slot} with the cursor - vanilla pickup / place / merge / swap. */
    private void pickup(int slot, byte button) {
        final ItemStack in = believed[slot];
        final ItemStack cursor = believedCursor;
        final boolean right = button == 1;
        if (cursor.isAir()) {
            if (in.isAir()) return;
            if (right) { int take = (in.amount() + 1) / 2; believedCursor = in.withAmount(take); believed[slot] = shrink(in, take); }
            else { believedCursor = in; believed[slot] = ItemStack.AIR; }
            return;
        }
        if (in.isAir()) {
            if (right) { believed[slot] = cursor.withAmount(1); believedCursor = decrement(cursor); }
            else { believed[slot] = cursor; believedCursor = ItemStack.AIR; }
            return;
        }
        if (in.isSimilar(cursor)) {
            final int room = in.maxStackSize() - in.amount();
            if (room <= 0) return;
            final int move = right ? 1 : Math.min(room, cursor.amount());
            believed[slot] = in.withAmount(in.amount() + move);
            believedCursor = shrink(cursor, move);
        } else {
            believed[slot] = cursor;
            believedCursor = in;
        }
    }

    private void dropCursor(byte button) {
        if (believedCursor.isAir()) return;
        believedCursor = button == 1 ? decrement(believedCursor) : ItemStack.AIR;
    }

    // ------------------------------------------------------------------ outgoing: drop echoes that match the mirror

    /** The outgoing packet, or {@code null} to drop it (the client already shows what it carries). */
    public @Nullable SendablePacket filter(SendablePacket packet) {
        // stateless unwrap only: extractServerPacket would frame a CachedPacket's shared cache for a hardcoded state
        // (the poisoned-tags-cache join crash); no inventory packet is ever cached, so nothing is lost
        final ServerPacket server = PacketShapes.unwrapStateless(packet);
        if (server == null) return packet;
        synchronized (lock) {
            return switch (server) {
                case SetPlayerInventorySlotPacket p -> reconcile(PlayerInventoryUtils.convertPlayerInventorySlotToMinestomSlot(p.slot()), p.itemStack()) ? packet : null;
                case SetSlotPacket p when p.windowId() == 0 -> reconcile(PlayerInventoryUtils.convertWindow0SlotToMinestomSlot(p.slot()), p.itemStack()) ? packet : null;
                case SetCursorItemPacket p -> {
                    if (p.itemStack().equals(believedCursor)) yield null;
                    believedCursor = p.itemStack();
                    yield packet;
                }
                case WindowItemsPacket p when p.windowId() == 0 -> {
                    if (redundant(p)) yield null; // a full resync the client already matches (e.g. a correctly-predicted drag)
                    baseline(p);
                    yield packet;
                }
                // the client forgets its inventory on these (respawn, dimension/skin change, (re)configuration) -
                // forget the mirror with it, so the flow's follow-up resend isn't dropped as a mirror match
                case RespawnPacket ignored -> { forget(); yield packet; }
                case JoinGamePacket ignored -> { forget(); yield packet; }
                case StartConfigurationPacket ignored -> { forget(); yield packet; }
                default -> packet;
            };
        }
    }

    /** {@code true} = send (mirror re-anchored to {@code item}); {@code false} = drop (already matches). */
    private boolean reconcile(int slot, ItemStack item) {
        if (slot < 0 || slot >= believed.length) return true;
        if (slot == unverifiedSlot) unverifiedSlot = -1;
        else if (item.equals(believed[slot])) return false;
        believed[slot] = item;
        return true;
    }

    /** Whether {@code packet} carries exactly what the mirror already holds - a redundant full resync safe to drop. */
    private boolean redundant(WindowItemsPacket packet) {
        if (unverifiedSlot >= 0 || !packet.carriedItem().equals(believedCursor)) return false;
        final List<ItemStack> items = packet.items();
        for (int wire = 0; wire < items.size(); wire++) {
            final int slot = PlayerInventoryUtils.convertWindow0SlotToMinestomSlot(wire);
            if (slot >= 0 && slot < believed.length && !items.get(wire).equals(believed[slot])) return false;
        }
        return true;
    }

    private void baseline(WindowItemsPacket packet) {
        final List<ItemStack> items = packet.items();
        for (int wire = 0; wire < items.size(); wire++) {
            final int slot = PlayerInventoryUtils.convertWindow0SlotToMinestomSlot(wire);
            if (slot >= 0 && slot < believed.length) believed[slot] = items.get(wire);
        }
        believedCursor = packet.carriedItem();
        unverifiedSlot = -1;
    }

    // ------------------------------------------------------------------ helpers

    private static int held(OptimizedPlayer player, PlayerHand hand) {
        return hand == PlayerHand.OFF ? PlayerInventoryUtils.OFFHAND_SLOT : player.getHeldSlot();
    }

    private static int slot(short wireSlot) {
        if (wireSlot < 0) return -1;
        final int slot = PlayerInventoryUtils.convertWindow0SlotToMinestomSlot(wireSlot);
        return slot >= 0 && slot < PlayerInventory.INVENTORY_SIZE ? slot : -1;
    }

    private static ItemStack decrement(ItemStack item) { return shrink(item, 1); }

    private static ItemStack shrink(ItemStack item, int by) {
        final int left = item.amount() - by;
        return left <= 0 ? ItemStack.AIR : item.withAmount(left);
    }
}
