package net.swofty.type.skyblockgeneric.gui.inventories.shop;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopAlda extends ShopView {
    public GUIShopAlda() {
        super(Text.key("gui_shop.alda.title"), SINGLE_SLOT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ABIPHONE_BASIC), 1, new CoinShopPrice(100000)));
    }
}
