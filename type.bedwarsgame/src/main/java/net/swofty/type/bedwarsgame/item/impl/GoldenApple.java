package net.swofty.type.bedwarsgame.item.impl;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;

public class GoldenApple extends SimpleInteractableItem {

	public GoldenApple() {
		super("golden_apple", new ShopData("Golden Apple", "Well-rounded healing.",
			3, 1, Currency.GOLD, DatapointBedWarsHotbar.HotbarItemType.UTILITY, 7));
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStack.of(Material.GOLDEN_APPLE);
	}
}
