package net.swofty.type.skywarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;

/**
 * Main Kits & Perks menu for SkyWars.
 * Allows players to access kit selectors and perk configuration for different game modes.
 */
public class GUIKitsPerks extends HypixelInventoryGUI {

    public GUIKitsPerks() {
        super("Kits & Perks", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) return;

        DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = handler.get(
                SkywarsDataHandler.Data.UNLOCKS,
                DatapointSkywarsUnlocks.class
        ).getValue();

        long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();

        // ===== KITS ROW =====

        // Mini Kits (slot 10) - NOT IMPLEMENTED
        set(new GUIClickableItem(10) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.WOODEN_SWORD, 1, """
                        <a>Mini Kits
                        <7>Selection of unique kits for Mini
                        <7>games.

                        <c>Not implemented yet!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.sendMessage("<c>Mini mode kits are not implemented yet!");
            }
        });

        // Normal Kits (slot 12)
        set(new GUIClickableItem(12) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.STONE_SWORD, 1, """
                        <a>Normal Kits
                        <7>Selection of unique kits for Normal
                        <7>games.

                        <e>Click to browse!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitSelector("NORMAL").open(player);
            }
        });

        // Insane Kits (slot 14)
        set(new GUIClickableItem(14) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.IRON_SWORD, 1, """
                        <a>Insane Kits
                        <7>Selection of unique kits for Insane
                        <7>games.

                        <e>Click to browse!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitSelector("INSANE").open(player);
            }
        });

        // Mega Kits (slot 16) - NOT IMPLEMENTED
        set(new GUIClickableItem(16) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND_SWORD, 1, """
                        <a>Mega Kits
                        <7>Selection of unique kits for Mega
                        <7>Mode.

                        <c>Not implemented yet!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.sendMessage("<c>Mega mode kits are not implemented yet!");
            }
        });

        // ===== PERKS ROW =====

        // Mini Perks (slot 19) - NOT IMPLEMENTED
        set(new GUIClickableItem(19) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.CAULDRON, 1, """
                        <a>Select Mini Perks
                        <7>Selection of unique perks for Mini
                        <7>games.

                        <c>Not implemented yet!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.sendMessage("<c>Mini mode perks are not implemented yet!");
            }
        });

        // Normal Perks (slot 21)
        set(new GUIClickableItem(21) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<String> selectedPerks = unlocks.getSelectedPerksForMode("NORMAL");
                boolean hasEmptySlot = selectedPerks.isEmpty();

                if (hasEmptySlot) {
                    return ItemStacks.item(Material.CAULDRON, 1, """
                            <a>Select Normal Perks
                            <7>Selection of unique perks for Normal
                            <7>games.

                            <c>You have 1 empty Perk Slot in this
                            <c>mode!

                            <e>Click to browse!""");
                } else {
                    return ItemStacks.item(Material.CAULDRON, 1, """
                            <a>Select Normal Perks
                            <7>Selection of unique perks for Normal
                            <7>games.

                            <e>Click to browse!""");
                }
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUISelectNormalPerks().open(player);
            }
        });

        // Insane Perks (slot 23)
        set(new GUIClickableItem(23) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.CAULDRON, 1, """
                        <a>Toggle Insane Perks
                        <7>All perks you own in this mode are
                        <7>enabled simultaneously.

                        <7>You can choose to disable any you
                        <7>don't want to have active.

                        <e>Click to browse!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIToggleInsanePerks().open(player);
            }
        });

        // Mega Perks (slot 25) - NOT IMPLEMENTED
        set(new GUIClickableItem(25) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.CAULDRON, 1, """
                        <a>Select Mega Perks
                        <7>Selection of unique perks for Mega
                        <7>Mode.

                        <c>Not implemented yet!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.sendMessage("<c>Mega mode perks are not implemented yet!");
            }
        });

        // ===== BOTTOM ROW =====

        // Go Back (slot 39)
        set(new GUIClickableItem(39) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, 1, """
                        <a>Go Back
                        <7>To SkyWars Menu""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUISkyWarsMenu().open(player);
            }
        });

        // Total Coins display (slot 40)
        set(new GUIItem(40) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.EMERALD, 1, """
                        <7>Total Coins: <6>{:,}
                        <6>https://store.hypixel.net""", coins);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
