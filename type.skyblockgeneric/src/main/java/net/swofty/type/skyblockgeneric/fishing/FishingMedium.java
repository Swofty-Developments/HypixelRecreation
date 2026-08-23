package net.swofty.type.skyblockgeneric.fishing;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public enum FishingMedium {
    WATER,
    LAVA;

    public boolean matches(Block block) {
        if (block == null || !block.liquid()) {
            return false;
        }

        String blockName = block.name();
        return switch (this) {
            case WATER -> blockName.contains("water");
            case LAVA -> blockName.contains("lava");
        };
    }

    public static @Nullable FishingMedium fromBlock(Block block) {
        for (FishingMedium medium : values()) {
            if (medium.matches(block)) {
                return medium;
            }
        }
        return null;
    }
}
