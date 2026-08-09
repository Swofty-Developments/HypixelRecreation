package net.swofty.type.skyblockgeneric.garden;

import net.minestom.server.coordinate.Point;

import java.util.Optional;

public interface SkyBlockEditableWorldHandle {
    WorldBuildLimits getBuildLimits();

    default boolean isWithinBounds(Point point) {
        return getBuildLimits().contains(point);
    }

    default boolean canEdit(Point point) {
        return isWithinBounds(point);
    }

    default Optional<String> getDeniedBuildMessage(Point point) {
        if (!isWithinBounds(point)) {
            return Optional.of("§cYou can't build any further in this direction!");
        }
        return Optional.of("§cYou can't edit that part yet!");
    }
}
