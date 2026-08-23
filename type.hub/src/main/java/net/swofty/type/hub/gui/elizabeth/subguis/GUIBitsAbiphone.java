package net.swofty.type.hub.gui.elizabeth.subguis;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.elizabeth.GUIBitsShop;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIBitsAbiphone extends HypixelInventoryGUI {

    public GUIBitsAbiphone() {
        super("Bits Shop - Abiphone", InventoryType.CHEST_4_ROW);
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getGoBackItem(31, new GUIBitsShop()));

        set(new GUIClickableItem(12) {
            final int price = 6450;
            final ItemType item = ItemType.ABIPHONE_CONTACTS_TRIO;

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (player.getBits() >= price) {
                    SkyBlockItem skyBlockItem = new SkyBlockItem(item);
                    ItemStack.Builder itemStack = new NonPlayerItemUpdater(skyBlockItem).getUpdatedItem();
                    SkyBlockItem finalItem = new SkyBlockItem(itemStack.build());
                    if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.PURCHASE_CONFIRMATION_BITS)) {
                        player.addAndUpdateItem(finalItem);
                        player.removeBits(price);
                    } else {
                        new GUIBitsConfirmBuy(finalItem, price).open(player);
                    }
                } else {
                    player.sendMessage("<c>You don't have enough Bits to buy that!");
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockItem skyBlockItem = new SkyBlockItem(item);
                ItemStack.Builder itemStack = new NonPlayerItemUpdater(skyBlockItem).getUpdatedItem();
                return ItemStacks.appendLore(itemStack, """
                        \s
                        <7>Cost
                        <b>{:,} Bits
                        <r>\s
                        <e>Click to trade!""", price);
            }
        });
        set(new GUIClickableItem(14) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIBitsAbicases().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.head("a3c153c391c34e2d328a60839e683a9f82ad3048299d8bc6a39e6f915cc5a", """
                        <5>Abicases
                        <7>Any expensive Abiphone needs some
                        <7>accessories!

                        <7>Get an Abicase! It keeps your
                        <7>accessory bag safe while you hold
                        <7>your Abiphone in your hands.

                        <d>Three brands to choose from!
                        <7>Only ONE Abicase will work at a time.
                        <e>Click to view Abicases!""");
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
