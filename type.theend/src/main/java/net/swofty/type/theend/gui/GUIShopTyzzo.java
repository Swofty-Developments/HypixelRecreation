package net.swofty.type.theend.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;

public class GUIShopTyzzo extends ShopView {
    public GUIShopTyzzo() {
        super("Tyzzo", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.DRAGON_SCALE), 1, new ItemShopPrice(ItemType.DRAGON_ESSENCE, 5)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.DRAGON_CLAW), 1, new ItemShopPrice(ItemType.DRAGON_ESSENCE, 10)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.DRAGON_HORN), 1, new ItemShopPrice(ItemType.DRAGON_ESSENCE, 20)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.PEARLESCENT_DYE), 1, new ItemShopPrice(ItemType.DRAGON_ESSENCE, 50)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ENDERMAN_CORTEX_REWRITER), 1, new ItemShopPrice(ItemType.DRAGON_ESSENCE, 100)));
    }
}
