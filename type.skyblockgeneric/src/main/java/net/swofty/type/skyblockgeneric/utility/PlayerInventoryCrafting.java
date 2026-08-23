package net.swofty.type.skyblockgeneric.utility;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.event.custom.ItemCraftEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerInventoryCrafting {
    private static final String[] DEFAULT_CRAFT_ERROR = new String[]{"<c>You cannot craft this item right now."};
    private static final int RESULT_SLOT = 36;
    private static final int[] CRAFT_SLOTS = new int[]{37, 38, 39, 40};
    private static final int[] RECIPE_GRID_INDEXES = new int[]{0, 1, 3, 4};
    private static final int RECIPE_GRID_SIZE = 9;

    public static boolean isCraftingSlot(int slot) {
        return Arrays.stream(CRAFT_SLOTS).anyMatch(craftSlot -> craftSlot == slot);
    }

    public static boolean isResultSlot(int slot) {
        return slot == RESULT_SLOT;
    }

    public static void refreshNextTick(SkyBlockPlayer player) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> refresh(player));
    }

    public static void refresh(SkyBlockPlayer player) {
        PlayerInventory inventory = player.getInventory();
        SkyBlockRecipe<?> recipe = parseRecipe(inventory);
        inventory.setItemStack(RESULT_SLOT, renderResult(player, recipe));
        inventory.update();
    }

    public static void returnCraftingGrid(SkyBlockPlayer player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] itemsToReturn = Arrays.stream(CRAFT_SLOTS)
            .mapToObj(inventory::getItemStack)
            .filter(item -> !item.isAir())
            .toArray(ItemStack[]::new);

        inventory.setItemStack(RESULT_SLOT, ItemStack.AIR);
        for (int slot : CRAFT_SLOTS) {
            inventory.setItemStack(slot, ItemStack.AIR);
        }

        for (ItemStack item : itemsToReturn) {
            returnToMainInventory(player, item);
        }

        inventory.update();
    }

    public static void craft(SkyBlockPlayer player, Click click) {
        PlayerInventory inventory = player.getInventory();
        SkyBlockRecipe<?> recipe = parseRecipe(inventory);
        if (recipe == null) {
            refresh(player);
            return;
        }

        SkyBlockRecipe.CraftingResult permissionResult = recipe.getCanCraft().apply(player);
        if (permissionResult == null || !permissionResult.allowed()) {
            refresh(player);
            return;
        }

        int amount = recipe.getAmount();
        ItemStack craftedItem = PlayerItemUpdater.playerUpdate(player, recipe.getResult().getItemStack())
            .amount(amount)
            .build();

        if (click instanceof Click.LeftShift || click instanceof Click.RightShift) {
            craftToInventory(player, recipe, craftedItem, amount);
        } else {
            craftToCursor(player, recipe, craftedItem, amount);
        }

        refresh(player);
        player.getInventory().update();
    }

    private static ItemStack renderResult(SkyBlockPlayer player, SkyBlockRecipe<?> recipe) {
        if (recipe == null) {
            return ItemStack.AIR;
        }

        SkyBlockRecipe.CraftingResult result = recipe.getCanCraft().apply(player);
        if (result == null || !result.allowed()) {
            String[] craftErrorMessages = getCraftErrorMessages(result);
            Text name = Text.parse(craftErrorMessages[0]);
            List<Text> lore = Arrays.stream(craftErrorMessages, 1, craftErrorMessages.length)
                .map(Text::parse)
                .toList();
            return ItemStacks.item(Material.BEDROCK, 1, name, lore).build();
        }

        ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(player, recipe.getResult().getItemStack())
            .amount(recipe.getAmount());

        ArrayList<Text> lore = new ArrayList<>();
        var existingLore = builder.build().get(DataComponents.LORE);
        if (existingLore != null) {
            existingLore.stream().map(line -> Text.of("<7>{}", StringUtility.getTextFromComponent(line))).forEach(lore::add);
        }
        return ItemStacks.lines(builder, lore).build();
    }

    private static void craftToInventory(SkyBlockPlayer player, SkyBlockRecipe<?> recipe, ItemStack craftedItem, int amountPerCraft) {
        int maxCraftsByInventory = getMaxCraftsByInventory(player, craftedItem, amountPerCraft);
        SkyBlockItem[] simulatedItems = getCurrentRecipeAsItems(player.getInventory());
        int craftedCount = 0;

        while (craftedCount < maxCraftsByInventory) {
            SkyBlockRecipe.CraftingResult craftResult = recipe.getCanCraft().apply(player);
            if (craftResult == null || !craftResult.allowed()) {
                break;
            }

            try {
                simulatedItems = recipe.consume(simulatedItems);
            } catch (Exception e) {
                break;
            }

            craftedCount++;
        }

        int totalCraftedAmount = craftedCount * amountPerCraft;
        if (totalCraftedAmount <= 0) {
            return;
        }

        applyConsumedGrid(player, simulatedItems);
        player.addAndUpdateItem(craftedItem.withAmount(totalCraftedAmount));
        HypixelEventHandler.callCustomEvent(
            new ItemCraftEvent(player, new SkyBlockItem(craftedItem.withAmount(totalCraftedAmount)), recipe)
        );
    }

    private static void craftToCursor(SkyBlockPlayer player, SkyBlockRecipe<?> recipe, ItemStack craftedItem, int amount) {
        PlayerInventory inventory = player.getInventory();
        ItemStack cursorItemStack = inventory.getCursorItem();
        SkyBlockItem cursorItem = new SkyBlockItem(cursorItemStack);
        ItemType cursorItemType = cursorItem.getAttributeHandler().getPotentialType();
        ItemType resultItemType = recipe.getResult().getAttributeHandler().getPotentialType();

        if (!cursorItemStack.isAir() && (cursorItemType == null || !cursorItemType.equals(resultItemType))) {
            return;
        }

        int currentCursorAmount = cursorItemStack.isAir() ? 0 : cursorItemStack.amount();
        int newAmount = currentCursorAmount + amount;
        if (newAmount > craftedItem.material().maxStackSize()) {
            return;
        }

        SkyBlockItem[] toReplace;
        try {
            toReplace = recipe.consume(getCurrentRecipeAsItems(inventory));
        } catch (Exception e) {
            return;
        }

        applyConsumedGrid(player, toReplace);
        inventory.setCursorItem(craftedItem.withAmount(newAmount));
        HypixelEventHandler.callCustomEvent(new ItemCraftEvent(player, new SkyBlockItem(craftedItem), recipe));
    }

    private static int getMaxCraftsByInventory(SkyBlockPlayer player, ItemStack craftedItem, int amountPerCraft) {
        if (amountPerCraft <= 0) {
            return 0;
        }

        int maxStackSize = craftedItem.material().maxStackSize();
        int availableSpace = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) {
                availableSpace += maxStackSize;
                continue;
            }
            if (stack.isSimilar(craftedItem) && stack.amount() < maxStackSize) {
                availableSpace += maxStackSize - stack.amount();
            }
        }

        return availableSpace / amountPerCraft;
    }

    private static void returnToMainInventory(SkyBlockPlayer player, ItemStack item) {
        ItemStack updatedItem = PlayerItemUpdater.playerUpdate(player, item, true).build();
        int remaining = updatedItem.amount();
        int maxStackSize = updatedItem.material().maxStackSize();
        PlayerInventory inventory = player.getInventory();

        for (int slot = 0; slot < 36 && remaining > 0; slot++) {
            ItemStack stack = inventory.getItemStack(slot);
            if (!stack.isSimilar(updatedItem) || stack.amount() >= maxStackSize) {
                continue;
            }

            int moved = Math.min(remaining, maxStackSize - stack.amount());
            inventory.setItemStack(slot, stack.withAmount(stack.amount() + moved));
            remaining -= moved;
        }

        for (int slot = 0; slot < 36 && remaining > 0; slot++) {
            ItemStack stack = inventory.getItemStack(slot);
            if (!stack.isAir()) {
                continue;
            }

            int moved = Math.min(remaining, maxStackSize);
            inventory.setItemStack(slot, updatedItem.withAmount(moved));
            remaining -= moved;
        }

        if (remaining > 0) {
            player.addToStash(new SkyBlockItem(updatedItem.withAmount(remaining)));
        }
    }

    private static SkyBlockRecipe<?> parseRecipe(PlayerInventory inventory) {
        return SkyBlockRecipe.parseRecipe(getCurrentRecipeStacks(inventory));
    }

    private static ItemStack[] getCurrentRecipeStacks(PlayerInventory inventory) {
        ItemStack[] stacks = new ItemStack[RECIPE_GRID_SIZE];
        Arrays.fill(stacks, ItemStack.AIR);

        for (int i = 0; i < CRAFT_SLOTS.length; i++) {
            stacks[RECIPE_GRID_INDEXES[i]] = inventory.getItemStack(CRAFT_SLOTS[i]);
        }
        return stacks;
    }

    private static SkyBlockItem[] getCurrentRecipeAsItems(PlayerInventory inventory) {
        ItemStack[] rawStacks = getCurrentRecipeStacks(inventory);
        SkyBlockItem[] stacks = new SkyBlockItem[RECIPE_GRID_SIZE];
        for (int i = 0; i < rawStacks.length; i++) {
            stacks[i] = new SkyBlockItem(rawStacks[i]);
        }
        return stacks;
    }

    private static void applyConsumedGrid(SkyBlockPlayer player, SkyBlockItem[] toReplace) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < CRAFT_SLOTS.length; i++) {
            SkyBlockItem replacement = toReplace[RECIPE_GRID_INDEXES[i]];
            if (replacement == null || replacement.getItemStack().material() == Material.BEDROCK) {
                inventory.setItemStack(CRAFT_SLOTS[i], ItemStack.AIR);
            } else {
                inventory.setItemStack(
                    CRAFT_SLOTS[i],
                    PlayerItemUpdater.playerUpdate(player, replacement.getItemStack()).build()
                );
            }
        }
    }

    private static String[] getCraftErrorMessages(SkyBlockRecipe.CraftingResult result) {
        if (result == null || result.errorMessage() == null || result.errorMessage().length == 0) {
            return DEFAULT_CRAFT_ERROR;
        }
        return result.errorMessage();
    }
}
