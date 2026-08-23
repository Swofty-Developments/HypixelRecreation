package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.GameMode;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.Layouts;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.click.ItemSlots;
import net.swofty.type.generic.gui.v2.click.SlotClicks;
import net.swofty.type.generic.gui.v2.click.SlotStack;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.RawClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.event.custom.ItemCraftEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.item.crafting.grid.CraftingGridModel;
import net.swofty.type.skyblockgeneric.item.crafting.grid.CraftingSlot;
import net.swofty.type.skyblockgeneric.item.crafting.grid.SkyBlockCraftingBench;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUICrafting implements StatefulView<GUICrafting.CraftingState> {
    private static final List<Text> DEFAULT_CRAFT_ERROR = List.of(Text.of("<c>You cannot craft this item right now."));
    private static final InventoryType INVENTORY_TYPE = InventoryType.CHEST_6_ROW;
    private static final int[] CRAFT_SLOTS = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 23;

    @Override
    public ViewConfiguration<CraftingState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.crafting.title", INVENTORY_TYPE);
    }

    @Override
    public CraftingState initialState() {
        return new CraftingState(0);
    }

    @Override
    public void layout(ViewLayout<CraftingState> layout, CraftingState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockCraftingBench.Resolution resolution = new SkyBlockCraftingBench(player).resolve(readGrid(ctx));

        Material borderMaterial = resolution.allowed()
                ? Material.LIME_STAINED_GLASS_PANE
                : Material.RED_STAINED_GLASS_PANE;

        Components.fill(layout);
        layout.slots(Layouts.row(5), (_, _) -> ItemStacks.named(borderMaterial, ""));
        Components.close(layout, 49);

        for (int slot : CRAFT_SLOTS) {
            layout.editable(slot);
        }

        if (!resolution.hasRecipe()) {
            layout.slot(RESULT_SLOT, (_, _) -> ItemStacks.item(Material.BARRIER, 1,
                    Text.key("gui_sbmenu.crafting.recipe_required"),
                    Text.keyLines("gui_sbmenu.crafting.recipe_required.lore")));
            return;
        }

        if (!resolution.allowed()) {
            List<Text> messages = errorMessages(resolution.permission());
            layout.slot(RESULT_SLOT, (_, _) -> ItemStacks.item(Material.BEDROCK, 1,
                    messages.getFirst(),
                    messages.subList(1, messages.size())));
            return;
        }

        layout.slot(RESULT_SLOT, (_, _) -> resultDisplay(resolution.output()));
    }

    @Override
    public boolean onRawClick(RawClickContext<CraftingState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Click raw = click.click();
        boolean inView = click.inViewInventory();

        if (!targetsCraftingSurface(raw, inView) || touchesProtectedItem(raw, inView, player, ctx)) {
            return false;
        }

        click.cancel();

        SkyBlockCraftingBench bench = new SkyBlockCraftingBench(player);
        CraftingGridModel model = snapshot(bench, player, ctx);
        boolean changed = apply(model, raw, inView, player);

        if (changed) {
            commit(player, ctx, model);
            announceCraft(player, bench, model);
            ctx.session(CraftingState.class).setState(new CraftingState(click.state().revision() + 1));
        }

        return true;
    }

    private boolean apply(CraftingGridModel model, Click click, boolean inView, SkyBlockPlayer player) {
        boolean creative = player.getGameMode() == GameMode.CREATIVE;

        return switch (click) {
            case Click.Left(int slot) -> pickUp(model, resolve(slot, inView), false);
            case Click.Right(int slot) -> pickUp(model, resolve(slot, inView), true);
            case Click.LeftShift(int slot) -> quickMove(model, resolve(slot, inView));
            case Click.RightShift(int slot) -> quickMove(model, resolve(slot, inView));
            case Click.Double(_) -> model.collect(model.cursor());
            case Click.Middle(int slot) -> creative && cloneAt(model, resolve(slot, inView));
            case Click.LeftDrag(List<Integer> slots) -> model.drag(resolveAll(slots, inView), SlotClicks.DragMode.EVEN);
            case Click.RightDrag(List<Integer> slots) -> model.drag(resolveAll(slots, inView), SlotClicks.DragMode.SINGLE);
            case Click.MiddleDrag(List<Integer> slots) ->
                    creative && model.drag(resolveAll(slots, inView), SlotClicks.DragMode.FULL);
            case Click.HotbarSwap(int hotbarSlot, int slot) -> swap(model, resolve(slot, inView), hotbarSlot);
            case Click.DropSlot(int slot, boolean all) -> dropSlot(player, model, resolve(slot, inView), all);
            case Click.LeftDropCursor() -> dropCursor(player, model, true);
            case Click.RightDropCursor() -> dropCursor(player, model, false);
            case Click.MiddleDropCursor() -> false;
            case Click.OffhandSwap(_) -> false;
        };
    }

    private boolean pickUp(CraftingGridModel model, CraftingSlot slot, boolean half) {
        return slot != null && model.pickUp(slot, half);
    }

    private boolean quickMove(CraftingGridModel model, CraftingSlot slot) {
        return slot != null && model.quickMove(slot);
    }

    private boolean cloneAt(CraftingGridModel model, CraftingSlot slot) {
        return slot != null && model.cloneToCursor(slot);
    }

    private boolean swap(CraftingGridModel model, CraftingSlot slot, int hotbarSlot) {
        return slot != null && model.swap(slot, hotbarSlot);
    }

    private boolean dropSlot(SkyBlockPlayer player, CraftingGridModel model, CraftingSlot slot, boolean all) {
        if (slot == null || slot.region() == CraftingSlot.Region.RESULT) return false;

        SlotStack current = model.peek(slot);
        if (current.isEmpty()) return false;

        int amount = all ? current.amount() : 1;
        if (!player.dropItem(ItemSlots.toStack(current.withAmount(amount)))) return false;

        return model.removeFromSlot(slot, amount);
    }

    private boolean dropCursor(SkyBlockPlayer player, CraftingGridModel model, boolean all) {
        SlotStack cursor = model.cursor();
        if (cursor.isEmpty()) return false;

        int amount = all ? cursor.amount() : 1;
        if (!player.dropItem(ItemSlots.toStack(cursor.withAmount(amount)))) return false;

        return model.removeFromCursor(amount);
    }

    private CraftingGridModel snapshot(SkyBlockCraftingBench bench, SkyBlockPlayer player, ViewContext ctx) {
        SlotStack[] items = new SlotStack[CraftingGridModel.PLAYER_SIZE];
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemSlots.toSlot(player.getInventory().getItemStack(i));
        }

        return new CraftingGridModel(bench, readGrid(ctx), items,
                ItemSlots.toSlot(player.getInventory().getCursorItem()));
    }

    private void commit(SkyBlockPlayer player, ViewContext ctx, CraftingGridModel model) {
        SlotStack[] grid = model.grid();
        for (int i = 0; i < CRAFT_SLOTS.length; i++) {
            ItemStack updated = ItemSlots.toStack(grid[i]);
            if (!ctx.inventory().getItemStack(CRAFT_SLOTS[i]).equals(updated)) {
                ctx.inventory().setItemStack(CRAFT_SLOTS[i], updated);
            }
        }

        SlotStack crafted = model.craftCount() > 0 ? model.craftedOutput() : SlotStack.EMPTY;
        SlotStack[] items = model.playerItems();
        for (int i = 0; i < items.length; i++) {
            ItemStack updated = ItemSlots.toStack(items[i]);
            if (player.getInventory().getItemStack(i).equals(updated)) continue;

            if (!updated.isAir() && items[i].stacksWith(crafted)) {
                updated = PlayerItemUpdater.playerUpdate(player, updated, true).build();
            }
            player.getInventory().setItemStack(i, updated);
        }

        player.getInventory().setCursorItem(ItemSlots.toStack(model.cursor()));
    }

    private void announceCraft(SkyBlockPlayer player, SkyBlockCraftingBench bench, CraftingGridModel model) {
        if (model.craftCount() <= 0) return;

        SkyBlockRecipe<?> recipe = bench.lastConsumedRecipe();
        if (recipe == null) return;

        ItemStack crafted = ItemSlots.toStack(model.craftedOutput()).withAmount(model.craftedTotal());
        HypixelEventHandler.callCustomEvent(new ItemCraftEvent(player, new SkyBlockItem(crafted), recipe));
    }

    private CraftingSlot resolve(int slot, boolean inView) {
        if (!inView) {
            return slot >= 0 && slot < CraftingGridModel.PLAYER_SIZE ? CraftingSlot.player(slot) : null;
        }

        int size = INVENTORY_TYPE.getSize();
        if (slot >= size) {
            int playerSlot = slot - size;
            return playerSlot < CraftingGridModel.PLAYER_SIZE ? CraftingSlot.player(playerSlot) : null;
        }
        if (slot == RESULT_SLOT) return CraftingSlot.RESULT;

        for (int i = 0; i < CRAFT_SLOTS.length; i++) {
            if (CRAFT_SLOTS[i] == slot) return CraftingSlot.grid(i);
        }
        return null;
    }

    private List<CraftingSlot> resolveAll(List<Integer> slots, boolean inView) {
        List<CraftingSlot> resolved = new ArrayList<>(slots.size());
        for (int slot : slots) {
            CraftingSlot target = resolve(slot, inView);
            if (target != null) resolved.add(target);
        }
        return resolved;
    }

    private boolean targetsCraftingSurface(Click click, boolean inView) {
        if (click instanceof Click.Drag || click instanceof Click.DropCursor) {
            return true;
        }
        return resolve(click.slot(), inView) != null;
    }

    private boolean touchesProtectedItem(Click click, boolean inView, SkyBlockPlayer player, ViewContext ctx) {
        if (click instanceof Click.Drag || click instanceof Click.DropCursor) {
            return isProtected(player.getInventory().getCursorItem());
        }
        if (click instanceof Click.HotbarSwap(int hotbarSlot, int slot)) {
            return isProtected(stackAt(resolve(slot, inView), player, ctx))
                    || isProtected(player.getInventory().getItemStack(hotbarSlot));
        }
        return isProtected(stackAt(resolve(click.slot(), inView), player, ctx));
    }

    private ItemStack stackAt(CraftingSlot slot, SkyBlockPlayer player, ViewContext ctx) {
        if (slot == null) return ItemStack.AIR;
        return switch (slot.region()) {
            case GRID -> ctx.inventory().getItemStack(CRAFT_SLOTS[slot.index()]);
            case PLAYER -> player.getInventory().getItemStack(slot.index());
            case RESULT -> ItemStack.AIR;
        };
    }

    private boolean isProtected(ItemStack stack) {
        if (stack == null || stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return false;
        return new SkyBlockItem(stack).getItemType() == ItemType.SKYBLOCK_MENU;
    }

    private SlotStack[] readGrid(ViewContext ctx) {
        SlotStack[] grid = new SlotStack[CraftingGridModel.GRID_SIZE];
        for (int i = 0; i < CRAFT_SLOTS.length; i++) {
            grid[i] = ItemSlots.toSlot(ctx.inventory().getItemStack(CRAFT_SLOTS[i]));
        }
        return grid;
    }

    private ItemStack.Builder resultDisplay(ItemStack output) {
        ItemStack.Builder builder = output.builder();

        List<Text> lore = new ArrayList<>();
        List<Component> existingLore = output.get(DataComponents.LORE);
        if (existingLore != null) {
            existingLore.forEach(line -> lore.add(Text.of("<7>{}", StringUtility.getTextFromComponent(line))));
        }
        lore.addAll(Text.keyLines("gui_sbmenu.crafting.crafting_item.lore"));

        return ItemStacks.lore(builder, lore);
    }

    private List<Text> errorMessages(SkyBlockRecipe.CraftingResult result) {
        if (result == null || result.errorMessage() == null || result.errorMessage().length == 0) {
            return DEFAULT_CRAFT_ERROR;
        }
        return Arrays.stream(result.errorMessage()).map(Text::parse).toList();
    }

    @Override
    public void onClose(CraftingState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        for (int slot : CRAFT_SLOTS) {
            ItemStack item = ctx.inventory().getItemStack(slot);
            if (item.isAir()) continue;

            ctx.inventory().setItemStack(slot, ItemStack.AIR);
            player.addAndUpdateItem(new SkyBlockItem(item));
        }
    }

    @Override
    public boolean onBottomClick(ClickContext<CraftingState> click, ViewContext ctx) {
        return true;
    }

    public record CraftingState(int revision) {
    }
}
