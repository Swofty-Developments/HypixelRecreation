package net.swofty.type.theend.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopLoneAdventurer extends ShopView {
    public GUIShopLoneAdventurer() {
        super("Lone Adventurer", SINGLE_SLOT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.VOID_SWORD), 1, new CoinShopPrice(200_000)));
    }
}
