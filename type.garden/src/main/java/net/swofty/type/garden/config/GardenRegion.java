package net.swofty.type.garden.config;

import net.minestom.server.coordinate.Point;

public record GardenRegion(int minX, int maxX, int minZ, int maxZ) {
    public boolean contains(Point point) {
        int x = point.blockX();
        int z = point.blockZ();
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public int width() {
        return (maxX - minX) + 1;
    }
}
