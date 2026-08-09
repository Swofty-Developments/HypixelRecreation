package net.swofty.service.jacobscontest;

import net.swofty.commons.YamlFileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class JacobsContestConfig {
    private static final Path CONFIG_PATH = Path.of("./configuration/skyblock/garden/jacobs_contests.yml");

    private final Map<String, Object> root;

    private JacobsContestConfig(Map<String, Object> root) {
        this.root = root;
    }

    public static JacobsContestConfig load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            return new JacobsContestConfig(Map.of());
        }
        try {
            Map<String, Object> loaded = YamlFileUtils.loadYaml(file);
            return new JacobsContestConfig(loaded == null ? Map.of() : loaded);
        } catch (IOException e) {
            return new JacobsContestConfig(Map.of());
        }
    }

    public long getYearLength() {
        return getLong(getSection(root, "calendar"), "year_length", 8_928_000L);
    }

    public long getDayLength() {
        return getLong(getSection(root, "calendar"), "day_length", 24_000L);
    }

    public int getIntervalDays() {
        return getInt(getSection(root, "calendar"), "contest_interval_days", 3);
    }

    public int getDurationDays() {
        return getInt(getSection(root, "calendar"), "contest_duration_days", 1);
    }

    public List<String> getCrops() {
        Object value = root.get("crops");
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(
            "WHEAT",
            "CARROT",
            "POTATO",
            "PUMPKIN",
            "MELON_SLICE",
            "MUSHROOM",
            "CACTUS",
            "SUGAR_CANE",
            "NETHER_WART",
            "COCOA_BEANS",
            "SUNFLOWER",
            "MOONFLOWER",
            "WILD_ROSE"
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getSection(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static int getInt(Map<String, Object> root, String key, int def) {
        Object value = root.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private static long getLong(Map<String, Object> root, String key, long def) {
        Object value = root.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }
}
