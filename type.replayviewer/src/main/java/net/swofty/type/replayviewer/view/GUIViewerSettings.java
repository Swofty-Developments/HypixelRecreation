package net.swofty.type.replayviewer.view;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointReplaySettings;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
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
                I18n.t("replays.chat_messages"),
            settings.isChatMessages(),
                I18n.t("replays.chat_messages_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setChatMessages(!replaySettings.isChatMessages()), false));

        layout.slot(11, createToggleItem(
                I18n.t("replays.chat_timeline"),
            settings.isChatTimeline(),
                I18n.t("replays.chat_timeline_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setChatTimeline(!replaySettings.isChatTimeline()), false));

        layout.slot(12, createToggleItem(
                I18n.t("replays.show_spectators"),
            settings.isShowSpectators(),
                I18n.t("replays.show_spectators_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setShowSpectators(!replaySettings.isShowSpectators()), false));

        layout.slot(13, createToggleItem(
                I18n.t("replays.night_vision"),
            settings.isNightVision(),
                I18n.t("replays.night_vision_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setNightVision(!replaySettings.isNightVision()), false));

        layout.slot(14, createToggleItem(
                I18n.t("replays.show_particles"),
            settings.isShowParticles(),
                I18n.t("replays.show_particles_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setShowParticles(!replaySettings.isShowParticles()), false));

        layout.slot(15, createToggleItem(
                I18n.t("replays.advancing_time"),
            settings.isAdvanceTime(),
                I18n.t("replays.advancing_time_description")
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setAdvanceTime(!replaySettings.isAdvanceTime()), false));

        layout.slot(16, ItemStackCreator.getStack(
                I18n.t("replays.fly_speed"),
            Material.PAPER,
            1,
                List.of(
                        I18n.t("replays.fly_speed_description"),
                        Component.empty(),
                        I18n.t("replays.currently_selected", Argument.string("option", currentFlySpeed + "x")),
                        Component.empty(),
                        I18n.t("replays.click_to_set",
                                Argument.component("setting", I18n.t("replays.fly_speed")),
                                Argument.string("option", nextFlySpeed + "x")),
                        Component.empty(),
                        I18n.t("replays.click_to_cycle")
                )
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setFlySpeed(ReplaySettingsUtil.cycleFlySpeed(replaySettings.getFlySpeed())), false));

        layout.slot(17, ItemStackCreator.getStack(
                I18n.t("replays.skip_intervals"),
            Material.PAPER,
            1,
                List.of(
                        I18n.t("replays.skip_intervals_description"),
                        Component.empty(),
                        I18n.t("replays.currently_selected", Argument.string("option", currentSkip + "s")),
                        Component.empty(),
                        I18n.t("replays.click_to_set",
                                Argument.component("setting", I18n.t("replays.skip_intervals")),
                                Argument.string("option", nextSkip + "s")),
                        Component.empty(),
                        I18n.t("replays.click_to_cycle")
                )
        ), (_, c) -> updateSetting(c, replaySettings -> replaySettings.setSkipIntervals(cycleSkip(replaySettings.getSkipIntervals())), true));

        Components.back(layout, 31, ctx);
    }

    private static ItemStack.Builder createToggleItem(Component title, boolean enabled, Component description) {
        return ItemStackCreator.getStack(
                title.color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED),
            enabled ? Material.LIME_DYE : Material.GRAY_DYE,
            1,
                List.of(
                        description,
                        Component.empty(),
                        I18n.t(enabled ? "replays.click_to_disable" : "replays.click_to_enable")
                )
        );
    }

    private static void updateSetting(ViewContext ctx,
                                      Consumer<DatapointReplaySettings.ReplaySettings> updater,
                                      boolean refreshReplayHotbar) {
        boolean success = ReplaySettingsUtil.updateSettings(ctx.player(), updater);
        if (!success) {
            ctx.player().sendMessage(I18n.t("replays.settings_update_failed"));
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
