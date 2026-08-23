package net.swofty.type.hub.gui.elizabeth.subguis;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.hub.gui.elizabeth.GUICityProjects;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIPreviousCityProjects extends HypixelInventoryGUI {

    private final int[] projectSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 15,
            28, 29, 30, 31, 32, 33, 34
    };

    private enum CityProjects {
        FARM_MERCHANTS_DWELLING(ItemStacks.item(Material.HAY_BLOCK, """
                <a>City project: <e>Farm Merchant's Dwelling
                <8>Released November 2020

                <7>Upgrade the farm merchant's
                <7>dwelling, adding new tools both to
                <7>build farms and harvest them quicker!

                <7>Bonuses:
                <7>▶ <b>4<7>: 1 day headstart
                <7>▶ <b>8<7>: 2 days headstart
                <7>▶ <b>16<7>: <6>5% discount <7>at farm merchant
                <7>▶ <b>24<7>: <6>10% discount <7>at farm merchant""")),
        BARTENDERS_BREWERY(ItemStacks.head("d672c57f4c7b9e962b45b55dd7bd7886880d7eef26db6c2cce03c8ff8c48", """
                <a>City project: <e>Bartender's Brewery
                <8>Released March 2021

                <7>Upgrade the Bartender's brewery,
                <7>offering new drinks and a new tier
                <7>of Zombie slayer.

                <7>Bonuses:
                <7>▶ <b>4<7>: <6>5% discount <7>at bartender
                <7>▶ <b>8<7>: <6>10% discount <7>at bartender
                <7>▶ <b>16<7>: <6>15% discount <7>at bartender
                <7>▶ <b>24<7>: <6>5% discount <7>at Maddox""")),
        BLACKSMITH_WORKSPACE(ItemStacks.item(Material.ANVIL, """
                <a>City project: <e>Blacksmith Workspace
                <8>Released October 2020

                <7>Add <a>3 <7>new reforges to the
                <7>Blacksmith and make his workspace
                <7>more comfortable.

                <7>Bonuses:
                <7>▶ <b>1<7>: 1 day headstart
                <7>▶ <b>2<7>: 3 days headstart
                <7>▶ <b>3<7>: 5 days headstart
                <7>▶ <b>4<7>: 1 week headstart""")),
        BUILDERS_HOUSE(ItemStacks.item(Material.BRICKS, """
                <a>City project: <e>Builder's House
                <8>Released October 2020

                <7>The Builder shop NPC will move from
                <7>a stall to its own house, with <e>tons <7>of
                <7>new blocks for sale without a daily
                <7>limit.

                <7>Bonuses:
                <7>▶ <b>1<7>: <6>5% discount <7>at builder
                <7>▶ <b>2<7>: <6>10% discount <7>at builder
                <7>▶ <b>3<7>: <6>15% discount <7>at builder
                <7>▶ <b>4<7>: <6>30% discount <7>at builder""")),
        WEAPONSMITH_WORKSHOP(ItemStacks.item(Material.BOW, """
                <a>City project: <e>Weaponsmith Workshop
                <8>Released September 2021

                <7>Upgrade the Weaponsmith Workshop,
                <7>offering brand new arrow items and
                <7>starter gear for beginner players!

                <7>Bonuses:
                <7>▶ <b>4<7>: 1 day headstart
                <7>▶ <b>8<7>: 2 days headstart
                <7>▶ <b>16<7>: <6>5% discount <7>at Jax
                <7>▶ <b>24<7>: <6>10% discount <7>at Jax""")),
        REPAIR_WIZARD_PORTAL(ItemStacks.item(Material.END_PORTAL_FRAME, """
                <a>City project: <e>Repair Wizard Portal
                <8>Released June 2023

                <7>Help out <9>Barry <7>with the last efforts
                <7>to open the <d>Wizard Portal<7>.

                <7>Bonuses:""")),
        PET_CARE_EXPANSION(ItemStacks.item(Material.EGG, """
                <a>City project: <e>Pet Care Expansion
                <8>Released December 2023

                <7>Introduces <d>Pet Care<7>, adding ways to
                <7>train and level up your <a>pets<7>!

                <7>Bonuses:""")),
        ;

        private final ItemStack.Builder item;

        CityProjects(ItemStack.Builder item) {
            this.item = item;
        }
    }

    public GUIPreviousCityProjects() {
        super("Previous City Projects", InventoryType.CHEST_5_ROW);
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        border(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getGoBackItem(40, new GUICityProjects()));

        CityProjects[] cityProjects = CityProjects.values();
        int index = 0;
        for (int slot : projectSlots) {
            if (index < cityProjects.length) {
                CityProjects cityProject = cityProjects[index];
                set(new GUIItem(slot) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                        return cityProject.item;
                    }
                });
                index++;
            }
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
