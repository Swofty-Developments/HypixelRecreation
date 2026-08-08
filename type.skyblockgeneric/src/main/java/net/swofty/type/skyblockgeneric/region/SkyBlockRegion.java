package net.swofty.type.skyblockgeneric.region;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.data.monogdb.RegionDatabase;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class SkyBlockRegion {
    private static final Map<String, SkyBlockRegion> REGION_CACHE = new HashMap<>();

    private final String name;
    private final RegionDatabase regionDatabase;

    private Pos firstLocation;
    private Pos secondLocation;
    private RegionType type;
    private ServerType serverType;

    public SkyBlockRegion(String name, Pos firstLocation, Pos secondLocation, RegionType type, ServerType serverType) {
        this.name = name.toLowerCase();
        this.firstLocation = firstLocation;
        this.secondLocation = secondLocation;
        this.type = type;
        this.regionDatabase = new RegionDatabase(name);
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

        REGION_CACHE.put(name, this);
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

    /**
     * Gets a random position for an entity to spawn in the region
     *
     * @param instance The instance to check if the position is valid in
     * @return A random position in the region, or null if there is no valid position
     * @see SkyBlockMob#spawn()
     * @see SkyBlockMob#setInstance(Instance, Pos)
     */
    public @Nullable Pos getRandomPositionForEntity(Instance instance) {
        // Checks if there is a 2 block gap up the Y level and if the block below is solid

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
                    && !instance.getBlock(blockBelow).air())
                return randomPosition;
        }
    }

    public void delete() {
        REGION_CACHE.remove(name);
        regionDatabase.remove(name);
    }

    public static List<SkyBlockRegion> getRegions() {
        return new ArrayList<>(REGION_CACHE.values());
    }

    public static SkyBlockRegion getFromID(String id) {
        if (REGION_CACHE.containsKey(id.toLowerCase()))
            return REGION_CACHE.get(id.toLowerCase());
        return null;
    }

    public static SkyBlockRegion getRegionOfEntity(Entity entity) {
        return getRegionOfPosition(entity.getPosition());
    }

    public static List<SkyBlockMob> getMobsInRegion(RegionType region) {
        List<SkyBlockMob> mobs = new ArrayList<>();
        for (SkyBlockMob mob : SkyBlockMob.getMobs()) {
            SkyBlockRegion regionOfEntity = getRegionOfEntity(mob);

            if (regionOfEntity != null && regionOfEntity.getType() == region)
                mobs.add(mob);
        }
        return mobs;
    }

    public static SkyBlockRegion getRegionOfPosition(Point point) {
        return getRegionOfPosition(point.asPos());
    }

    public static SkyBlockRegion getRandomRegionOfType(RegionType type) {
        List<SkyBlockRegion> regions = new ArrayList<>();
        for (SkyBlockRegion region : getRegions()) {
            if (region.getType() == type)
                regions.add(region);
        }
        if (regions.isEmpty()) return null;
        double random = Math.random();
        int index = (int) (random * regions.size());
        return regions.get(index);
    }

    private long cachedVolume = -1;

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

    public static SkyBlockRegion getRegionOfPosition(Pos position) {
        SkyBlockRegion smallest = null;
        long smallestVolume = Long.MAX_VALUE;

        for (SkyBlockRegion region : getRegions()) {
            if (!region.insideRegion(position))
                continue;

            long volume = region.getVolume();
            if (volume < smallestVolume) {
                smallestVolume = volume;
                smallest = region;
            }
        }

        return smallest;
    }

    public boolean insideRegion(Entity entity) {
        return insideRegion(entity.getPosition());
    }

    public boolean insideRegion(Pos location) {
        List<Integer> bounds = getBounds();
        double x = location.x();
        double y = location.y();
        double z = location.z();
        if (firstLocation == null)
            return false;
        return x >= (double) bounds.get(0) && x <= (double) bounds.get(1) &&
            y >= (double) bounds.get(2) && y <= (double) bounds.get(3) &&
            z >= (double) bounds.get(4) && z <= (double) bounds.get(5);
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

    public static void cacheRegions() {
        for (SkyBlockRegion region : RegionDatabase.getAllRegions()) {
            if (region.getType() == null || region.getType() == RegionType.PRIVATE_ISLAND) {
                region.delete();
            } else {
                ServerType typeOfRegion = region.getServerType();
                if (typeOfRegion != HypixelConst.getTypeLoader().getType())
                    continue;

                REGION_CACHE.put(region.getName(), region);
            }
        }
        REGION_CACHE.put("island", new SkyBlockRegion("island",
            new Pos(0, 0, 0),
            new Pos(0, 0, 0),
            RegionType.PRIVATE_ISLAND,
            ServerType.SKYBLOCK_ISLAND));
    }

    public static SkyBlockRegion getIslandRegion() {
        return REGION_CACHE.get("island");
    }
}
