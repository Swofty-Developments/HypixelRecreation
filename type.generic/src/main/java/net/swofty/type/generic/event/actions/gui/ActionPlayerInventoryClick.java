package net.swofty.type.generic.event.actions.gui;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

public class ActionPlayerInventoryClick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(InventoryPreClickEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        try {
            handle(event, player);
        } catch (Exception e) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage("<c>Something went wrong while handling your click!");
            Logger.error(e);
        }
    }

    public void handle(InventoryPreClickEvent event, final HypixelPlayer player) {
        ItemStack clickedItem = event.getClickedItem();
        ItemStack cursorItem = player.getInventory().getCursorItem();

        if (event.getSlot() == 45 && event.getInventory() instanceof PlayerInventory) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory() instanceof PlayerInventory) {
            Component displayNameCursor = cursorItem.get(DataComponents.CUSTOM_NAME);
            Component displayNameClicked = clickedItem.get(DataComponents.CUSTOM_NAME);
            if ((displayNameCursor != null && StringUtility.getTextFromComponent(displayNameCursor).contains("Switch your held"))
                || (displayNameClicked != null && StringUtility.getTextFromComponent(displayNameClicked).contains("Switch your held"))) {
                event.setCancelled(true);
                return;
            }
        }

        if (HypixelInventoryGUI.GUI_MAP.containsKey(player.getUuid())) {
            HypixelInventoryGUI gui = HypixelInventoryGUI.GUI_MAP.get(player.getUuid());
            if (gui == null) return;

            if (event.getClick() instanceof Click.Double) {
                event.setCancelled(true);
                return;
            }

            if (!gui.allowHotkeying() && isHotKey(event)) {
                event.setCancelled(true);
                return;
            }

            if (event.getInventory() instanceof PlayerInventory) {
                gui.onBottomClick(event);
            } else {
                int slot = event.getSlot();
                GUIItem item = gui.get(slot);

                if (item == null) {
                    return;
                }

                if (!item.canPickup()) {
                    event.setCancelled(true);
                } else if (!gui.allowHotkeying() && isHotKey(event)) {
                    event.setCancelled(true);
                    return;
                }

                if (item instanceof GUIClickableItem clickable) {
                    clickable.run(event, player);
                    if (!(cursorItem.material() == Material.AIR) && player.getOpenInventory() != event.getInventory()
                        && player.getOpenInventory() != null && !(event.getClick() instanceof Click.HotbarSwap)) {
                        player.getInventory().addItemStack(cursorItem);
                    }
                }

                if (item instanceof GUIQueryItem query) {
                    gui.onClose(new InventoryCloseEvent(
                        player.getOpenInventory(),
                        player,
                        false
                    ), HypixelInventoryGUI.CloseReason.SIGN_OPENED);

                    new HypixelSignGUI(player).open(query.lines()).thenAccept(string -> {
                        HypixelInventoryGUI nextGui = query.onQueryFinish(string, player);
                        if (nextGui != null && string != null)
                            nextGui.open(player);
                    });
                }
            }
        }
    }

    public boolean isHotKey(InventoryPreClickEvent inventoryClick) {
        return inventoryClick.getClick() instanceof Click.Drag ||
            inventoryClick.getClick() instanceof Click.HotbarSwap ||
            inventoryClick.getClick() instanceof Click.LeftShift ||
            inventoryClick.getClick() instanceof Click.RightShift;
    }
}
