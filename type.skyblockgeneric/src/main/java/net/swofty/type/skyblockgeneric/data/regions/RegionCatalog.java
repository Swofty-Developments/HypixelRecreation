package net.swofty.type.skyblockgeneric.data.regions;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.config.YamlConfigLoader;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RegionCatalog {
    private static final String RESOURCE = "/Minestom.regions.yml";
    private static final Path LOCATION_FILE = Path.of("configuration/skyblock/Minestom.regions.yml");
    private static final List<SkyBlockRegion> REGIONS = new ArrayList<>();

    private static RegionConfiguration configuration;
    private static boolean loaded;

    private RegionCatalog() {
    }

    public static synchronized List<SkyBlockRegion> getAllRegions() {
        load();
        return List.copyOf(REGIONS);
    }

    public static synchronized void save(SkyBlockRegion region) {
        load();
        REGIONS.removeIf(existing -> existing.getName().equalsIgnoreCase(region.getName()));
        REGIONS.add(region);
        write();
    }

    public static synchronized void delete(String id) {
        load();
        REGIONS.removeIf(existing -> existing.getName().equalsIgnoreCase(id));
        write();
    }

    private static void load() {
        if (loaded) {
            return;
        }

        configuration = YamlConfigLoader.load(
            LOCATION_FILE,
            RegionCatalog.class,
            RESOURCE,
            RegionConfiguration.class
        );
        if (configuration.regions() == null) {
            throw new IllegalStateException("Region catalog has no regions list in " + LOCATION_FILE);
        }

        for (int index = 0; index < configuration.regions().size(); index++) {
            REGIONS.add(parseRegion(index, configuration.regions().get(index)));
        }
        loaded = true;
    }

    private static SkyBlockRegion parseRegion(int index, RegionDefinition definition) {
        try {
            String name = requireText(definition.id(), "id");
            RegionType type = RegionType.valueOf(requireText(definition.type(), "type").toUpperCase(Locale.ROOT));
            ServerType serverType = ServerType.getSkyblockServer(
                definition.serverType() == null
                    ? ServerType.SKYBLOCK_HUB.name()
                    : definition.serverType().toUpperCase(Locale.ROOT));

            return new SkyBlockRegion(
                name,
                new Pos(definition.x1(), definition.y1(), definition.z1()),
                new Pos(definition.x2(), definition.y2(), definition.z2()),
                type,
                serverType);
        } catch (RuntimeException exception) {
            throw invalidEntry(index, definition, exception);
        }
    }

    private static void write() {
        configuration = new RegionConfiguration(REGIONS.stream()
            .map(region -> new RegionDefinition(
                region.getName(),
                region.getType().name(),
                region.getFirstLocation().blockX(),
                region.getFirstLocation().blockY(),
                region.getFirstLocation().blockZ(),
                region.getSecondLocation().blockX(),
                region.getSecondLocation().blockY(),
                region.getSecondLocation().blockZ(),
                region.getServerType().name()))
            .toList());
        YamlConfigLoader.save(LOCATION_FILE, RegionConfiguration.class, configuration);
    }

    private static String requireText(String value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("Missing text field " + name);
        }
        return value;
    }

    private static IllegalStateException invalidEntry(int index, Object entry, RuntimeException cause) {
        return new IllegalStateException("Invalid region catalog entry " + (index + 1) + ": " + entry, cause);
    }

    private record RegionConfiguration(List<RegionDefinition> regions) {
    }

    private record RegionDefinition(
        String id,
        String type,
        int x1,
        int y1,
        int z1,
        int x2,
        int y2,
        int z2,
        String serverType
    ) {
    }
}
