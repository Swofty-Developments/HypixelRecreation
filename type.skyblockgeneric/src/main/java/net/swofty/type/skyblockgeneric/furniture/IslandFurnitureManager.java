package net.swofty.type.skyblockgeneric.furniture;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.block.impl.EntityBackedBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.SkyBlockIsland;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IslandFurnitureManager {
    private static final String COUNTS_KEY = "furniture_counts";
    private static final String PLACEMENTS_KEY = "furniture_placements";

    private final SkyBlockIsland island;
    private final Map<FurnitureLimitPool, Integer> counts = new EnumMap<>(FurnitureLimitPool.class);
    private final Map<UUID, FurniturePlacement> placements = new LinkedHashMap<>();
    private final Map<UUID, List<LivingEntity>> activeEntities = new LinkedHashMap<>();
    private boolean placementsMigrated;

    public IslandFurnitureManager(SkyBlockIsland island) {
        this.island = island;
        loadCounts();
        loadPlacements();
        if (placementsMigrated) save();
        normalizeExperimentationTableCount();
    }

    public synchronized boolean place(SkyBlockPlayer player, FurnitureLimitPool pool, String displayName) {
        int current = count(pool);
        if (current >= pool.limit()) {
            player.sendMessage("<c>You have the maximum number of this furniture allowed on your island. (<e>"
                    + current + "<c>/<e>" + pool.limit() + "<c>)");
            return false;
        }
        counts.put(pool, current + 1);
        save();
        player.sendMessage("<7>You placed an <e>" + displayName + "<7>. (" + (current + 1) + "/" + pool.limit() + ")");
        return true;
    }

    public synchronized void remove(SkyBlockPlayer player, FurnitureLimitPool pool, String displayName) {
        int remaining = decrement(pool);
        save();
        sendRemovedMessage(player, displayName, remaining, pool.limit());
    }

    public synchronized boolean remove(SkyBlockPlayer player, UUID placementId) {
        FurniturePlacement placement = removePlacement(placementId);
        if (placement == null) return false;

        sendRemovedMessage(player, placement.displayName(), count(placement.pool()), placement.pool().limit());
        return true;
    }

    public synchronized void rollbackPlace(FurnitureLimitPool pool) {
        decrement(pool);
        save();
    }

    public synchronized void register(FurniturePlacement placement, List<? extends LivingEntity> entities) {
        FurniturePlacement previous = placements.put(placement.id(), placement);
        if (previous != null) {
            removeActiveEntities(placement.id());
        }

        List<LivingEntity> active = new ArrayList<>(entities);
        tagEntities(placement, active);
        activeEntities.put(placement.id(), active);
        save();
    }

    public synchronized FurniturePlacement getPlacement(UUID placementId) {
        return placements.get(placementId);
    }

    public synchronized FurniturePlacement findPlacementAt(double x, double y, double z) {
        return placements.values().stream()
                .filter(placement -> placement.blockX() == x
                        && placement.blockY() == y
                        && placement.blockZ() == z)
                .findFirst()
                .orElse(null);
    }

    public synchronized boolean hasPlacement(UUID placementId) {
        return placements.containsKey(placementId);
    }

    public synchronized List<FurniturePlacement> placements() {
        return List.copyOf(placements.values());
    }

    public synchronized void restore(Instance instance) {
        clearRuntime();

        placements.values().forEach(placement -> {
            try {
                List<LivingEntity> entities = Furniture.load(
                        placement.furnitureName(), instance, placement.offset(), placement.rotationYaw());
                tagEntities(placement, entities);
                activeEntities.put(placement.id(), entities);
            } catch (RuntimeException exception) {
                Logger.error(exception, "[{}] Failed to restore furniture '{}'", island.getIslandID(),
                        placement.furnitureName());
            }
        });
    }

    public synchronized void clearRuntime() {
        activeEntities.values().forEach(Furniture::remove);
        activeEntities.clear();
    }

    public synchronized int clearAll() {
        int removed = placements.size();
        clearRuntime();
        placements.clear();
        counts.clear();
        save();
        return removed;
    }

    public synchronized int count(FurnitureLimitPool pool) {
        return counts.getOrDefault(pool, 0);
    }

    private FurniturePlacement removePlacement(UUID placementId) {
        FurniturePlacement placement = placements.remove(placementId);
        if (placement == null) return null;

        removeActiveEntities(placementId);
        decrement(placement.pool());
        save();
        return placement;
    }

    private void loadCounts() {
        Object stored = island.getDatabase().get(COUNTS_KEY, Map.of());
        if (stored instanceof Map<?, ?> values) {
            values.forEach((key, value) -> {
                try {
                    counts.put(FurnitureLimitPool.valueOf(String.valueOf(key)), ((Number) value).intValue());
                } catch (IllegalArgumentException | ClassCastException ignored) {
                }
            });
        }
    }

    private void loadPlacements() {
        Object stored = island.getDatabase().get(PLACEMENTS_KEY, List.of());
        if (stored instanceof List<?> values) {
            values.forEach(this::loadPlacement);
            return;
        }
        if (stored instanceof Map<?, ?> values) {
            values.values().forEach(this::loadPlacement);
        }
    }

    private void loadPlacement(Object value) {
        if (!(value instanceof Map<?, ?> data)) return;

        try {
            FurniturePlacement placement = FurniturePlacement.deserialize(data);
            if (placement.furnitureName().equalsIgnoreCase("experimentation_table")
                    && Math.abs(placement.offsetY() - (placement.blockY() - 0.5)) < 0.001) {
                placementsMigrated = true;
                placement = new FurniturePlacement(
                        placement.id(),
                        placement.furnitureName(),
                        placement.pool(),
                        placement.displayName(),
                        placement.blockX(),
                        placement.blockY(),
                        placement.blockZ(),
                        placement.offsetX(),
                        placement.blockY(),
                        placement.offsetZ(),
                        placement.entityBackedBlockType(),
                        placement.rotationYaw()
                );
            }
            placements.put(placement.id(), placement);
        } catch (RuntimeException exception) {
            Logger.warn(exception, "[{}] Ignoring invalid furniture placement", island.getIslandID());
        }
    }

    private void normalizeExperimentationTableCount() {
        int placementCount = (int) placements.values().stream()
                .filter(placement -> placement.pool() == FurnitureLimitPool.EXPERIMENTATION_TABLE)
                .count();

        if (placementCount == 0) {
            if (counts.remove(FurnitureLimitPool.EXPERIMENTATION_TABLE) != null) save();
            return;
        }

        if (counts.getOrDefault(FurnitureLimitPool.EXPERIMENTATION_TABLE, 0) != placementCount) {
            counts.put(FurnitureLimitPool.EXPERIMENTATION_TABLE, placementCount);
            save();
        }
    }

    private int decrement(FurnitureLimitPool pool) {
        int remaining = Math.max(0, count(pool) - 1);
        counts.put(pool, remaining);
        return remaining;
    }

    private void removeActiveEntities(UUID placementId) {
        List<LivingEntity> entities = activeEntities.remove(placementId);
        if (entities != null) Furniture.remove(entities);
    }

    private static void tagEntities(FurniturePlacement placement, List<? extends LivingEntity> entities) {
        if (placement.entityBackedBlockType() == null) return;

        entities.forEach(entity -> {
            entity.setTag(EntityBackedBlock.BLOCK_TYPE_TAG, placement.entityBackedBlockType());
            entity.setTag(EntityBackedBlock.BLOCK_ID_TAG, placement.id().toString());
        });
    }

    private static void sendRemovedMessage(SkyBlockPlayer player, String displayName, int current, int maximum) {
        player.sendMessage("<7>You removed an <e>" + displayName + "<7>. (" + current + "/" + maximum + ")");
    }

    private void save() {
        Map<String, Integer> serialized = new ConcurrentHashMap<>();
        counts.forEach((pool, count) -> serialized.put(pool.name(), count));
        island.getDatabase().set(COUNTS_KEY, serialized);

        List<Map<String, Object>> serializedPlacements = placements.values().stream()
                .map(FurniturePlacement::serialize)
                .toList();
        island.getDatabase().set(PLACEMENTS_KEY, serializedPlacements);
    }
}
