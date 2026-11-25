package net.swofty.type.garden.config;

import net.swofty.commons.YamlFileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GardenConfigRegistry {
    public static final Path CONFIG_DIR = Path.of("./configuration/skyblock/garden");
    private static final List<String> CONFIG_FILES = List.of(
        "levels.yml",
        "plots.yml",
        "barn_skins.yml",
        "crop_upgrades.yml",
        "composter.yml",
        "copper_shop.yml",
        "visitors.yml",
        "visitor_dialogue.yml",
        "pests.yml",
        "pest_drops.yml",
        "chips.yml",
        "greenhouse.yml",
        "mutations.yml",
        "npc_anchors.yml",
        "jacobs_contests.yml",
        "milestones.yml"
    );

    private static final Map<String, Map<String, Object>> CONFIGS = new HashMap<>();

    private GardenConfigRegistry() {
    }

    public static synchronized void reload() {
        CONFIGS.clear();
        File directory = CONFIG_DIR.toFile();
        YamlFileUtils.ensureDirectoryExists(directory);

        for (String fileName : CONFIG_FILES) {
            File file = CONFIG_DIR.resolve(fileName).toFile();
            if (!file.exists()) {
                CONFIGS.put(fileName, Collections.emptyMap());
                continue;
            }

            try {
                Map<String, Object> loaded = YamlFileUtils.loadYaml(file);
                CONFIGS.put(fileName, loaded == null ? Collections.emptyMap() : loaded);
            } catch (IOException e) {
                Logger.error(e, "Failed to load Garden config {}", fileName);
                CONFIGS.put(fileName, Collections.emptyMap());
            }
        }
    }

    public static Map<String, Object> getConfig(String fileName) {
        return CONFIGS.getOrDefault(fileName, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getSection(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getMapList(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return List.of();
    }

    public static String getString(Map<String, Object> root, String key, String def) {
        Object value = root.get(key);
        return value == null ? def : String.valueOf(value);
    }

    public static int getInt(Map<String, Object> root, String key, int def) {
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

    public static long getLong(Map<String, Object> root, String key, long def) {
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

    public static double getDouble(Map<String, Object> root, String key, double def) {
        Object value = root.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return 0.0d; // kinda dumb this hides problems
            }
        }
        return def;
    }

    public static boolean getBoolean(Map<String, Object> root, String key, boolean def) {
        Object value = root.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return def;
    }
}
