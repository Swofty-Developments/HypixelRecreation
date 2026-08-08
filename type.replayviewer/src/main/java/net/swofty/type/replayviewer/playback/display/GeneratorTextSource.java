package net.swofty.type.replayviewer.playback.display;

import net.kyori.adventure.text.minimessage.translation.Argument;
import net.swofty.type.generic.i18n.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class GeneratorTextSource implements DynamicTextSource {

    private final String identifier;
    private final String generatorType; // "diamond" or "emerald"
    private final TreeMap<Integer, Integer> tierChanges; // tick -> tier
    private final List<String> baseTextLines;

    public GeneratorTextSource(DynamicTextConfig config) {
        this.identifier = config.identifier();
        this.generatorType = config.getMeta("generatorType", "diamond");
        this.tierChanges = new TreeMap<>();
        this.baseTextLines = config.initialText();

        // Initialize with tier 1
        tierChanges.put(0, 1);

        // Load any pre-configured tier changes
        List<int[]> changes = config.getMeta("tierChanges");
        if (changes != null) {
            for (int[] change : changes) {
                if (change.length >= 2) {
                    tierChanges.put(change[0], change[1]);
                }
            }
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDisplayType() {
        return "generator";
    }

    @Override
    public List<String> getTextAt(int currentTick) {
        int tier = getCurrentTier(currentTick);
        return formatText(tier);
    }

    @Override
    public boolean hasChangedSince(int lastTick, int currentTick) {
        Integer lastChange = tierChanges.floorKey(currentTick);
        return lastChange != null && lastChange > lastTick;
    }

    /**
     * Records a tier upgrade at a specific tick.
     *
     * @param tick the tick when the upgrade occurs
     * @param tier the new tier
     */
    public void recordTierChange(int tick, int tier) {
        tierChanges.put(tick, tier);
    }

    private int getCurrentTier(int tick) {
        Integer floor = tierChanges.floorKey(tick);
        if (floor == null) {
            return 1;
        }
        return tierChanges.get(floor);
    }

    private List<String> formatText(int tier) {
        List<String> result = new ArrayList<>();

        // Format based on generator type
        result.add(ReplayDisplayTranslations.toLegacy(I18n.t(
                generatorType.equalsIgnoreCase("diamond") ? "replays.generator_diamond" : "replays.generator_emerald")));
        result.add(ReplayDisplayTranslations.toLegacy(I18n.t(
                "replays.generator_tier", Argument.numeric("tier", tier))));

        if (tier < 3) {
            String spawnRate = switch (tier) {
                case 1 -> generatorType.equalsIgnoreCase("diamond") ? "30s" : "60s";
                case 2 -> generatorType.equalsIgnoreCase("diamond") ? "23s" : "45s";
                default -> "???";
            };
            result.add(ReplayDisplayTranslations.toLegacy(I18n.t(
                    "replays.generator_spawn_rate", Argument.string("rate", spawnRate))));
        } else {
            result.add(ReplayDisplayTranslations.toLegacy(I18n.t("replays.generator_max_tier")));
        }

        return result;
    }
}
