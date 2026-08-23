package net.swofty.type.skyblockgeneric.item.crafting.grid;

import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.gui.v2.click.ItemSlots;
import net.swofty.type.generic.gui.v2.click.SlotStack;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

public final class SkyBlockCraftingBench implements CraftingResolver {

    public record Resolution(SkyBlockRecipe<?> recipe, SkyBlockRecipe.CraftingResult permission, ItemStack output) {
        public static final Resolution NONE = new Resolution(null, null, ItemStack.AIR);

        public boolean hasRecipe() {
            return recipe != null;
        }

        public boolean allowed() {
            return permission != null && permission.allowed();
        }
    }

    private final SkyBlockPlayer player;

    private ItemStack[] cachedGrid;
    private Resolution cachedResolution = Resolution.NONE;
    private SkyBlockRecipe<?> lastConsumedRecipe;

    public SkyBlockCraftingBench(SkyBlockPlayer player) {
        this.player = player;
    }

    public SkyBlockRecipe<?> lastConsumedRecipe() {
        return lastConsumedRecipe;
    }

    public Resolution resolve(SlotStack[] grid) {
        ItemStack[] stacks = toStacks(grid);
        if (cachedGrid != null && Arrays.equals(cachedGrid, stacks)) {
            return cachedResolution;
        }

        SkyBlockRecipe<?> recipe = SkyBlockRecipe.parseRecipe(stacks);
        Resolution resolution;
        if (recipe == null) {
            resolution = Resolution.NONE;
        } else {
            SkyBlockRecipe.CraftingResult permission = recipe.getCanCraft().apply(player);
            ItemStack output = PlayerItemUpdater.playerUpdate(player, recipe.getResult().getItemStack())
                    .amount(recipe.getAmount())
                    .build();
            resolution = new Resolution(recipe, permission, output);
        }

        cachedGrid = stacks;
        cachedResolution = resolution;
        return resolution;
    }

    @Override
    public SlotStack result(SlotStack[] grid) {
        Resolution resolution = resolve(grid);
        return resolution.allowed() ? ItemSlots.toSlot(resolution.output()) : SlotStack.EMPTY;
    }

    @Override
    public SlotStack[] consume(SlotStack[] grid) {
        Resolution resolution = resolve(grid);
        if (!resolution.allowed()) return grid;

        SkyBlockItem[] items = new SkyBlockItem[CraftingGridModel.GRID_SIZE];
        ItemStack[] stacks = toStacks(grid);
        for (int i = 0; i < items.length; i++) {
            items[i] = new SkyBlockItem(stacks[i]);
        }

        SkyBlockItem[] leftovers;
        try {
            leftovers = resolution.recipe().consume(items);
        } catch (Exception exception) {
            return grid;
        }

        lastConsumedRecipe = resolution.recipe();

        SlotStack[] out = new SlotStack[CraftingGridModel.GRID_SIZE];
        for (int i = 0; i < out.length; i++) {
            SkyBlockItem leftover = i < leftovers.length ? leftovers[i] : null;
            if (leftover == null || leftover.getItemStack().isAir()) {
                out[i] = SlotStack.EMPTY;
                continue;
            }
            out[i] = ItemSlots.toSlot(PlayerItemUpdater.playerUpdate(player, leftover.getItemStack()).build());
        }
        return out;
    }

    private static ItemStack[] toStacks(SlotStack[] grid) {
        ItemStack[] stacks = new ItemStack[CraftingGridModel.GRID_SIZE];
        Arrays.fill(stacks, ItemStack.AIR);
        for (int i = 0; i < Math.min(stacks.length, grid.length); i++) {
            stacks[i] = ItemSlots.toStack(grid[i] == null ? SlotStack.EMPTY : grid[i]);
        }
        return stacks;
    }
}
