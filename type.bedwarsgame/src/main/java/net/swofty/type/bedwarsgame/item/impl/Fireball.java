package net.swofty.type.bedwarsgame.item.impl;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;

public class Fireball extends SimpleInteractableItem {

	public Fireball() {
		super("fireball", new ShopData("Fireball", "Right-click to launch! Great to knock\nback enemies walking on thin bridges.",
			40, 1, Currency.IRON, DatapointBedWarsHotbar.HotbarItemType.UTILITY, 7));
	}


	@Override
	public ItemStack getBlandItem() {
		return ItemStack.of(Material.FIRE_CHARGE);
	}
}
