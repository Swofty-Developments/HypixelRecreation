package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterDefinition;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterReward;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterService;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterState;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Locale;

public abstract class RNGMeterView extends StatefulPaginatedView<RNGMeterReward, RNGMeterView.State> {
    protected abstract RNGMeterDefinition definition();

    protected abstract ItemStack.Builder rewardItem(RNGMeterReward reward, SkyBlockPlayer player);

    @Override
    public State initialState() {
        return new State(List.copyOf(definition().rewards()), 0, "");
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.of("({}/{}) {} RNG",
                        Math.min(state.page() + 1, totalPages(state)), totalPages(state), definition().displayName()),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected ItemStack.Builder renderItem(RNGMeterReward reward, int index, HypixelPlayer rawPlayer) {
        SkyBlockPlayer player = (SkyBlockPlayer) rawPlayer;
        RNGMeterState state = RNGMeterService.get(player, definition());
        return ItemStacks.appendLore(rewardItem(reward, player),
                RNGMeterGuiSupport.rewardLore(definition(), state, reward));
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, RNGMeterReward reward, int index) {
        RNGMeterService.select((SkyBlockPlayer) ctx.player(), definition(), reward);
        ctx.session(State.class).refresh();
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, RNGMeterReward reward) {
        if (state.query().isBlank()) return false;
        String name = reward.displayName().replaceAll("<[^>]+>", "").toLowerCase(Locale.ROOT);
        return !name.contains(state.query().toLowerCase(Locale.ROOT));
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        layout.slot(4, (s, c) -> RNGMeterGuiSupport.meterItem(definition(), (SkyBlockPlayer) c.player()));
        if (!Components.back(layout, 48, ctx)) Components.close(layout, 48);
        Components.close(layout, 49);
        layout.slot(50, ItemStacks.item(Material.OAK_SIGN, Text.of("<a>Search"), List.of(
                        Text.of("<7>Find drops by name!"), Text.empty(), Text.of("<e>Click to search!"))),
                (_, c) -> search(c, state.query()));
        layout.slot(51, resetItem(player), (_, c) -> {
            RNGMeterService.reset((SkyBlockPlayer) c.player(), definition());
            c.session(State.class).refresh();
        });
    }

    private ItemStack.Builder resetItem(SkyBlockPlayer player) {
        RNGMeterState state = RNGMeterService.get(player, definition());
        RNGMeterReward selected = RNGMeterGuiSupport.selectedReward(definition(), state);
        return RNGMeterGuiSupport.icon(definition(), Text.of("<d>Reset RNG Drop"), List.of(
                Text.of("<7>Resets the selected drop for this"),
                Text.of("<7>RNG Meter. You still earn progress"),
                Text.of("<7>without a drop selected."),
                Text.empty(),
                Text.of("<7>Selected Drop"),
                Text.of(selected == null ? "<c>None" : selected.displayName()),
                Text.empty(),
                Text.of("<e>Click to reset!")
        ));
    }

    private void search(ViewContext ctx, String query) {
        HypixelSignGUI sign = new HypixelSignGUI(ctx.player());
        sign.open(new String[]{"Enter query", query}).thenAccept(input -> {
            if (input == null) return;
            ctx.session(State.class).setState(new State(definition().rewards().stream()
                    .map(reward -> (RNGMeterReward) reward).toList(), 0, input.trim()));
        });
    }

    private int totalPages(State state) {
        int itemCount = getFilteredItems(state).size();
        return Math.max(1, (itemCount + DEFAULT_SLOTS.length - 1) / DEFAULT_SLOTS.length);
    }

    public record State(List<RNGMeterReward> items, int page, String query)
            implements PaginatedView.PaginatedState<RNGMeterReward> {
        public State {
            items = List.copyOf(items);
            query = query == null ? "" : query;
        }

        @Override
        public State withPage(int page) {
            return new State(items, page, query);
        }

        @Override
        public State withItems(List<RNGMeterReward> items) {
            return new State(items, page, query);
        }
    }
}
