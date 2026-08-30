package net.swofty.type.replayviewer.view;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointReplaySettings;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.commons.text.Text;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.util.ReplaySettingsUtil;

import java.util.List;
import java.util.function.Consumer;

public class GUIViewerSettings extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("replays.viewer_settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        DatapointReplaySettings.ReplaySettings settings = ReplaySettingsUtil.getSettings(ctx.player());
        short currentFlySpeed = settings.getFlySpeed();
        short nextFlySpeed = ReplaySettingsUtil.cycleFlySpeed(currentFlySpeed);
        short currentSkip = settings.getSkipIntervals();
        short nextSkip = cycleSkip(currentSkip);

        layout.slot(10, createToggleItem(
                Text.key("replays.chat_messages"),
            settings.isChatMessages(),
                Text.key("replays.chat_messages_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setChatMessages(!replaySettings.isChatMessages()), false));

        layout.slot(11, createToggleItem(
                Text.key("replays.chat_timeline"),
            settings.isChatTimeline(),
                Text.key("replays.chat_timeline_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setChatTimeline(!replaySettings.isChatTimeline()), false));

        layout.slot(12, createToggleItem(
                Text.key("replays.show_spectators"),
            settings.isShowSpectators(),
                Text.key("replays.show_spectators_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setShowSpectators(!replaySettings.isShowSpectators()), false));

        layout.slot(13, createToggleItem(
                Text.key("replays.night_vision"),
            settings.isNightVision(),
                Text.key("replays.night_vision_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setNightVision(!replaySettings.isNightVision()), false));

        layout.slot(14, createToggleItem(
                Text.key("replays.show_particles"),
            settings.isShowParticles(),
                Text.key("replays.show_particles_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setShowParticles(!replaySettings.isShowParticles()), false));

        layout.slot(15, createToggleItem(
                Text.key("replays.advancing_time"),
            settings.isAdvanceTime(),
                Text.key("replays.advancing_time_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setAdvanceTime(!replaySettings.isAdvanceTime()), false));

        layout.slot(16, ItemStacks.item(
            Material.PAPER,
            1,
                Text.key("replays.fly_speed"),
                List.of(
                        Text.key("replays.fly_speed_description"),
                        Text.empty(),
                        Text.key("replays.currently_selected", currentFlySpeed + "x"),
                        Text.empty(),
                        Text.key("replays.click_to_set",
                                Text.key("replays.fly_speed"),
                                nextFlySpeed + "x"),
                        Text.empty(),
                        Text.key("replays.click_to_cycle")
                )
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setFlySpeed(ReplaySettingsUtil.cycleFlySpeed(replaySettings.getFlySpeed())), false));

        layout.slot(17, ItemStacks.item(
            Material.PAPER,
            1,
                Text.key("replays.skip_intervals"),
                List.of(
                        Text.key("replays.skip_intervals_description"),
                        Text.empty(),
                        Text.key("replays.currently_selected", currentSkip + "s"),
                        Text.empty(),
                        Text.key("replays.click_to_set",
                                Text.key("replays.skip_intervals"),
                                nextSkip + "s"),
                        Text.empty(),
                        Text.key("replays.click_to_cycle")
                )
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setSkipIntervals(cycleSkip(replaySettings.getSkipIntervals())), true));

        Components.back(layout, 31, ctx);
    }

    private static ItemStack.Builder createToggleItem(Text title, boolean enabled, Text description) {
        return ItemStacks.item(
            enabled ? Material.LIME_DYE : Material.GRAY_DYE,
            1,
                Text.of("<color:{}>{}", enabled ? NamedTextColor.GREEN : NamedTextColor.RED, title),
                List.of(
                        description,
                        Text.empty(),
                        Text.key(enabled ? "replays.click_to_disable" : "replays.click_to_enable")
                )
        );
    }

    private static void updateSetting(ViewContext ctx,
                                      Consumer<DatapointReplaySettings.ReplaySettings> updater,
                                      boolean refreshReplayHotbar) {
        boolean success = ReplaySettingsUtil.updateSettings(ctx.player(), updater);
        if (!success) {
            ctx.player().sendMessage(Text.key("replays.settings_update_failed"));
            return;
        }

        ReplaySettingsUtil.applyVisualSettings(ctx.player());
        TypeReplayViewerLoader.getSession(ctx.player().getUuid())
                .ifPresent(session -> session.refreshViewerProjection(ctx.player()));
        if (refreshReplayHotbar) {
            TypeReplayViewerLoader.populateInventory(ctx.player());
        }

        ctx.session(DefaultState.class).refresh();
    }

    private static short cycleSkip(short previous) {
        for (short preset : ReplaySession.SKIP_PRESETS) {
            if (preset > previous) {
                return preset;
            }
        }
        return ReplaySession.SKIP_PRESETS[0];
    }
}
