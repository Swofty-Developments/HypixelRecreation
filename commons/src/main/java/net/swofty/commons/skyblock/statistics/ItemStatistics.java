package net.swofty.commons.skyblock.statistics;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ItemStatistics {
    private static final ItemStatistic[] STATISTICS = ItemStatistic.values();
    private static final ItemStatistics EMPTY = new ItemStatistics(new double[0], new double[0], new double[0]);

    private final double[] statisticsBase;
    private final double[] statisticsAdditive;
    private final double[] statisticsMultiplicative;

    private ItemStatistics(double[] statisticsBase, double[] statisticsAdditive, double[] statisticsMultiplicative) {
        this.statisticsBase = trim(statisticsBase);
        this.statisticsAdditive = trim(statisticsAdditive);
        this.statisticsMultiplicative = trim(statisticsMultiplicative);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ItemStatistics empty() {
        return EMPTY;
    }

    public static List<String> getDescription(ItemStatistic statistic) {
        return switch (statistic) {
            case HEALTH -> List.of("<7>Your Health stat increases your", "<7>maximum health.");
            case DEFENSE -> List.of("<7>Your Defense stat reduces the", "<7>damage that you take from enemies.");
            case TRUE_DEFENSE ->
                List.of("<7>Your True Defense stat reduces the", "<7>true damage that you take from enemies.");
            case STRENGTH -> List.of("<7>Strength increases the damage you", "<7>deal.");
            case INTELLIGENCE ->
                List.of("<7>Intelligence increases the damage of", "<7>your magical items and your mana pool.");
            case CRITICAL_CHANCE ->
                List.of("<7>Critical Chance is the percent chance", "<7>that you land a Critical Hit.");
            case CRITICAL_DAMAGE ->
                List.of("<7>Critical Damage multiplies the damage", "<7>that you deal with a Critical Hit.");
            case BONUS_ATTACK_SPEED -> List.of("<7>Attack Speed decreases the time", "<7>between hits on your opponent.");
            case ABILITY_DAMAGE -> List.of("<7>Ability Damage increases damage from", "<7>spells and item abilities.");
            case FEROCITY -> List.of("<7>Ferocity grants a chance to strike", "<7>enemies additional times.");
            case HEALTH_REGENERATION ->
                List.of("<7>Health Regen increases the health", "<7>you naturally regenerate over time.");
            case VITALITY -> List.of("<7>Vitality increases your incoming", "<7>healing, including health regen.");
            case MENDING -> List.of("<7>Mending increases your outgoing", "<7>healing.");
            case SWING_RANGE -> List.of("<7>Increases your melee hit range.");
            case RIFT_DAMAGE -> List.of("<7>Damage dealt to enemies in the Rift.");
            case BREAKING_POWER -> List.of("<7>Breaking Power allows you to mine", "<7>stronger blocks.");
            case MINING_SPEED -> List.of("<7>Increases the speed of breaking", "<7>mining blocks.");
            case MINING_SPREAD ->
                List.of("<7>Chance to automatically mine adjacent", "<7>Blocks, Ores, and Dwarven Metals.");
            case GEMSTONE_SPREAD -> List.of("<7>Chance to automatically mine adjacent", "<7>Gemstones.");
            case PRISTINE -> List.of("<7>Chance to increase the quality of a", "<7>Gemstone when it is dropped.");
            case SWEEP -> List.of("<7>Sweep is the ability to cut multiple", "<7>logs at once.");
            case PULL -> List.of("<7>Pull controls which fish you can grab", "<7>and how quickly fishing nets work.");
            case FISHING_SPEED -> List.of("<7>Fishing Speed decreases the delay", "<7>before a fish appears.");
            case SEA_CREATURE_CHANCE -> List.of("<7>Your chance to catch Sea Creatures", "<7>while fishing.");
            case DOUBLE_HOOK_CHANCE -> List.of("<7>Chance to catch two Sea Creatures", "<7>at once.");
            case TROPHY_FISH_CHANCE -> List.of("<7>Increases your chance to catch", "<6>Trophy Fish<7>.");
            case TREASURE_CHANCE -> List.of("<7>Increases your chance to catch", "<6>Treasure <7>while fishing.");
            case BONUS_PEST_CHANCE -> List.of("<7>Chance to spawn bonus <2>ൠ Pests", "<7>while on <a>The Garden<7>.");
            case OVERBLOOM -> List.of("<7>Increases your chance of dropping", "<6><l>RARE CROPS </l><7>while farming.");
            case SPEED -> List.of("<7>Your Speed stat increases how fast", "<7>you can walk.");
            case MAGIC_FIND -> List.of("<7>Magic Find increases how many rare", "<7>items you find.");
            case PET_LUCK -> List.of("<7>Pet Luck improves pet drops and", "<7>the quality of crafted pets.");
            case HEAT_RESISTANCE -> List.of("<7>Increases time spent safely in hot", "<7>environments.");
            case COLD_RESISTANCE -> List.of("<7>Increases time spent safely in cold", "<7>environments.");
            case FEAR -> List.of("<7>Makes Primal Fears spawn more often", "<7>and reduces their damage.");
            case RESPIRATION -> List.of("<7>Extends underwater breathing time.");
            case PRESSURE_RESISTANCE -> List.of("<7>Reduces the effects of Pressure", "<7>when diving.");
            case TRACKING -> List.of("<7>Increases your chance of finding", "<d>❃ Elusive <7>mobs.");
            default -> statistic.name().endsWith("_WISDOM")
                ? List.of("<7>Increases the Skill XP that you gain.")
                : statistic.name().endsWith("_FORTUNE")
                  ? List.of("<7>Increases your chance to gain", "<7>multiple relevant drops.")
                  : List.of("<7>Augments this part of your gameplay.");
        };
    }

    @Override
    public @NonNull ItemStatistics clone() {
        return this;
    }

    public Map<ItemStatistic, Double> getStatisticsBase() {
        return asMap(statisticsBase);
    }

    public Map<ItemStatistic, Double> getStatisticsAdditive() {
        return asMap(statisticsAdditive);
    }

    public Map<ItemStatistic, Double> getStatisticsMultiplicative() {
        Map<ItemStatistic, Double> result = new EnumMap<>(ItemStatistic.class);
        for (int i = 0; i < statisticsMultiplicative.length; i++) {
            if (statisticsMultiplicative[i] != 0D) result.put(STATISTICS[i], statisticsMultiplicative[i] + 1);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ItemStatistic stat : STATISTICS) {
            double baseValue = get(statisticsBase, stat);
            double additiveValue = get(statisticsAdditive, stat);
            double multiplicativeValue = getMultiplicative(stat);
            boolean hasBase = Math.abs(baseValue) > 1e-9;
            boolean hasAdditive = Math.abs(additiveValue) > 1e-9;
            boolean hasMultiplicative = Math.abs(multiplicativeValue - 1.0) > 1e-9;

            if (hasBase || hasAdditive || hasMultiplicative) {
                builder.append(stat.name()).append(":");
                boolean needsComma = false;
                if (hasBase) {
                    builder.append("B").append(baseValue);
                    needsComma = true;
                }
                if (hasAdditive) {
                    if (needsComma) builder.append(",");
                    builder.append("A").append(additiveValue);
                    needsComma = true;
                }
                if (hasMultiplicative) {
                    if (needsComma) builder.append(",");
                    builder.append("M").append(multiplicativeValue);
                }
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public static ItemStatistics fromString(String string) {
        if (string == null || string.isBlank()) return empty();

        Builder builder = builder();
        for (String statPair : string.split(";")) {
            if (statPair.isEmpty()) continue;
            String[] parts = statPair.split(":");
            if (parts.length != 2) continue;
            ItemStatistic stat;
            try {
                stat = ItemStatistic.valueOf(parts[0]);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            for (String value : parts[1].split(",")) {
                if (value.length() < 2) continue;
                char type = value.charAt(0);
                if (type != 'B' && type != 'A' && type != 'M') continue;

                try {
                    double parsed = Double.parseDouble(value.substring(1));
                    if (!Double.isFinite(parsed)) continue;
                    if (type == 'B') builder.withBase(stat, parsed);
                    else if (type == 'A') builder.withAdditive(stat, parsed);
                    else builder.withMultiplicative(stat, parsed);
                } catch (NumberFormatException _) {
                    Logger.warn("Could not parse number");
                }
            }
        }
        return builder.build();
    }

    public static final class Builder {
        private final double[] statisticsBase = new double[STATISTICS.length];
        private final double[] statisticsAdditive = new double[STATISTICS.length];
        private final double[] statisticsMultiplicative = new double[STATISTICS.length];

        public Builder withBase(ItemStatistic stat, Double value) {
            statisticsBase[stat.ordinal()] = value;
            return this;
        }

        public Builder withAdditive(ItemStatistic stat, Double value) {
            statisticsAdditive[stat.ordinal()] = value;
            return this;
        }

        public Builder withAdditivePercentage(ItemStatistic stat, Double valuePercentage) {
            statisticsAdditive[stat.ordinal()] = valuePercentage / 100;
            return this;
        }

        public Builder withMultiplicative(ItemStatistic stat, Double multiplicationValue) {
            statisticsMultiplicative[stat.ordinal()] = multiplicationValue - 1;
            return this;
        }

        public Builder withMultiplicativePercentage(ItemStatistic stat, Double multiplicationValuePercentage) {
            statisticsMultiplicative[stat.ordinal()] = multiplicationValuePercentage / 100;
            return this;
        }

        public ItemStatistics build() {
            return create(statisticsBase, statisticsAdditive, statisticsMultiplicative);
        }
    }

    public ItemStatistics addBase(ItemStatistic stat, Double value) {
        double[] base = expandedCopy(statisticsBase);
        base[stat.ordinal()] += value;
        return create(base, statisticsAdditive, statisticsMultiplicative);
    }

    public ItemStatistics addAdditive(ItemStatistic stat, Double value) {
        double[] additive = expandedCopy(statisticsAdditive);
        additive[stat.ordinal()] += value;
        return create(statisticsBase, additive, statisticsMultiplicative);
    }

    public ItemStatistics addMultiplicative(ItemStatistic stat, Double value) {
        double[] multiplicative = expandedCopy(statisticsMultiplicative);
        multiplicative[stat.ordinal()] += value;
        return create(statisticsBase, statisticsAdditive, multiplicative);
    }

    public Map<ItemStatistic, Double> getOverall() {
        Map<ItemStatistic, Double> result = new EnumMap<>(ItemStatistic.class);
        for (ItemStatistic stat : STATISTICS) result.put(stat, getOverall(stat));
        return result;
    }

    public @NonNull Double getOverall(@Nullable ItemStatistic stat) {
        if (stat == null) return 0D;
        double value = getBase(stat) * getAdditive(stat) * getMultiplicative(stat);
        Double cap = stat.getCap();
        return cap == null ? value : Math.min(value, cap);
    }

    public @NonNull Double getBase(@Nullable ItemStatistic stat) {
        return stat == null ? 0D : get(statisticsBase, stat);
    }

    public @NonNull Double getAdditive(@Nullable ItemStatistic stat) {
        return stat == null ? 1D : get(statisticsAdditive, stat) + 1;
    }

    public @NonNull Double getMultiplicative(@Nullable ItemStatistic stat) {
        return stat == null ? 1D : get(statisticsMultiplicative, stat) + 1;
    }

    public @NonNull Double getMultiplicativeAsPercentage(@Nullable ItemStatistic stat) {
        return stat == null ? 100D : get(statisticsMultiplicative, stat) * 100 + 100;
    }

    public static ItemStatistics add(ItemStatistics first, ItemStatistics other) {
        double[] base = new double[STATISTICS.length];
        double[] additive = new double[STATISTICS.length];
        double[] multiplicative = new double[STATISTICS.length];
        for (ItemStatistic stat : STATISTICS) {
            int i = stat.ordinal();
            base[i] = first.getBase(stat) + other.getBase(stat);
            additive[i] = get(first.statisticsAdditive, stat) + get(other.statisticsAdditive, stat);
            multiplicative[i] = first.getMultiplicative(stat) * other.getMultiplicative(stat) - 1;
        }
        return create(base, additive, multiplicative);
    }

    public static ItemStatistics multiply(ItemStatistics statistics, double multiplier) {
        double[] base = new double[STATISTICS.length];
        double[] additive = new double[STATISTICS.length];
        double[] multiplicative = new double[STATISTICS.length];
        for (ItemStatistic stat : STATISTICS) {
            int i = stat.ordinal();
            base[i] = get(statistics.statisticsBase, stat) * multiplier;
            additive[i] = get(statistics.statisticsAdditive, stat) * multiplier;
            multiplicative[i] = get(statistics.statisticsMultiplicative, stat) * multiplier;
        }
        return create(base, additive, multiplicative);
    }

    public ItemStatistics sub(ItemStatistics other) {
        double[] base = new double[STATISTICS.length];
        double[] additive = new double[STATISTICS.length];
        double[] multiplicative = new double[STATISTICS.length];
        for (ItemStatistic stat : STATISTICS) {
            int i = stat.ordinal();
            base[i] = getBase(stat) - other.getBase(stat);
            additive[i] = get(statisticsAdditive, stat) - get(other.statisticsAdditive, stat);
            multiplicative[i] = getMultiplicative(stat) / other.getMultiplicative(stat) - 1;
        }
        return create(base, additive, multiplicative);
    }

    private static ItemStatistics create(double[] base, double[] additive, double[] multiplicative) {
        if (lastNonZero(base) == 0 && lastNonZero(additive) == 0 && lastNonZero(multiplicative) == 0) return EMPTY;
        return new ItemStatistics(base, additive, multiplicative);
    }

    private static double get(double[] values, ItemStatistic stat) {
        int ordinal = stat.ordinal();
        return ordinal < values.length ? values[ordinal] : 0D;
    }

    private static double[] expandedCopy(double[] values) {
        return Arrays.copyOf(values, STATISTICS.length);
    }

    private static double[] trim(double[] values) {
        return Arrays.copyOf(values, lastNonZero(values));
    }

    private static int lastNonZero(double[] values) {
        for (int i = values.length - 1; i >= 0; i--) {
            if (values[i] != 0D) return i + 1;
        }
        return 0;
    }

    private static Map<ItemStatistic, Double> asMap(double[] values) {
        Map<ItemStatistic, Double> result = new EnumMap<>(ItemStatistic.class);
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0D) result.put(STATISTICS[i], values[i]);
        }
        return result;
    }
}
