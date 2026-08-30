package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.item.ReplayItem;
import net.swofty.type.replayviewer.view.GUIReplayViewer;

public class MoreItem extends ReplayItem {

	public MoreItem() {
		super("menu");
	}

	@Override
	public ItemStack getBlandItem() {
        return ItemStacks.named(Material.NETHER_STAR, Text.key("replays.more")).build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		player.openView(new GUIReplayViewer());
	}
}
