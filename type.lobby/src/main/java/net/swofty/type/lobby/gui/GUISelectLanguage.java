package net.swofty.type.lobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointLocale;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUISelectLanguage extends PaginatedView<DatapointLocale.SupportedLocale, GUISelectLanguage.State> {

    private static final List<DatapointLocale.SupportedLocale> LOCALES = Arrays.stream(DatapointLocale.SupportedLocale.values())
            .filter(locale -> locale != DatapointLocale.SupportedLocale.UNSET)
            .toList();

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Select Language", InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return SLIM;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected ItemStack.Builder renderItem(DatapointLocale.SupportedLocale locale, int index, HypixelPlayer player) {
        boolean selected = currentLocale(player) == locale;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Change your language to {}.", locale.getName()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Currently available:"));
        for (String game : List.of("Arcade Games", "Bed Wars", "Blitz SG", "Build Battle", "Cops and Crims",
                "Duels", "Housing", "Main Lobby", "Mega Walls", "Murder Mystery", "Pit", "Replay", "SkyBlock",
                "SkyWars", "Speed UHC", "The TNT Games", "Tournament Hall", "UHC Champions", "Warlords", "Wool Games")) {
            lore.add(Text.of("<7>   ∙ <f>{}", game));
        }
        lore.add(Text.empty());
        lore.add(Text.of(selected ? "<a>Selected!" : "<e>Click to change your language!"));
        return ItemStacks.head(locale.getIcon(), 1, Text.of("<a>{}", locale.getName()), lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, DatapointLocale.SupportedLocale locale, int index) {
        ctx.player().updateLocale(locale);
        ctx.player().closeInventory();
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, DatapointLocale.SupportedLocale item) {
        return false;
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.slot(49, ItemStacks.item(Material.ARROW, "<a>Go Back\n<7>To My Profile"), (_, viewCtx) -> {
            viewCtx.player().closeInventory();
            new GUIMyProfile().open(viewCtx.player());
        });

        layout.slot(51, ItemStacks.item(Material.BOOK,
                "<a>Help us Translate Hypixel\n<7>Help us translate Hypixel into even more languages!"));
    }

    private static DatapointLocale.SupportedLocale currentLocale(HypixelPlayer player) {
        return player.getDataHandler().get(HypixelDataHandler.Data.LOCALE, DatapointLocale.class)
                .getValue().getCurrentLocale();
    }

    @Override
    protected boolean shouldRenderNavBackground() {
        return false;
    }

    @Override
    protected void layoutBackground(ViewLayout<State> layout, State state, ViewContext ctx) {
        // i dont want a background :)
    }

    public record State(List<DatapointLocale.SupportedLocale> items,
                        int page) implements PaginatedState<DatapointLocale.SupportedLocale> {
        public State() {
            this(LOCALES, 0);
        }

        @Override
        public State withPage(int page) {
            return new State(items, page);
        }

        @Override
        public State withItems(List<DatapointLocale.SupportedLocale> items) {
            return new State(items, page);
        }
    }
}
