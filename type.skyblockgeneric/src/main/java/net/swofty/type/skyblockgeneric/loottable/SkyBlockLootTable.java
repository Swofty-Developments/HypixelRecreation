package net.swofty.type.skyblockgeneric.loottable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.swofty.commons.rates.GameRates;
import net.swofty.commons.rates.RateCategory;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;
import java.util.function.Function;

public abstract class SkyBlockLootTable {
    public abstract @NonNull List<LootRecord> getLootTable();
    public abstract @NonNull CalculationMode getCalculationMode();

    public int makeAmountBetween(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public @NonNull List<ItemType> getLootTableItems() {
        List<ItemType> items = new ArrayList<>();
        for (LootRecord record : getLootTable()) {
            items.add(record.itemType);
        }
        return items;
    }

    /**
     * Runs the chances without a player
     * @return A map of the loot that the player will receive
     */
    public @NonNull Map<ItemType, LootRecord> runChancesNoPlayer(RateCategory category) {
        Map<ItemType, LootRecord> loot = new HashMap<>();
        CalculationMode mode = getCalculationMode();

        if (mode == CalculationMode.PICK_ONE) {
            double cumulativeChance = 0;

            for (LootRecord record : getLootTable()) {
                cumulativeChance += record.getChancePercent(category);
            }

            int random = (int) (Math.random() * cumulativeChance);
            double current = 0;

            for (LootRecord record : getLootTable()) {
                current += record.getChancePercent(category);
                if (random < current) {
                    loot.put(record.itemType, record);
                    break;
                }
            }
        } else if (mode == CalculationMode.CALCULATE_INDIVIDUAL) {
            for (LootRecord record : getLootTable()) {
                double adjustedChancePercent = record.getChancePercent(category);

                if (Math.random() * 100 < adjustedChancePercent) {
                    loot.put(record.itemType, record);
                }
            }
        }

        return loot;
    }

    /**
     * Function will always return a LootRecord for every itemtype
     * @param player The player to run the chances for
     * @return A map of the loot that the player will receive
     */
    public @NonNull Map<ItemType, LootRecord> runChances(RateCategory category, SkyBlockPlayer player, LootAffector... affectors) {
        Map<ItemType, LootRecord> loot = new HashMap<>();
        CalculationMode mode = getCalculationMode();

        if (mode == CalculationMode.PICK_ONE) {
            double cumulativeChance = 0;

            for (LootRecord record : getLootTable()) {
                if (record.shouldCalculate.apply(player)) {
                    cumulativeChance += record.getChancePercent(category);
                }
            }

            int random = (int) (Math.random() * cumulativeChance);
            double current = 0;

            List<LootRecord> records = getLootTable();
            Collections.shuffle(records);

            for (LootRecord record : records) {
                if (record.shouldCalculate.apply(player)) {
                    current += record.getChancePercent(category);
                    if (random < current) {
                        loot.put(record.itemType, record);
                        break;
                    }
                }
            }
        } else if (mode == CalculationMode.CALCULATE_INDIVIDUAL) {
            for (LootRecord record : getLootTable()) {
                if (record.shouldCalculate.apply(player)) {
                    double adjustedChancePercent = record.getChancePercent(category);

                    // Apply LootAffectors to the chance percentage
                    for (LootAffector affector : affectors) {
                        adjustedChancePercent = affector.getAffector().apply(player, adjustedChancePercent, null);
                    }

                    if (Math.random() * 100 < adjustedChancePercent) {
                        loot.put(record.itemType, record);
                    }
                }
            }
        }

        return loot;
    }

    @Getter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class LootRecord {
        private final ItemType itemType;
        private final int amount;
        private final double chancePercent;
        private Function<SkyBlockPlayer, Boolean> shouldCalculate = player -> true;

        public static LootRecord none(int chance) {
            return new LootRecord(ItemType.AIR, 0, chance, player -> true);
        }

        public static LootRecord none(int chance, Function<SkyBlockPlayer, Boolean> shouldCalculate) {
            return new LootRecord(ItemType.AIR, 0, chance, shouldCalculate);
        }

        public static boolean isNone(LootRecord lootRecord) {
            return lootRecord.itemType == ItemType.AIR && lootRecord.amount == 0;
        }

        public double getChancePercent(RateCategory category) {
            if (isNone(this) || chancePercent >= 100) return chancePercent;
            return chancePercent * GameRates.multiplier(category);
        }
    }

    public enum CalculationMode {
        // Adds up all the chances and picks one based on that
        PICK_ONE,
        // Calculates each item individually, and gives the player each item that they have a chance to get
        CALCULATE_INDIVIDUAL
    }
}
