package net.swofty.type.skyblockgeneric.loottable;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobLootTableTest {
    @Test
    void weightedPoolNeverDropsMoreThanOneEntry() {
        MobLootTable table = MobLootTable.withPools("TEST_MOB",
                MobLootTable.Pool.independent("materials",
                        new MobLootTable.Drop(ItemType.ROTTEN_FLESH, 1, 100, BestiaryDropRarity.COMMON)),
                MobLootTable.Pool.weighted("armor", 0,
                        new MobLootTable.Drop(ItemType.LEATHER_BOOTS, 1, 1, BestiaryDropRarity.RARE),
                        new MobLootTable.Drop(ItemType.LEATHER_HELMET, 1, 1, BestiaryDropRarity.RARE)));

        for (int roll = 0; roll < 100; roll++) {
            List<SkyBlockLootTable.LootRecord> loot = table.roll(null, null);
            assertEquals(1, loot.stream().filter(record -> record.getItemType() == ItemType.ROTTEN_FLESH).count());
            assertEquals(1, loot.stream().filter(record -> record.getItemType() != ItemType.ROTTEN_FLESH).count());
        }
    }

    @Test
    void rollsAmountsInsideConfiguredRange() {
        MobLootTable table = new MobLootTable("TEST_MOB",
                new MobLootTable.Drop(ItemType.ENDER_PEARL, 1, 6, 100, BestiaryDropRarity.COMMON));

        for (int roll = 0; roll < 100; roll++) {
            int amount = table.roll(null, null).getFirst().getAmount();
            assertTrue(amount >= 1 && amount <= 6);
        }
    }

    @Test
    void combinesMultipleSuccessfulRollsOfTheSameItem() {
        MobLootTable table = new MobLootTable("TEST_MOB",
                new MobLootTable.Drop(ItemType.SLIME_BALL, 3, 100, BestiaryDropRarity.COMMON),
                new MobLootTable.Drop(ItemType.SLIME_BALL, 1, 100, BestiaryDropRarity.COMMON),
                new MobLootTable.Drop(ItemType.SLIME_BALL, 1, 100, BestiaryDropRarity.COMMON));

        List<SkyBlockLootTable.LootRecord> loot = table.roll(null, null);
        assertEquals(1, loot.size());
        assertEquals(5, loot.getFirst().getAmount());
    }

    @Test
    void rejectsInvalidDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> new MobLootTable.Drop(
                ItemType.BONE, 2, 1, 100, BestiaryDropRarity.COMMON));
        assertThrows(IllegalArgumentException.class, () -> new MobLootTable.Drop(
                ItemType.BONE, 1, 100.01, BestiaryDropRarity.COMMON));
        assertThrows(IllegalArgumentException.class, () -> MobLootTable.Pool.weighted("armor", -1));
    }
}
