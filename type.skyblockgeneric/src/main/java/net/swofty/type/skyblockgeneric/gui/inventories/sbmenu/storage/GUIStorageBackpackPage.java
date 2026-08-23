package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.storage;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBackpacks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.BackpackComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIStorageBackpackPage extends StatelessView {
    private final int page;
    private final int slots;
    private final SkyBlockItem item;
    private final Text title;
    private final InventoryType inventoryType;

    public GUIStorageBackpackPage(int page, SkyBlockItem item) {
        this.page = page;
        this.item = item;
        this.slots = item.getComponent(BackpackComponent.class).getRows() * 9;
        this.title = Text.of("{} (Slot #{})", item.getDisplayNameText(), page);
        this.inventoryType = MathUtility.getFromSize(9 + slots);
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, inventoryType);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointBackpacks.PlayerBackpacks data = player.getSkyblockDataHandler()
                .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue();

        // Fill top row with glass panes
        for (int i = 0; i < 9; i++) {
            layout.slot(i, ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        }

        Components.close(layout, 0);

        layout.slot(1, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                        Text.key("gui_sbmenu.storage.backpack_page.go_back"),
                        Text.keyLines("gui_sbmenu.storage.backpack_page.go_back.lore")),
                (click, c) -> {
                    saveItems((SkyBlockPlayer) c.player(), c);
                    c.player().openView(new GUIStorage());
                });

        if (page != data.getHighestBackpackSlot()) {
            layout.slot(8, (s, c) -> ItemStacks.head("1ceb50d0d79b9fb790a7392660bc296b7ad2f856c5cbe1c566d99cfec191e668",
                            Text.key("gui_sbmenu.storage.backpack_page.last_page"), List.of()),
                    (click, c) -> {
                        saveItems((SkyBlockPlayer) c.player(), c);
                        SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                        DatapointBackpacks.PlayerBackpacks playerData = p.getSkyblockDataHandler()
                                .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue();
                        p.openView(new GUIStorageBackpackPage(playerData.getHighestBackpackSlot(),
                                new SkyBlockItem(playerData.getBackpacks().get(playerData.getHighestBackpackSlot()))));
                    });

            if (data.getBackpacks().containsKey(page + 1)) {
                layout.slot(7, (s, c) -> ItemStacks.head("848ca732a6e35dafd15e795ebc10efedd9ef58ff2df9b17af6e3d807bdc0708b",
                                Text.key("gui_sbmenu.storage.backpack_page.next_page"), List.of()),
                        (click, c) -> {
                            saveItems((SkyBlockPlayer) c.player(), c);
                            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                            DatapointBackpacks.PlayerBackpacks playerData = p.getSkyblockDataHandler()
                                    .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue();
                            p.openView(new GUIStorageBackpackPage(page + 1, new SkyBlockItem(playerData.getBackpacks().get(page + 1))));
                        });
            }
        }

        if (page != data.getLowestBackpackSlot()) {
            layout.slot(5, (s, c) -> ItemStacks.head("8af22a97292de001079a5d98a0ae3a82c427172eabc370ed6d4a31c7e3a0024f",
                            Text.key("gui_sbmenu.storage.backpack_page.first_page"), List.of()),
                    (click, c) -> {
                        saveItems((SkyBlockPlayer) c.player(), c);
                        SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                        DatapointBackpacks.PlayerBackpacks playerData = p.getSkyblockDataHandler()
                                .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue();
                        p.openView(new GUIStorageBackpackPage(playerData.getLowestBackpackSlot(),
                                new SkyBlockItem(playerData.getBackpacks().get(playerData.getLowestBackpackSlot()))));
                    });

            if (data.getBackpacks().containsKey(page - 1)) {
                layout.slot(6, (s, c) -> ItemStacks.head("9c042597eda9f061794fe11dacf78926d247f9eea8ddef39dfbe6022989b8395",
                                Text.key("gui_sbmenu.storage.backpack_page.previous_page"), List.of()),
                        (click, c) -> {
                            saveItems((SkyBlockPlayer) c.player(), c);
                            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                            DatapointBackpacks.PlayerBackpacks playerData = p.getSkyblockDataHandler()
                                    .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue();
                            p.openView(new GUIStorageBackpackPage(page - 1, new SkyBlockItem(playerData.getBackpacks().get(page - 1))));
                        });
            }
        }

        // Backpack content slots
        var backpackItems = item.getAttributeHandler().getBackpackData().items();
        for (int itemIndex = 0; itemIndex < slots; itemIndex++) {
            int slot = itemIndex + 9;
            var itemData = itemIndex < backpackItems.size() ? backpackItems.get(itemIndex) : null;

            layout.editable(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                if (itemData == null || new SkyBlockItem(itemData).isNA())
                    return ItemStack.builder(Material.AIR);
                return PlayerItemUpdater.playerUpdate(p, new SkyBlockItem(itemData).getItemStack());
            }, (slotNum, oldItem, newItem, s) -> {
            });
        }
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        saveItems((SkyBlockPlayer) ctx.player(), ctx);
    }

    private void saveItems(SkyBlockPlayer player, ViewContext ctx) {
        item.getAttributeHandler().getBackpackData().items().clear();

        for (int i = 9; i < slots + 9; i++) {
            item.getAttributeHandler().getBackpackData().items().add(new SkyBlockItem(ctx.inventory().getItemStack(i)).toUnderstandable());
        }

        player.getSkyblockDataHandler()
                .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class)
                .getValue().getBackpacks().put(page, item.toUnderstandable());
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        return true;
    }
}
