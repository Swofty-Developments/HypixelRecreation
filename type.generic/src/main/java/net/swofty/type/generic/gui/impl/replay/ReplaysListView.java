package net.swofty.type.generic.gui.impl.replay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class ReplaysListView extends StatefulPaginatedView<ReplayEntry, ReplaysListView.State> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final Consumer<ReplayEntry> onReplaySelect;

    public ReplaysListView(Consumer<ReplayEntry> onReplaySelect) {
        this.onReplaySelect = onReplaySelect;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.translatable("replays.replays", InventoryType.CHEST_6_ROW);
    }

    @Override
    public State initialState() {
        return new State(
                Collections.emptyList(),
                0
        );
    }

    @Override
    protected int[] getPaginatedSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    @Override
    protected int getNextPageSlot() {
        return 50;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 48;
    }

    @Override
    protected boolean shouldRenderNavBackground() {
        return false;
    }

    @Override
    protected void layoutBackground(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.close(layout, 48);

        layout.slot(47, (_, _) -> ItemStackCreator.getStack(I18n.t("replays.replays_title"), Material.BOOK, 1, List.of(
                I18n.t("replays.replays_description"),
                Component.empty(),
                I18n.t("replays.replays_supported"),
                I18n.t("replays.bedwars_supported")
        )));

        // TODO: behaviour
        layout.slot(49, (_, _) -> ItemStackCreator.getStack(I18n.t("replays.show_replays_only"), Material.GRAY_DYE, 1, List.of(
                I18n.t("replays.show_replays_only_description"),
                Component.empty(),
                I18n.t("replays.click_to_toggle")
        )));
    }

    @Override
    protected ItemStack.Builder renderItem(ReplayEntry item, int index, HypixelPlayer player) {
        Material material = switch (item.serverType()) {
            case BEDWARS_GAME -> Material.RED_BED;
            case SKYWARS_GAME -> Material.ENDER_EYE;
            case MURDER_MYSTERY_GAME -> Material.IRON_SWORD;
            default -> Material.PAPER;
        };


        List<Component> lore = new ArrayList<>();
        lore.add(I18n.t("replays.replay_date", Argument.string("date", DATE_FORMAT.format(new Date(item.startTime())))));
        lore.add(I18n.t("replays.replay_duration", Argument.string("duration", item.formattedDuration())));
        lore.add(Component.empty());
        lore.add(I18n.t("replays.replay_mode", Argument.string("mode", formatMode(item.gameTypeName()))));
        lore.add(I18n.t("replays.replay_map", Argument.string("map", item.mapName())));
        lore.add(Component.empty());
        lore.add(I18n.t("replays.server", Argument.string("server", item.serverId())));
        lore.add(I18n.t("replays.player_count", Argument.numeric("players", item.players().size())));
        lore.add(Component.empty());

        // add this properly on Duels
        /*if (item.winnerId() != null) {
            boolean won = item.players().containsKey(player.getUuid()) &&
                    (item.winnerId().equals(player.getUuid().toString()) ||
						item.players().keySet().stream()
								.anyMatch(uuid -> item.winnerId().contains(uuid.toString())));
            lore.add(won ? Component.text("VICTORY!", NamedTextColor.GREEN, TextDecoration.BOLD) :
                    Component.text("DEFEAT", NamedTextColor.RED));
        }*/

        lore.add(I18n.t("replays.click_to_view_replay"));

        return ItemStackCreator.getStack(
                I18n.t("replays.replay_item_name", Argument.string("name", item.displayName())),
                material,
                1,
                lore
        );
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, ReplayEntry item, int index) {
        ctx.player().closeInventory();
        onReplaySelect.accept(item);
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, ReplayEntry item) {
        return false;
    }

    private static String formatMode(String gameTypeName) {
        if ("ONE_EIGHT".equalsIgnoreCase(gameTypeName)) {
            return "Solo";
        }
        return StringUtility.capitalize(gameTypeName.replace('_', ' '));
    }

    public record State(
            List<ReplayEntry> items,
            int page
    ) implements PaginatedState<ReplayEntry> {
        @Override
        public State withPage(int page) {
            return new State(items, page);
        }

        @Override
        public State withItems(List<ReplayEntry> items) {
            return new State(items, page);
        }
    }
}
