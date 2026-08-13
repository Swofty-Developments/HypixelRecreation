package net.swofty.type.skyblockgeneric.experimentation;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

@Getter
@Accessors(fluent = true)
public enum ExperimentTier {
    BEGINNER("Beginner", Material.WHITE_DYE),
    HIGH("High", Material.LIME_DYE),
    GRAND("Grand", Material.YELLOW_DYE),
    SUPREME("Supreme", Material.ORANGE_DYE),
    TRANSCENDENT("Transcendent", Material.RED_DYE),
    METAPHYSICAL("Metaphysical", Material.PURPLE_DYE);

    private final String displayName;
    private final Material icon;

    ExperimentTier(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }

    public boolean isUnlocked(SkyBlockPlayer player) {
        return ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, this).isUnlocked(player);
    }

    public int requiredEnchantingLevel() {
        return ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, this).requiredEnchantingLevel();
    }

    public int colorCount() {
        return this == BEGINNER ? 0
                : ExperimentRules.forExperiment(ExperimentType.CHRONOMATRON, this).colorCount();
    }

    public int xpPerStep() {
        return this == BEGINNER ? 0
                : ExperimentRules.forExperiment(ExperimentType.CHRONOMATRON, this).xpPerStep();
    }

    public int superPairsXpPerPair() {
        return ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, this).xpPerPair();
    }

    public int baseClicks() {
        return ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, this).baseClicks();
    }

    public List<Integer> slotsForColor(int color) {
        return ExperimentRules.forExperiment(ExperimentType.CHRONOMATRON, this).slotsForColor(color);
    }

    public int xpReward(ExperimentType type) {
        return ExperimentRules.forExperiment(type, this).xpPerStep();
    }

    public static ExperimentTier fromName(String name) {
        return Arrays.stream(values())
                .filter(tier -> tier.name().equalsIgnoreCase(name) || tier.displayName.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown experiment tier: " + name));
    }
}
