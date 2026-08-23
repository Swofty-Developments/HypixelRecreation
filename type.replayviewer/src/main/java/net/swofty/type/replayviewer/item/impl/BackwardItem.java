package net.swofty.type.replayviewer.item.impl;

import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.data.datapoints.DatapointReplaySettings;
import net.swofty.type.generic.data.handlers.ReplayDataHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.item.ReplayItem;
import org.tinylog.Logger;

import java.util.List;

public class BackwardItem extends ReplayItem {

    public BackwardItem() {
        super("backward");
    }

    @Override
    public ItemStack getBlandItem() {
        return null;
    }

    @Override
    public ItemStack getItemStack(HypixelPlayer... p) {
        HypixelPlayer player = p[0];
        short duration;
        ReplayDataHandler handler = ReplayDataHandler.getUser(player);
        if (handler == null) {
            duration = 30;
            Logger.error("Something went wrong while trying to access player data.");
        } else {
            DatapointReplaySettings.ReplaySettings settings = handler.get(ReplayDataHandler.Data.REPLAY_SETTINGS, DatapointReplaySettings.class)
                .getValue();
            duration = settings.getSkipIntervals();
        }
        return appendData(ItemStacks.head(
                "a6e1cd0067855b67e0fd5b7eb7457281c41d13bd5bc9158c4a82f518198a1d22",
                Text.key("replays.skip_backward", String.valueOf(duration)),
                List.of(Text.key("replays.click_to_change_duration"))
        )).build();
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        ((CancellableEvent) event).setCancelled(true);
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        short duration;
        ReplayDataHandler handler = ReplayDataHandler.getUser(player);
        if (handler == null) {
            duration = 30;
            Logger.error("Something went wrong while trying to access player data.");
        } else {
            DatapointReplaySettings.ReplaySettings settings = handler.get(ReplayDataHandler.Data.REPLAY_SETTINGS, DatapointReplaySettings.class)
                .getValue();
            duration = settings.getSkipIntervals();
        }
        TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
            (session) -> session.skipBackward(duration),
                () -> player.sendMessage(Text.key("replays.no_active_session"))
        );
        Logger.info("Player {} set skip backward duration to {} seconds", player.getUsername(), duration);
    }

    @Override
    public void onHandAnimation(PlayerHandAnimationEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
            (session) -> {
                ReplayDataHandler handler = ReplayDataHandler.getUser(player);
                if (handler == null) {
                    Logger.error("Something went wrong while trying to access player data.");
                    return;
                }
                DatapointReplaySettings.ReplaySettings settings = handler.get(ReplayDataHandler.Data.REPLAY_SETTINGS, DatapointReplaySettings.class)
                    .getValue();
                short newDuration = session.cycleSkip(settings.getSkipIntervals());
                settings.setSkipIntervals(newDuration);
                handler.get(ReplayDataHandler.Data.REPLAY_SETTINGS, DatapointReplaySettings.class).setValue(settings);
                TypeReplayViewerLoader.populateInventory(player);
            },
                () -> player.sendMessage(Text.key("replays.no_active_session"))
        );
    }
}
