package net.swofty.type.skyblockgeneric.furniture;

import net.minestom.server.coordinate.Pos;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record FurniturePlacement(
        UUID id,
        String furnitureName,
        FurnitureLimitPool pool,
        String displayName,
        double blockX,
        double blockY,
        double blockZ,
        double offsetX,
        double offsetY,
        double offsetZ,
        String entityBackedBlockType,
        float rotationYaw) {

    public FurniturePlacement(UUID id,
                              String furnitureName,
                              FurnitureLimitPool pool,
                              String displayName,
                              double blockX,
                              double blockY,
                              double blockZ,
                              double offsetX,
                              double offsetY,
                              double offsetZ,
                              String entityBackedBlockType) {
        this(id, furnitureName, pool, displayName, blockX, blockY, blockZ,
                offsetX, offsetY, offsetZ, entityBackedBlockType, 0f);
    }

    public Pos offset() {
        return new Pos(offsetX, offsetY, offsetZ);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id.toString());
        data.put("furnitureName", furnitureName);
        data.put("pool", pool.name());
        data.put("displayName", displayName);
        data.put("blockX", blockX);
        data.put("blockY", blockY);
        data.put("blockZ", blockZ);
        data.put("offsetX", offsetX);
        data.put("offsetY", offsetY);
        data.put("offsetZ", offsetZ);
        data.put("entityBackedBlockType", entityBackedBlockType);
        data.put("rotationYaw", rotationYaw);
        return data;
    }

    public static FurniturePlacement deserialize(Map<?, ?> data) {
        return new FurniturePlacement(
                UUID.fromString(String.valueOf(data.get("id"))),
                String.valueOf(data.get("furnitureName")),
                FurnitureLimitPool.valueOf(String.valueOf(data.get("pool"))),
                String.valueOf(data.get("displayName")),
                number(data, "blockX"),
                number(data, "blockY"),
                number(data, "blockZ"),
                number(data, "offsetX"),
                number(data, "offsetY"),
                number(data, "offsetZ"),
                data.get("entityBackedBlockType") == null
                        ? null
                        : String.valueOf(data.get("entityBackedBlockType")),
                number(data, "rotationYaw", 0f)
        );
    }

    private static double number(Map<?, ?> data, String key) {
        Object value = data.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Furniture placement field '" + key + "' is not numeric");
        }
        return number.doubleValue();
    }

    private static float number(Map<?, ?> data, String key, float defaultValue) {
        Object value = data.get(key);
        return value instanceof Number number ? number.floatValue() : defaultValue;
    }
}
