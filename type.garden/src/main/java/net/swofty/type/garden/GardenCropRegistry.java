package net.swofty.type.garden;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class GardenCropRegistry {
    private static final List<String> VISITOR_CROP_POOL = List.of(
        "WHEAT",
        "CARROT",
        "POTATO",
        "PUMPKIN",
        "SUGAR_CANE",
        "MELON_SLICE",
        "CACTUS",
        "COCOA_BEANS",
        "MUSHROOM",
        "NETHER_WART",
        "SUNFLOWER",
        "MOONFLOWER",
        "WILD_ROSE"
    );

    private static final Map<Material, CropContext> CROP_BY_MATERIAL = Map.ofEntries(
        Map.entry(Material.WHEAT, new CropContext("WHEAT", ItemStatistic.WHEAT_FORTUNE, false)),
        Map.entry(Material.CARROT, new CropContext("CARROT", ItemStatistic.CARROT_FORTUNE, false)),
        Map.entry(Material.POTATO, new CropContext("POTATO", ItemStatistic.POTATO_FORTUNE, false)),
        Map.entry(Material.PUMPKIN, new CropContext("PUMPKIN", ItemStatistic.PUMPKIN_FORTUNE, false)),
        Map.entry(Material.CARVED_PUMPKIN, new CropContext("PUMPKIN", ItemStatistic.PUMPKIN_FORTUNE, false)),
        Map.entry(Material.MELON, new CropContext("MELON_SLICE", ItemStatistic.MELON_FORTUNE, false)),
        Map.entry(Material.SUGAR_CANE, new CropContext("SUGAR_CANE", ItemStatistic.SUGAR_CANE_FORTUNE, true)),
        Map.entry(Material.CACTUS, new CropContext("CACTUS", ItemStatistic.CACTUS_FORTUNE, true)),
        Map.entry(Material.BROWN_MUSHROOM, new CropContext("MUSHROOM", ItemStatistic.MUSHROOM_FORTUNE, false)),
        Map.entry(Material.RED_MUSHROOM, new CropContext("MUSHROOM", ItemStatistic.MUSHROOM_FORTUNE, false)),
        Map.entry(Material.NETHER_WART, new CropContext("NETHER_WART", ItemStatistic.NETHER_WART_FORTUNE, false)),
        Map.entry(Material.SUNFLOWER, new CropContext("SUNFLOWER", null, false)),
        Map.entry(Material.BLUE_ORCHID, new CropContext("MOONFLOWER", null, false)),
        Map.entry(Material.ROSE_BUSH, new CropContext("WILD_ROSE", null, false))
    );

    private GardenCropRegistry() {
    }

    public static CropContext fromMaterial(Material material) {
        return CROP_BY_MATERIAL.get(material);
    }

    public static List<String> getVisitorCropPool() {
        return VISITOR_CROP_POOL;
    }

    public record CropContext(String cropId, @Nullable ItemStatistic specificFortune, boolean doubleBreakCrop) {
    }
}
