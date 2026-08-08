package net.swofty.type.replayviewer.view;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.entity.Entity;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.entity.ReplayPlayerEntity;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// TODO: use PaginatedView?
public class GUIPlayers implements StatefulView<GUIPlayers.State> {

    private static final int[] PLAYER_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    public record State(int page) {
    }

    private record PlayerEntry(int entityId, ReplayPlayerEntity entity) {
    }

    @Override
    public State initialState() {
        return new State(0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.translatable("replays.players", InventoryType.CHEST_6_ROW);
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
        List<PlayerEntry> players = collectPlayers(replaySession);

        int pageSize = PLAYER_SLOTS.length;
        int totalPages = Math.max(1, (players.size() + pageSize - 1) / pageSize);
        int currentPage = Math.min(state.page(), totalPages - 1);
        int startIndex = currentPage * pageSize;

        for (int i = 0; i < PLAYER_SLOTS.length; i++) {
            int slot = PLAYER_SLOTS[i];
            int index = startIndex + i;

            if (index >= players.size()) {
                layout.slot(slot, ItemStack.AIR.builder());
                continue;
            }

            PlayerEntry entry = players.get(index);
            ReplayPlayerEntity replayPlayer = entry.entity();
            Component playerName = I18n.t("replays.player_view_name",
                    Argument.component("player", getDisplayName(replayPlayer)));
            int health = Math.max(0, Math.round(replayPlayer.getHealth()));
            List<Component> playerLore = List.of(
                    I18n.t("replays.health", Argument.numeric("health", health)),
                    Component.empty(),
                    I18n.t("replays.click_to_teleport"),
                    I18n.t("replays.right_click_first_person")
            );

            ItemStack.Builder head = replayPlayer.getSkin() != null
                ? ItemStackCreator.getStackHead(
                    playerName,
                replayPlayer.getSkin(),
                1,
                    playerLore
            )
                : ItemStackCreator.getStack(
                    playerName,
                Material.PLAYER_HEAD,
                1,
                    playerLore
            );

            layout.slot(slot, head, (click, c) -> {
                if (click.click() instanceof Click.Right) {
                    replaySession.followEntity(c.player(), entry.entityId());
                    c.player().closeInventory();
                    return;
                }

                c.player().teleport(replayPlayer.getPosition());
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

        if (players.isEmpty()) {
            layout.slot(22, ItemStackCreator.getStack(
                    I18n.t("replays.no_players_title"),
                Material.BARRIER,
                1,
                    I18n.t("replays.no_players_description")
            ));
        }
    }

    private static List<PlayerEntry> collectPlayers(ReplaySession session) {
        List<PlayerEntry> entries = new ArrayList<>();
        for (int entityId : session.getEntityManager().getEntityIds()) {
            Entity entity = session.getEntityManager().getEntity(entityId);
            if (entity instanceof ReplayPlayerEntity replayPlayerEntity) {
                entries.add(new PlayerEntry(entityId, replayPlayerEntity));
            }
        }

        entries.sort(Comparator.comparing(entry -> entry.entity().getPlayerName(), String.CASE_INSENSITIVE_ORDER));
        return entries;
    }

    private static Component getDisplayName(ReplayPlayerEntity replayPlayer) {
        try {
            return LegacyComponentSerializer.legacySection().deserialize(
                    HypixelPlayer.getDisplayName(replayPlayer.getActualUuid()));
        } catch (Exception ignored) {
            return Component.text(replayPlayer.getPlayerName(), NamedTextColor.GRAY);
        }
    }
}
