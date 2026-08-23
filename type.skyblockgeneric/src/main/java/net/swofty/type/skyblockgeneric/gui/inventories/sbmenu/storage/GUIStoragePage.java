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
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStorage;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIStoragePage extends StatelessView {
    private final int page;

    public GUIStoragePage(int page) {
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText(
                (_, ctx) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
                    int highestPage = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class)
                            .getValue().getHighestPage();
                    return Text.key("gui_sbmenu.storage.page.title", page, highestPage);
                },
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int highestPage = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class)
                .getValue().getHighestPage();

        // Fill top row with glass panes
        for (int i = 0; i < 9; i++) {
            layout.slot(i, ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        }

        Components.close(layout, 0);

        layout.slot(1, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                        Text.key("gui_sbmenu.storage.page.go_back"),
                        Text.keyLines("gui_sbmenu.storage.page.go_back.lore")),
                (_, c) -> c.player().openView(new GUIStorage()));

        if (page != highestPage) {
            layout.slot(8, (s, c) -> ItemStacks.head("1ceb50d0d79b9fb790a7392660bc296b7ad2f856c5cbe1c566d99cfec191e668",
                            Text.key("gui_sbmenu.storage.page.last_page"), List.of()),
                    (click, c) -> {
                        saveItems((SkyBlockPlayer) c.player(), c);
                        c.player().openView(new GUIStoragePage(highestPage));
                    });

            layout.slot(7, (s, c) -> ItemStacks.head("848ca732a6e35dafd15e795ebc10efedd9ef58ff2df9b17af6e3d807bdc0708b",
                            Text.key("gui_sbmenu.storage.page.next_page"), List.of()),
                    (click, c) -> {
                        saveItems((SkyBlockPlayer) c.player(), c);
                        c.player().openView(new GUIStoragePage(page + 1));
                    });
        }

        if (page != 1) {
            layout.slot(5, (s, c) -> ItemStacks.head("8af22a97292de001079a5d98a0ae3a82c427172eabc370ed6d4a31c7e3a0024f",
                            Text.key("gui_sbmenu.storage.page.first_page"), List.of()),
                    (click, c) -> {
                        saveItems((SkyBlockPlayer) c.player(), c);
                        c.player().openView(new GUIStoragePage(1));
                    });

            layout.slot(6, (s, c) -> ItemStacks.head("9c042597eda9f061794fe11dacf78926d247f9eea8ddef39dfbe6022989b8395",
                            Text.key("gui_sbmenu.storage.page.previous_page"), List.of()),
                    (click, c) -> {
                        saveItems((SkyBlockPlayer) c.player(), c);
                        c.player().openView(new GUIStoragePage(page - 1));
                    });
        }

        DatapointStorage.PlayerStorage storage = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class).getValue();

        // Storage slots (9-53)
        SkyBlockItem[] items = storage.getPage(page).getItems();
        for (int i = 0; i < 45; i++) {
            int slot = i + 9;
            SkyBlockItem item = items[i];

            layout.editable(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                if (item == null || item.isNA())
                    return ItemStack.builder(Material.AIR);
                return PlayerItemUpdater.playerUpdate(p, item.getItemStack());
            }, (_, _, _, _) -> {
            });
        }

        layout.allowHotkey(true);
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        saveItems((SkyBlockPlayer) ctx.player(), ctx);
    }

    private void saveItems(SkyBlockPlayer player, ViewContext ctx) {
        DatapointStorage.PlayerStorage storage = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class).getValue();

        SkyBlockItem[] items = new SkyBlockItem[45];
        for (int i = 9; i < 54; i++) {
            items[i - 9] = new SkyBlockItem(ctx.inventory().getItemStack(i));
        }

        storage.setItems(page, items);
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        return true;
    }
}
