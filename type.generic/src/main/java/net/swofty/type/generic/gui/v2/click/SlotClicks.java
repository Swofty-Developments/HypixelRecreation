package net.swofty.type.generic.gui.v2.click;

public final class SlotClicks {

    private SlotClicks() {
    }

    public enum DragMode {
        EVEN,
        SINGLE,
        FULL
    }

    public record Transfer(SlotStack slot, SlotStack cursor, boolean changed) {
        static Transfer unchanged(SlotStack slot, SlotStack cursor) {
            return new Transfer(slot, cursor, false);
        }

        static Transfer of(SlotStack slot, SlotStack cursor) {
            return new Transfer(slot, cursor, true);
        }
    }

    public record DragResult(SlotStack[] slots, SlotStack cursor, boolean changed) {
    }

    public record QuickMove(SlotStack[] slots, SlotStack leftover, boolean changed) {
    }

    public static Transfer left(SlotStack slot, SlotStack cursor) {
        if (cursor.isEmpty()) {
            if (slot.isEmpty()) return Transfer.unchanged(slot, cursor);
            return Transfer.of(SlotStack.EMPTY, slot);
        }
        if (slot.isEmpty()) {
            int placed = Math.min(cursor.amount(), cursor.maxStackSize());
            return Transfer.of(cursor.withAmount(placed), cursor.shrink(placed));
        }
        if (slot.stacksWith(cursor)) {
            int moved = Math.min(cursor.amount(), slot.room());
            if (moved <= 0) return Transfer.unchanged(slot, cursor);
            return Transfer.of(slot.grow(moved), cursor.shrink(moved));
        }
        return Transfer.of(cursor, slot);
    }

    public static Transfer right(SlotStack slot, SlotStack cursor) {
        if (cursor.isEmpty()) {
            if (slot.isEmpty()) return Transfer.unchanged(slot, cursor);
            int taken = (slot.amount() + 1) / 2;
            return Transfer.of(slot.shrink(taken), slot.withAmount(taken));
        }
        if (slot.isEmpty()) {
            return Transfer.of(cursor.withAmount(1), cursor.shrink(1));
        }
        if (slot.stacksWith(cursor)) {
            if (slot.room() <= 0) return Transfer.unchanged(slot, cursor);
            return Transfer.of(slot.grow(1), cursor.shrink(1));
        }
        return Transfer.of(cursor, slot);
    }

    public static Transfer swap(SlotStack slot, SlotStack other) {
        if (slot.isEmpty() && other.isEmpty()) return Transfer.unchanged(slot, other);
        return Transfer.of(other, slot);
    }

    public static DragResult drag(SlotStack cursor, SlotStack[] targets, DragMode mode) {
        if (cursor.isEmpty() || targets.length == 0) {
            return new DragResult(targets, cursor, false);
        }
        if (mode != DragMode.FULL && cursor.amount() < targets.length) {
            return new DragResult(targets, cursor, false);
        }

        int perSlot = switch (mode) {
            case EVEN -> cursor.amount() / targets.length;
            case SINGLE -> 1;
            case FULL -> cursor.maxStackSize();
        };
        if (perSlot <= 0) {
            return new DragResult(targets, cursor, false);
        }

        SlotStack[] result = targets.clone();
        int remaining = cursor.amount();
        boolean changed = false;

        for (int i = 0; i < result.length; i++) {
            SlotStack target = result[i];
            if (!target.isEmpty() && !target.stacksWith(cursor)) continue;

            int existing = target.isEmpty() ? 0 : target.amount();
            int placed = Math.min(existing + perSlot, cursor.maxStackSize()) - existing;
            if (mode != DragMode.FULL) {
                placed = Math.min(placed, remaining);
            }
            if (placed <= 0) continue;

            result[i] = cursor.withAmount(existing + placed);
            remaining -= placed;
            changed = true;
        }

        if (!changed) {
            return new DragResult(targets, cursor, false);
        }
        return new DragResult(result, mode == DragMode.FULL ? cursor : cursor.withAmount(remaining), true);
    }

    public static QuickMove quickMove(SlotStack moving, SlotStack[] slots, int[] order) {
        if (moving.isEmpty()) {
            return new QuickMove(slots, moving, false);
        }

        SlotStack[] result = slots.clone();
        SlotStack remaining = moving;
        boolean changed = false;

        for (int index : order) {
            if (remaining.isEmpty()) break;
            SlotStack target = result[index];
            if (!target.stacksWith(remaining)) continue;

            int moved = Math.min(remaining.amount(), target.room());
            if (moved <= 0) continue;

            result[index] = target.grow(moved);
            remaining = remaining.shrink(moved);
            changed = true;
        }

        if (!remaining.isEmpty()) {
            for (int index : order) {
                if (!result[index].isEmpty()) continue;

                int moved = Math.min(remaining.amount(), remaining.maxStackSize());
                result[index] = remaining.withAmount(moved);
                remaining = remaining.shrink(moved);
                changed = true;
                break;
            }
        }

        return changed ? new QuickMove(result, remaining, true) : new QuickMove(slots, moving, false);
    }

    public static QuickMove collect(SlotStack cursor, SlotStack[] slots, int[] order) {
        if (cursor.isEmpty() || cursor.room() <= 0) {
            return new QuickMove(slots, cursor, false);
        }

        SlotStack[] result = slots.clone();
        SlotStack held = cursor;
        boolean changed = false;

        for (int pass = 0; pass < 2; pass++) {
            for (int index : order) {
                if (held.room() <= 0) break;
                SlotStack target = result[index];
                if (!target.stacksWith(held)) continue;
                if (pass == 0 && target.amount() >= target.maxStackSize()) continue;

                int taken = Math.min(held.room(), target.amount());
                if (taken <= 0) continue;

                result[index] = target.shrink(taken);
                held = held.grow(taken);
                changed = true;
            }
        }

        return changed ? new QuickMove(result, held, true) : new QuickMove(slots, cursor, false);
    }
}
