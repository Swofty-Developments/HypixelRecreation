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

public class GUIGuests extends HypixelInventoryGUI {
    public GUIGuests() {
        super ("Jerry's Guide to Guesting", InventoryType.CHEST_4_ROW);
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

        set(new GUIClickableItem(10) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {}

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.FEATHER, """
                        <a>Visit player islands
                        <7>You can get Guest on other islands
                        <7>using <a>/visit \\<player>

                        <7>Guests <c>Can't interact with the
                        <7>world, but it's always fun to see
                        <7>what others are up to!""");
            }
        });

        set(new GUIClickableItem(12) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                player.sendMessage("<e>Visit our web store: <6>https://store.hypixel.net");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.SHORT_GRASS, """
                        <a>Guests limit
                        <7>You can only host a limited
                        <7>number of <a>guests <7>on your
                        <7>island concurrently.

                        <7>The limit depends on your rank:
                        <7>- <c>[<f>YOUTUBE<c>] <f>= <a>15
                        <7>- <6>[MVP<c>++<6>] <f>= <a>7
                        <7>- <b>[MVP] <f>= <a>5
                        <7>- <a>[VIP] <f>= <a>3
                        <7>- Default <f>= <a>1

                        <7>Limit on the island: <a>1 guests

                        <b>Co-op profile use the partner
                        <b>with the highest rank!

                        <e>purchase rank at store.hypixel.net!""");
            }
        });

        set(new GUIClickableItem(14) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {}

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.OAK_FENCE, """
                        <a>Access Permissions
                        <7>You may edit who is able to
                        <7>guest on your island in your
                        <e>Island Settings<7>.

                        <7>Use <c>/ignore add \\<username> to
                        <7>prevent a specific player from
                        <7>joining.

                        <e>Click to open island settings!""");
            }
        });

        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.REPEATER, """
                        <a>Moderation
                        <7>Manage online guests using the
                        <e>Guests Management <7>menu.

                        <7>Alternatively, use the <c>/sbkick &
                        <a>/sbkickall commands or <a>clicking
                        <a>on them<7>.""");
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
