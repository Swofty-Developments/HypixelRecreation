package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.item.ReplayItem;
import net.swofty.type.replayviewer.playback.ReplaySession;

public class FasterItem extends ReplayItem {

    public FasterItem() {
        super("faster");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStackCreator.getStackHead(I18n.t("replays.increase_speed"), "cf3821aab0a5abfe7f4937ac28ec8e31a3360cb515c11046ff750ae2a0a391af", 1).build();
    }


    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        ((CancellableEvent) event).setCancelled(true);
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
            ReplaySession::cycleSpeedUp,
                () -> player.sendMessage(I18n.t("replays.no_active_session"))
        );
    }

}
