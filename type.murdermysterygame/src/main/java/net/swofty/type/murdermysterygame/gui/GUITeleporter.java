package net.swofty.type.murdermysterygame.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUITeleporter extends HypixelInventoryGUI {

    private final Game game;

    public GUITeleporter(Game game) {
        super("Teleporter", InventoryType.CHEST_4_ROW);
        this.game = game;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        int slot = 10;
        for (MurderMysteryPlayer target : game.getPlayers()) {
            if (target.isEliminated()) continue;
            if (slot > 25) break;

            final MurderMysteryPlayer targetPlayer = target;
            set(new GUIClickableItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.head(targetPlayer.getPlayerSkin(), """
                            <a>{}
                            <7>Status: <a>Alive

                            <e>Click to teleport!""", targetPlayer.getUsername());
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    player.closeInventory();
                    player.teleport(targetPlayer.getPosition());
                    player.sendMessage("<a>Teleported to {}", targetPlayer.getUsername());
                }
            });
            slot++;
        }

        set(GUIClickableItem.getCloseItem(31));
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
