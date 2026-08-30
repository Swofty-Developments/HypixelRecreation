package net.swofty.type.replayviewer.view;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.commons.text.Text;
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
        layout.slot(9, ItemStacks.item(
            Material.OAK_SIGN,
            1,
                Text.key("replays.settings"),
                List.of(
                        Text.key("replays.settings_description"),
                        Text.empty(),
                        Text.key("replays.click_to_open")
                )
        ), (s, c) -> {
            c.push(new GUIViewerSettings());
        });

        layout.slot(11, ItemStacks.item(
            Material.BOOK,
            1,
                Text.key("replays.bookmarks"),
                List.of(
                        Text.key("replays.bookmarks_description"),
                        Text.empty(),
                        Text.key("replays.click_to_view")
                )
        ), (_, c) -> c.push(new GUIBookmarks()));

        layout.slot(13, ItemStacks.item(
            Material.PAPER,
            1,
                Text.key("replays.share"),
                List.of(
                        Text.key("replays.share_description"),
                        Text.empty(),
                        Text.key("replays.click_to_share")
                )
        ), (_, c) -> TypeReplayViewerLoader.getSession(c.player()).ifPresentOrElse(
            session -> ReplayShareUtil.sendShareCommandMessage(c.player(), session),
                () -> c.player().sendMessage(Text.key("replays.no_active_session"))
        ));

        // for now, this can't be implemented
        layout.slot(15, ItemStacks.item(
            Material.FILLED_MAP,
            1,
                Text.key("replays.submit_highlight"),
                List.of(
                        Text.key("replays.highlight_description"),
                        Text.empty(),
                        Text.key("replays.highlight_terms"),
                        Text.empty(),
                        Text.key("replays.click_to_submit")
                )
        ), (_, viewContext) -> viewContext.player().notImplemented());

        layout.slot(17, ItemStacks.item(
            Material.DARK_OAK_DOOR,
            1,
                Text.key("replays.leave_replay"),
                List.of(Text.key("replays.click_to_leave"))
        ), (_, c) -> {
            TypeReplayViewerLoader.getSession(c.player())
                .ifPresent(session -> session.removeViewer(c.player()));
            c.player().sendTo(ServerType.PROTOTYPE_LOBBY);
        });
    }
}
