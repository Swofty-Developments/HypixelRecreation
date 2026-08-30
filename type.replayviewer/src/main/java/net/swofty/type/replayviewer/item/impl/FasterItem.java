package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.item.ReplayItem;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.util.List;

public class FasterItem extends ReplayItem {

    public FasterItem() {
        super("faster");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.head("cf3821aab0a5abfe7f4937ac28ec8e31a3360cb515c11046ff750ae2a0a391af", Text.key("replays.increase_speed"), List.of()).build();
    }


    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        ((CancellableEvent) event).setCancelled(true);
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
            ReplaySession::cycleSpeedUp,
                () -> player.sendMessage(Text.key("replays.no_active_session"))
        );
    }

}
