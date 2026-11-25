package net.swofty.type.generic.gui.v2;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;

public abstract class PaginatedView<T, S extends PaginatedView.PaginatedState<T>> implements View<S> {

    protected static final int[] DEFAULT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    protected static final int[] SLIM = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    public interface PaginatedState<T> {
        List<T> items();

        int page();

        PaginatedState<T> withPage(int page);

        PaginatedState<T> withItems(List<T> items);
    }

    @Override
    public void layout(ViewLayout<S> layout, S state, ViewContext ctx) {
        layoutBackground(layout, state, ctx);
        List<T> filteredItems = getFilteredItems(state);
        int[] slots = getPaginatedSlots();
        int itemsPerPage = slots.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / itemsPerPage));
        int currentPage = Math.min(state.page(), totalPages - 1);

        // Calculate page boundaries
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredItems.size());
        List<T> pageItems = startIndex < filteredItems.size() ? filteredItems.subList(startIndex, endIndex) : List.of();

        // Render paginated items
        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];
            if (i < pageItems.size()) {
                T item = pageItems.get(i);
                int itemIndex = startIndex + i;
                layout.slot(slot, (s, c) -> renderItem(item, itemIndex, ctx.player()), (click, viewCtx) -> onItemClick(click, viewCtx, item, itemIndex));
            } else {
                layout.slot(slot, (s, c) -> ItemStack.AIR.builder());
            }
        }

        layoutNavigation(layout, state, ctx, currentPage, totalPages);
        layoutCustom(layout, state, ctx);
    }

    protected boolean shouldRenderNavBackground() {
        return true;
    }

    protected List<T> getFilteredItems(S state) {
        return state.items().stream().filter(item -> !shouldFilterFromSearch(state, item)).toList();
    }

    protected void layoutBackground(ViewLayout<S> layout, S state, ViewContext ctx) {
        layout.filler(Components.FILLER);
    }

    protected void layoutNavigation(ViewLayout<S> layout, S state, ViewContext ctx, int currentPage, int totalPages) {
        int prevSlot = getPreviousPageSlot();
        int nextSlot = getNextPageSlot();

        if (prevSlot >= 0) {
            if (currentPage > 0) {
                layout.slot(prevSlot, (s, c) -> createPrevPageItem(currentPage, totalPages), (click, viewCtx) -> viewCtx.session(Object.class).updateUnchecked(s -> {
                    @SuppressWarnings("unchecked") S typedState = (S) s;
                    return typedState.withPage(Math.max(0, typedState.page() - 1));
                }));
            } else if (shouldRenderNavBackground()) {
                layout.slot(prevSlot, Components.FILLER);
            }
        }

        if (nextSlot >= 0) {
            if (currentPage < totalPages - 1) {
                layout.slot(nextSlot, (s, c) -> createNextPageItem(currentPage, totalPages), (click, viewCtx) -> viewCtx.session(Object.class).updateUnchecked(s -> {
                    @SuppressWarnings("unchecked") S typedState = (S) s;
                    return typedState.withPage(typedState.page() + 1);
                }));
            } else if (shouldRenderNavBackground()) {
                layout.slot(nextSlot, Components.FILLER);
            }
        }
    }

    protected abstract int getNextPageSlot();

    protected abstract int getPreviousPageSlot();

    protected void layoutCustom(ViewLayout<S> layout, S state, ViewContext ctx) {
    }

    protected ItemStack.Builder createPrevPageItem(int currentPage, int totalPages) {
        return ItemStack.builder(Material.ARROW).set(DataComponents.CUSTOM_NAME, Component.text("§aPrevious Page")).set(DataComponents.LORE, List.of(Component.text("§ePage " + currentPage)));
    }

    protected ItemStack.Builder createNextPageItem(int currentPage, int totalPages) {
        return ItemStack.builder(Material.ARROW).set(DataComponents.CUSTOM_NAME, Component.text("§aNext Page")).set(DataComponents.LORE, List.of(Component.text("§ePage " + (currentPage + 2))));
    }

    protected abstract int[] getPaginatedSlots();

    protected abstract ItemStack.Builder renderItem(T item, int index, HypixelPlayer player);

    protected abstract void onItemClick(ClickContext<S> click, ViewContext ctx, T item, int index);

    protected abstract boolean shouldFilterFromSearch(S state, T item);

    public static int[] createGrid(int startSlot, int endSlot) {
        return Layouts.rectangle(startSlot, endSlot).stream().mapToInt(Integer::intValue).toArray();
    }

}
