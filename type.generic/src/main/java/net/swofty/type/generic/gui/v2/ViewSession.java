package net.swofty.type.generic.gui.v2;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.RawClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.text.HypixelTextRenderer;
import net.swofty.type.generic.text.RenderContext;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ViewSession<S> {

    @Getter
    @Accessors(fluent = true)
    private final View<S> view;

    @Getter
    @Accessors(fluent = true)
    private final HypixelPlayer player;

    @Getter
    @Accessors(fluent = true)
    private final Inventory inventory;

    @Getter
    @Accessors(fluent = true)
    private final SharedContext<S> sharedContext;

    @Getter
    @Accessors(fluent = true)
    private final ViewContext context;

    @Getter
    @Accessors(fluent = true)
    private S state;

    private ViewLayout<S> cachedLayout;
    private boolean layoutDirty = true;
    private Consumer<CloseReason> onCloseHandler;

    @Getter
    private boolean closed;

    @Getter
    @Setter
    private boolean suppressCloseEvent;

    private final Int2ObjectOpenHashMap<ItemStack> trackedSlotItems = new Int2ObjectOpenHashMap<>();
    private final Set<Integer> recentlyModifiedSlots = new HashSet<>();
    private final Set<Integer> initializedEditableSlots = new HashSet<>();
    private final Map<Integer, Task> autoUpdateTasks = new HashMap<>();
    private final Map<UUID, Set<Integer>> componentSlots = new HashMap<>();

    private final Int2ObjectOpenHashMap<ItemStack> pendingOldItems = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<ItemStack> pendingNewItems = new Int2ObjectOpenHashMap<>();
    private boolean flushScheduled;

    private ViewSession(View<S> view, HypixelPlayer player, S initialState, SharedContext<S> sharedContext) {
        this.view = view;
        this.player = player;
        this.state = sharedContext != null ? sharedContext.state() : initialState;
        this.sharedContext = sharedContext;
        this.inventory = new Inventory(view.configuration().getInventoryType(), "");
        this.context = new ViewContext(player, inventory, this);

        if (sharedContext != null) {
            sharedContext.registerSession(this);
        }
    }

    static <S> ViewSession<S> open(View<S> view, HypixelPlayer player, S initialState) {
        ViewSession<S> session = new ViewSession<>(view, player, initialState, null);
        session.render();
        player.openInventory(session.inventory);
        return session;
    }

    static <S> ViewSession<S> openShared(View<S> view, HypixelPlayer player, SharedContext<S> sharedContext) {
        ViewSession<S> session = new ViewSession<>(view, player, sharedContext.state(), sharedContext);
        session.render();
        player.openInventory(session.inventory);
        return session;
    }

    public void onOpenEvent(@NonNull InventoryOpenEvent event) {
        if (event.getInventory() != inventory || closed) return;
        view.onOpen(state, context);
    }

    public void onPreClickEvent(@NonNull InventoryPreClickEvent event) {
        boolean inViewInventory = event.getInventory() == inventory;
        boolean inPlayerInventory = event.getInventory() instanceof PlayerInventory
                && player.getOpenInventory() == inventory;

        if (!closed && (inViewInventory || inPlayerInventory)
                && view.onRawClick(new RawClickContext<>(event, inViewInventory, player, state), context)) {
            return;
        }

        if (event.getInventory() instanceof PlayerInventory) {
            if (player.getOpenInventory() == inventory) {
                var click = new ClickContext<>(event.getSlot(), event.getClick(), player, state);
                if (!view.onBottomClick(click, context)) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        if (event.getInventory() != inventory || closed) return;

        int slot = event.getSlot();
        SlotBehavior behavior = cachedLayout != null ? cachedLayout.getBehavior(slot) : SlotBehavior.UI;

        if (behavior == SlotBehavior.EDITABLE) {
            if (event.getClick() instanceof Click.LeftShift) {
                ItemStack oldItem = inventory.getItemStack(slot);

                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    ItemStack newItem = inventory.getItemStack(slot);

                    if (!oldItem.equals(newItem)) {
                        queueSlotChange(slot, oldItem, newItem);
                    }
                });
            }
            trackedSlotItems.put(slot, inventory.getItemStack(slot));
            return;
        }

        if (event.getClick() instanceof Click.LeftDrag(List<Integer> slots)) {
            for (int dragSlot : slots) {
                if (cachedLayout == null || !cachedLayout.isEditable(dragSlot)) {
                    event.setCancelled(true);
                    return;
                }
                trackedSlotItems.put(dragSlot, inventory.getItemStack(dragSlot));
            }
            return;
        }

        event.setCancelled(true);
        ViewComponent<S> component = cachedLayout != null ? cachedLayout.components().get(slot) : null;
        var click = new ClickContext<>(slot, event.getClick(), player, state);

        if (component == null) {
            view.onClick(click, context);
        } else {
            component.onClick().accept(click, context);
        }
    }

    public void onPostClickEvent(@NonNull InventoryClickEvent event) {
        if (event.getInventory() != inventory || closed) return;
        if (cachedLayout == null) return;

        for (Int2ObjectMap.Entry<ItemStack> entry : trackedSlotItems.int2ObjectEntrySet()) {
            int trackedSlot = entry.getIntKey();
            if (!cachedLayout.isEditable(trackedSlot)) continue;

            ItemStack oldItem = entry.getValue();
            ItemStack newItem = inventory.getItemStack(trackedSlot);
            if (!oldItem.equals(newItem)) {
                queueSlotChange(trackedSlot, oldItem, newItem);
            }
        }
    }

    private void queueSlotChange(int slot, ItemStack oldItem, ItemStack newItem) {
        if (!pendingOldItems.containsKey(slot)) {
            pendingOldItems.put(slot, oldItem);
        }
        pendingNewItems.put(slot, newItem);
        trackedSlotItems.put(slot, newItem);

        if (!flushScheduled) {
            flushScheduled = true;
            MinecraftServer.getSchedulerManager().submitTask(() -> {
                flushPendingSlotChanges();
                return TaskSchedule.stop();
            });
        }
    }

    private void flushPendingSlotChanges() {
        if (closed) return;
        flushScheduled = false;
        if (pendingNewItems.isEmpty()) return;

        recentlyModifiedSlots.addAll(pendingNewItems.keySet());

        boolean anyChanged = false;
        for (Int2ObjectMap.Entry<ItemStack> entry : pendingNewItems.int2ObjectEntrySet()) {
            int slot = entry.getIntKey();
            ItemStack newItem = entry.getValue();
            ItemStack oldItem = pendingOldItems.getOrDefault(slot, ItemStack.AIR);

            ViewComponent<S> component = cachedLayout != null ? cachedLayout.components().get(slot) : null;
            if (component != null && component.changeHandler() != null) {
                component.changeHandler().onChange(slot, oldItem, newItem, state);
            }
            anyChanged = true;

            if (sharedContext != null) {
                sharedContext.setSlotItem(slot, newItem);
            }
        }

        pendingOldItems.clear();
        pendingNewItems.clear();
        recentlyModifiedSlots.clear();

        if (!anyChanged) return;

        if (sharedContext != null) {
            sharedContext.broadcastRender();
        } else {
            render();
        }
    }

    public void refresh() {
        layoutDirty = true;
        render();
    }

    public void render() {
        if (closed) return;

        ViewConfiguration<S> config = view.configuration();

        if (cachedLayout == null || layoutDirty) {
            cachedLayout = new ViewLayout<>(config.getInventoryType());
            view.layout(cachedLayout, state, context);
            layoutDirty = false;
            componentSlots.clear();

            applyTitle(config);

            cachedLayout.components().forEach((slot, component) -> {
                if (component.behavior() == SlotBehavior.EDITABLE) {
                    renderEditableSlot(slot, component);
                    return;
                }

                renderSlot(slot, component);
                if (component.updateInterval() != null) {
                    scheduleAutoUpdate(slot, component);
                }
            });

            view.onRefresh(state, context);
            return;
        }

        componentSlots.clear();

        applyTitle(config);

        cachedLayout.components().forEach((slot, component) -> {
            if (component.behavior() == SlotBehavior.EDITABLE) {
                renderEditableSlot(slot, component);
                return;
            }
            renderSlot(slot, component);
        });

        view.onRefresh(state, context);
    }

    private void renderEditableSlot(int slot, ViewComponent<S> component) {
        if (sharedContext != null) {
            ItemStack contextItem = sharedContext.getSlotItem(slot);
            if (!initializedEditableSlots.contains(slot)) {
                if (contextItem.isAir()) {
                    ItemStack initialItem = materialize(component);
                    if (!initialItem.isAir()) {
                        inventory.setItemStack(slot, initialItem);
                        sharedContext.setSlotItem(slot, initialItem);
                        contextItem = initialItem;
                    }
                }
                initializedEditableSlots.add(slot);
            }
            if (!inventory.getItemStack(slot).equals(contextItem)) {
                inventory.setItemStack(slot, contextItem);
            }
            trackedSlotItems.put(slot, contextItem);
        } else {
            if (!initializedEditableSlots.contains(slot)) {
                ItemStack currentItem = inventory.getItemStack(slot);
                if (currentItem.isAir()) {
                    ItemStack initialItem = materialize(component);
                    if (!initialItem.isAir()) {
                        inventory.setItemStack(slot, initialItem);
                        currentItem = initialItem;
                    }
                }
                initializedEditableSlots.add(slot);
                trackedSlotItems.put(slot, currentItem);
            } else if (!recentlyModifiedSlots.contains(slot)) {
                ItemStack currentItem = inventory.getItemStack(slot);
                trackedSlotItems.put(slot, currentItem);
            }
        }
    }

    private void applyTitle(ViewConfiguration<S> config) {
        Component title = renderedTitle(config);
        if (!title.equals(inventory.getTitle())) {
            inventory.setTitle(title);
        }
    }

    private Component renderedTitle(ViewConfiguration<S> config) {
        BiFunction<S, ViewContext, Text> titleFunction = config.getTitleFunction();
        return HypixelTextRenderer.render(titleFunction.apply(state, context).asComponent(),
                RenderContext.of(context.player()));
    }

    private ItemStack materialize(ViewComponent<S> component) {
        return HypixelTextRenderer.renderStack(component.render().apply(state, context).build(),
                RenderContext.of(context.player()));
    }

    private void renderSlot(int slot, ViewComponent<S> component) {
        ItemStack item = materialize(component);
        if (!inventory.getItemStack(slot).equals(item)) {
            inventory.setItemStack(slot, item);
        }

        if (component.id() != null) {
            componentSlots.computeIfAbsent(component.id(), _ -> new HashSet<>()).add(slot);
        }
    }

    private void scheduleAutoUpdate(int slot, ViewComponent<S> component) {
        if (autoUpdateTasks.containsKey(slot)) return;

        Task task = MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (closed) return TaskSchedule.stop();
            if (component.update() != null) {
                component.update().accept(context);
            }
            renderSlot(slot, component);
            return TaskSchedule.duration(component.updateInterval());
        });
        autoUpdateTasks.put(slot, task);
    }

    public void setState(S newState) {
        this.state = newState;
        layoutDirty = true;

        if (sharedContext != null) {
            sharedContext.setState(newState);
        } else {
            render();
        }
    }

    public void setStateQuiet(S newState) {
        this.state = newState;
        layoutDirty = true;
        render();
    }

    void setStateFromShared(S newState) {
        this.state = newState;
        layoutDirty = true;
        render();
    }

    void renderFromShared() {
        render();
    }

    public void update(UnaryOperator<S> updater) {
        setState(updater.apply(state));
    }

    public void updateQuiet(UnaryOperator<S> updater) {
        setStateQuiet(updater.apply(state));
    }

    @SuppressWarnings("unchecked")
    public <T> void updateUnchecked(Function<T, T> updater) {
        setState((S) updater.apply((T) state));
    }

    public ViewSession<S> onClose(Consumer<CloseReason> handler) {
        this.onCloseHandler = handler;
        return this;
    }

    public ViewSession<S> refreshEvery(Duration interval) {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (closed) return TaskSchedule.stop();
            render();
            return TaskSchedule.millis(interval.toMillis());
        });
        return this;
    }

    public void close(CloseReason reason) {
        if (closed) return;
        if (reason == CloseReason.REPLACED) {
            suppressCloseEvent = true;
        }
        closed = true;

        autoUpdateTasks.values().forEach(Task::cancel);
        autoUpdateTasks.clear();

        if (sharedContext != null) {
            sharedContext.unregisterSession(this);
        }

        flushPendingSlotChanges();

        view.onClose(state, context, reason);
        if (onCloseHandler != null) onCloseHandler.accept(reason);
        if (reason == CloseReason.SERVER_EXITED) player.closeInventory();
    }

    public enum CloseReason {
        PLAYER_EXITED,
        SERVER_EXITED,
        REPLACED
    }
}
