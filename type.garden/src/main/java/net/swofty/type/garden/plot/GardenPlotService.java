package net.swofty.type.garden.plot;

import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.util.Rotation;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.garden.config.GardenBarnSkinDefinition;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.config.GardenPlotDefinition;
import net.swofty.type.garden.config.GardenRegion;
import net.swofty.type.garden.user.SkyBlockGarden;
import net.swofty.type.garden.world.GardenAssetRegistry;
import net.swofty.type.skyblockgeneric.garden.GardenData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GardenPlotService {
    private final SkyBlockGarden garden;
    private final Map<String, GardenPlotDefinition> plots = new LinkedHashMap<>();
    private GardenRegion barnSwapRegion = new GardenRegion(-33, 35, -47, -5);
    private final int BASE_Y = 67;

    public GardenPlotService(SkyBlockGarden garden) {
        this.garden = garden;
        reload();
    }

    public void reload() {
        plots.clear();
        Map<String, Object> config = GardenConfigRegistry.getConfig("plots.yml");
        Map<String, Object> world = GardenConfigRegistry.getSection(config, "world");
        Map<String, Object> barnSwap = GardenConfigRegistry.getSection(world, "barn_swap_region");
        barnSwapRegion = new GardenRegion(
            GardenConfigRegistry.getInt(barnSwap, "min_x", -33),
            GardenConfigRegistry.getInt(barnSwap, "max_x", 35),
            GardenConfigRegistry.getInt(barnSwap, "min_z", -47),
            GardenConfigRegistry.getInt(barnSwap, "max_z", -5)
        );

        for (Map<String, Object> plot : GardenConfigRegistry.getMapList(config, "plots")) {
            Map<String, Object> bounds = GardenConfigRegistry.getSection(plot, "bounds");
            GardenPlotDefinition definition = new GardenPlotDefinition(
                GardenConfigRegistry.getString(plot, "id", "unknown"),
                GardenConfigRegistry.getString(plot, "display_name", GardenConfigRegistry.getString(plot, "id", "Unknown")),
                GardenConfigRegistry.getString(plot, "group", "A"),
                GardenConfigRegistry.getInt(plot, "garden_level", 0),
                GardenConfigRegistry.getBoolean(plot, "default_unlocked", false),
                new GardenRegion(
                    GardenConfigRegistry.getInt(bounds, "min_x", 0),
                    GardenConfigRegistry.getInt(bounds, "max_x", 0),
                    GardenConfigRegistry.getInt(bounds, "min_z", 0),
                    GardenConfigRegistry.getInt(bounds, "max_z", 0)
                )
            );
            plots.put(definition.id(), definition);
        }
    }

    public boolean canEdit(Point point, GardenData.GardenCoreData coreData, boolean barnSwapInProgress) {
        if (!garden.isWithinBounds(point)) {
            return false;
        }
        if (isInBarnSwapRegion(point)) {
            return false;
        }
        GardenPlotDefinition plot = getPlotAt(point);
        if (plot == null) {
            return false;
        }
        return plot.defaultUnlocked() || coreData.getUnlockedPlots().contains(plot.id());
    }

    public boolean isUnlocked(Point point, GardenData.GardenCoreData coreData) {
        GardenPlotDefinition plot = getPlotAt(point);
        if (plot == null) {
            return false;
        }
        return plot.defaultUnlocked() || coreData.getUnlockedPlots().contains(plot.id());
    }

    public GardenPlotDefinition getPlotAt(Point point) {
        for (GardenPlotDefinition definition : plots.values()) {
            if (definition.region().contains(point)) {
                return definition;
            }
        }
        return null;
    }

    public boolean isInBarnSwapRegion(Point point) {
        return barnSwapRegion.contains(point);
    }

    public CompletableFuture<Void> applyBarnSkin(String skinId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        GardenBarnSkinDefinition definition = getBarnSkinDefinition(skinId);
        if (definition == null) {
            future.completeExceptionally(new IllegalArgumentException("Unknown barn skin: " + skinId));
            return future;
        }
        if (garden.isBarnSwapInProgress()) {
            future.completeExceptionally(new IllegalStateException("A barn skin swap is already running"));
            return future;
        }

        SharedInstance instance = garden.getGardenInstance();
        if (instance == null) {
            future.completeExceptionally(new IllegalStateException("Garden instance is not loaded"));
            return future;
        }

        Schematic schematic = GardenAssetRegistry.getBarnSkinSchematic(definition);
        int delayTicks = Math.max(1, Math.round((30f * 20f) / barnSwapRegion.width()));
        final int[] currentX = {barnSwapRegion.minX()};

        garden.setBarnSwapInProgress(true);
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (currentX[0] > barnSwapRegion.maxX()) {
                garden.setBarnSwapInProgress(false);
                future.complete(null);
                return TaskSchedule.stop();
            }

            pasteSlice(instance, schematic, definition, currentX[0]);
            currentX[0]++;
            return TaskSchedule.tick(delayTicks);
        });

        return future;
    }

    private void pasteSlice(SharedInstance instance, Schematic schematic, GardenBarnSkinDefinition definition, int targetX) {
        schematic.forEachBlock(Rotation.NONE, (point, block) -> {
            int worldX = barnSwapRegion.minX() + point.blockX() + definition.offsetX();
            int worldY = BASE_Y + point.blockY() + definition.offsetY();
            int worldZ = barnSwapRegion.minZ() + point.blockZ() + definition.offsetZ();

            if (worldX != targetX) {
                return;
            }
            if (worldZ < barnSwapRegion.minZ() || worldZ > barnSwapRegion.maxZ()) {
                return;
            }
            instance.setBlock(worldX, worldY, worldZ, block);
        });
    }

    private int detectBarnBaseY(SharedInstance instance) {
        int detected = Integer.MAX_VALUE;
        for (int x = barnSwapRegion.minX(); x <= barnSwapRegion.maxX(); x++) {
            for (int z = barnSwapRegion.minZ(); z <= barnSwapRegion.maxZ(); z++) {
                for (int y = -64; y <= 255; y++) {
                    if (!instance.getBlock(x, y, z).isAir()) {
                        detected = Math.min(detected, y);
                        break;
                    }
                }
            }
        }
        return detected == Integer.MAX_VALUE ? 0 : detected;
    }

    private GardenBarnSkinDefinition getBarnSkinDefinition(String skinId) {
        return getBarnSkins().stream()
            .filter(definition -> definition.id().equalsIgnoreCase(skinId))
            .findFirst()
            .orElse(null);
    }

    public List<GardenBarnSkinDefinition> getBarnSkins() {
        Map<String, Object> config = GardenConfigRegistry.getConfig("barn_skins.yml");
        List<GardenBarnSkinDefinition> skins = new ArrayList<>();
        for (Map<String, Object> entry : GardenConfigRegistry.getMapList(config, "skins")) {
            Map<String, Object> offsets = GardenConfigRegistry.getSection(entry, "offsets");
            skins.add(new GardenBarnSkinDefinition(
                GardenConfigRegistry.getString(entry, "id", "default"),
                GardenConfigRegistry.getString(entry, "display_name", "Default"),
                GardenConfigRegistry.getString(entry, "rarity", "COMMON"),
                GardenConfigRegistry.getString(entry, "schematic", "default.litematic"),
                GardenConfigRegistry.getString(entry, "unlock_source", ""),
                GardenConfigRegistry.getInt(offsets, "x", 0),
                GardenConfigRegistry.getInt(offsets, "y", 0),
                GardenConfigRegistry.getInt(offsets, "z", 0)
            ));
        }
        skins.sort(Comparator.comparing(GardenBarnSkinDefinition::id));
        return skins;
    }

    public List<GardenPlotDefinition> getPlots() {
        return new ArrayList<>(plots.values());
    }

    public GardenRegion getBarnSwapRegion() {
        return barnSwapRegion;
    }
}
