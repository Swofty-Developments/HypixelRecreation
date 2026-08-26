package net.swofty.type.bedwarsgame.shop.impl;

import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.shop.ShopItem;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeId;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ReplaceAdderItem extends ShopItem {

	private final Material replacementMaterial;

	public ReplaceAdderItem(String id, String name, String description, Function<BedWarsGameType, Integer> cost, Currency currency, Material material) {
		super(id, name, description, cost, 1, currency, material);
		this.replacementMaterial = Material.WOODEN_SWORD;
	}

	public ReplaceAdderItem(String id, String name, String description, Function<BedWarsGameType, Integer> cost, Currency currency, Material material,
                            @Nullable DatapointBedWarsHotbar.HotbarItemType hotbarItemType) {
		super(id, name, description, cost, 1, currency, material, hotbarItemType);
		this.replacementMaterial = Material.WOODEN_SWORD;
	}

	public ReplaceAdderItem(String id, String name, String description, int cost, Currency currency, Material material) {
		super(id, name, description, cost, 1, currency, material);
		this.replacementMaterial = Material.WOODEN_SWORD;
	}

	public ReplaceAdderItem(String id, String name, String description, int cost, Currency currency, Material material,
	                        @Nullable DatapointBedWarsHotbar.HotbarItemType hotbarItemType) {
		super(id, name, description, cost, 1, currency, material, hotbarItemType);
		this.replacementMaterial = Material.WOODEN_SWORD;
	}

	public ReplaceAdderItem(String id, String name, String description, int cost, Currency currency, Material material, Material replacementMaterial) {
		super(id, name, description, cost, 1, currency, material);
		this.replacementMaterial = replacementMaterial;
	}

	public ReplaceAdderItem(String id, String name, String description, int cost, Currency currency, Material material, Material replacementMaterial,
	                        @Nullable DatapointBedWarsHotbar.HotbarItemType hotbarItemType) {
		super(id, name, description, cost, 1, currency, material, hotbarItemType);
		this.replacementMaterial = replacementMaterial;
	}

	@Override
	public void onPurchase(BedWarsPlayer player) {
		var inventory = player.getInventory();
		for (int i = 0; i < inventory.getSize(); i++) {
			var item = inventory.getItemStack(i);
			if (item.material() == replacementMaterial) {
				inventory.setItemStack(i, item.withMaterial(this.getDisplay().material()));
				return;
			}
		}

		giveItem(player, getDisplay());
		if (player.hasTag(TeamUpgradeId.SHARPNESS.levelTag())) {
			BedWarsGame game = player.getGame();
			TypeBedWarsGameLoader.getTeamShopManager().getUpgrade(TeamUpgradeId.SHARPNESS).applyEffect(game, player.getTeamKey(), player.getTag(TeamUpgradeId.SHARPNESS.levelTag()));
		}
	}

}
