package net.swofty.type.replayviewer.view;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.util.ReplayShareUtil;

import java.util.List;

public class GUIReplayViewer extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("replays.replay_viewer", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.slot(9, ItemStackCreator.getStack(
                I18n.t("replays.settings"),
            Material.OAK_SIGN,
            1,
                List.of(
                        I18n.t("replays.settings_description"),
                        net.kyori.adventure.text.Component.empty(),
                        I18n.t("replays.click_to_open")
                )
        ), (s, c) -> {
            c.push(new GUIViewerSettings());
        });

        layout.slot(11, ItemStackCreator.getStack(
                I18n.t("replays.bookmarks"),
            Material.BOOK,
            1,
                List.of(
                        I18n.t("replays.bookmarks_description"),
                        net.kyori.adventure.text.Component.empty(),
                        I18n.t("replays.click_to_view")
                )
        ), (_, c) -> c.push(new GUIBookmarks()));

        layout.slot(13, ItemStackCreator.getStack(
                I18n.t("replays.share"),
            Material.PAPER,
            1,
                List.of(
                        I18n.t("replays.share_description"),
                        net.kyori.adventure.text.Component.empty(),
                        I18n.t("replays.click_to_share")
                )
        ), (_, c) -> TypeReplayViewerLoader.getSession(c.player()).ifPresentOrElse(
            session -> ReplayShareUtil.sendShareCommandMessage(c.player(), session),
                () -> c.player().sendMessage(I18n.t("replays.no_active_session"))
        ));

        // for now, this can't be implemented
        layout.slot(15, ItemStackCreator.getStack(
                I18n.t("replays.submit_highlight"),
            Material.FILLED_MAP,
            1,
                List.of(
                        I18n.t("replays.highlight_description"),
                        net.kyori.adventure.text.Component.empty(),
                        I18n.t("replays.highlight_terms"),
                        net.kyori.adventure.text.Component.empty(),
                        I18n.t("replays.click_to_submit")
                )
        ), (_, viewContext) -> viewContext.player().notImplemented());

        layout.slot(17, ItemStackCreator.getStack(
                I18n.t("replays.leave_replay"),
            Material.DARK_OAK_DOOR,
            1,
                I18n.t("replays.click_to_leave")
        ), (_, c) -> {
            TypeReplayViewerLoader.getSession(c.player())
                .ifPresent(session -> session.removeViewer(c.player()));
            c.player().sendTo(ServerType.PROTOTYPE_LOBBY);
        });
    }
}
