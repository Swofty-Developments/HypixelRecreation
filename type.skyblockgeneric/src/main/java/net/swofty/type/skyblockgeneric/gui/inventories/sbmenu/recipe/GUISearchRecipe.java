package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.recipe;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.Layouts;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.crafting.ShapedRecipe;
import net.swofty.type.skyblockgeneric.item.crafting.ShapelessRecipe;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUISearchRecipe extends PaginatedView<SkyBlockRecipe<?>, GUISearchRecipe.SearchState> {
    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    @Override
    protected ItemStack.Builder renderItem(SkyBlockRecipe<?> item, int index, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        SkyBlockRecipe.CraftingResult result = item.getCanCraft().apply(player);
        ItemStack.Builder itemStack = PlayerItemUpdater.playerUpdate(
                player, item.getResult().getItemStack()
        );

        if (result.allowed()) {
            return ItemStacks.appendLore(itemStack, List.of(
                    Text.of("<e> "),
                    Text.key("gui_sbmenu.recipe.category.click_to_view")));
        } else {
            return ItemStacks.item(Material.GRAY_DYE, 1,
                    Text.key("gui_sbmenu.recipe.category.locked"),
                    Arrays.stream(result.errorMessage())
                            .map(message -> Text.of("<7>{}", Text.parse(message)))
                            .toList());
        }
    }

    @Override
    protected void onItemClick(ClickContext<SearchState> click, ViewContext ctx, SkyBlockRecipe<?> item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockRecipe.CraftingResult result = item.getCanCraft().apply(player);

        if (result.allowed()) {
            ctx.push(new GUIRecipe(item.getResult().getAttributeHandler().getPotentialType()));
        } else {
            player.sendMessage(Text.key("gui_sbmenu.recipe.category.msg.not_unlocked"));
        }
    }

    @Override
    public void layout(ViewLayout<SearchState> layout, SearchState state, ViewContext ctx) {
        layout.filler(Layouts.border(0, 53), Components.asFiller(Material.BLACK_STAINED_GLASS_PANE));
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        layout.slot(50, (_, c) -> ItemStacks.item(Material.OAK_SIGN, 1,
                Text.key("gui_sbmenu.recipe.book.search"),
                Text.keyLines("gui_sbmenu.recipe.book.search.lore")), (_, c) -> {
            new HypixelSignGUI(c.player()).open(new String[]{"Enter query", ""}).thenAccept(line -> {
                if (line == null) {
                    return;
                }

                c.replace(new GUISearchRecipe(), GUISearchRecipe.createInitialState(line));
            });
        });

        // if no results
        if (state.items().isEmpty()) {
            layout.slot(22, (searchState, c) -> ItemStacks.item(Material.BARRIER, 1,
                    Text.key("gui_sbmenu.recipe.search.no_results"),
                    Text.keyLines("gui_sbmenu.recipe.search.no_results.lore", searchState.query())));
        }
    }

    @Override
    protected boolean shouldFilterFromSearch(SearchState state, SkyBlockRecipe<?> item) {
        return false;
    }

    @Override
    public ViewConfiguration<SearchState> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.key("gui_sbmenu.recipe.search.title",
                state.query(),
                state.page() + 1,
                Math.max(1, (int) Math.ceil((double) state.items().size() / DEFAULT_SLOTS.length))),
                InventoryType.CHEST_6_ROW);
    }

    public static SearchState createInitialState(String query) {
        List<SkyBlockRecipe<?>> recipes = new ArrayList<>();
        recipes.addAll(ShapedRecipe.CACHED_RECIPES);
        recipes.addAll(ShapelessRecipe.CACHED_RECIPES);

        recipes.removeIf(recipe -> recipe.getResult().getCleanName().toLowerCase().startsWith(query.toLowerCase()));

        List<ItemType> shownItems = new ArrayList<>();
        recipes.removeIf(recipe -> {
            ItemType itemType = recipe.getResult().getAttributeHandler().getPotentialType();
            if (shownItems.contains(itemType)) {
                return true;
            } else {
                shownItems.add(itemType);
                return false;
            }
        });


        return new SearchState(recipes, 0, query);
    }

    public record SearchState(List<SkyBlockRecipe<?>> items, int page, String query) implements PaginatedState<SkyBlockRecipe<?>> {
        @Override
        public PaginatedState<SkyBlockRecipe<?>> withPage(int page) {
            return new SearchState(items, page, query);
        }

        @Override
        public PaginatedState<SkyBlockRecipe<?>> withItems(List<SkyBlockRecipe<?>> items) {
            return new SearchState(this.items, page, query);
        }
    }

}
