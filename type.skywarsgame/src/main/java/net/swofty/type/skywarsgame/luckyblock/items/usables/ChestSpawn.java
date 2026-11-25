package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.Inventories;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChestSpawn implements LuckyBlockConsumable {

    private static final Random RANDOM = new Random();

    @Override
    public String getId() {
        return "chest_spawn";
    }

    @Override
    public String getDisplayName() {
        return "Chest Spawn";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.CHEST, """
                <6><l>Chest Spawn</l>
                <7>Place a chest filled with
                <7>2-4 random lucky block items!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        Point targetBlock = player.getTargetBlockPosition(50);
        if (targetBlock == null) {
            player.sendMessage("<c>No block in range!");
            return;
        }

        Instance instance = player.getInstance();
        if (instance == null) return;

        Point chestPos = targetBlock.add(0, 1, 0);
        if (!instance.getBlock(chestPos).air()) {
            player.sendMessage("<c>Not enough space to place chest!");
            return;
        }

        instance.setBlock(chestPos, Block.CHEST);

        List<LuckyBlockItem> allItems = new ArrayList<>(LuckyBlockItemRegistry.getAllItems());
        int itemCount = 2 + RANDOM.nextInt(3);

        Inventory chestInventory = Inventories.of(InventoryType.CHEST_3_ROW, "Lucky Chest");
        for (int i = 0; i < itemCount; i++) {
            LuckyBlockItem randomItem = allItems.get(RANDOM.nextInt(allItems.size()));
            int slot = RANDOM.nextInt(27);
            while (chestInventory.getItemStack(slot) != ItemStack.AIR) {
                slot = RANDOM.nextInt(27);
            }
            chestInventory.setItemStack(slot, randomItem.createItemStack());
        }

        player.openInventory(chestInventory);
        player.sendMessage("<6>A lucky chest appears!");
    }
}
