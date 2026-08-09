package net.swofty.type.garden.config;

public record GardenBarnSkinDefinition(
    String id,
    String displayName,
    String rarity,
    String schematicFile,
    String unlockSource,
    int offsetX,
    int offsetY,
    int offsetZ
) {
}
