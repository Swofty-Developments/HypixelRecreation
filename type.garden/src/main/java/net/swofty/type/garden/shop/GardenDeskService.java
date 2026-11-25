package net.swofty.type.garden.shop;

import net.swofty.type.garden.config.GardenConfigRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GardenDeskService {
    private List<Map<String, Object>> skyMartEntries = List.of();
    private List<Map<String, Object>> cropUpgradeTiers = List.of();
    private final Map<String, List<String>> cropGroups = new HashMap<>();

    public void reload() {
        Map<String, Object> copperShop = GardenConfigRegistry.getConfig("copper_shop.yml");
        skyMartEntries = GardenConfigRegistry.getMapList(copperShop, "skymart");

        cropGroups.clear();
        GardenConfigRegistry.getSection(copperShop, "categories").forEach((key, value) -> {
            if (value instanceof List<?> list) {
                cropGroups.put(key, list.stream().map(String::valueOf).toList());
            }
        });

        Map<String, Object> cropUpgrades = GardenConfigRegistry.getConfig("crop_upgrades.yml");
        cropUpgradeTiers = GardenConfigRegistry.getMapList(cropUpgrades, "tiers");
    }

    public List<Map<String, Object>> getSkyMartEntries() {
        return skyMartEntries;
    }

    public List<Map<String, Object>> getSkyMartEntries(String category) {
        return skyMartEntries.stream()
            .filter(entry -> GardenConfigRegistry.getString(entry, "category", "").equalsIgnoreCase(category))
            .toList();
    }

    public Map<String, Object> getSkyMartEntry(String id) {
        return skyMartEntries.stream()
            .filter(entry -> GardenConfigRegistry.getString(entry, "id", "").equalsIgnoreCase(id))
            .findFirst()
            .orElse(Map.of());
    }

    public List<Map<String, Object>> getCropUpgradeTiers() {
        return cropUpgradeTiers;
    }

    public int getCropUpgradeFortunePerTier() {
        return 5;
    }

    public List<String> getCategoryEntries(String category) {
        return cropGroups.getOrDefault(category, List.of());
    }
}
