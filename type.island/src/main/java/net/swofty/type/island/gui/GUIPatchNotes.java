package net.swofty.type.island.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIPatchNotes extends HypixelInventoryGUI {
    public GUIPatchNotes() {
        super("Patch Notes", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);

        set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIJerry().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To Jerry the Assistant""");
            }
        });

        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<click:url:'https://discord.com/channels/830345347867476000/849739331278733332/1225968992909525056'><f>View Patch Notes <e><l>CLICK HERE");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BOOK, """
                        <a>SkyBlock v1.0.2
                        <7>6th April 2024

                        <e>Click to view!""");
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<click:url:'https://discord.com/channels/830345347867476000/849739331278733332/1226864370122887229'><f>View Patch Notes <e><l>CLICK HERE");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.STICK, """
                        <a>SkyBlock v1.0.3
                        <7>8th April 2024

                        <e>Click to view!""");
            }
        });

        set(new GUIClickableItem(14) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<click:url:'https://discord.com/channels/830345347867476000/849739331278733332/1227143736669114459'><f>View Patch Notes <e><l>CLICK HERE");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BLAZE_POWDER, """
                        <a>SkyBlock v1.1.0
                        <7>9th April 2024

                        <e>Click to view!""");
            }
        });

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<click:url:'https://discord.com/channels/830345347867476000/849739331278733332/1227909009336569906'><f>View Patch Notes <e><l>CLICK HERE");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.HOPPER, """
                        <a>SkyBlock v1.1.1
                        <7>11th April 2024

                        <e>Click to view!""");
            }
        });

        set(new GUIClickableItem(12) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<click:url:'https://discord.com/channels/830345347867476000/849739331278733332/1229007700302495765'><f>View Patch Notes <e><l>CLICK HERE");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.GOLD_INGOT, """
                        <a>SkyBlock v1.1.3
                        <7>15th April 2024

                        <e>Click to view!""");
            }
        });

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<click:url:'https://discord.com/channels/830345347867476000/849739331278733332/1230477957764612146'><f>View Patch Notes <e><l>CLICK HERE");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.DISPENSER, """
                        <a>SkyBlock v1.1.4
                        <7>18th April 2024

                        <e>Click to view!""");
            }
        });

        set(new GUIClickableItem(10) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<click:url:'https://discord.com/channels/830345347867476000/849739331278733332/1231214757114282065'><f>View Patch Notes <e><l>CLICK HERE");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.DIAMOND, """
                        <a>SkyBlock v1.1.5
                        <7>20th April 2024

                        <e>Click to view!""");
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
