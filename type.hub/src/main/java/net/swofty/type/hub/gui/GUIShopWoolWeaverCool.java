package net.swofty.type.hub.gui;

import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopWoolWeaverCool extends ShopView {
    public GUIShopWoolWeaverCool() {
        super("Wool Weaver (Cool)", WOOLWEAVER_COOL);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        super.onOpen(state, ctx);
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        super.layoutCustom(layout, state, ctx);
        layout.slot(45, (s, c) -> {
            return ItemStacks.item(Material.ARROW, """
                    <a>Previous Page
                    <e>Page 1""");
        }, ((stateClickContext, context) -> {
            context.pop();
        }));
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.CYAN_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_BLUE_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLUE_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BROWN_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLACK_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GRAY_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_GRAY_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.WHITE_TERRACOTTA), 1, new CoinShopPrice(8)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.CYAN_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLUE_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BROWN_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLACK_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GRAY_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.WHITE_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.CYAN_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_BLUE_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLUE_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BROWN_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLACK_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GRAY_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_GRAY_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.WHITE_STAINED_GLASS), 1, new CoinShopPrice(16)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.CYAN_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_BLUE_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLUE_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BROWN_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLACK_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GRAY_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_GRAY_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.WHITE_CARPET), 1, new CoinShopPrice(32)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.CYAN_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_BLUE_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLUE_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BROWN_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BLACK_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GRAY_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIGHT_GRAY_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.WHITE_WOOL), 1, new CoinShopPrice(32)));
    }
}
