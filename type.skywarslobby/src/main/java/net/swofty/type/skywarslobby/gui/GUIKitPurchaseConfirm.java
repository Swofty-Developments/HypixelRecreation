package net.swofty.type.skywarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.kit.SkywarsKit;

/**
 * Confirmation dialog for purchasing kits.
 */
public class GUIKitPurchaseConfirm extends HypixelInventoryGUI {
    private final SkywarsKit kit;
    private final String mode;
    private final int returnPage;

    public GUIKitPurchaseConfirm(SkywarsKit kit, String mode, int returnPage) {
        super("Are you sure?", InventoryType.CHEST_3_ROW);
        this.kit = kit;
        this.mode = mode;
        this.returnPage = returnPage;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();

        // Confirm button (slot 11)
        set(new GUIClickableItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.item(Material.GREEN_TERRACOTTA, 1, """
                        <a>Confirm
                        <7>Purchase {} for {:,} coins.""", kit.getName(), kit.getCost());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
                if (handler == null) return;

                long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();

                if (coins >= kit.getCost()) {
                    // Deduct coins
                    handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class)
                            .setValue(coins - kit.getCost());

                    // Unlock kit
                    DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = handler.get(
                            SkywarsDataHandler.Data.UNLOCKS,
                            DatapointSkywarsUnlocks.class
                    ).getValue();
                    unlocks.unlockKit(kit.getId());

                    player.sendMessage("<a>You purchased the <e>{} <a>kit!", kit.getName());

                    // Return to kit selector
                    new GUIKitSelector(mode, returnPage).open(player);
                } else {
                    player.sendMessage("<c>You don't have enough coins!");
                    new GUIKitSelector(mode, returnPage).open(player);
                }
            }
        });

        // Cancel button (slot 15)
        set(new GUIClickableItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.item(Material.RED_TERRACOTTA, 1, """
                        <c>Cancel
                        <7>Return to previous menu.""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitSelector(mode, returnPage).open(player);
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
