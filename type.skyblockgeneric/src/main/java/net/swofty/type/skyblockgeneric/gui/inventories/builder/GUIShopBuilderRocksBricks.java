package net.swofty.type.skyblockgeneric.gui.inventories.builder;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopBuilderRocksBricks extends ShopView {
    public GUIShopBuilderRocksBricks() {
        super(Text.key("gui_builder.rocks_bricks.title"), UPPER5ROWS);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.STONE), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.GRANITE), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.POLISHED_GRANITE), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.DIORITE), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.POLISHED_DIORITE), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.ANDESITE), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.POLISHED_ANDESITE), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.GRAVEL), 1, new CoinShopPrice(32)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.OBSIDIAN), 1, new CoinShopPrice(99)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SMOOTH_STONE_SLAB), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.COBBLESTONE_STAIRS), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.COBBLESTONE_SLAB), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.COBBLESTONE), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.COBBLESTONE_WALL), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.MOSSY_COBBLESTONE), 1, new CoinShopPrice(12)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.MOSSY_COBBLESTONE_WALL), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.DIRT), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.COARSE_DIRT), 1, new CoinShopPrice(2)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.STONE_BRICKS), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.STONE_BRICK_STAIRS), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.STONE_BRICK_SLAB), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.CRACKED_STONE_BRICKS), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.CHISELED_STONE_BRICKS), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.MOSSY_STONE_BRICKS), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.GRASS_BLOCK), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.PODZOL), 1, new CoinShopPrice(10)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SANDSTONE), 1, new CoinShopPrice(6)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SANDSTONE_STAIRS), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SANDSTONE_SLAB), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SMOOTH_SANDSTONE), 1, new CoinShopPrice(6)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.CHISELED_SANDSTONE), 1, new CoinShopPrice(6)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SAND), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.BRICKS), 1, new CoinShopPrice(6)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.BRICK_STAIRS), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.BRICK_SLAB), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.RED_SANDSTONE), 1, new CoinShopPrice(6)));

        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.RED_SANDSTONE_STAIRS), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.RED_SANDSTONE_SLAB), 1, new CoinShopPrice(2)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SMOOTH_RED_SANDSTONE), 1, new CoinShopPrice(6)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.CHISELED_RED_SANDSTONE), 1, new CoinShopPrice(6)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.IRON_DOOR), 1, new CoinShopPrice(36)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.IRON_TRAPDOOR), 1, new CoinShopPrice(24)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.TERRACOTTA), 1, new CoinShopPrice(4)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.ICE), 1, new CoinShopPrice(1)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.PACKED_ICE), 1, new CoinShopPrice(9)));
    }
}
