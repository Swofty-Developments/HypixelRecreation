package net.swofty.type.bedwarslobby.gui.cosmetics;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.bedwarslobby.gui.cosmetics.prestige.GUIPrestigeBrackets;
import net.swofty.type.bedwarslobby.gui.cosmetics.prestige.GUIPrestigeSchemes;
import net.swofty.type.bedwarslobby.gui.cosmetics.prestige.GUIPrestigeStars;
import net.swofty.type.generic.collectibles.CollectibleCategory;
import net.swofty.type.generic.collectibles.CollectibleDefinition;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleCatalog;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleStateService;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;
import java.util.Optional;

public class GUIPrestigeCustomizer extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Prestige Customizer", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        CategorySummary schemes = summarize(ctx.player(), CollectibleCategory.PRESTIGE_SCHEMES);
        CategorySummary stars = summarize(ctx.player(), CollectibleCategory.PRESTIGE_STARS);
        CategorySummary brackets = summarize(ctx.player(), CollectibleCategory.PRESTIGE_BRACKETS);

        Components.backOrClose(layout, 48, ctx);

        layout.slot(19, ItemStacks.item(Material.ORANGE_DYE, """
                <a>Prestige Schemes\s
                <7>Customize what colors are in your
                <7>prestige.

                <7>Unlocked: <a>{}/{} <8>({}%)
                <7>Currently Selected:
                <a>{}

                <e>Click to view!""",
            schemes.unlocked(), schemes.total(), schemes.percent(), schemes.selectedName()
        ), (_, context) -> context.push(new GUIPrestigeSchemes()));
        layout.slot(21, ItemStacks.item(Material.NETHER_STAR, """
                <a>Prestige Stars\s
                <7>Customize what star icon is within
                <7>your prestige.

                <7>Unlocked: <a>{}/{} <8>({}%)
                <7>Currently Selected:
                <a>{}

                <e>Click to view!""",
            stars.unlocked(), stars.total(), stars.percent(), stars.selectedName()
        ), (_, context) -> context.push(new GUIPrestigeStars()));
        layout.slot(23, ItemStacks.item(Material.OAK_FENCE, """
                <a>Prestige Brackets\s
                <7>Customize what brackets surround
                <7>your prestige.

                <7>Unlocked: <a>{}/{} <8>({}%)
                <7>Currently Selected:
                <a>{}

                <e>Click to view!""",
            brackets.unlocked(), brackets.total(), brackets.percent(), brackets.selectedName()
        ), (_, context) -> context.push(new GUIPrestigeBrackets()));
        layout.slot(25, ItemStacks.item(Material.RED_STAINED_GLASS, """
                <c>Prestige Formatting
                <8>You'll need to talk to the Hotel Owner
                <8>first..."""));
        layout.slot(49, ItemStacks.item(Material.EMERALD, """
                <7>Total Tokens: <2>{:,}
                <6>https://store.hypixel.net""", tokenBalance(ctx.player())));
        layout.slot(50, ItemStacks.item(Material.COMPASS, """
                <a>Search
                <7>Use this feature to easily find a
                <7>specific cosmetic item."""));
    }

    private CategorySummary summarize(HypixelPlayer player, CollectibleCategory category) {
        BedWarsCollectibleCatalog.initialize();
        BedWarsCollectibleStateService.reconcileSelected(player, category);
        List<CollectibleDefinition> items = BedWarsCollectibleCatalog.getCategoryItems(category);
        long unlocked = items.stream()
            .filter(item -> BedWarsCollectibleStateService.checkSelectable(player, item).selectable())
            .count();
        String selectedId = BedWarsCollectibleStateService.getSelectedId(player, category);
        String selectedName = Optional.ofNullable(selectedId)
            .flatMap(BedWarsCollectibleCatalog::findItemById)
            .map(CollectibleDefinition::name)
            .orElse("None");
        return new CategorySummary((int) unlocked, items.size(), selectedName);
    }

    private long tokenBalance(HypixelPlayer player) {
        BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(player);
        if (dataHandler == null) {
            return 0L;
        }
        return dataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class).getValue();
    }

    private record CategorySummary(int unlocked, int total, String selectedName) {
        private int percent() {
            return total == 0 ? 0 : (int) Math.floor(unlocked * 100.0 / total);
        }
    }
}
