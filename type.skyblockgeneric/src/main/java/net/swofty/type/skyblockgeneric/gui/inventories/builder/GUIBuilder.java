package net.swofty.type.skyblockgeneric.gui.inventories.builder;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIBuilder extends HypixelInventoryGUI {
    public GUIBuilder() {
        super(Text.key("gui_builder.title"), InventoryType.CHEST_4_ROW);
    }
    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(new GUIClickableItem(10) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.openView(new GUIShopBuilderWoodworking());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.OAK_PLANKS, 1, """
                        <key:gui_builder.woodworking_button>
                        <key:gui_builder.woodworking_button.lore>""");
            }
        });
        set(new GUIClickableItem(12) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.openView(new GUIShopBuilderRocksBricks());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.STONE, 1, """
                        <key:gui_builder.rocks_bricks_button>
                        <key:gui_builder.rocks_bricks_button.lore.1>
                        <key:gui_builder.rocks_bricks_button.lore.2>""");
            }
        });
        set(new GUIClickableItem(14) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.openView(new GUIShopBuilderGreenThumb());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ROSE_BUSH, 1, """
                        <key:gui_builder.green_thumb_button>
                        <key:gui_builder.green_thumb_button.lore.1>
                        <key:gui_builder.green_thumb_button.lore.2>""");
            }
        });
        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.openView(new GUIShopBuilderVariety());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.head("3c2d8e8ec2737b599a48fc07ea58b806969e6021802019992dda32a653794df6", """
                        <key:gui_builder.variety_button>
                        <key:gui_builder.variety_button.lore.1>
                        <key:gui_builder.variety_button.lore.2>""");
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
