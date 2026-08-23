package net.swofty.type.hub.gui.elizabeth.subguis;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.hub.gui.elizabeth.CommunityShopItem;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Objects;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIBitsAbicases extends HypixelInventoryGUI {

    public GUIBitsAbicases() {
        super("Bits Shop - Abicases", InventoryType.CHEST_4_ROW);
    }

    private final int[] displaySlots = {
            11,     13,     15
    };

    @Getter
    private enum SubCategorys {
        SUMSUNG_ABICASES("Sumsung© Abicases", new GUIBitsAbicases(), ItemStacks.head("36a10ee2155fc0134d9392000a9eb9ebcba8526eff3893e54434e825e558fb55", """
                <f>Sumsung© Abicases
                <7>Sumsung focuses on the
                <7>technology.

                <7>Upgrade your Abiphone remotely
                <7>through cloud-based blockchain
                <7>agile immutable dev-ops fuzzy
                <7>cases.

                <e>Click to view models!"""),
                List.of(
                        new CommunityShopItem(ItemType.SUMSUNG_G3_ABICASE, 15000, 1),
                        new CommunityShopItem(ItemType.SUMSUNG_GG_ABICASE, 25000, 1)
                )),
        REZAR_ABICASES("Rezar Abicase", new GUIBitsAbicases(), ItemStacks.head("b2128f48d997186563fbc5b47a88c0d0aac92fa2c285cd1fae420c34fa8f2010", """
                <a>Rezar® Abicase
                <7>Play hard, play fair and do it all in
                <7>green.

                <7>Perfect for those who have time
                <7>to grind but no time to call their
                <7>close ones.

                <e>Click to view THE model!"""),
                List.of(
                        new CommunityShopItem(ItemType.REZAR_ABICASE, 26000, 1)
                )),
        BLUE_ABICASES("Blue™ Abicases", new GUIBitsAbicases(), ItemStacks.head("a3c153c391c34e2d328a60839e683a9f82ad3048299d8bc6a39e6f915cc5a", """
                <9>Blue™ Abicases
                <7>Blue Abicases are not all blue.
                <7>Their color reflects your
                <7>personality, your life and your
                <7>legacy.

                <7>Think of it this way: Is your
                <7>personality just a recolor of last
                <7>year's?

                <7>Blue™ says... well <o>maybe</o>!

                <e>Click to pick a color!"""),
                List.of(
                        new CommunityShopItem(ItemType.BLUE_BUT_RED_ABICASE, 17000, 1),
                        new CommunityShopItem(ItemType.ACTUALLY_BLUE_ABICASE, 17000, 1),
                        new CommunityShopItem(ItemType.BLUE_BUT_GREEN_ABICASE, 17000, 1),
                        new CommunityShopItem(ItemType.BLUE_BUT_YELLOW_ABICASE, 17000, 1),
                        new CommunityShopItem(ItemType.LIGHTER_BLUE_ABICASE, 17000, 1)
                )),
        ;

        private final String guiName;
        private final HypixelInventoryGUI previousGUI;
        private final ItemStack.Builder item;
        private final List<CommunityShopItem> shopItems;

        SubCategorys(String guiName, HypixelInventoryGUI previousGUI, ItemStack.Builder item, List<CommunityShopItem> shopItems) {
            this.guiName = guiName;
            this.previousGUI = previousGUI;
            this.item = item;
            this.shopItems = shopItems;
        }
    }


    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getGoBackItem(31, new GUIBitsAbiphone()));

        SubCategorys[] allSubCategorys = SubCategorys.values();
        int indexSubCategorys = 0;
        for (int slot : displaySlots) {
            SubCategorys subCategorys = allSubCategorys[indexSubCategorys];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    new GUIBitsSubCategorys(subCategorys.getShopItems(), subCategorys.getGuiName(), subCategorys.getPreviousGUI()).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    ItemStack.Builder itemstack = subCategorys.item;
                    List<Component> lore = itemstack.build().get(DataComponents.LORE);
                    String last = lore.isEmpty() ? "" : StringUtility.getTextFromComponent(lore.getLast());
                    if (!Objects.equals(last, "Click to browse!")) {
                        itemstack = ItemStacks.appendLore(itemstack, """
                                \s
                                <e>Click to browse!""");
                    }
                    return itemstack;
                }
            });
            indexSubCategorys++;
        }

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
