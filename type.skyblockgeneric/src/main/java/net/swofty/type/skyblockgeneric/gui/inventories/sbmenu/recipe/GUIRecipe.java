package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.recipe;

import lombok.SneakyThrows;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.CraftableComponent;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIRecipe extends StatelessView {
    private static final int[] CRAFT_SLOTS = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};

    private final SkyBlockItem item;
    private final int recipeIndex;

    public GUIRecipe(ItemType type) {
        this(new SkyBlockItem(type), 0);
    }

    public GUIRecipe(SkyBlockItem item) {
        this(item, 0);
    }

    public GUIRecipe(SkyBlockItem item, int recipeIndex) {
        this.item = item;
        this.recipeIndex = recipeIndex;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        ItemType type = item.getAttributeHandler().getPotentialType();
        String name = type != null ? type.getDisplayName() : "Unknown";
        return new ViewConfiguration<>(Text.key("gui_sbmenu.recipe.view.title", name), InventoryType.CHEST_6_ROW);
    }

    @SneakyThrows
    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(23, (s, c) -> ItemStacks.item(Material.CRAFTING_TABLE, 1,
                Text.key("gui_sbmenu.recipe.view.crafting_table"),
                Text.keyLines("gui_sbmenu.recipe.view.crafting_table.lore")));

        ItemType itemTypeLinker = item.getAttributeHandler().getPotentialType();
        if (item.toConfigurableItem() == null) {
            return;
        }

        List<SkyBlockRecipe<?>> recipes = SkyBlockRecipe.getFromType(itemTypeLinker);
        if (recipes.isEmpty()) {
            return;
        }

        int actualRecipeIndex = recipeIndex >= recipes.size() ? 0 : recipeIndex;
        SkyBlockRecipe<?> recipe = recipes.get(actualRecipeIndex);

        // Next recipe button
        if (recipes.size() > actualRecipeIndex + 1) {
            layout.slot(32, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.recipe.view.next"),
                            Text.keyLines("gui_sbmenu.recipe.view.next.lore")),
                    (click, c) -> {
                        c.push(new GUIRecipe(item, actualRecipeIndex + 1));
                    });
        }

        // Previous recipe button
        if (actualRecipeIndex > 0) {
            layout.slot(14, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.recipe.view.previous"),
                            Text.keyLines("gui_sbmenu.recipe.view.previous.lore")),
                    (click, c) -> {
                        c.push(new GUIRecipe(item, actualRecipeIndex - 1));
                    });
        }

        // Result
        layout.slot(25, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            return PlayerItemUpdater.playerUpdate(player, recipe.getResult().getItemStack())
                    .amount(recipe.getResult().getAmount());
        });

        // Ingredients
        SkyBlockItem[] ingredients = recipe.getRecipeDisplay();
        for (int i = 0; i < CRAFT_SLOTS.length && i < ingredients.length; i++) {
            SkyBlockItem ingredient = ingredients[i];
            int craftSlot = CRAFT_SLOTS[i];

            if (ingredient != null) {
                layout.slot(craftSlot, (s, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(player, ingredient.getItemStack());

                    if (ingredient.hasComponent(CraftableComponent.class)) {
                        ItemStacks.appendLore(builder, List.of(
                                Text.literal(" "),
                                Text.key("gui_sbmenu.recipe.view.click_to_view")));
                    }

                    if (ingredient.getAttributeHandler().shouldBeEnchanted())
                        ItemStacks.enchanted(builder);

                    return builder;
                }, (click, c) -> {
                    if (ingredient.hasComponent(CraftableComponent.class)) {
                        c.push(new GUIRecipe(ingredient, 0));
                    }
                });
            } else {
                layout.slot(craftSlot, ItemStack.AIR.builder());
            }
        }
    }
}
