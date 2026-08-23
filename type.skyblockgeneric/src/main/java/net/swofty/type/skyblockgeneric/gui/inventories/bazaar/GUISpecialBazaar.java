package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.time.Duration;

public class GUISpecialBazaar extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Special Bazaar", InventoryType.CHEST_3_ROW);
    }

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.slots(Layouts.border(0, 44), (_, _) -> Components.asFiller(Material.YELLOW_STAINED_GLASS_PANE));
        layout.slots(Layouts.rectangle(10, 35));
        layout.autoUpdating(20, (_, _) -> ItemStacks.item(
                System.currentTimeMillis() % 2 == 0 ? Material.IRON_CHESTPLATE : Material.OAK_WOOD, 1, """
                        <e>Special Bazaar Rules
                        <7>Your profile's mode prevents you
                        <7>from using the bazaar

                        <7>Mode: Ironman

                        <a>EXCEPT: You may BUY booster cookies!"""), Duration.ofSeconds(1));
        layout.slot(22, new NonPlayerItemUpdater(new SkyBlockItem(ItemType.BOOSTER_COOKIE)).getUpdatedItem(), (_, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) context.player();
            cookie(player);
        });
        layout.slot(24, ItemStacks.item(Material.COOKIE, """
                <a>Buy <e>half a dozen <a>cookies!
                <8>Booster Cookie

                <7>Amount: <a>6<7>x

                <7>Per unit: <e>0
                <7>Price: <e>0

                <e>Click to buy now!"""), (defaultStateClickContext, viewContext) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) viewContext.player();
            cookie(player);
        });
        Components.close(layout, 40);
    }

    private void cookie(SkyBlockPlayer player) {
        player.getBazaarConnector().getItemStatistics(ItemType.BOOSTER_COOKIE)
                .thenAccept(stats -> {
                    if (stats.bestAsk() <= 0) {
                        player.sendMessage(Text.key("gui_bazaar.item.buy_no_offers_message"));
                        return;
                    }

                    int maxSpace = player.maxItemFit(ItemType.BOOSTER_COOKIE);
                    if (maxSpace <= 0) {
                        player.sendMessage(Text.key("gui_bazaar.item.buy_inventory_full"));
                        return;
                    }

                    double priceWithFee = stats.bestAsk() * 1.04;

                    if (priceWithFee > player.getCoins()) {
                        player.sendMessage(Text.key("gui_bazaar.item.buy_need_coins", FORMATTER.format(priceWithFee)));
                        return;
                    }

                    player.getBazaarConnector().instantBuy(ItemType.BOOSTER_COOKIE, 1)
                            .thenAccept(result -> {
                                player.sendMessage(Text.key("gui_bazaar.item.bazaar_result_prefix")
                                        .append(result.success() ? "<a> {}" : "<c> {}", result.message()));
                                if (result.success()) player.closeInventory();
                            });
                });
    }
}
