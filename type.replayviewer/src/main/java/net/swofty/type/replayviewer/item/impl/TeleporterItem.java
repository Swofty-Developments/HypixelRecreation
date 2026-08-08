package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.item.ReplayItem;
import net.swofty.type.replayviewer.view.GUIPlayers;

public class TeleporterItem extends ReplayItem {

	public TeleporterItem() {
		super("teleporter");
	}

	@Override
	public ItemStack getBlandItem() {
        return ItemStackCreator.createNamedItemStack(Material.COMPASS, I18n.t("replays.teleport_to_player")).build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		player.openView(new GUIPlayers());
	}
}
