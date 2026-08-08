package net.swofty.type.lobby.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointLocale;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

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
        return ItemStackCreator.getStackHead(
                Component.text(locale.getName(), NamedTextColor.GREEN),
                locale.getIcon(),
                1,
                Component.text("Change your language to " + locale.getName() + ".", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Currently available:", NamedTextColor.GRAY),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Arcade Games").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Bed Wars").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Blitz SG").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Build Battle").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Cops and Crims").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Duels").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Housing").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Main Lobby").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Mega Walls").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Murder Mystery").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Pit").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Replay").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("SkyBlock").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("SkyWars").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Speed UHC").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("The TNT Games").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Tournament Hall").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("UHC Champions").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Warlords").color(NamedTextColor.WHITE)),
                Component.text("").append(Component.text("   ∙ ").color(NamedTextColor.GRAY)).append(Component.text("Wool Games").color(NamedTextColor.WHITE)),
                Component.empty(),
                selected
                        ? Component.text("Selected!", NamedTextColor.GREEN)
                        : Component.text("Click to change your language!", NamedTextColor.YELLOW)
        );
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
        layout.slot(49, ItemStackCreator.getStack(
                Component.text("Go Back", NamedTextColor.GREEN),
                Material.ARROW,
                1,
                Component.text("To My Profile", NamedTextColor.GRAY)
        ), (_, viewCtx) -> {
            viewCtx.player().closeInventory();
            new GUIMyProfile().open(viewCtx.player());
        });

        layout.slot(51, ItemStackCreator.getStack(
                Component.text("Help us Translate Hypixel", NamedTextColor.GREEN),
                Material.BOOK,
                1,
                Component.text("Help us translate Hypixel into even more languages!", NamedTextColor.GRAY)
        ));
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
