package net.swofty.type.ravengardgeneric.region;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.swofty.commons.ServerType;
import net.swofty.type.ravengardgeneric.data.monogdb.RavengardRegionDatabase;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Getter
@Setter
public class RavengardRegion {
    private static final Map<String, RavengardRegion> REGION_CACHE = new HashMap<>();

    private final String name;
    private final RavengardRegionDatabase regionDatabase;

    private Pos firstLocation;
    private Pos secondLocation;
    private RavengardRegionType type;
    private ServerType serverType;

    private long cachedVolume = -1;

    public RavengardRegion(String name, Pos firstLocation, Pos secondLocation,
                           RavengardRegionType type, ServerType serverType) {
        this.name = name.toLowerCase();
        this.firstLocation = firstLocation;
        this.secondLocation = secondLocation;
        this.type = type;
        this.regionDatabase = new RavengardRegionDatabase(name);
        this.serverType = serverType;
    }

    public void save() {
        regionDatabase.insertOrUpdate("x1", firstLocation.blockX());
        regionDatabase.insertOrUpdate("y1", firstLocation.blockY());
        regionDatabase.insertOrUpdate("z1", firstLocation.blockZ());

        regionDatabase.insertOrUpdate("x2", secondLocation.blockX());
        regionDatabase.insertOrUpdate("y2", secondLocation.blockY());
        regionDatabase.insertOrUpdate("z2", secondLocation.blockZ());

        regionDatabase.insertOrUpdate("type", type.name());
        regionDatabase.insertOrUpdate("serverType", serverType.name());

        cache();
    }

    /** Registers the region for lookups without writing it to the database. */
    public void cache() {
        REGION_CACHE.put(name, this);
    }

    public void delete() {
        REGION_CACHE.remove(name);
        regionDatabase.remove(name);
    }

    public Pos getRandomPosition() {
        List<Integer> bounds = getBounds();

        int xMin = bounds.get(0), xMax = bounds.get(1);
        int yMin = bounds.get(2), yMax = bounds.get(3);
        int zMin = bounds.get(4), zMax = bounds.get(5);

        Random random = new Random();

        int x = xMax > xMin ? random.nextInt(xMax - xMin) + xMin : xMin;
        int y = yMax > yMin ? random.nextInt(yMax - yMin) + yMin : yMin;
        int z = zMax > zMin ? random.nextInt(zMax - zMin) + zMin : zMin;

        return new Pos(x, y, z);
    }

    public @Nullable Pos getRandomPositionForEntity(Instance instance) {
        int tries = 0;
        while (true) {
            tries++;
            Pos randomPosition = getRandomPosition();
            Pos blockAbove = randomPosition.add(0, 1, 0);
            Pos blockBelow = randomPosition.sub(0, 1, 0);

            if (tries > 5) {
                return null;
            }

            if (instance.isChunkLoaded(randomPosition)
                    && instance.getBlock(randomPosition).air()
                    && instance.getBlock(blockAbove).air()
                    && !instance.getBlock(blockBelow).air()) {
                return randomPosition;
            }
        }
    }

    public long getVolume() {
        if (cachedVolume == -1) {
            List<Integer> bounds = getBounds();
            long x = (long) bounds.get(1) - bounds.get(0) + 1;
            long y = (long) bounds.get(3) - bounds.get(2) + 1;
            long z = (long) bounds.get(5) - bounds.get(4) + 1;
            cachedVolume = x * y * z;
        }
        return cachedVolume;
    }

    public boolean insideRegion(Entity entity) {
        return insideRegion(entity.getPosition());
    }

    public boolean insideRegion(Pos location) {
        if (firstLocation == null) {
            return false;
        }
        List<Integer> bounds = getBounds();
        double x = location.x();
        double y = location.y();
        double z = location.z();
        return x >= (double) bounds.get(0) && x <= (double) bounds.get(1)
                && y >= (double) bounds.get(2) && y <= (double) bounds.get(3)
                && z >= (double) bounds.get(4) && z <= (double) bounds.get(5);
    }

    public List<Integer> getBounds() {
        int sx = Math.min(firstLocation.blockX(), secondLocation.blockX()),
                ex = Math.max(firstLocation.blockX(), secondLocation.blockX()),
                sy = Math.min(firstLocation.blockY(), secondLocation.blockY()),
                ey = Math.max(firstLocation.blockY(), secondLocation.blockY()),
                sz = Math.min(firstLocation.blockZ(), secondLocation.blockZ()),
                ez = Math.max(firstLocation.blockZ(), secondLocation.blockZ());
        return Arrays.asList(sx, ex, sy, ey, sz, ez);
    }

    public static List<RavengardRegion> getRegions() {
        return new ArrayList<>(REGION_CACHE.values());
    }

    public static RavengardRegion getFromID(String id) {
        return id == null ? null : REGION_CACHE.get(id.toLowerCase());
    }

    public static RavengardRegion getRegionOfEntity(Entity entity) {
        return getRegionOfPosition(entity.getPosition());
    }

    public static RavengardRegion getRegionOfPosition(Point point) {
        return getRegionOfPosition(point.asPos());
    }

    /** Overlapping regions resolve to the smallest, so a room inside an area wins over the area. */
    public static RavengardRegion getRegionOfPosition(Pos position) {
        RavengardRegion smallest = null;
        long smallestVolume = Long.MAX_VALUE;

        for (RavengardRegion region : getRegions()) {
            if (!region.insideRegion(position)) {
                continue;
            }
            long volume = region.getVolume();
            if (volume < smallestVolume) {
                smallestVolume = volume;
                smallest = region;
            }
        }

        return smallest;
    }

    public static RavengardRegion getRandomRegionOfType(RavengardRegionType type) {
        List<RavengardRegion> regions = new ArrayList<>();
        for (RavengardRegion region : getRegions()) {
            if (region.getType() == type) {
                regions.add(region);
            }
        }
        if (regions.isEmpty()) {
            return null;
        }
        return regions.get((int) (Math.random() * regions.size()));
    }

    public static void cacheRegions() {
        for (RavengardRegion region : RavengardRegionDatabase.getAllRegions()) {
            if (region.getType() == null) {
                region.delete();
                continue;
            }
            REGION_CACHE.put(region.getName(), region);
        }
        RavengardRegions.registerDefaults();
    }
}
