package net.swofty.type.dwarvenmines.gui.fragilis;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIHandyBlockGuide extends HypixelInventoryGUI {

    public GUIHandyBlockGuide() {
        super("Handy Block Guide", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BOOK, """
                        <2>Handy Block Guide
                        <7>View a list of important mineable blocks:
                        <8> - <6>Ores
                        <8> - <9>Blocks
                        <8> - <a>Dwarven Metals
                        <8> - <d>Gemstones""");
            }
        });
        set(new GUIClickableItem(19) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIOres().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GOLD_ORE, """
                        <6>Ores
                        <8>Block Classification

                        <7>These blocks are affected by:\s
                        <8> - <6>☘ Ore Fortune
                        <8> - <6>☘ Mining Fortune
                        <8> - <e>▚ Mining Spread

                        <e>Click to view!""");
            }
        });
        set(new GUIClickableItem(21) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIBlocks().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.COBBLESTONE, """
                        <9>Blocks
                        <8>Block Classification

                        <7>These blocks are affected by:\s
                        <8> - <6>☘ Block Fortune
                        <8> - <6>☘ Mining Fortune
                        <8> - <e>▚ Mining Spread

                        <e>Click to view!""");
            }
        });
        set(new GUIClickableItem(23) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIDwarvenMetals().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.PRISMARINE, """
                        <a>Dwarven Metals
                        <8>Block Classification

                        <7>These blocks are affected by:\s
                        <8> - <6>☘ Dwarven Metal Fortune
                        <8> - <6>☘ Mining Fortune
                        <8> - <e>▚ Mining Spread

                        <e>Click to view!""");
            }
        });
        set(new GUIClickableItem(25) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGemstones().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.RED_STAINED_GLASS, """
                        <d>Gemstones
                        <8>Block Classification

                        <7>These blocks are affected by:\s
                        <8> - <6>☘ Gemstone Fortune
                        <8> - <6>☘ Mining Fortune
                        <8> - <e>▚ Gemstone Spread
                        <8> - <5>✧ Pristine

                        <e>Click to view!""");
            }
        });
        set(GUIClickableItem.getCloseItem(40));
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
