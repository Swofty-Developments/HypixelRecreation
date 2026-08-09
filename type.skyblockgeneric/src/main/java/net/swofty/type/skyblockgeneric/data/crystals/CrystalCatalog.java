package net.swofty.type.skyblockgeneric.data.crystals;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.config.YamlConfigLoader;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import org.tinylog.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CrystalCatalog {
    private static final String RESOURCE = "/Minestom.crystals.yml";
    private static final Path LOCATION_FILE = Path.of("configuration/skyblock/Minestom.crystals.yml");
    private static final List<CrystalEntry> CRYSTALS = new ArrayList<>();

    private static CrystalConfiguration configuration;
    private static boolean loaded;

    private CrystalCatalog() {
    }

    public static synchronized List<CrystalData> getFromAround(ServerType type, Pos position, double distance) {
        load();
        List<CrystalData> crystals = new ArrayList<>();
        for (CrystalEntry crystal : CRYSTALS) {
            if (crystal.serverType != type || crystal.position.distance(position) > distance) {
                continue;
            }
            crystals.add(crystal.toData());
        }
        return crystals;
    }

    public static synchronized List<CrystalData> getAllCrystals() {
        load();
        return CRYSTALS.stream().map(CrystalEntry::toData).toList();
    }

    public static synchronized void addCrystal(String url, Pos position, ItemType itemType) {
        load();
        int id = CRYSTALS.stream().mapToInt(crystal -> crystal.id).max().orElse(0) + 1;
        CRYSTALS.add(new CrystalEntry(
            id,
            url,
            position,
            itemType,
            HypixelConst.getTypeLoader().getType()));
        write();
    }

    public static synchronized void removeCrystals(Pos position, double distance) {
        load();
        boolean removed = CRYSTALS.removeIf(crystal -> crystal.position.distance(position) <= distance);
        if (removed) {
            write();
        }
    }

    private static void load() {
        if (loaded) {
            return;
        }

        configuration = YamlConfigLoader.load(
            LOCATION_FILE,
            CrystalCatalog.class,
            RESOURCE,
            CrystalConfiguration.class
        );
        if (configuration.crystals() == null) {
            throw new IllegalStateException("Crystal catalog has no crystals list in " + LOCATION_FILE);
        }

        for (int index = 0; index < configuration.crystals().size(); index++) {
            CrystalDefinition definition = configuration.crystals().get(index);
            try {
                CRYSTALS.add(parseCrystal(definition));
            } catch (RuntimeException exception) {
                Logger.error(exception, "Error parsing crystal catalog entry {} - skipping.", index + 1);
            }
        }
        loaded = true;
    }

    private static CrystalEntry parseCrystal(CrystalDefinition definition) {
        String url = requireText(definition.url(), "url");
        Pos position = new Pos(definition.x(), definition.y(), definition.z());
        ServerType serverType = ServerType.getSkyblockServer(
            requireText(definition.serverType(), "serverType").toUpperCase(Locale.ROOT));
        ItemType itemType = ItemType.valueOf(
            requireText(definition.itemType(), "itemType").toUpperCase(Locale.ROOT));
        return new CrystalEntry(definition.id(), url, position, itemType, serverType);
    }

    private static void write() {
        configuration = new CrystalConfiguration(CRYSTALS.stream()
            .map(crystal -> new CrystalDefinition(
                crystal.id,
                crystal.url,
                crystal.position.x(),
                crystal.position.y(),
                crystal.position.z(),
                crystal.serverType.name(),
                crystal.itemType.name()))
            .toList());
        YamlConfigLoader.save(LOCATION_FILE, CrystalConfiguration.class, configuration);
    }

    private static String requireText(String value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("Missing text field " + name);
        }
        return value;
    }

    private record CrystalConfiguration(List<CrystalDefinition> crystals) {
    }

    private record CrystalDefinition(
        int id,
        String url,
        double x,
        double y,
        double z,
        String serverType,
        String itemType
    ) {
    }

    private record CrystalEntry(int id, String url, Pos position, ItemType itemType, ServerType serverType) {
        private CrystalData toData() {
            return new CrystalData(url, position.add(0.5, 0, 0.5), itemType, serverType);
        }
    }

    public static class CrystalData {
        public final String url;
        public final Pos position;
        public final ItemType itemType;
        public final ServerType serverType;

        private CrystalData(String url, Pos position, ItemType itemType, ServerType serverType) {
            this.url = url;
            this.position = position;
            this.itemType = itemType;
            this.serverType = serverType;
        }
    }
}
