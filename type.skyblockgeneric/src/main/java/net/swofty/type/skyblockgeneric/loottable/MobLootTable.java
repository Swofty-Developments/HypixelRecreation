package net.swofty.type.skyblockgeneric.loottable;

import net.kyori.adventure.key.Key;
import net.swofty.commons.loot.LootPool;
import net.swofty.commons.skyblock.item.ItemType;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MobLootTable extends SkyBlockLootTable {
    private final List<Pool> pools;

    public MobLootTable(String mobId, Drop... drops) {
        this(mobId, List.of(Pool.independent("drops", drops)));
    }

    private MobLootTable(String mobId, List<Pool> pools) {
        super(Key.key("skyblock", "mob/" + mobId.toLowerCase()));
        this.pools = List.copyOf(pools);
    }

    public static MobLootTable withPools(String mobId, Pool... pools) {
        return new MobLootTable(mobId, List.of(pools));
    }

    @Override
    public List<LootRecord> getLootTable() {
        Map<ItemType, LootRecord> records = new LinkedHashMap<>();
        pools.stream().flatMap(pool -> pool.drops().stream()).forEach(drop -> records.putIfAbsent(
                drop.itemType(), new LootRecord(drop.itemType(), drop.minimum(), drop.chancePercent(), drop.rarity())));
        return List.copyOf(records.values());
    }

    @Override
    public CalculationMode getCalculationMode() {
        return CalculationMode.CALCULATE_INDIVIDUAL;
    }

    @Override
    public List<LootPoolDefinition> getLootPools() {
        return pools.stream().map(pool -> new LootPoolDefinition(
                pool.id(),
                pool.mode(),
                1,
                pool.emptyWeight(),
                pool.drops().stream().map(drop -> new LootRecord(
                        drop.itemType(),
                        makeAmountBetween(drop.minimum(), drop.maximum()),
                        drop.chancePercent(),
                        drop.rarity()
                )).toList()
        )).toList();
    }

    public record Pool(String id, LootPool.Mode mode, double emptyWeight, List<Drop> drops) {
        public Pool {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Loot pool id must be present");
            if (mode == null) throw new IllegalArgumentException("Loot pool mode must be present");
            if (!Double.isFinite(emptyWeight) || emptyWeight < 0) {
                throw new IllegalArgumentException("Loot pool empty weight must be finite and non-negative");
            }
            drops = List.copyOf(drops);
        }

        public static Pool independent(String id, Drop... drops) {
            return new Pool(id, LootPool.Mode.INDEPENDENT, 0, Arrays.asList(drops));
        }

        public static Pool weighted(String id, double emptyWeight, Drop... drops) {
            return new Pool(id, LootPool.Mode.WEIGHTED, emptyWeight, Arrays.asList(drops));
        }
    }

    public record Drop(ItemType itemType, int minimum, int maximum, double chancePercent,
                       BestiaryDropRarity rarity) {
        public Drop {
            if (minimum < 0 || maximum < minimum) throw new IllegalArgumentException("Invalid drop amount range");
            if (!Double.isFinite(chancePercent) || chancePercent < 0 || chancePercent > 100) {
                throw new IllegalArgumentException("Drop chance must be between 0 and 100");
            }
        }

        public Drop(ItemType itemType, int amount, double chancePercent, BestiaryDropRarity rarity) {
            this(itemType, amount, amount, chancePercent, rarity);
        }
    }
}
