package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.commons.text.Text;
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
            return appendData(ItemStacks.named(Material.MAGENTA_DYE, Text.key("replays.click_to_pause"))).build();
        }
        return appendData(ItemStacks.item(Material.GRAY_DYE, Text.key("replays.click_to_resume"), List.of(
                Text.key("replays.replay_currently_paused")
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
                () -> player.sendMessage(Text.key("replays.no_active_session"))
        );
    }
}
