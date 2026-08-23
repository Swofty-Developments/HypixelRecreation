package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.recipe;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.crafting.ShapedRecipe;
import net.swofty.type.skyblockgeneric.item.crafting.ShapelessRecipe;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIRecipeBook extends StatelessView {
    private static final String LOADING_BAR = "─────────────────";

    private static final int[] CATEGORY_SLOTS = {
            20, 21, 22, 23, 24,
            29, 30, 31, 33
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.recipe.book.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        ArrayList<SkyBlockRecipe> allRecipes = new ArrayList<>();
        allRecipes.addAll(ShapedRecipe.CACHED_RECIPES);
        allRecipes.addAll(ShapelessRecipe.CACHED_RECIPES);

        layout.slot(51, (_, c) -> ItemStacks.item(Material.OAK_SIGN, 1,
                Text.key("gui_sbmenu.recipe.book.search"),
                Text.keyLines("gui_sbmenu.recipe.book.search.lore")), (_, c) -> {
            new HypixelSignGUI(c.player()).open(new String[]{"Enter query", ""}).thenAccept(line -> {
                if (line == null) {
                    return;
                }

                c.push(new GUISearchRecipe(), GUISearchRecipe.createInitialState(line));
            });
        });

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<String> missionLore = new ArrayList<>();
            SkyBlockRecipe.getMissionDisplay(missionLore, player.getUuid());
            Text missionDisplay = Text.join(Text.literal("\n"),
                    missionLore.stream().map(Text::parse).toList());
            return ItemStacks.item(Material.BOOK, 1,
                    Text.key("gui_sbmenu.recipe.book.info"),
                    Text.keyLines("gui_sbmenu.recipe.book.info.lore", missionDisplay));
        });

        for (int i = 0; i < CATEGORY_SLOTS.length && i < SkyBlockRecipe.RecipeType.values().length; i++) {
            SkyBlockRecipe.RecipeType type = SkyBlockRecipe.RecipeType.values()[i];
            int slot = CATEGORY_SLOTS[i];

            ArrayList<SkyBlockRecipe> typeRecipes = new ArrayList<>();
            allRecipes.forEach(recipe -> {
                if (recipe.getRecipeType() == type) {
                    typeRecipes.add(recipe);
                }
            });

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                ArrayList<SkyBlockRecipe> allowedRecipes = new ArrayList<>();

                typeRecipes.forEach(recipe -> {
                    SkyBlockRecipe.CraftingResult result = (SkyBlockRecipe.CraftingResult) recipe.getCanCraft().apply(player);
                    if (result.allowed()) {
                        allowedRecipes.add(recipe);
                    }
                });

                String unlockedPercentage = String.format("%.2f", (allowedRecipes.size() / (double) typeRecipes.size()) * 100);
                String categoryName = StringUtility.toNormalCase(type.name());

                return ItemStacks.item(type.getMaterial(), 1,
                        Text.key("gui_sbmenu.recipe.book.category", categoryName),
                        Text.keyLines("gui_sbmenu.recipe.book.category.lore", categoryName, unlockedPercentage,
                                progressBar(allowedRecipes.size(), typeRecipes.size())));
            }, (_, c) -> c.push(new GUIRecipeCategory(type), GUIRecipeCategory.createInitialState((SkyBlockPlayer) c.player(), type)));
        }

        // Slayer recipes
        layout.slot(32, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            SkyBlockRecipe.RecipeType type = SkyBlockRecipe.RecipeType.SLAYER;

            ArrayList<SkyBlockRecipe.RecipeType> recipeTypes = new ArrayList<>();
            recipeTypes.add(SkyBlockRecipe.RecipeType.REVENANT_HORROR);
            recipeTypes.add(SkyBlockRecipe.RecipeType.TARANTULA_BROODFATHER);
            recipeTypes.add(SkyBlockRecipe.RecipeType.SVEN_PACKMASTER);
            recipeTypes.add(SkyBlockRecipe.RecipeType.VOIDGLOOM_SERAPH);
            recipeTypes.add(SkyBlockRecipe.RecipeType.INFERNO_DEMONLORD);

            ArrayList<SkyBlockRecipe> typeRecipes = new ArrayList<>();
            ArrayList<SkyBlockRecipe> allowedRecipes = new ArrayList<>();
            allRecipes.forEach(recipe -> {
                if (recipeTypes.contains(recipe.getRecipeType())) {
                    typeRecipes.add(recipe);
                }
            });

            typeRecipes.forEach(recipe -> {
                SkyBlockRecipe.CraftingResult result = (SkyBlockRecipe.CraftingResult) recipe.getCanCraft().apply(player);
                if (result.allowed()) {
                    allowedRecipes.add(recipe);
                }
            });

            String unlockedPercentage = String.format("%.2f", (allowedRecipes.size() / (double) typeRecipes.size()) * 100);
            String categoryName = StringUtility.toNormalCase(type.name());

            return ItemStacks.item(type.getMaterial(), 1,
                    Text.key("gui_sbmenu.recipe.book.category", categoryName),
                    Text.keyLines("gui_sbmenu.recipe.book.category.lore", categoryName, unlockedPercentage,
                            progressBar(allowedRecipes.size(), typeRecipes.size())));
        }, (click, c) -> c.push(new GUIRecipeSlayers()));
    }

    static Text progressBar(int allowed, int total) {
        int maxBarLength = LOADING_BAR.length();
        int completedLength = Math.min((int) ((allowed / (double) total) * maxBarLength), maxBarLength);
        return Text.of("<2><m>{}<7>{}<r> <e>{}<6>/<e>{}",
                LOADING_BAR.substring(0, completedLength),
                LOADING_BAR.substring(completedLength),
                allowed, total);
    }
}
