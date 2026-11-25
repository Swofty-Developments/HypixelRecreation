package net.swofty.type.garden.world;

import net.hollowcube.schem.Schematic;
import net.swofty.type.garden.config.GardenBarnSkinDefinition;
import net.swofty.type.garden.config.GardenConfigRegistry;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class GardenAssetRegistry {
    private static final Path GARDEN_SEED = Path.of("./configuration/skyblock/islands/garden_default.polar");
    private static final Map<String, Schematic> BARN_SCHEMATICS = new HashMap<>();

    private GardenAssetRegistry() {
    }

    public static Path getGardenSeed() {
        return GARDEN_SEED;
    }

    public static Path getBarnSkinSchematicPath(String fileName) {
        return GardenConfigRegistry.CONFIG_DIR.resolve(fileName);
    }

    public static Schematic getBarnSkinSchematic(GardenBarnSkinDefinition definition) {
        return BARN_SCHEMATICS.computeIfAbsent(definition.id(), ignored -> loadSchematic(definition));
    }

    private static Schematic loadSchematic(GardenBarnSkinDefinition definition) {
        Path file = getBarnSkinSchematicPath(definition.schematicFile());
        try {
            return new LitematicaSchematicReader().read(Files.readAllBytes(file));
        } catch (IOException e) {
            Logger.error(e, "Failed to load barn skin schematic {}", file);
            throw new RuntimeException("Failed to load barn skin schematic " + definition.id(), e);
        }
    }
}
