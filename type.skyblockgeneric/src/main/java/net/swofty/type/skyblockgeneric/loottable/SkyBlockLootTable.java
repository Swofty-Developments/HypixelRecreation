package net.swofty.type.skyblockgeneric.loottable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.loot.LootEntry;
import net.swofty.commons.loot.LootPool;
import net.swofty.commons.loot.LootRoll;
import net.swofty.commons.loot.LootTable;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * SkyBlock item adapter for the shared pool engine. Existing mob declarations remain data-only while
 * all selection semantics live in {@link LootTable}.
 */
public abstract class SkyBlockLootTable {
    private static final String NAMESPACE = "skyblock";
    private final Key key;

    protected SkyBlockLootTable(Key key) {
        this.key = key;
    }

    public abstract @NonNull List<LootRecord> getLootTable();
    public abstract @NonNull CalculationMode getCalculationMode();

    public @NonNull List<LootPoolDefinition> getLootPools() {
        LootPool.Mode mode = getCalculationMode() == CalculationMode.PICK_ONE
                ? LootPool.Mode.WEIGHTED
                : LootPool.Mode.INDEPENDENT;
        return List.of(new LootPoolDefinition("main", mode, 1, 0, getLootTable()));
    }

    public int makeAmountBetween(int min, int max) {
        return RandomGenerator.getDefault().nextInt(min, max + 1);
    }

    public @NonNull List<ItemType> getLootTableItems() {
        return getLootTable().stream().map(LootRecord::getItemType).toList();
    }

    public @NonNull List<LootRecord> roll(@Nullable SkyBlockPlayer player, @Nullable LivingEntity source,
                                          LootAffector... affectors) {
        return roll(player, source, ignored -> List.of(affectors));
    }

    public @NonNull List<LootRecord> roll(@Nullable SkyBlockPlayer player, @Nullable LivingEntity source,
                                          Function<LootRecord, List<LootAffector>> affectors) {
        List<LootPool<RollContext, LootRecord>> pools = new ArrayList<>();
        for (LootPoolDefinition pool : getLootPools()) {
            List<LootEntry<RollContext, LootRecord>> entries = new ArrayList<>(pool.records().size());
            for (int index = 0; index < pool.records().size(); index++) {
                LootRecord record = pool.records().get(index);
                List<LootEntry.LootModifier<RollContext, LootRecord>> modifiers = new ArrayList<>();
                modifiers.add((context, ignored, chance) -> context.player() == null
                        ? chance
                        : adjustChance(context.player(), record, chance));
                for (LootAffector affector : affectors.apply(record)) {
                    modifiers.add((context, ignored, chance) -> context.player() == null
                            ? chance
                            : affector.apply(context.player(), chance, context.source()));
                }
                entries.add(new LootEntry<>(
                        Key.key(NAMESPACE, pool.id() + "/" + record.itemType.name().toLowerCase() + "/" + index),
                        record,
                        record.chancePercent / 100D,
                        context -> context.player() == null || record.shouldCalculate.apply(context.player()),
                        modifiers
                ));
            }
            pools.add(new LootPool<>(Key.key(NAMESPACE, pool.id()), pool.mode(), pool.rolls(),
                    pool.emptyWeight(), entries));
        }
        LootTable<RollContext, LootRecord> table = new LootTable<>(key(), pools);
        List<LootRecord> rolled = mergeDuplicateItems(
                table.roll(new RollContext(player, source)).stream().map(LootRoll::value).toList());
        if (player != null) {
            rolled.forEach(record -> onRolled(player, record));
            afterRoll(player, rolled);
        }
        return rolled;
    }

    protected double adjustChance(SkyBlockPlayer player, LootRecord record, double chance) {
        return chance;
    }

    protected void onRolled(SkyBlockPlayer player, LootRecord record) {
    }

    protected void afterRoll(SkyBlockPlayer player, List<LootRecord> records) {
    }

    public Key key() {
        return key;
    }

    /**
     * Compatibility for callers not yet able to consume duplicate item rolls.
     */
    public @NonNull Map<ItemType, LootRecord> runChancesNoPlayer() {
        return asMap(roll(null, null));
    }

    /** Compatibility for callers not yet able to consume duplicate item rolls. */
    public @NonNull Map<ItemType, LootRecord> runChances(SkyBlockPlayer player, LootAffector... affectors) {
        return asMap(roll(player, null, affectors));
    }

    private Map<ItemType, LootRecord> asMap(List<LootRecord> rolls) {
        Map<ItemType, LootRecord> result = new LinkedHashMap<>();
        for (LootRecord roll : rolls) result.put(roll.itemType, roll);
        return result;
    }

    private List<LootRecord> mergeDuplicateItems(List<LootRecord> rolls) {
        Map<ItemType, LootRecord> merged = new LinkedHashMap<>();
        for (LootRecord roll : rolls) {
            merged.merge(roll.itemType, roll, (first, duplicate) -> new LootRecord(
                    first.itemType,
                    first.amount + duplicate.amount,
                    first.chancePercent,
                    first.rarity,
                    first.shouldCalculate
            ));
        }
        return List.copyOf(merged.values());
    }

    private record RollContext(@Nullable SkyBlockPlayer player, @Nullable LivingEntity source) {
    }

    public record LootPoolDefinition(String id, LootPool.Mode mode, int rolls, double emptyWeight,
                                     List<LootRecord> records) {
        public LootPoolDefinition {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Loot pool id must be present");
            if (mode == null) throw new IllegalArgumentException("Loot pool mode must be present");
            if (rolls < 0) throw new IllegalArgumentException("Loot pool rolls cannot be negative");
            if (!Double.isFinite(emptyWeight) || emptyWeight < 0) {
                throw new IllegalArgumentException("Loot pool empty weight must be finite and non-negative");
            }
            records = List.copyOf(records);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class LootRecord {
        private final ItemType itemType;
        private final int amount;
        private final double chancePercent;
        private final BestiaryDropRarity rarity;
        private Function<SkyBlockPlayer, Boolean> shouldCalculate = player -> true;

        public LootRecord(ItemType itemType, int amount, double chancePercent, BestiaryDropRarity rarity) {
            this(itemType, amount, chancePercent, rarity, player -> true);
        }

        public static LootRecord none(int chance) {
            return new LootRecord(ItemType.AIR, 0, chance, BestiaryDropRarity.COMMON, player -> true);
        }

        public static LootRecord none(int chance, Function<SkyBlockPlayer, Boolean> shouldCalculate) {
            return new LootRecord(ItemType.AIR, 0, chance, BestiaryDropRarity.COMMON, shouldCalculate);
        }

        public static boolean isNone(LootRecord lootRecord) {
            return lootRecord.itemType == ItemType.AIR && lootRecord.amount == 0;
        }
    }

    public enum CalculationMode {
        PICK_ONE,
        CALCULATE_INDIVIDUAL
    }
}
