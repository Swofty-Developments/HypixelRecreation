package net.swofty.type.skyblockgeneric.item.handlers.customstatistics;

import java.util.HashMap;
import java.util.Map;

public class CustomStatisticsRegistry {
    private static final Map<String, CustomStatisticsConfig> REGISTERED_HANDLERS = new HashMap<>();

    public static void register(String id, CustomStatisticsConfig handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static CustomStatisticsConfig getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }
}