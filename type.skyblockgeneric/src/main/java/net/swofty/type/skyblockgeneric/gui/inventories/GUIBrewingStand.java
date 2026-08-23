package net.swofty.type.skyblockgeneric.gui.inventories;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.attribute.attributes.BlockAttributeBrewingData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.BrewingIngredientComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.potion.PotionEffectType;
import net.swofty.type.skyblockgeneric.potion.PotionModifier;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIBrewingStand extends HypixelInventoryGUI implements RefreshingGUI {

    private static final int INGREDIENT_SLOT = 13;
    private static final int[] ANIMATION_SLOTS = {20, 21, 22, 23, 24, 29, 31, 33};
    private static final int[] POTION_SLOTS = {38, 40, 42};
    private static final int CLOSE_SLOT = 49;

    private final Instance instance;
    private final Point blockPosition;
    private SkyBlockBlock block;
    private boolean animationToggle = false;

    // Store items in instance variables like the Anvil does
    private SkyBlockItem ingredientItem = null;
    private SkyBlockItem[] potionItems = new SkyBlockItem[3];

    public GUIBrewingStand(Instance instance, Point position, SkyBlockBlock block) {
        super(Text.key("gui_misc.brewing_stand.title"), InventoryType.CHEST_6_ROW);
        this.instance = instance;
        this.blockPosition = position;
        this.block = block;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(CLOSE_SLOT));

        // Load persisted brewing data into instance variables
        loadBrewingDataToGUI();

        // Set up ingredient slot
        setupIngredientSlot();

        // Set up potion slots
        setupPotionSlots();

        // Set initial animation panes
        updateAnimationPanes(getBrewingData(), e.player());
    }

    private void setupIngredientSlot() {
        set(new GUIClickableItem(INGREDIENT_SLOT) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                if (ingredientItem != null && !ingredientItem.isNA() && !ingredientItem.isAir()) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return PlayerItemUpdater.playerUpdate(player, ingredientItem.getItemStack());
                }
                return ItemStack.builder(Material.AIR);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                // Allow placement/pickup
                e.setCancelled(false);
            }

            @Override
            public void runPost(InventoryClickEvent e, HypixelPlayer p) {
                // Use the GUI's inventory directly to check the slot state
                // e.getInventory() may return wrong inventory for shift-clicks
                ItemStack slotItem = getInventory().getItemStack(INGREDIENT_SLOT);
                if (slotItem.isAir()) {
                    ingredientItem = null;
                } else {
                    ingredientItem = new SkyBlockItem(slotItem);
                }
            }

            @Override
            public boolean canPickup() {
                return true;
            }
        });
    }

    private void setupPotionSlots() {
        for (int i = 0; i < POTION_SLOTS.length; i++) {
            final int index = i;
            final int slot = POTION_SLOTS[i];

            set(new GUIClickableItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    if (potionItems[index] != null && !potionItems[index].isNA() && !potionItems[index].isAir()) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        return PlayerItemUpdater.playerUpdate(player, potionItems[index].getItemStack());
                    }
                    return ItemStack.builder(Material.AIR);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    // Allow placement/pickup
                    e.setCancelled(false);
                }

                @Override
                public void runPost(InventoryClickEvent e, HypixelPlayer p) {
                    // Use the GUI's inventory directly to check the slot state
                    // e.getInventory() may return wrong inventory for shift-clicks
                    ItemStack slotItem = getInventory().getItemStack(slot);
                    if (slotItem.isAir()) {
                        potionItems[index] = null;
                    } else {
                        potionItems[index] = new SkyBlockItem(slotItem);
                    }
                }

                @Override
                public boolean canPickup() {
                    return true;
                }
            });
        }
    }

    private void loadBrewingDataToGUI() {
        BlockAttributeBrewingData.BrewingData data = getBrewingData();

        // Load into instance variables - the getItem() methods will return these
        if (data.getIngredient() != null && !data.getIngredient().isNA()) {
            ingredientItem = data.getIngredient();
        }

        SkyBlockItem[] potions = data.getPotionSlots();
        for (int i = 0; i < POTION_SLOTS.length; i++) {
            if (potions[i] != null && !potions[i].isNA()) {
                potionItems[i] = potions[i];
            }
        }
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        BlockAttributeBrewingData.BrewingData brewingData = getBrewingData();

        // Check if items were removed during brewing
        if (brewingData.isBrewing() && !validateBrewingItems()) {
            cancelBrewing();
            brewingData = getBrewingData();
        }

        // Check if brewing is complete
        if (brewingData.isBrewingComplete()) {
            completeBrewing((SkyBlockPlayer) player);
            brewingData = getBrewingData();
        }

        // Try to start brewing if not already brewing
        if (!brewingData.isBrewing()) {
            tryStartBrewing();
            brewingData = getBrewingData();
        }

        // Update animation panes
        updateAnimationPanes(brewingData, player);

        // Toggle animation state
        animationToggle = !animationToggle;
    }

    private void updateAnimationPanes(BlockAttributeBrewingData.BrewingData brewingData, HypixelPlayer player) {
        Material paneMaterial;
        Text paneName;
        List<Text> paneLore;

        if (brewingData.isBrewing()) {
            paneMaterial = animationToggle ? Material.RED_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE;
            long remainingSeconds = brewingData.getRemainingTimeMs() / 1000;
            paneName = Text.key("gui_misc.brewing_stand.remaining_time", remainingSeconds);
            paneLore = List.of();
        } else {
            paneMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            paneName = Text.key("gui_misc.brewing_stand.place_bottles");
            paneLore = List.of(Text.key("gui_misc.brewing_stand.place_bottles_below"));
        }

        for (int slot : ANIMATION_SLOTS) {
            set(slot, ItemStacks.item(paneMaterial, 1, paneName, paneLore));
        }
    }

    private boolean validateBrewingItems() {
        // Check ingredient from instance variable
        if (ingredientItem == null || ingredientItem.isNA() || ingredientItem.isAir()) return false;

        // Check all potion slots from instance variables
        for (SkyBlockItem potion : potionItems) {
            if (potion == null || potion.isNA() || potion.isAir()) return false;
        }

        return true;
    }

    private void tryStartBrewing() {
        // Check ingredient from instance variable
        if (ingredientItem == null || ingredientItem.isNA() || ingredientItem.isAir()) return;
        if (ingredientItem.getConfig() == null) return;
        if (!ingredientItem.hasComponent(BrewingIngredientComponent.class)) return;

        BrewingIngredientComponent brewComp = ingredientItem.getComponent(BrewingIngredientComponent.class);

        // Validate all 3 potion slots have valid items
        for (SkyBlockItem potion : potionItems) {
            if (potion == null || potion.isNA() || potion.isAir()) return;
            if (!isValidPotionItem(potion)) return;
        }

        // All conditions met - start brewing
        BlockAttributeBrewingData.BrewingData data = new BlockAttributeBrewingData.BrewingData();
        data.setIngredient(ingredientItem);

        SkyBlockItem[] potions = data.getPotionSlots();
        System.arraycopy(potionItems, 0, potions, 0, potionItems.length);

        data.setBrewingStartTime(System.currentTimeMillis());
        data.setBrewingDurationSeconds(brewComp.getBrewingTimeSeconds());

        saveBrewingData(data);
    }

    private void cancelBrewing() {
        BlockAttributeBrewingData.BrewingData data = new BlockAttributeBrewingData.BrewingData();
        saveBrewingData(data);
    }

    private void completeBrewing(SkyBlockPlayer player) {
        BlockAttributeBrewingData.BrewingData data = getBrewingData();

        SkyBlockItem ingredient = data.getIngredient();
        if (ingredient == null || !ingredient.hasComponent(BrewingIngredientComponent.class)) {
            cancelBrewing();
            return;
        }

        BrewingIngredientComponent brewComp = ingredient.getComponent(BrewingIngredientComponent.class);

        // Apply brewing effect to all potions using instance variables
        for (int i = 0; i < potionItems.length; i++) {
            SkyBlockItem potionItem = potionItems[i];
            if (potionItem != null && !potionItem.isNA() && !potionItem.isAir()) {
                potionItems[i] = applyBrewingEffect(potionItem, brewComp);
            }
        }

        // Award Alchemy XP
        int alchemyXp = brewComp.getAlchemyXp();
        if (alchemyXp > 0) {
            DatapointSkills.PlayerSkills skills = player.getSkills();
            skills.increase(player, SkillCategories.ALCHEMY, (double) alchemyXp);
        }

        // Remove one ingredient from instance variable
        if (ingredientItem != null && !ingredientItem.isAir()) {
            ItemStack ingredientStack = ingredientItem.getItemStack();
            if (ingredientStack.amount() > 1) {
                ingredientItem = new SkyBlockItem(ingredientStack.withAmount(ingredientStack.amount() - 1));
            } else {
                ingredientItem = null;
            }
        }

        // Reset brewing state
        cancelBrewing();
    }

    private SkyBlockItem applyBrewingEffect(SkyBlockItem potion, BrewingIngredientComponent brewComp) {
        String effectName = brewComp.getPotionEffect();
        int baseDuration = brewComp.getEffectDuration();
        int amplifier = brewComp.getEffectAmplifier();

        // Get current potion data if exists
        ItemAttributePotionData.PotionData currentData = potion.getAttributeHandler().getPotionData();

        // Check if ingredient is a modifier (glowstone, redstone, gunpowder)
        String ingredientTypeName = ingredientItem != null ?
                ingredientItem.getAttributeHandler().getTypeAsString() : null;
        ItemType ingredientType = ingredientTypeName != null ? ItemType.get(ingredientTypeName) : null;
        PotionModifier modifier = ingredientType != null ? PotionModifier.fromIngredient(ingredientType) : null;

        ItemAttributePotionData.PotionData newData;

        if (modifier != null && currentData != null && !currentData.getEffectType().equals("WATER")) {
            // This is a modifier being applied to an existing potion
            newData = applyModifier(currentData, modifier);
        } else if (effectName.equals("MODIFIER")) {
            // This is a modifier item but can't be applied (no existing potion data or water bottle)
            // Return the potion unchanged
            return potion;
        } else if (effectName.equals("AWKWARD") || effectName.equals("THICK") || effectName.equals("MUNDANE")) {
            // Creating a base potion from water bottle
            newData = new ItemAttributePotionData.PotionData(effectName, 1, 0, false, false);
        } else if (currentData != null && currentData.getEffectType().equals("AWKWARD")) {
            // Creating an effect potion from awkward potion
            newData = new ItemAttributePotionData.PotionData(
                    effectName,
                    amplifier + 1, // Level = amplifier + 1
                    baseDuration,
                    false,
                    false
            );
        } else if (currentData != null && currentData.getEffectType().equals("WATER")) {
            // Some potions can be brewed directly from water (e.g., Weakness)
            if (effectName.equals("WEAKNESS") || effectName.equals("THICK") || effectName.equals("MUNDANE")) {
                newData = new ItemAttributePotionData.PotionData(
                        effectName,
                        amplifier + 1,
                        baseDuration,
                        false,
                        false
                );
            } else {
                // Default to awkward for most ingredients from water
                newData = new ItemAttributePotionData.PotionData(effectName, 1, baseDuration, false, false);
            }
        } else {
            // Create new potion data (e.g., water bottle becoming awkward)
            newData = new ItemAttributePotionData.PotionData(effectName, amplifier + 1, baseDuration, false, false);
        }

        // Set the potion data on the item
        potion.getAttributeHandler().setPotionData(newData);

        // Update rarity based on level
        Rarity newRarity = getRarityForLevel(newData.getLevel());
        potion.getAttributeHandler().setRarity(newRarity);

        // Material change (splash/lingering) is handled dynamically by the item updaters
        // based on the PotionData.isSplash() flag, so no need to create a new item

        return potion;
    }

    private ItemAttributePotionData.PotionData applyModifier(ItemAttributePotionData.PotionData currentData,
                                                              PotionModifier modifier) {
        String effectType = currentData.getEffectType();
        int level = currentData.getLevel();
        int duration = currentData.getBaseDurationSeconds();
        boolean isSplash = currentData.isSplash();
        boolean isExtended = currentData.isExtended();

        switch (modifier.getType()) {
            case LEVEL -> {
                // Get max level for this effect
                PotionEffectType effect = PotionEffectType.fromName(effectType);
                int maxLevel = effect != null ? effect.getMaxLevel() : 8;
                level = Math.min(level + modifier.getLevelIncrease(), maxLevel);
            }
            case DURATION -> {
                duration = modifier.getDurationSeconds();
                isExtended = true;
            }
            case LEVEL_AND_DURATION -> {
                // Enchanted Redstone Lamp - increases both
                PotionEffectType effect = PotionEffectType.fromName(effectType);
                int maxLevel = effect != null ? effect.getMaxLevel() : 8;
                level = Math.min(level + modifier.getLevelIncrease(), maxLevel);
                duration = modifier.getDurationSeconds();
                isExtended = true;
            }
            case SPLASH -> {
                isSplash = true;
                // Enchanted gunpowder preserves duration, regular gunpowder doesn't
                if (modifier == PotionModifier.GUNPOWDER) {
                    isExtended = false; // This will halve duration when calculating
                }
            }
        }

        return new ItemAttributePotionData.PotionData(effectType, level, duration, isSplash, isExtended);
    }

    private Rarity getRarityForLevel(int level) {
        if (level <= 2) return Rarity.COMMON;
        if (level <= 4) return Rarity.UNCOMMON;
        if (level <= 6) return Rarity.RARE;
        return Rarity.EPIC;
    }

    private boolean isValidPotionItem(SkyBlockItem item) {
        if (item.isNA()) return false;
        Material material = item.getMaterial();
        return material == Material.POTION ||
                material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION ||
                material == Material.GLASS_BOTTLE;
    }

    private BlockAttributeBrewingData.BrewingData getBrewingData() {
        // Reload block from world to get latest data
        if (SkyBlockBlock.isSkyBlockBlock(instance.getBlock(blockPosition))) {
            block = new SkyBlockBlock(instance.getBlock(blockPosition));
        }

        BlockAttributeBrewingData attr = (BlockAttributeBrewingData) block.getAttribute("brewing_data");
        if (attr != null) {
            return attr.getValue();
        }
        return new BlockAttributeBrewingData.BrewingData();
    }

    private void saveBrewingData(BlockAttributeBrewingData.BrewingData data) {
        BlockAttributeBrewingData attr = (BlockAttributeBrewingData) block.getAttribute("brewing_data");
        if (attr != null) {
            attr.setValue(data);
            instance.setBlock(blockPosition, block.toBlock());
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        BlockAttributeBrewingData.BrewingData data = getBrewingData();

        // If not brewing, return items to player from instance variables
        if (!data.isBrewing()) {
            SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();

            if (ingredientItem != null && !ingredientItem.isNA() && !ingredientItem.isAir()) {
                player.addAndUpdateItem(ingredientItem);
            }

            for (SkyBlockItem potion : potionItems) {
                if (potion != null && !potion.isNA() && !potion.isAir()) {
                    player.addAndUpdateItem(potion);
                }
            }
        } else {
            // If brewing, save current items back to block data from instance variables
            BlockAttributeBrewingData.BrewingData newData = new BlockAttributeBrewingData.BrewingData();

            if (ingredientItem != null && !ingredientItem.isNA() && !ingredientItem.isAir()) {
                newData.setIngredient(ingredientItem);
            }

            SkyBlockItem[] potions = newData.getPotionSlots();
            for (int i = 0; i < potionItems.length; i++) {
                if (potionItems[i] != null && !potionItems[i].isNA() && !potionItems[i].isAir()) {
                    potions[i] = potionItems[i];
                }
            }

            newData.setBrewingStartTime(data.getBrewingStartTime());
            newData.setBrewingDurationSeconds(data.getBrewingDurationSeconds());
            saveBrewingData(newData);
        }
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer p) {
        // Same logic as onClose using instance variables
        BlockAttributeBrewingData.BrewingData data = getBrewingData();

        if (!data.isBrewing()) {
            SkyBlockPlayer player = (SkyBlockPlayer) p;

            if (ingredientItem != null && !ingredientItem.isNA() && !ingredientItem.isAir()) {
                player.addAndUpdateItem(ingredientItem);
            }

            for (SkyBlockItem potion : potionItems) {
                if (potion != null && !potion.isNA() && !potion.isAir()) {
                    player.addAndUpdateItem(potion);
                }
            }
        } else {
            // Save items to block from instance variables
            BlockAttributeBrewingData.BrewingData newData = new BlockAttributeBrewingData.BrewingData();

            if (ingredientItem != null && !ingredientItem.isNA() && !ingredientItem.isAir()) {
                newData.setIngredient(ingredientItem);
            }

            SkyBlockItem[] potions = newData.getPotionSlots();
            for (int i = 0; i < potionItems.length; i++) {
                if (potionItems[i] != null && !potionItems[i].isNA() && !potionItems[i].isAir()) {
                    potions[i] = potionItems[i];
                }
            }

            newData.setBrewingStartTime(data.getBrewingStartTime());
            newData.setBrewingDurationSeconds(data.getBrewingDurationSeconds());
            saveBrewingData(newData);
        }
    }

    @Override
    public int refreshRate() {
        return 10; // Refresh every 10 ticks (0.5 seconds)
    }

    @Override
    public boolean allowHotkeying() {
        return true;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        // Allow clicking on player inventory
    }
}
