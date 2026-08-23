package net.swofty.type.generic.gui.inventory.item;

import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.Arrays;
import java.util.List;

public abstract class GUIClickableItem extends GUIItem {
    public GUIClickableItem(int slot) {
        super(slot);
    }

    public abstract void run(InventoryPreClickEvent e, HypixelPlayer player);

    public void runPost(InventoryClickEvent e, HypixelPlayer player) {}

    public static GUIClickableItem getCloseItem(int slot) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.named(Material.BARRIER, "<c>Close");
            }
        };
    }

    public static GUIClickableItem getGoBackItem(int slot, HypixelInventoryGUI gui) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                gui.open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To {}""", gui.getTitle());
            }
        };
    }

    static GUIClickableItem createGUIOpenerItem(HypixelInventoryGUI gui,
                                                String name, int slot,
                                                Material type, String... lore) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> loreLines = Arrays.stream(lore).map(Text::of).toList();
                return ItemStacks.item(type, 1, Text.of(name), loreLines);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (gui == null) return;
                gui.open(player);
            }
        };
    }
}