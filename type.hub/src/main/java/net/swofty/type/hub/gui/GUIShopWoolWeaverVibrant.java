package net.swofty.type.hub.gui;

import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopWoolWeaverVibrant extends ShopView {
    public GUIShopWoolWeaverVibrant() {
        super("Wool Weaver (Vibrant)", WOOLWEAVER_VIBRANT);
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        super.layoutCustom(layout, state, ctx);
        layout.slot(
                53,
                (_, _) -> ItemStacks.item(Material.ARROW, """
                        <a>Next Page
                        <e>Page 2"""),
                ((_, context) -> context.push(new GUIShopWoolWeaverCool()))
        );
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PURPLE_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.MAGENTA_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PINK_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.RED_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.ORANGE_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.YELLOW_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIME_TERRACOTTA), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GREEN_TERRACOTTA), 1, new CoinShopPrice(8)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PURPLE_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.MAGENTA_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PINK_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.RED_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.ORANGE_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.YELLOW_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIME_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GREEN_STAINED_GLASS_PANE), 1, new CoinShopPrice(16)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PURPLE_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.MAGENTA_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PINK_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.RED_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.ORANGE_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.YELLOW_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIME_STAINED_GLASS), 1, new CoinShopPrice(16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GREEN_STAINED_GLASS), 1, new CoinShopPrice(16)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PURPLE_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.MAGENTA_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PINK_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.RED_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.ORANGE_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.YELLOW_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIME_CARPET), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GREEN_CARPET), 1, new CoinShopPrice(32)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PURPLE_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.MAGENTA_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PINK_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.RED_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.ORANGE_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.YELLOW_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.LIME_WOOL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GREEN_WOOL), 1, new CoinShopPrice(32)));
    }
}
