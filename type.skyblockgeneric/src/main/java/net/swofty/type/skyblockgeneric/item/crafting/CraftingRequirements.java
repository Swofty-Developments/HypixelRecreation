package net.swofty.type.skyblockgeneric.item.crafting;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

public final class CraftingRequirements {
    private static final CraftingRequirements NONE = new CraftingRequirements(List.of());

    private final List<CraftingRequirement> requirements;

    private CraftingRequirements(List<CraftingRequirement> requirements) {
        this.requirements = List.copyOf(requirements);
    }

    public static CraftingRequirements none() {
        return NONE;
    }

    public static CraftingRequirements allOf(List<CraftingRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) return NONE;
        return new CraftingRequirements(requirements);
    }

    public static CraftingRequirements allOf(CraftingRequirement... requirements) {
        return allOf(Arrays.asList(requirements));
    }

    public List<CraftingRequirement> requirements() {
        return requirements;
    }

    public SkyBlockRecipe.CraftingResult evaluate(SkyBlockPlayer player) {
        if (requirements.isEmpty()) return new SkyBlockRecipe.CraftingResult(true, null);

        List<String> failures = requirements.stream()
                .map(requirement -> requirement.evaluate(player))
                .filter(result -> !result.allowed())
                .flatMap(result -> result.failureMessages().stream())
                .filter(message -> message != null && !message.isBlank())
                .toList();

        return failures.isEmpty()
                ? new SkyBlockRecipe.CraftingResult(true, null)
                : new SkyBlockRecipe.CraftingResult(false, failures.toArray(String[]::new));
    }
}
