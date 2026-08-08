package net.swofty.type.theend.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopGregory extends ShopView {
    public GUIShopGregory() {
        super("Gregory", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.DECENT_BOW), 1, new CoinShopPrice(500)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.FLINT_ARROW), 20, new CoinShopPrice(240)));
    }
}
