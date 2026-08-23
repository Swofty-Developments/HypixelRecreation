package net.swofty.type.hub.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.List;

public class GUIShopBea extends ShopView {
    public GUIShopBea() {
        super("Pea", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachBeePet(Rarity.COMMON, new CombinedShopPrice(List.of(
                new CoinShopPrice(4999),
                new ItemShopPrice(ItemType.COAL_BLOCK, 2),
                new ItemShopPrice(ItemType.GOLD_BLOCK, 2)
        )));
        attachBeePet(Rarity.RARE, new CombinedShopPrice(List.of(
                new CoinShopPrice(50000),
                new ItemShopPrice(ItemType.COAL_BLOCK, 128),
                new ItemShopPrice(ItemType.GOLD_BLOCK, 128)
        )));
        attachBeePet(Rarity.EPIC, new CombinedShopPrice(List.of(
                new CoinShopPrice(200000),
                new ItemShopPrice(ItemType.ENCHANTED_COAL_BLOCK, 1),
                new ItemShopPrice(ItemType.ENCHANTED_GOLD_BLOCK, 1)
        )));
        attachBeePet(Rarity.LEGENDARY, new CombinedShopPrice(List.of(
                new CoinShopPrice(650000),
                new ItemShopPrice(ItemType.ENCHANTED_COAL_BLOCK, 8),
                new ItemShopPrice(ItemType.ENCHANTED_GOLD_BLOCK, 8)
        )));
    }

    public void attachBeePet(Rarity rarity, CombinedShopPrice price) {
        SkyBlockItem beeItem = new SkyBlockItem(ItemType.BEE_PET);
        beeItem.getAttributeHandler().setRarity(rarity);
        ShopItem bee = ShopItem.Single(beeItem,
                1,
                price);

        SkyBlockItem beeDisplayItem = new SkyBlockItem(ItemType.BEE_PET);
        beeDisplayItem.getAttributeHandler().setRarity(rarity);
        ItemAttributePetData.PetData petData = beeDisplayItem.getAttributeHandler().getPetData();
        petData.setLevel(100, rarity);
        beeDisplayItem.getAttributeHandler().setPetData(petData);

        List<Text> lore = beeDisplayItem.getLoreText();
        lore.add(Text.literal(" "));
        lore.add(Text.of("<c>This is a preview of Lvl 100"));
        lore.add(Text.of("<c>New pets are lowest level!"));

        bee.setLore(lore);
        bee.setDisplayName(beeDisplayItem.getDisplayName());
        attachItem(bee);
    }
}
