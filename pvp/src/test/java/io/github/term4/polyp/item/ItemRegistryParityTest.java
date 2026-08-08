package io.github.term4.polyp.item;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Golden pin for the 1.8 weapon attack-damage table. */
class ItemRegistryParityTest {

    private static final double FIST = 1.0;
    private static final ItemRegistry LEGACY = new ItemRegistry(ItemDef.Version.LEGACY, VanillaItems.weapons());

    private static final Map<Material, Double> EXPECTED = Map.ofEntries(
            Map.entry(Material.WOODEN_SWORD, 4.0), Map.entry(Material.GOLDEN_SWORD, 4.0), Map.entry(Material.STONE_SWORD, 5.0),
            Map.entry(Material.IRON_SWORD, 6.0), Map.entry(Material.DIAMOND_SWORD, 7.0), Map.entry(Material.NETHERITE_SWORD, 8.0),
            Map.entry(Material.WOODEN_AXE, 3.0), Map.entry(Material.GOLDEN_AXE, 3.0), Map.entry(Material.STONE_AXE, 4.0),
            Map.entry(Material.IRON_AXE, 5.0), Map.entry(Material.DIAMOND_AXE, 6.0), Map.entry(Material.NETHERITE_AXE, 7.0),
            Map.entry(Material.WOODEN_PICKAXE, 2.0), Map.entry(Material.GOLDEN_PICKAXE, 2.0), Map.entry(Material.STONE_PICKAXE, 3.0),
            Map.entry(Material.IRON_PICKAXE, 4.0), Map.entry(Material.DIAMOND_PICKAXE, 5.0), Map.entry(Material.NETHERITE_PICKAXE, 6.0),
            Map.entry(Material.WOODEN_SHOVEL, 1.0), Map.entry(Material.GOLDEN_SHOVEL, 1.0), Map.entry(Material.STONE_SHOVEL, 2.0),
            Map.entry(Material.IRON_SHOVEL, 3.0), Map.entry(Material.DIAMOND_SHOVEL, 4.0), Map.entry(Material.NETHERITE_SHOVEL, 5.0));

    @Test
    void legacyAttackDamagePinsTheVanillaTable() {
        for (Map.Entry<Material, Double> e : EXPECTED.entrySet()) {
            double actual = LEGACY.value(ItemStack.of(e.getKey()), null, ItemStat.ATTACK_DAMAGE, FIST);
            assertEquals(e.getValue(), actual, 1e-9, e.getKey().name());
        }
    }

    @Test
    void unlistedAndAirFallBackToFist() {
        assertEquals(FIST, LEGACY.value(ItemStack.of(Material.STICK), null, ItemStat.ATTACK_DAMAGE, FIST), 1e-9);
        assertEquals(FIST, LEGACY.value(ItemStack.AIR, null, ItemStat.ATTACK_DAMAGE, FIST), 1e-9);
    }
}
