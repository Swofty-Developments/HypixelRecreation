package net.swofty.type.skyblockgeneric.item.crafting.grid;

import net.swofty.type.generic.gui.v2.click.SlotClicks;
import net.swofty.type.generic.gui.v2.click.SlotStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CraftingGridModel {

    public static final int GRID_SIZE = 9;
    public static final int PLAYER_SIZE = 36;
    public static final int HOTBAR_SIZE = 9;

    private static final int MAX_CRAFT_ITERATIONS = 1024;

    private static final int[] GRID_ORDER = range(0, GRID_SIZE, 1);
    private static final int[] HOTBAR_FORWARD = range(0, HOTBAR_SIZE, 1);
    private static final int[] STORAGE_FORWARD = range(HOTBAR_SIZE, PLAYER_SIZE, 1);
    private static final int[] STORAGE_THEN_HOTBAR = concat(STORAGE_FORWARD, HOTBAR_FORWARD);
    private static final int[] HOTBAR_THEN_STORAGE_REVERSED =
            concat(range(HOTBAR_SIZE - 1, -1, -1), range(PLAYER_SIZE - 1, HOTBAR_SIZE - 1, -1));

    private final CraftingResolver resolver;
    private final SlotStack[] grid;
    private SlotStack[] player;
    private SlotStack cursor;
    private SlotStack result;

    private SlotStack craftedOutput = SlotStack.EMPTY;
    private int craftedTotal;
    private int craftCount;

    public CraftingGridModel(CraftingResolver resolver, SlotStack[] grid, SlotStack[] player, SlotStack cursor) {
        this.resolver = resolver;
        this.grid = padded(grid, GRID_SIZE);
        this.player = padded(player, PLAYER_SIZE);
        this.cursor = cursor == null ? SlotStack.EMPTY : cursor;
        this.result = resolver.result(this.grid);
    }

    public SlotStack[] grid() {
        return grid.clone();
    }

    public SlotStack[] playerItems() {
        return player.clone();
    }

    public SlotStack cursor() {
        return cursor;
    }

    public SlotStack result() {
        return result;
    }

    public SlotStack craftedOutput() {
        return craftedOutput;
    }

    public int craftedTotal() {
        return craftedTotal;
    }

    public int craftCount() {
        return craftCount;
    }

    public SlotStack peek(CraftingSlot slot) {
        return switch (slot.region()) {
            case GRID -> grid[slot.index()];
            case PLAYER -> player[slot.index()];
            case RESULT -> result;
        };
    }

    public boolean pickUp(CraftingSlot slot, boolean half) {
        if (slot.region() == CraftingSlot.Region.RESULT) {
            return takeResult();
        }

        SlotStack current = peek(slot);
        SlotClicks.Transfer transfer = half ? SlotClicks.right(current, cursor) : SlotClicks.left(current, cursor);
        if (!transfer.changed()) return false;

        set(slot, transfer.slot());
        cursor = transfer.cursor();
        if (slot.region() == CraftingSlot.Region.GRID) recomputeResult();
        return true;
    }

    public boolean quickMove(CraftingSlot slot) {
        return switch (slot.region()) {
            case RESULT -> craftUntilBlocked();
            case GRID -> quickMoveFromGrid(slot.index());
            case PLAYER -> quickMoveFromPlayer(slot.index());
        };
    }

    public boolean drag(List<CraftingSlot> slots, SlotClicks.DragMode mode) {
        if (cursor.isEmpty()) return false;

        List<CraftingSlot> targets = new ArrayList<>(slots.size());
        for (CraftingSlot slot : slots) {
            if (slot.region() == CraftingSlot.Region.RESULT) continue;
            SlotStack current = peek(slot);
            if (!current.isEmpty() && !current.stacksWith(cursor)) continue;
            targets.add(slot);
        }
        if (targets.isEmpty()) return false;

        SlotStack[] before = new SlotStack[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            before[i] = peek(targets.get(i));
        }

        SlotClicks.DragResult dragged = SlotClicks.drag(cursor, before, mode);
        if (!dragged.changed()) return false;

        boolean touchedGrid = false;
        for (int i = 0; i < targets.size(); i++) {
            CraftingSlot target = targets.get(i);
            set(target, dragged.slots()[i]);
            touchedGrid |= target.region() == CraftingSlot.Region.GRID;
        }
        cursor = dragged.cursor();
        if (touchedGrid) recomputeResult();
        return true;
    }

    public boolean swap(CraftingSlot slot, int playerIndex) {
        if (playerIndex < 0 || playerIndex >= PLAYER_SIZE) return false;

        if (slot.region() == CraftingSlot.Region.RESULT) {
            if (!player[playerIndex].isEmpty()) return false;
            SlotStack output = result;
            if (output.isEmpty()) return false;

            consumeOnce();
            player[playerIndex] = output;
            recordCraft(output);
            return true;
        }

        SlotClicks.Transfer transfer = SlotClicks.swap(peek(slot), player[playerIndex]);
        if (!transfer.changed()) return false;

        set(slot, transfer.slot());
        player[playerIndex] = transfer.cursor();
        if (slot.region() == CraftingSlot.Region.GRID) recomputeResult();
        return true;
    }

    public boolean collect(SlotStack held) {
        if (held.isEmpty() || held.room() <= 0) return false;

        SlotClicks.QuickMove fromGrid = SlotClicks.collect(held, grid, GRID_ORDER);
        SlotStack collected = fromGrid.leftover();
        boolean changed = fromGrid.changed();
        if (changed) {
            System.arraycopy(fromGrid.slots(), 0, grid, 0, GRID_SIZE);
            recomputeResult();
        }

        SlotClicks.QuickMove fromPlayer = SlotClicks.collect(collected, player, STORAGE_THEN_HOTBAR);
        if (fromPlayer.changed()) {
            player = fromPlayer.slots();
            collected = fromPlayer.leftover();
            changed = true;
        }

        if (!changed) return false;
        cursor = collected;
        return true;
    }

    public boolean cloneToCursor(CraftingSlot slot) {
        if (slot.region() == CraftingSlot.Region.RESULT) return false;
        if (!cursor.isEmpty()) return false;

        SlotStack current = peek(slot);
        if (current.isEmpty()) return false;

        cursor = current.withAmount(current.maxStackSize());
        return true;
    }

    public boolean removeFromSlot(CraftingSlot slot, int amount) {
        if (slot.region() == CraftingSlot.Region.RESULT) return false;

        SlotStack current = peek(slot);
        if (current.isEmpty() || amount <= 0) return false;

        set(slot, current.shrink(amount));
        if (slot.region() == CraftingSlot.Region.GRID) recomputeResult();
        return true;
    }

    public boolean removeFromCursor(int amount) {
        if (cursor.isEmpty() || amount <= 0) return false;
        cursor = cursor.shrink(amount);
        return true;
    }

    public void setCursor(SlotStack stack) {
        this.cursor = stack == null ? SlotStack.EMPTY : stack;
    }

    private boolean takeResult() {
        SlotStack output = result;
        if (output.isEmpty()) return false;

        if (cursor.isEmpty()) {
            consumeOnce();
            cursor = output;
        } else if (cursor.stacksWith(output) && cursor.amount() + output.amount() <= cursor.maxStackSize()) {
            consumeOnce();
            cursor = cursor.grow(output.amount());
        } else {
            return false;
        }

        recordCraft(output);
        return true;
    }

    private boolean craftUntilBlocked() {
        int crafted = 0;
        SlotStack first = SlotStack.EMPTY;

        while (crafted < MAX_CRAFT_ITERATIONS) {
            SlotStack output = result;
            if (output.isEmpty()) break;
            if (!first.isEmpty() && !first.stacksWith(output)) break;
            if (first.isEmpty()) first = output;

            SlotClicks.QuickMove moved = SlotClicks.quickMove(output, player, HOTBAR_THEN_STORAGE_REVERSED);
            if (!moved.changed() || !moved.leftover().isEmpty()) break;

            player = moved.slots();
            consumeOnce();
            recordCraft(output);
            crafted++;
        }

        return crafted > 0;
    }

    private boolean quickMoveFromGrid(int index) {
        SlotClicks.QuickMove moved = SlotClicks.quickMove(grid[index], player, STORAGE_THEN_HOTBAR);
        if (!moved.changed()) return false;

        player = moved.slots();
        grid[index] = moved.leftover();
        recomputeResult();
        return true;
    }

    private boolean quickMoveFromPlayer(int index) {
        SlotClicks.QuickMove intoGrid = SlotClicks.quickMove(player[index], grid, GRID_ORDER);
        if (intoGrid.changed()) {
            System.arraycopy(intoGrid.slots(), 0, grid, 0, GRID_SIZE);
            player[index] = intoGrid.leftover();
            recomputeResult();
            return true;
        }

        int[] fallback = index < HOTBAR_SIZE ? STORAGE_FORWARD : HOTBAR_FORWARD;
        SlotClicks.QuickMove shuffled = SlotClicks.quickMove(player[index], player, fallback);
        if (!shuffled.changed()) return false;

        player = shuffled.slots();
        player[index] = shuffled.leftover();
        return true;
    }

    private void set(CraftingSlot slot, SlotStack stack) {
        switch (slot.region()) {
            case GRID -> grid[slot.index()] = stack;
            case PLAYER -> player[slot.index()] = stack;
            case RESULT -> {
            }
        }
    }

    private void consumeOnce() {
        SlotStack[] consumed = resolver.consume(grid);
        System.arraycopy(padded(consumed, GRID_SIZE), 0, grid, 0, GRID_SIZE);
        recomputeResult();
    }

    private void recomputeResult() {
        result = resolver.result(grid);
    }

    private void recordCraft(SlotStack output) {
        craftedOutput = output;
        craftedTotal += output.amount();
        craftCount++;
    }

    private static SlotStack[] padded(SlotStack[] source, int size) {
        SlotStack[] out = new SlotStack[size];
        Arrays.fill(out, SlotStack.EMPTY);
        if (source != null) {
            for (int i = 0; i < Math.min(size, source.length); i++) {
                out[i] = source[i] == null ? SlotStack.EMPTY : source[i];
            }
        }
        return out;
    }

    private static int[] range(int from, int toExclusive, int step) {
        int length = step > 0 ? Math.max(0, toExclusive - from) : Math.max(0, from - toExclusive);
        int[] values = new int[length];
        int value = from;
        for (int i = 0; i < length; i++) {
            values[i] = value;
            value += step;
        }
        return values;
    }

    private static int[] concat(int[] first, int[] second) {
        int[] out = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, out, first.length, second.length);
        return out;
    }
}
