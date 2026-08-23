package net.swofty.type.skyblockgeneric.gui.inventories.banker;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIPersonalVault extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Personal Vault", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointBankData.BankData data = PersonalBankService.data(player);
        for (int i = 0; i < 27; i++) {
            SkyBlockItem item = data.getPersonalVault()[i];
            layout.editable(i, (s, c) -> item == null || item.isNA() ? ItemStack.AIR.builder()
                : PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), item.getItemStack()), (_, _, _, _) -> {
            });
        }
        for (int i = 27; i < 36; i++)
            layout.slot(i, ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        Components.back(layout, 30, ctx);
        Components.close(layout, 31);
        layout.allowHotkey(true);
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointBankData.BankData data = PersonalBankService.data(player);
        SkyBlockItem[] items = new SkyBlockItem[27];
        for (int i = 0; i < items.length; i++) {
            SkyBlockItem item = new SkyBlockItem(ctx.inventory().getItemStack(i));
            items[i] = item.isNA() ? null : item;
        }
        data.setPersonalVault(items);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).setValue(data);
    }
}
