package net.swofty.type.skyblockgeneric.shop;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
public interface ShopPrice {

    /**
     * @return the display name of the item, used for the GUI (with the amount)
     */
    List<Text> getGUIDisplay();

    /**
     * @return the plural name of the item type, used for displaying "Not enough [plural name]!" type messages
     */
    String getNamePlural();

    /**
     * @param player the player who is purchasing the item
     * @return whether the player can afford the item
     */
    boolean canAfford(SkyBlockPlayer player);

    /**
     * @param player the player who is purchasing the item
     */
    void processPurchase(SkyBlockPlayer player);

    /**
     * @param amount the amount to multiply the price by
     * @return new ShopPrice with the multiplied amount
     */
    ShopPrice multiply(int amount);

    /**
     * @param amount the amount to divide the price by
     * @return new ShopPrice with the divided amount
     */
    ShopPrice divide(double amount);

}
