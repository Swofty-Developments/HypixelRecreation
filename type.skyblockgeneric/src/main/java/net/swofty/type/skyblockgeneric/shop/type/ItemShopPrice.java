package net.swofty.type.skyblockgeneric.shop.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemShopPrice implements ShopPrice {
    ItemType type;
    int amount;

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<6>" + Text.colorTag(type.rarity.getColor()) + type.getDisplayName() + " <8>x" + amount));
    }

    @Override
    public String getNamePlural() {
        return "configuration/items";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return player.getAmountInInventory(type) >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        player.takeItem(type, amount);
    }

    @Override
    public ShopPrice multiply(int amount) {
        return new ItemShopPrice(type, this.amount * amount);
    }

    @Override
    public ShopPrice divide(double amount) {
        return new ItemShopPrice(type, Math.max((int) (this.amount / amount), 1));
    }

}
