package net.swofty.type.garden.pest;

import net.swofty.type.garden.config.GardenConfigRegistry;

import java.util.List;
import java.util.Map;

public class GardenPestService {
    private Map<String, Object> config = Map.of();
    private List<Map<String, Object>> pests = List.of();
    private List<Map<String, Object>> drops = List.of();

    public void reload() {
        config = GardenConfigRegistry.getConfig("pests.yml");
        pests = GardenConfigRegistry.getMapList(config, "registry");
        drops = GardenConfigRegistry.getMapList(GardenConfigRegistry.getConfig("pest_drops.yml"), "drops");
    }

    public double getBaseSpawnChance() {
        return GardenConfigRegistry.getDouble(config, "base_spawn_chance", 0.002D);
    }

    public long getBaseCooldownSeconds() {
        return GardenConfigRegistry.getLong(config, "base_cooldown_seconds", 300);
    }

    public long calculateSpawnCooldownSeconds(CooldownModifiers modifiers) {
        double reduction = 0D;
        reduction += modifiers.finnegan() ? 0.20D : 0D;
        reduction += modifiers.pesthunterPieces() * 0.10D;
        reduction += modifiers.squeakyPieces() * 0.025D;
        reduction += modifiers.hasPestVest() ? 0.15D : 0D;

        double cooldown = getBaseCooldownSeconds() * Math.max(0D, 1D - reduction);
        if (modifiers.repellentMax()) {
            cooldown *= 4D;
        } else if (modifiers.repellent()) {
            cooldown *= 2D;
        }
        cooldown = Math.max(75D, Math.min(1200D, cooldown));
        return Math.round(cooldown);
    }

    public int getFortunePenaltyPercent(int pestCount, int bonusPestChance) {
        int thresholdShift = Math.max(0, bonusPestChance / 100);
        int effectivePests = pestCount - thresholdShift;
        if (effectivePests <= 3) {
            return 0;
        }
        return switch (effectivePests) {
            case 4 -> 5;
            case 5 -> 15;
            case 6 -> 30;
            case 7 -> 50;
            default -> 75;
        };
    }

    public List<Map<String, Object>> getPests() {
        return pests;
    }

    public List<Map<String, Object>> getDrops() {
        return drops;
    }

    public record CooldownModifiers(
        boolean finnegan,
        int pesthunterPieces,
        boolean hasPestVest,
        int squeakyPieces,
        boolean repellent,
        boolean repellentMax
    ) {
    }
}
