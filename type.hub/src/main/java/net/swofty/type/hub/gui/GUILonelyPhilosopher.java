package net.swofty.type.hub.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUILonelyPhilosopher extends HypixelInventoryGUI {

    public GUILonelyPhilosopher() {
        super("Lonely Philosopher", InventoryType.CHEST_6_ROW);
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                double coins = player.getCoins();
                double price = 150000;
                if (coins < price) {
                    player.sendMessage("<c>You don't have enough coins!");
                    return;
                }
                player.addAndUpdateItem(ItemType.HUB_CASTLE_TRAVEL_SCROLL);
                player.playSuccessSound();
                player.removeCoins(price);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.MAP, """
                        <5>Travel Scroll To Hub Castle
                        <7>Consume this item to add its
                        <7>destination to your fast travel
                        <7>options.

                        <7>Requires <b>MVP<c>+ <7>to consume!

                        <7>Island: <a>Hub
                        <7>Teleport: <e>Castle

                        <5><l>EPIC TRAVEL SCROLL</l>

                        <7>Cost
                        <6>150,000 Coins""");
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }

}
