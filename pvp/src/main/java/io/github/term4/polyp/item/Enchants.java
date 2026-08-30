package io.github.term4.polyp.item;

import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** Reads an enchantment level off an item's {@code ENCHANTMENTS} component by {@link Key}. */
public final class Enchants {

    private Enchants() {}

    /** Level of {@code key} on {@code stack}, or {@code 0} if absent / air / unenchanted. */
    public static int level(@Nullable ItemStack stack, Key key) {
        if (stack == null || stack.isAir()) return 0;
        EnchantmentList list = stack.get(DataComponents.ENCHANTMENTS);
        if (list == null) return 0;
        for (Map.Entry<RegistryKey<Enchantment>, Integer> e : list.enchantments().entrySet()) {
            if (e.getKey().key().equals(key)) return e.getValue();
        }
        return 0;
    }
}
