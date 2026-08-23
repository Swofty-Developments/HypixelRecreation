package net.swofty.type.generic.gui.v2.click;

import org.jetbrains.annotations.Nullable;

public record SlotStack(@Nullable Object item, int amount, int maxStackSize) {

    public static final SlotStack EMPTY = new SlotStack(null, 0, 0);

    public static SlotStack of(@Nullable Object item, int amount, int maxStackSize) {
        if (item == null || amount <= 0 || maxStackSize <= 0) return EMPTY;
        return new SlotStack(item, amount, maxStackSize);
    }

    public boolean isEmpty() {
        return item == null || amount <= 0;
    }

    public SlotStack withAmount(int newAmount) {
        if (isEmpty()) return EMPTY;
        return newAmount <= 0 ? EMPTY : new SlotStack(item, newAmount, maxStackSize);
    }

    public SlotStack grow(int delta) {
        return withAmount(amount + delta);
    }

    public SlotStack shrink(int delta) {
        return withAmount(amount - delta);
    }

    public boolean stacksWith(SlotStack other) {
        return !isEmpty() && !other.isEmpty() && item.equals(other.item);
    }

    public int room() {
        return isEmpty() ? 0 : Math.max(0, maxStackSize - amount);
    }
}
