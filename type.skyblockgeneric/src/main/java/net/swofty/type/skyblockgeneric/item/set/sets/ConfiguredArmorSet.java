package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ConfiguredArmorSet implements ArmorSet {
    private static final Map<String, ItemStatistic> STATISTICS_BY_NAME = Arrays.stream(ItemStatistic.values())
            .collect(java.util.stream.Collectors.toMap(
                    statistic -> statistic.getDisplayName().toLowerCase(Locale.ROOT),
                    statistic -> statistic
            ));
    private static final Pattern NUMERIC_BONUS = Pattern.compile(
            "(?i)\\+(\\d+(?:\\.\\d+)?)\\s*(%)?\\s+(" + STATISTICS_BY_NAME.keySet().stream()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .map(Pattern::quote)
                    .collect(java.util.stream.Collectors.joining("|")) + ")(?![A-Za-z])"
    );

    private final ArmorSetRegistry registry;

    public ConfiguredArmorSet(ArmorSetRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ArmorSetRegistry getRegistry() {
        return registry;
    }

    @Override
    public String getName() {
        for (String line : getDescription()) {
            int colon = line.indexOf(':');
            int dash = line.indexOf('\u2014', colon + 1);
            if (colon >= 0 && dash > colon) {
                return line.substring(colon + 1, dash).trim();
            }
        }
        return registry.getDisplayName();
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(readLore());
    }

    @Override
    public ItemStatistics getStatistics() {
        return getStatistics(null);
    }

    @Override
    public ItemStatistics getStatistics(@Nullable SkyBlockPlayer player) {
        Map<ItemStatistic, Double> base = new LinkedHashMap<>();
        Map<ItemStatistic, Double> additive = new LinkedHashMap<>();
        int wornPieces = player == null ? ArmorSetRegistry.getPieceCount(registry) : getWornPieceCount(player);
        if (wornPieces == 0) return ItemStatistics.empty();

        for (String line : readLore()) {
            if (!isActive(line, wornPieces)) continue;

            var tieredMatcher = TIERED_NUMERIC_BONUS.matcher(line);
            if (tieredMatcher.find()) {
                addStatistic(base, additive, tieredMatcher.group(3), tieredMatcher.group(2),
                        tieredMatcher.group(1).split("/"), wornPieces);
            }

            var matcher = NUMERIC_BONUS.matcher(line);
            while (matcher.find()) {
                addStatistic(base, additive, matcher.group(3), matcher.group(2), new String[]{matcher.group(1)}, wornPieces);
            }
        }

        ItemStatistics.Builder builder = ItemStatistics.builder();
        base.forEach(builder::withBase);
        additive.forEach(builder::withAdditive);
        return builder.build();
    }

    private boolean isActive(String line, int wornPieces) {
        String lowerLine = line.toLowerCase(Locale.ROOT);
        int totalPieces = ArmorSetRegistry.getPieceCount(registry);
        if (lowerLine.startsWith("full set bonus:") && wornPieces < totalPieces) return false;

        var pieceMatcher = PIECE_REQUIREMENT.matcher(lowerLine);
        boolean hasPieceRequirement = pieceMatcher.find();
        if (hasPieceRequirement && wornPieces < Integer.parseInt(pieceMatcher.group(1))) return false;
        if (lowerLine.startsWith("ability:") && !hasPieceRequirement && wornPieces < totalPieces) return false;

        return !lowerLine.contains("deal ") && !lowerLine.contains("damage")
                || lowerLine.contains("damage reduction");
    }

    private void addStatistic(Map<ItemStatistic, Double> base, Map<ItemStatistic, Double> additive,
                              String statisticName, String percent, String[] values, int wornPieces) {
        ItemStatistic statistic = STATISTICS_BY_NAME.get(statisticName.toLowerCase(Locale.ROOT));
        if (statistic == null || statistic == ItemStatistic.DAMAGE) return;

        int valueIndex = Math.min(Math.max(wornPieces, 1), values.length) - 1;
        double value = Double.parseDouble(values[valueIndex]);
        if (percent != null || statistic.getIsPercentage()) {
            additive.merge(statistic, value / 100, Double::sum);
        } else {
            base.merge(statistic, value, Double::sum);
        }
    }

    private List<String> readLore() {
        List<String> lore = new ArrayList<>();
        for (ItemType itemType : registry.getItemTypes()) {
            ConfigurableSkyBlockItem item = ConfigurableSkyBlockItem.getFromID(itemType.name());
            if (item == null || item.getLore() == null) continue;
            for (String line : item.getLore()) {
                if (!lore.contains(line)) lore.add(line);
            }
        }
        return lore;
    }

    private static final Pattern PIECE_REQUIREMENT = Pattern.compile("(\\d+)\\s*-?piece");
    private static final Pattern TIERED_NUMERIC_BONUS = Pattern.compile(
            "(?i)\\+((?:\\d+(?:\\.\\d+)?/)+\\d+(?:\\.\\d+)?)\\s*(%)?\\s+(" + STATISTICS_BY_NAME.keySet().stream()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .map(Pattern::quote)
                    .collect(java.util.stream.Collectors.joining("|")) + ")(?![A-Za-z])"
    );
}
