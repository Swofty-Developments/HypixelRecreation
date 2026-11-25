package net.swofty.type.garden.config;

public record GardenPlotDefinition(
    String id,
    String displayName,
    String group,
    int gardenLevel,
    boolean defaultUnlocked,
    GardenRegion region
) {
}
