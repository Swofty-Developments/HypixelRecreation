package net.swofty.type.skyblockgeneric.item.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.utility.RecipeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class CraftableComponent extends SkyBlockItemComponent {
    private final List<SkyBlockRecipe<?>> recipes;
    private boolean defaultCraftable;

    public CraftableComponent(List<Map<String, Object>> recipeConfigs) {
        List<SkyBlockRecipe<?>> parsedRecipes = new ArrayList<>();
        for (Map<String, Object> config : recipeConfigs) {
            try {
                parsedRecipes.add(RecipeParser.parseRecipe(config));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse craftable recipe " + config.get("result"), e);
            }
        }
        this.recipes = parsedRecipes;
        this.defaultCraftable = true;
    }

    public static CraftableComponent fromConfig(List<Map<String, Object>> recipeConfigs, boolean defaultCraftable) {
        CraftableComponent component = new CraftableComponent(recipeConfigs);
        component.setDefaultCraftable(defaultCraftable);
        return component;
    }

    public CraftableComponent(boolean defaultCraftable, SkyBlockRecipe<?>... recipes) {
        this.recipes = List.of(recipes);
        this.defaultCraftable = defaultCraftable;
    }

    public CraftableComponent(SkyBlockRecipe<?>[] array, boolean defaultCraftable) {
        this.recipes = List.of(array);
        this.defaultCraftable = defaultCraftable;
    }

    public CraftableComponent(SkyBlockRecipe<?> recipe, boolean defaultCraftable) {
        this.recipes = List.of(recipe);
        this.defaultCraftable = defaultCraftable;
    }
}
