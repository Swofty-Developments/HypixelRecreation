package net.swofty.type.generic.gui.v2.click;

import net.minestom.server.item.ItemStack;

public final class ItemSlots {

    private ItemSlots() {
    }

    public static SlotStack toSlot(ItemStack stack) {
        if (stack == null || stack.isAir()) return SlotStack.EMPTY;
        return SlotStack.of(stack.withAmount(1), stack.amount(), stack.maxStackSize());
    }

    public static ItemStack toStack(SlotStack slot) {
        if (slot.isEmpty()) return ItemStack.AIR;
        return ((ItemStack) slot.item()).withAmount(slot.amount());
    }

    public static SlotStack[] toSlots(ItemStack[] stacks) {
        SlotStack[] slots = new SlotStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            slots[i] = toSlot(stacks[i]);
        }
        return slots;
    }
}
