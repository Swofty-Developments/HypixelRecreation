package net.swofty.type.theend.gui;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopPearlDealer extends ShopView {
    public GUIShopPearlDealer() {
        super("Pearl Dealer", DEFAULT);
    }

    private SkyBlockItem enchantedBook(EnchantmentType type, int level) {
        ItemAttributeHandler handler = new SkyBlockItem(Material.ENCHANTED_BOOK).getAttributeHandler();
        handler.addEnchantment(new SkyBlockEnchantment(type, level));
        return handler.asSkyBlockItem();
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(enchantedBook(EnchantmentType.GRAVITY, 6), 1, new CoinShopPrice(5_000_000)));
        attachItem(ShopItem.Single(enchantedBook(EnchantmentType.ENDER_SLAYER, 6), 1, new CoinShopPrice(1_500_000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.END_STONE), 1, new CoinShopPrice(10)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.OBSIDIAN), 1, new CoinShopPrice(50)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.REMNANT_OF_THE_EYE), 1, new CoinShopPrice(200_000)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.STONK), 1, new CoinShopPrice(499_999)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SILENT_PEARL), 1, new CoinShopPrice(1_200)));
    }
}
