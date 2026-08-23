package net.swofty.type.generic.entity.drop;

import net.minestom.server.item.ItemStack;

public final class ItemStackMerging {

    private ItemStackMerging() {
    }

    public static boolean canMerge(int firstAmount, int secondAmount, int maxStackSize) {
        if (firstAmount <= 0 || secondAmount <= 0) return false;
        return firstAmount + secondAmount <= maxStackSize;
    }

    public static int mergedAmount(int firstAmount, int secondAmount) {
        return firstAmount + secondAmount;
    }

    public static boolean canMerge(ItemStack first, ItemStack second) {
        if (first.isAir() || second.isAir()) return false;
        if (!first.isSimilar(second)) return false;
        return canMerge(first.amount(), second.amount(), first.maxStackSize());
    }

    public static ItemStack merge(ItemStack first, ItemStack second) {
        return first.withAmount(mergedAmount(first.amount(), second.amount()));
    }
}
