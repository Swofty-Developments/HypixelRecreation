package net.swofty.type.skyblockgeneric.data.fairysouls;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.config.YamlConfigLoader;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulZone;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FairySoulCatalog {
    private static final String CATALOG_RESOURCE = "/Minestom.fairysouls.yml";
    private static final Path LOCATION_FILE = Path.of("configuration/skyblock/Minestom.fairysouls.yml");
    private static final FairySoulConfiguration CONFIGURATION = YamlConfigLoader.load(
        LOCATION_FILE,
        FairySoulCatalog.class,
        CATALOG_RESOURCE,
        FairySoulConfiguration.class
    );
    private static final List<FairySoul> SOULS = load(CONFIGURATION);

    private FairySoulCatalog() {
    }

    public static List<FairySoul> getAllSouls() {
        return SOULS;
    }

    private static List<FairySoul> load(FairySoulConfiguration configuration) {
        if (configuration.souls() == null) {
            throw new IllegalStateException("Fairy soul catalog has no souls list in " + LOCATION_FILE);
        }

        List<FairySoul> souls = new ArrayList<>(configuration.souls().size());
        for (int index = 0; index < configuration.souls().size(); index++) {
            FairySoulDefinition definition = configuration.souls().get(index);
            try {
                FairySoulZone zone = FairySoulZone.valueOf(definition.zone());
                Pos location = zone.getServerType() == null
                    ? null
                    : new Pos(definition.x() + 0.5, definition.y(), definition.z() + 0.5);
                souls.add(new FairySoul(definition.id(), location, zone));
            } catch (RuntimeException exception) {
                throw invalidEntry(index, definition, exception);
            }
        }

        return List.copyOf(souls);
    }

    private static IllegalStateException invalidEntry(int index, Object entry, RuntimeException cause) {
        return new IllegalStateException("Invalid fairy soul catalog entry " + (index + 1) + ": " + entry, cause);
    }

    private record FairySoulConfiguration(int max_souls, List<FairySoulDefinition> souls) {
    }

    private record FairySoulDefinition(int id, String zone, int x, int y, int z) {
    }
}
