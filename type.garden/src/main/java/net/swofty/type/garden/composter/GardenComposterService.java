package net.swofty.type.garden.composter;

import net.swofty.type.garden.config.GardenConfigRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GardenComposterService {
    private Map<String, Object> config = Map.of();
    private final Map<String, Map<String, Object>> branches = new HashMap<>();
    private final Map<String, Double> organicMatterValues = new HashMap<>();
    private final Map<String, Double> fuelValues = new HashMap<>();
    private List<Map<String, Object>> upgradeTiers = List.of();

    public void reload() {
        config = GardenConfigRegistry.getConfig("composter.yml");
        branches.clear();
        GardenConfigRegistry.getSection(config, "branches").forEach((key, value) -> {
            if (value instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) map;
                branches.put(key, typed);
            }
        });
        organicMatterValues.clear();
        GardenConfigRegistry.getSection(config, "organic_matter_values")
            .forEach((key, value) -> organicMatterValues.put(key, Double.parseDouble(String.valueOf(value))));

        fuelValues.clear();
        GardenConfigRegistry.getSection(config, "fuel_values")
            .forEach((key, value) -> fuelValues.put(key, Double.parseDouble(String.valueOf(value))));

        upgradeTiers = GardenConfigRegistry.getMapList(config, "upgrade_tiers");
    }

    public long getBaseProductionSeconds() {
        return GardenConfigRegistry.getLong(config, "base_production_seconds", 600);
    }

    public int getBaseOrganicMatterCost() {
        return GardenConfigRegistry.getInt(config, "base_organic_matter_cost", 4000);
    }

    public int getBaseFuelCost() {
        return GardenConfigRegistry.getInt(config, "base_fuel_cost", 2000);
    }

    public double getOrganicMatterValue(String itemId) {
        return organicMatterValues.getOrDefault(itemId, 0D);
    }

    public double getFuelValue(String itemId) {
        return fuelValues.getOrDefault(itemId, 0D);
    }

    public Map<String, Double> getOrganicMatterValues() {
        return Map.copyOf(organicMatterValues);
    }

    public Map<String, Double> getFuelValues() {
        return Map.copyOf(fuelValues);
    }

    public double calculateOrganicMatterCost(int costReductionTier) {
        return getBaseOrganicMatterCost() * Math.max(0D, 1D - (costReductionTier * 0.01D));
    }

    public double calculateFuelCost(int costReductionTier) {
        return getBaseFuelCost() * Math.max(0D, 1D - (costReductionTier * 0.01D));
    }

    public double calculateSpeedMultiplier(int speedTier) {
        return 1D + (speedTier * 0.20D);
    }

    public double calculateMultiDropChance(int multiDropTier) {
        return multiDropTier * 0.03D;
    }

    public List<Map<String, Object>> getUpgradeTiers() {
        return upgradeTiers;
    }

    public int getBranchUnlockGardenLevel(String branch) {
        return GardenConfigRegistry.getInt(branches.getOrDefault(branch, Map.of()), "unlock_garden_level", 0);
    }

    public int getFuelCapacity(int tier) {
        Map<String, Object> branch = branches.getOrDefault("fuel_cap", Map.of());
        int base = GardenConfigRegistry.getInt(branch, "base_capacity", 100000);
        int perTier = GardenConfigRegistry.getInt(branch, "per_tier_capacity", 30000);
        int max = GardenConfigRegistry.getInt(branch, "max_capacity", base);
        return Math.min(max, base + (Math.max(0, tier) * perTier));
    }

    public int getOrganicMatterCapacity(int tier) {
        Map<String, Object> branch = branches.getOrDefault("organic_matter_cap", Map.of());
        int base = GardenConfigRegistry.getInt(branch, "base_capacity", 40000);
        int perTier = GardenConfigRegistry.getInt(branch, "per_tier_capacity", 30000);
        int max = GardenConfigRegistry.getInt(branch, "max_capacity", base);
        return Math.min(max, base + (Math.max(0, tier) * perTier));
    }
}
