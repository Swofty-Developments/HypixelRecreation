package net.swofty.type.skyblockgeneric.gui.inventories.shop;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class ConfirmBuyView implements View<ConfirmBuyView.State> {

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.translatable("gui_shop.confirm_buy.title", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(12,
            (_, _) -> ItemStacks.item(
                Material.LIME_TERRACOTTA,
                1,
                Text.key("gui_shop.confirm_buy.confirm_button"),
                Text.keyLines("gui_shop.confirm_buy.confirm_button.lore", state.item.getDisplayName(), StringUtility.commaify(state.price))
            ),
            (click, c) -> {
                if (!(click.click() instanceof Click.Left || click.click() instanceof Click.Right)) return;

                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (player.getCoins() >= state.price) {
                    player.addAndUpdateItem(state.item);
                    player.removeCoins(state.price);
                    player.sendMessage(Text.key("gui_shop.confirm_buy.bought_message", state.item.getDisplayName(), state.price));
                } else {
                    player.sendMessage(Text.key("gui_shop.confirm_buy.not_enough_coins"));
                }
                player.closeInventory();
            }
        );

        layout.slot(16,
            (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, 1, Text.key("gui_shop.confirm_buy.cancel_button"), List.of()),
            (_, c) -> c.player().closeInventory()
        );
    }

    public record State(SkyBlockItem item, int price) {
    }
}
