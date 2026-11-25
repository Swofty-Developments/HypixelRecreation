package net.swofty.type.skyblockgeneric.item.crafting;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.ItemQuantifiable;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class ShapelessRecipe extends SkyBlockRecipe<ShapelessRecipe> {
    public static final List<ShapelessRecipe> CACHED_RECIPES = new ArrayList<>();

    private final List<ItemQuantifiable> ingredientList;
    @Setter
    private SkyBlockItem[] customRecipeDisplay = null;

    public ShapelessRecipe(RecipeType type, SkyBlockItem result,
                           int amount, Function<SkyBlockPlayer, CraftingResult> canCraft) {
        super(result, type, canCraft);

        setAmount(amount);
        this.ingredientList = new ArrayList<>();
    }

    public ShapelessRecipe(RecipeType type, SkyBlockItem result, Function<SkyBlockPlayer, CraftingResult> canCraft) {
        super(result, type, canCraft);

        this.ingredientList = new ArrayList<>();
    }

    public ShapelessRecipe(RecipeType type, SkyBlockItem result, int amount) {
        super(result, type, (player) -> new CraftingResult(true, null));

        setAmount(amount);
        this.ingredientList = new ArrayList<>();
    }

    public ShapelessRecipe(RecipeType type, SkyBlockItem result) {
        super(result, type, (player) -> new CraftingResult(true, null));

        this.ingredientList = new ArrayList<>();
    }

    @Override
    public ShapelessRecipe setResult(SkyBlockItem result) {
        this.result = result;
        return this;
    }

    @Override
    public void init() {
        CACHED_RECIPES.add(this);
    }

    @Override
    public SkyBlockItem[] consume(SkyBlockItem[] stacks) {
        List<ItemQuantifiable> materialsToConsume = ingredientList.stream()
            .map(ItemQuantifiable::clone)
            .collect(Collectors.toCollection(ArrayList::new));
        SkyBlockItem[] modifiedStacks = Arrays.copyOf(stacks, stacks.length);

        for (int i = 0; i < modifiedStacks.length && !materialsToConsume.isEmpty(); i++) {
            if (modifiedStacks[i] == null) {
                continue;
            }

            ItemQuantifiable currentStackMaterial = ItemQuantifiable.of(modifiedStacks[i].getItemStack());
            ItemQuantifiable toConsume = materialsToConsume.stream()
                    .filter(material -> material.matchesType(currentStackMaterial.getItem())
                            || ExchangeableType.isExchangeable(
                            material.getItem().getAttributeHandler().getPotentialType(),
                            currentStackMaterial.getItem().getAttributeHandler().getPotentialType()
                    ))
                    .findFirst()
                    .orElse(null);

            if (toConsume != null) {
                int stackAmount = currentStackMaterial.getAmount();
                int consumeAmount = toConsume.getAmount();

                if (stackAmount >= consumeAmount) {
                    currentStackMaterial.setAmount(stackAmount - consumeAmount);
                    materialsToConsume.remove(toConsume);
                    modifiedStacks[i] = (currentStackMaterial.getAmount() > 0) ?
                            currentStackMaterial.toSkyBlockItem() :
                            null;
                } else {
                    toConsume.setAmount(consumeAmount - stackAmount);
                    modifiedStacks[i] = null;
                }
            }
        }

        if (!materialsToConsume.isEmpty()) {
            throw new IllegalStateException("Not enough materials to consume!");
        }

        return modifiedStacks;
    }

    @Override
    public SkyBlockItem[] getRecipeDisplay() {
        if (customRecipeDisplay != null)
            return customRecipeDisplay;
        SkyBlockItem[] display = new SkyBlockItem[9];
        int i = 0;

        for (ItemQuantifiable material : ingredientList) {
            display[i] = material.getItem().clone();
            display[i].setAmount(material.getAmount());
            i++;
        }

        return display;
    }

    @Override
    public SkyBlockRecipe<?> clone() {
        ShapelessRecipe recipe = new ShapelessRecipe(recipeType, result.clone(), amount, canCraft);
        recipe.ingredientList.addAll(ingredientList.stream().map(ItemQuantifiable::clone).toList());
        if (customRecipeDisplay != null) {
            recipe.customRecipeDisplay = Arrays.stream(customRecipeDisplay)
                .map(item -> item != null ? item.clone() : null)
                .toArray(SkyBlockItem[]::new);
        }
        recipe.setCraftingRequirements(getCraftingRequirements());
        return recipe;
    }

    public ShapelessRecipe add(ItemType material, int amount) {
        return add(new ItemQuantifiable(material, amount));
    }

    public ShapelessRecipe add(ItemQuantifiable material) {
        ingredientList.add(material.clone());
        return this;
    }

    public static ShapelessRecipe parseShapelessRecipe(ItemStack[] stacks) {
        // Extract valid, non-AIR items from the crafting grid
        List<ItemQuantifiable> nonAirInput = Arrays.stream(stacks)
                .map(ItemQuantifiable::of)
                .filter(iq -> iq.getItem().getMaterial() != Material.AIR)
                .map(ItemQuantifiable::clone)
                .toList();

        return CACHED_RECIPES.stream()
                .filter(recipe -> recipe.getIngredientList().size() == nonAirInput.size())
                .filter(recipe -> {
                    List<ItemType> recipeTypes = recipe.getIngredientList().stream()
                            .map(iq -> iq.getItem().getAttributeHandler().getPotentialType())
                            .toList();

                    return nonAirInput.stream().allMatch(input -> {
                        ItemType inputType = input.getItem().getAttributeHandler().getPotentialType();
                        return recipeTypes.stream().anyMatch(recipeType ->
                                inputType == recipeType || ExchangeableType.isExchangeable(inputType, recipeType));
                    });
                })
                .filter(recipe -> {
                    List<ItemQuantifiable> materialsNeeded = recipe.getIngredientList().stream()
                            .map(ItemQuantifiable::clone)
                            .collect(Collectors.toList());

                    for (ItemQuantifiable input : nonAirInput) {
                        ItemQuantifiable match = materialsNeeded.stream()
                                .filter(needed -> needed.matchesType(input.getItem())
                                        || ExchangeableType.isExchangeable(
                                        needed.getItem().getAttributeHandler().getPotentialType(),
                                        input.getItem().getAttributeHandler().getPotentialType()))
                                .findFirst()
                                .orElse(null);

                        if (match != null) {
                            int remaining = match.getAmount() - input.getAmount();
                            if (remaining > 0) {
                                match.setAmount(remaining);
                            } else {
                                materialsNeeded.remove(match);
                            }
                        }
                    }

                    return materialsNeeded.isEmpty();
                })
                .findFirst()
                .orElse(null);
    }


    @Override
    public String toString() {
        return "ShapelessRecipe{" +
                "recipeType=" + recipeType +
                ", result=" + result +
                ", ingredientList=" + ingredientList +
                ", canCraft=" + canCraft +
            ", amount=" + amount +
            '}';
    }
}
