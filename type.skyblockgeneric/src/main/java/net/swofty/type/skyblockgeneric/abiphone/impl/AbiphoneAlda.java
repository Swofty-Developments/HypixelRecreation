package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.gui.inventories.shop.GUIShopAlda;

import java.util.List;

public class AbiphoneAlda extends AbiphoneNPC {

	public AbiphoneAlda() {
		super("alda", "<6>Alda", "Sells <a>Abiphones <7>for beginners.");
	}

	@Override
	public void onCall(HypixelPlayer player) {
		player.openView(new GUIShopAlda());
	}

	@Override
	public ItemStack.Builder getIcon() {
		return ItemStacks.head("db5647f93fd8e1da9cdb151dd9bdf4f48bb59a1d11748f1918c136c86804b2", Text.empty(), List.of());
	}
}
