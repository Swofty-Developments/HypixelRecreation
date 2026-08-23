package net.swofty.type.skyblockgeneric.item;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SafeConfig {
    final Map<String, Object> config;
    private final String path;

    public SafeConfig(Map<String, Object> config) {
        this(config, "");
    }

    private SafeConfig(Map<String, Object> config, String path) {
        this.config = config != null ? config : new HashMap<>();
        this.path = path;
    }

    @Nullable
    public String getString(String key) {
        return get(key, String.class, null);
    }

    public String getString(String key, String defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }

        return value.toString();
    }

    public String getString(String key, String nestedKey, String defaultValue) {
        Object value = config.get(key);
        if (value instanceof Map<?, ?>) {
            return getNested(key).getString(nestedKey, defaultValue);
        }
        return value == null ? defaultValue : value.toString();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        Object value = config.get(key);
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.intValue();
            }
            case String string -> {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
            default -> {
            }
        }

        return defaultValue;
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, int defaultValue) {
        Object value = config.get(key);
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.intValue();
            }
            case String string -> {
                try {
                    return Long.parseLong(string);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
            default -> {
            }
        }

        return defaultValue;
    }

    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    public double getDouble(String key, double defaultValue) {
        Object value = config.get(key);
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.doubleValue();
            }
            case String string -> {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
            default -> {
            }
        }

        return defaultValue;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        return switch (value) {
            case Boolean bool -> bool;
            case String string -> Boolean.parseBoolean(string);
            case null, default -> defaultValue;
        };

    }

    @Nullable
    public <T> T get(String key, Class<T> type, @Nullable T defaultValue) {
        Object value = config.get(key);
        if (value == null) return defaultValue;

        try {
            return (T) value;
        } catch (ClassCastException e) {
            throw new ConfigParseException(
                    String.format("Expected type %s for key '%s%s', got %s",
                            type.getSimpleName(), path, key, value.getClass().getSimpleName())
            );
        }
    }

    @NotNull
    public List<Map<String, Object>> getMapList(String key) {
        Object value = config.get(key);
        if (value == null) return new ArrayList<>();

        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object element : list) {
                if (element instanceof Map<?, ?>) {
                    try {
                        result.add((Map<String, Object>) element);
                    } catch (ClassCastException e) {
                        throw new ConfigParseException(
                                String.format("Expected List<Map<String, Object>> for key '%s%s'", path, key)
                        );
                    }
                } else {
                    throw new ConfigParseException(
                            String.format("Expected Map in list for key '%s%s', got %s",
                                    path, key, element.getClass().getSimpleName())
                    );
                }
            }
            return result;
        }
        throw new ConfigParseException(
                String.format("Expected List for key '%s%s', got %s", path, key, value.getClass().getSimpleName())
        );
    }

    @NotNull
    public <T> List<T> getList(String key, Class<T> elementType) {
        Object value = config.get(key);
        if (value == null) return new ArrayList<>();

        if (value instanceof List<?> list) {
            List<T> result = new ArrayList<>();
            for (Object element : list) {
                try {
                    result.add((T) element);
                } catch (ClassCastException e) {
                    throw new ConfigParseException(
                            String.format("Expected List<%s> for key '%s%s'",
                                    elementType.getSimpleName(), path, key)
                    );
                }
            }
            return result;
        }
        throw new ConfigParseException(
                String.format("Expected List for key '%s%s', got %s", path, key, value.getClass().getSimpleName())
        );
    }

    @NotNull
    public Map<String, Object> getMap(String key) {
        Object value = config.get(key);
        if (value == null) return new HashMap<>();

        if (value instanceof Map<?, ?>) {
            try {
                return (Map<String, Object>) value;
            } catch (ClassCastException e) {
                throw new ConfigParseException(
                        String.format("Expected Map<String, Object> for key '%s%s'", path, key)
                );
            }
        }
        throw new ConfigParseException(
                String.format("Expected Map for key '%s%s', got %s", path, key, value.getClass().getSimpleName())
        );
    }

    @NotNull
    public SafeConfig getNested(String key) {
        return new SafeConfig(getMap(key), path + key + ".");
    }

    public boolean containsKey(String key) {
        return config.containsKey(key);
    }

    @NotNull
    public Set<String> getKeys() {
        return config.keySet();
    }

    @NotNull
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
        String value = getString(key);
        if (value == null) {
            throw new ConfigParseException(
                    String.format("Missing required enum value for key '%s%s'", path, key)
            );
        }

        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigParseException(
                    String.format("Invalid enum value '%s' for key '%s%s'. Valid values: %s",
                            value, path, key, Arrays.toString(enumClass.getEnumConstants()))
            );
        }
    }

    public static SafeConfig of(Map<String, Object> config) {
        return new SafeConfig(config);
    }

    public static class ConfigParseException extends RuntimeException {
        public ConfigParseException(String message) {
            super(message);
        }
    }
}
