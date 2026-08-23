package net.swofty.type.hub.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUIAccessoryBag;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIMaxwell extends HypixelInventoryGUI {

    public GUIMaxwell() {
        super("Accessory Bag Thaumaturgy", InventoryType.CHEST_6_ROW);
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIClickableItem(47) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.openView(new GUIAccessoryBag());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.head("961a918c0c49ba8d053e522cb91abc74689367b4d8aa06bfc1ba9154730985ff", """
                        <a>Accessory Bag Shortcut
                        <7>Quickly access your accessory bag
                        <7>from right here!

                        <e>Click to open!""");
            }
        });
        set(new GUIItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                int mythic = player.getAccessoryBag().getUniqueAccessories(Rarity.MYTHIC).size();
                int legendary = player.getAccessoryBag().getUniqueAccessories(Rarity.LEGENDARY).size();
                int epic = player.getAccessoryBag().getUniqueAccessories(Rarity.EPIC).size();
                int rare = player.getAccessoryBag().getUniqueAccessories(Rarity.RARE).size();
                int uncommon = player.getAccessoryBag().getUniqueAccessories(Rarity.UNCOMMON).size();
                int common = player.getAccessoryBag().getUniqueAccessories(Rarity.COMMON).size();
                int special = player.getAccessoryBag().getUniqueAccessories(Rarity.SPECIAL).size();
                int verySpecial = player.getAccessoryBag().getUniqueAccessories(Rarity.VERY_SPECIAL).size();

                return ItemStacks.item(Material.FILLED_MAP, """
                        <a>Accessories Breakdown
                        <8>From your bag

                        <6>22 MP <7>x <d>{} Accs. <7>= <6>{} MP
                        <6>16 MP <7>x <6>{} Accs. <7>= <6>{} MP
                        <6>12 MP <7>x <5>{} Accs. <7>= <6>{} MP
                        <6>8 MP <7>x <9>{} Accs. <7>= <6>{} MP
                        <6>5 MP <7>x <a>{} Accs. <7>= <6>{} MP
                        <6>3 MP <7>x <f>{} Accs. <7>= <6>{} MP
                        <6>3 MP <7>x <c>{} Accs. <7>= <6>{} MP
                        <6>5 MP <7>x <c>{} Accs. <7>= <6>{} MP

                        <7>Total: <6>{:,} Magical Power""",
                        mythic, mythic * 22,
                        legendary, legendary * 16,
                        epic, epic * 12,
                        rare, rare * 8,
                        uncommon, uncommon * 5,
                        common, common * 3,
                        special, special * 3,
                        verySpecial, verySpecial * 5,
                        player.getMagicalPower());
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
        e.setCancelled(true);
    }
}
