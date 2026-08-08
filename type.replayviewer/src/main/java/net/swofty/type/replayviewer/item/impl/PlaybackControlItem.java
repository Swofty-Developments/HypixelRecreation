package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.item.ReplayItem;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaybackControlItem extends ReplayItem {

    public PlaybackControlItem() {
        super("playback");
    }

    @Override
    public ItemStack getBlandItem() {
        return null;
    }

    @Override
    public ItemStack getItemStack(HypixelPlayer... p) {
        HypixelPlayer player = p[0];
        AtomicBoolean isPlaying = new AtomicBoolean(true);
        TypeReplayViewerLoader.getSession(player).ifPresent(
            session -> isPlaying.set(session.isPlaying())
        );
        if (isPlaying.get()) {
            return appendData(ItemStackCreator.createNamedItemStack(Material.MAGENTA_DYE, I18n.t("replays.click_to_pause"))).build();
        }
        return appendData(ItemStackCreator.getStack(I18n.t("replays.click_to_resume"), Material.GRAY_DYE, 1, List.of(
                I18n.t("replays.replay_currently_paused")
        ))).build();
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        if (event instanceof CancellableEvent cancellable) {
            cancellable.setCancelled(true);
        }

        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
            session -> {
                if (session.isPlaying()) {
                    session.pause();
                } else {
                    session.play();
                }
                TypeReplayViewerLoader.populateInventory(player);
            },
                () -> player.sendMessage(I18n.t("replays.no_active_session"))
        );
    }
}
