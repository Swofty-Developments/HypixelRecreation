package io.github.term4.polyp.mechanics.blocking.catalog;

import io.github.term4.polyp.mechanics.blocking.BlockingSystem;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlocksAttacks;

import java.util.List;

/**
 * Helpers for blockable vanilla items + the sword material set. An item blocks only if it carries the
 * {@code blocks_attacks} component (stamped by {@link #withBlocking}) AND its material is configured blockable in the
 * scope {@link io.github.term4.polyp.mechanics.blocking.BlockingConfig} (see {@code Vanilla18.blocking()}).
 */
public final class VanillaBlocking {

    private VanillaBlocking() {}

    /** Sword materials (the 1.8 sword-block set); {@code Vanilla18.blocking()} maps these to the sword behavior. */
    public static final Material[] SWORDS = {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD
    };

    /** All-zero block component: drives the use time + block pose and predicts no client-side reduction (the server reduces). */
    private static final BlocksAttacks BLOCK_COMPONENT = new BlocksAttacks(
            0f, 0f, List.of(), new BlocksAttacks.ItemDamageFunction(0f, 0f, 0f), null, null, null);

    /**
     * Stamps the all-zero {@code blocks_attacks} component - what makes an item block. The client predicts the block
     * from it on every version (the use pose, incl. the 1.8 {@code 0x10} via Via); the server does the actual reduction.
     * A component-less item never blocks, which avoids the modern-client desync.
     */
    public static ItemStack withBlocking(ItemStack stack) {
        if (stack == null || stack.isAir()) return stack;
        return stack.with(DataComponents.BLOCKS_ATTACKS, BLOCK_COMPONENT);
    }

    /** A blocking item of {@code material} (e.g. {@code VanillaBlocking.item(Material.DIAMOND_SWORD)}); its material must also be configured blockable. */
    public static ItemStack item(Material material) {
        return withBlocking(ItemStack.of(material));
    }

    /**
     * Marks {@code stack} non-blockable even when its material is configured blockable: it won't enter the block state or
     * affect damage. Sets the {@link BlockingSystem#BLOCKABLE} opt-out tag and strips the block component.
     */
    public static ItemStack nonBlocking(ItemStack stack) {
        if (stack == null || stack.isAir()) return stack;
        return stack.withTag(BlockingSystem.BLOCKABLE, false).without(DataComponents.BLOCKS_ATTACKS);
    }
}
