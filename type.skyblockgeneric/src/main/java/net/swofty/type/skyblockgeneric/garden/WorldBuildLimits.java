package net.swofty.type.skyblockgeneric.garden;

import net.minestom.server.coordinate.Point;

public record WorldBuildLimits(int minX, int maxX, int minZ, int maxZ) {
    public boolean contains(Point point) {
        int x = point.blockX();
        int z = point.blockZ();
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
