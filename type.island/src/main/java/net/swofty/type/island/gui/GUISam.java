package net.swofty.type.island.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.warps.TravelScrollIslands;

public class GUISam extends HypixelInventoryGUI {
    public GUISam() {
        super("Sam the Garden Assistant", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent event) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(31));

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                player.closeInventory();
                player.sendTo(ServerType.SKYBLOCK_GARDEN);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.getStackHead(
                    "§aWarp to Garden",
                    TravelScrollIslands.GARDEN.getTexture(),
                    1,
                    "§7Teleports you to your",
                    "§7§aGarden§7, where you can grow",
                    "§7and harvest §ecrops §7and sell",
                    "§7them to §bcustomers§7.",
                    "",
                    "§eClick to warp!"
                );
            }
        });

        set(new GUIClickableItem(35) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                player.closeInventory();
                player.sendMessage("§aI have given you an egg, place this where you would like me to move to!");
                ((SkyBlockPlayer) player).addAndUpdateItem(ItemType.MOVE_SAM);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.createNamedItemStack(Material.BEDROCK, "§aMove Sam");
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent event, CloseReason reason) {
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent event) {
    }
}
