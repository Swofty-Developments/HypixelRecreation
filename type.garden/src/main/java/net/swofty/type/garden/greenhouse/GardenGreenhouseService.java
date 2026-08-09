package net.swofty.type.garden.greenhouse;

import net.swofty.type.garden.config.GardenConfigRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GardenGreenhouseService {
    private Map<String, Object> greenhouse = Map.of();
    private Map<String, Object> mutations = Map.of();
    private final Map<String, Double> mutationVineChances = new HashMap<>();
    private final Map<Integer, Integer> greenhouseUnlockCosts = new HashMap<>();
    private List<Map<String, Object>> crops = List.of();
    private List<Map<String, Object>> harvestBounty = List.of();
    private List<Map<String, Object>> dnaMilestones = List.of();

    public void reload() {
        greenhouse = GardenConfigRegistry.getConfig("greenhouse.yml");
        mutations = GardenConfigRegistry.getConfig("mutations.yml");
        crops = GardenConfigRegistry.getMapList(greenhouse, "crops");
        harvestBounty = GardenConfigRegistry.getMapList(greenhouse, "harvest_bounty");
        dnaMilestones = GardenConfigRegistry.getMapList(greenhouse, "dna_milestones");

        mutationVineChances.clear();
        GardenConfigRegistry.getSection(greenhouse, "ethereal_vine_chances")
            .forEach((key, value) -> mutationVineChances.put(key, Double.parseDouble(String.valueOf(value))));

        greenhouseUnlockCosts.clear();
        for (Map<String, Object> unlock : GardenConfigRegistry.getMapList(greenhouse, "greenhouse_unlock_costs")) {
            greenhouseUnlockCosts.put(
                GardenConfigRegistry.getInt(unlock, "greenhouse", 1),
                GardenConfigRegistry.getInt(unlock, "ethereal_vines", 0)
            );
        }
    }

    public int getUnlockCostForGreenhouse(int greenhouseIndex) {
        return greenhouseUnlockCosts.getOrDefault(greenhouseIndex, 0);
    }

    public double getMutationVineChance(String rarity) {
        return mutationVineChances.getOrDefault(rarity, 0D);
    }

    public List<Map<String, Object>> getCrops() {
        return crops;
    }

    public List<Map<String, Object>> getHarvestBounty() {
        return harvestBounty;
    }

    public List<Map<String, Object>> getDnaMilestones() {
        return dnaMilestones;
    }

    public List<Map<String, Object>> getMutations() {
        return GardenConfigRegistry.getMapList(mutations, "mutations");
    }

    public int getUnlockGardenLevel() {
        return GardenConfigRegistry.getInt(greenhouse, "unlock_garden_level", 7);
    }

    public String getUnlockVisitor() {
        return GardenConfigRegistry.getString(greenhouse, "unlock_visitor", "CARPENTER");
    }

    public String getUnlockItem() {
        return GardenConfigRegistry.getString(greenhouse, "unlock_item", "GREENHOUSE_BLUEPRINT");
    }

    public int getMaxGreenhouses() {
        return greenhouseUnlockCosts.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
    }
}
