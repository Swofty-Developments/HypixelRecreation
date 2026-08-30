package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.item.ReplayItem;
import net.swofty.type.replayviewer.view.GUIPlayers;

public class TeleporterItem extends ReplayItem {

	public TeleporterItem() {
		super("teleporter");
	}

	@Override
	public ItemStack getBlandItem() {
        return ItemStacks.named(Material.COMPASS, Text.key("replays.teleport_to_player")).build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		player.openView(new GUIPlayers());
	}
}
