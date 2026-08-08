package net.swofty.type.replayviewer.view;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.game.replay.event.ReplayBookmarkEvent;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.util.ReplayShareUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIBookmarks implements StatefulView<GUIBookmarks.State> {

    private static final int[] BOOKMARK_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    public record State(int page) {
    }

    private record BookmarkEntry(int tick, Component title, Component playerDisplayName) {
    }

    @Override
    public State initialState() {
        return new State(0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.translatable("replays.bookmarks", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        var sessionOpt = TypeReplayViewerLoader.getSession(ctx.player());
        if (sessionOpt.isEmpty()) {
            layout.slot(22, ItemStackCreator.getStack(
                    I18n.t("replays.no_replay_session_title"),
                Material.BARRIER,
                1,
                    List.of(
                            I18n.t("replays.no_replay_session_description"),
                            I18n.t("replays.no_replay_session_description_line")
                    )
            ));
            Components.back(layout, 49, ctx);
            return;
        }

        ReplaySession replaySession = sessionOpt.get();
        List<BookmarkEntry> bookmarks = collectBookmarks(replaySession);

        int pageSize = BOOKMARK_SLOTS.length;
        int totalPages = Math.max(1, (bookmarks.size() + pageSize - 1) / pageSize);
        int currentPage = Math.min(state.page(), totalPages - 1);
        int startIndex = currentPage * pageSize;

        for (int i = 0; i < BOOKMARK_SLOTS.length; i++) {
            int slot = BOOKMARK_SLOTS[i];
            int index = startIndex + i;

            if (index >= bookmarks.size()) {
                layout.slot(slot, ItemStack.AIR.builder());
                continue;
            }

            BookmarkEntry entry = bookmarks.get(index);
            layout.slot(slot, ItemStackCreator.getStack(
                    entry.title(),
                    Material.PAPER,
                    1,
                    List.of(
                            I18n.t("replays.bookmark_time", Argument.string("time", formatBookmarkTime(entry.tick()))),
                            Component.empty(),
                            I18n.t("replays.player_label", Argument.component("player", entry.playerDisplayName())),
                            Component.empty(),
                            I18n.t("replays.click_to_seek"),
                            I18n.t("replays.right_click_share"))
            ), (click, c) -> {
                if (click.click() instanceof Click.Right) {
                    ReplayShareUtil.sendShareCommandMessage(c.player(), replaySession, entry.tick());
                    return;
                }

                replaySession.seekTo(entry.tick());
            });
        }

        if (currentPage > 0) {
            layout.slot(45, ItemStackCreator.getStack(
                    I18n.t("replays.previous_page"),
                Material.ARROW,
                1,
                    I18n.t("replays.page", Argument.numeric("page", currentPage))
            ), (_, c) -> c.session(State.class).setState(new State(currentPage - 1)));
        }

        if (currentPage < totalPages - 1) {
            layout.slot(53, ItemStackCreator.getStack(
                    I18n.t("replays.next_page"),
                Material.ARROW,
                1,
                    I18n.t("replays.page", Argument.numeric("page", currentPage + 2))
            ), (_, c) -> c.session(State.class).setState(new State(currentPage + 1)));
        }

        Components.back(layout, 49, ctx);
        if (bookmarks.isEmpty()) {
            layout.slot(22, ItemStackCreator.getStack(
                    I18n.t("replays.no_bookmarks_found"),
                Material.BARRIER,
                1,
                    I18n.t("replays.no_bookmarks")
            ));
        }
    }

    private static List<BookmarkEntry> collectBookmarks(ReplaySession session) {
        List<BookmarkEntry> entries = new ArrayList<>();

        for (int tick : session.getReplayData().getAllTicks()) {
            for (var event : session.getReplayData().transientEventsAt(tick)) {
                if (event instanceof ReplayBookmarkEvent bookmark) {
                    entries.add(new BookmarkEntry(tick, bookmark.title(),
                            resolveDisplayName(bookmark.participantUuid())));
                }
            }
        }

        return entries;
    }

    private static String formatBookmarkTime(int tick) {
        int totalSeconds = tick / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d seconds", minutes, seconds);
    }

    private static Component resolveDisplayName(UUID uuid) {
        if (uuid == null) {
            return I18n.t("replays.unknown_player");
        }

        try {
            return LegacyComponentSerializer.legacySection().deserialize(HypixelPlayer.getDisplayName(uuid));
        } catch (Exception ignored) {
            return I18n.t("replays.unknown_player");
        }
    }

}
