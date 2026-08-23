package net.swofty.type.generic.gui.v2;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Components {

    // use Components#fill(ViewLayout) instead which uses this
    static final ItemStack.Builder FILLER = ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE);
    public static final ItemStack.Builder CLOSE_BUTTON = ItemStacks.named(Material.BARRIER, "<c>Close");

    public static final ItemStack.Builder BACK_BUTTON = ItemStacks.named(Material.ARROW, "<a>Go Back");

    public static ItemStack.Builder asFiller(Material material) {
        return FILLER.material(material);
    }

    public static <S> void fill(ViewLayout<S> layout) {
        layout.filler(FILLER);
    }

    public static <S> void close(ViewLayout<S> layout, int slot) {
        layout.slot(slot, (s, c) -> CLOSE_BUTTON, (click, ctx) -> ctx.player().closeInventory());
    }

    public static <S> boolean back(ViewLayout<S> layout, int slot, ViewContext context) {
        ViewNavigator navigator = ViewNavigator.get(context.player());
        if (!navigator.hasStack()) {
            return false;
        }
        Optional<ViewNavigator.NavigationEntry<Object>> prev = context.navigator().peekPrevious();
        Text prevTitle = prev
                .map(entry -> entry.view().configuration().getTitleFunction().apply(entry.state(), context))
                .orElse(Text.of("<7>previous page"));
        layout.slot(slot, (s, c) -> ItemStacks.lore(BACK_BUTTON, "<7>To {}", prevTitle), (_, ctx) -> {
            ViewNavigator.get(ctx.player()).pop();
        });
        return true;
    }

    public static <S> boolean back(ViewLayout<S> layout, int slot, ViewContext context, ItemStack.Builder customButton) {
        ViewNavigator navigator = ViewNavigator.get(context.player());
        if (!navigator.hasStack()) {
            return false;
        }
        layout.slot(slot, (s, c) -> customButton, (click, ctx) -> {
            ViewNavigator.get(ctx.player()).pop();
        });
        return true;
    }

    public static <S> void backOrClose(ViewLayout<S> layout, int slot, ViewContext context) {
        if (!back(layout, slot, context)) {
            close(layout, slot);
        }
    }

    public static <S> void back(ViewLayout<S> layout, int slot, View<?> target, Object targetState) {
        layout.slot(slot, (s, c) -> BACK_BUTTON, (click, ctx) -> open(ctx, target, targetState));
    }

    @SuppressWarnings("unchecked")
    public static <T> void open(ViewContext ctx, View<T> view, Object state) {
        ViewNavigator.get(ctx.player()).push(view, (T) state);
    }

    public static <S> void editableSlot(ViewLayout<S> layout, int slot, Function<S, ItemStack> itemGetter, SlotChangeHandler<S> onChange) {
        layout.editable(slot, (s, c) -> itemGetter.apply(s).builder(), onChange);
    }

    public static <S> void editableSlotFromMap(ViewLayout<S> layout, int slot, Function<S, Map<Integer, ItemStack>> mapGetter, SlotChangeHandler<S> onChange) {
        layout.editable(slot, (s, c) -> mapGetter.apply(s).getOrDefault(slot, ItemStack.AIR).builder(), onChange);
    }

    public static <S> void editableGrid(ViewLayout<S> layout, List<Integer> slots, Function<S, List<ItemStack>> itemsGetter, SlotChangeHandler<S> onChange) {
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            int index = i;
            layout.editable(slot, (s, c) -> {
                List<ItemStack> items = itemsGetter.apply(s);
                return index < items.size() ? items.get(index).builder() : ItemStack.AIR.builder();
            }, onChange);
        }
    }

    public static <S> void editableGridFromMap(ViewLayout<S> layout, Collection<Integer> slots, Function<S, Map<Integer, ItemStack>> mapGetter, SlotChangeHandler<S> onChange) {
        for (int slot : slots) {
            layout.editable(slot, (s, c) -> mapGetter.apply(s).getOrDefault(slot, ItemStack.AIR).builder(), onChange);
        }
    }

    public static <S> void containerGrid(ViewLayout<S> layout, int startSlot, int endSlot, SlotChangeHandler<S> onChange) {
        layout.editableGrid(startSlot, endSlot, onChange);
    }

    public static <S> void sharedContainerGrid(ViewLayout<S> layout, int startSlot, int endSlot) {
        List<Integer> slots = Layouts.rectangle(startSlot, endSlot);
        layout.editableSlots(slots);
    }

    public static <S> void prevPage(ViewLayout<S> layout, int slot, Function<S, Integer> currentPageGetter, BiFunction<S, Integer, S> pageUpdater) {
        layout.slot(slot, (s, c) -> {
            int page = currentPageGetter.apply(s);
            if (page > 0) {
                return ItemStacks.named(Material.ARROW, "<e>Previous Page");
            }
            return FILLER;
        }, (click, ctx) -> {
            int page = currentPageGetter.apply(click.state());
            if (page > 0) {
                ctx.session(Object.class).updateUnchecked(state -> pageUpdater.apply((S) state, page - 1));
            }
        });
    }

    public static <S> void nextPage(ViewLayout<S> layout, int slot, Function<S, Integer> currentPageGetter, Function<S, Integer> totalPagesGetter, BiFunction<S, Integer, S> pageUpdater) {
        layout.slot(slot, (s, c) -> {
            int page = currentPageGetter.apply(s);
            int total = totalPagesGetter.apply(s);
            if (page < total - 1) {
                return ItemStacks.named(Material.ARROW, "<e>Next Page");
            }
            return FILLER;
        }, (click, ctx) -> {
            int page = currentPageGetter.apply(click.state());
            int total = totalPagesGetter.apply(click.state());
            if (page < total - 1) {
                ctx.session(Object.class).updateUnchecked(state -> pageUpdater.apply((S) state, page + 1));
            }
        });
    }
}
