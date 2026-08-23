package net.swofty.type.bedwarslobby.gui.cosmetics.prestige;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.collectibles.CollectibleCategory;
import net.swofty.type.generic.collectibles.CollectibleDefinition;
import net.swofty.type.generic.collectibles.CollectibleSelectionCheck;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleCatalog;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleStateService;
import net.swofty.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.gui.impl.collectibles.CollectibleSelectionView;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.Comparator;
import java.util.List;

public abstract class GUIPrestigeSelection extends CollectibleSelectionView {
    private static final int[] PRESTIGE_SLOTS = {
        10, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };
    private static final int[] PRESTIGE_SLOTS_WITHOUT_FIRST = {
        13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    private final CollectibleCategory category;
    private final String itemType;
    private final boolean firstSlotIsItem;

    protected GUIPrestigeSelection(String title, CollectibleCategory category, String itemType, boolean firstSlotIsItem) {
        super(title);
        this.category = category;
        this.itemType = itemType;
        this.firstSlotIsItem = firstSlotIsItem;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return super.configuration();
    }

    @Override
    protected int[] getPaginatedSlots() {
        return firstSlotIsItem ? PRESTIGE_SLOTS : PRESTIGE_SLOTS_WITHOUT_FIRST;
    }

    @Override
    protected List<CollectibleDefinition> loadItems(HypixelPlayer player) {
        BedWarsCollectibleCatalog.initialize();
        BedWarsCollectibleStateService.reconcileSelected(player, category);
        List<CollectibleDefinition> items = BedWarsCollectibleCatalog.getCategoryItems(category);
        if (category == CollectibleCategory.PRESTIGE_SCHEMES) {
            return items.stream()
                .filter(item -> !"prestige_scheme_none".equals(item.id()))
                .toList();
        }
        return items;
    }

    @Override
    protected CollectibleSelectionCheck selectionCheck(HypixelPlayer player, CollectibleDefinition definition) {
        return BedWarsCollectibleStateService.checkSelectable(player, definition);
    }

    @Override
    protected boolean isSelected(HypixelPlayer player, CollectibleDefinition definition) {
        return BedWarsCollectibleStateService.isSelected(player, category, definition.id());
    }

    @Override
    protected SelectionOutcome selectItem(HypixelPlayer player, ViewContext ctx, CollectibleDefinition definition) {
        BedWarsCollectibleStateService.SelectionResult result = BedWarsCollectibleStateService.select(player, definition);
        return result.success() ? SelectionOutcome.success(result.message()) : SelectionOutcome.failure(result.message());
    }

    @Override
    protected boolean supportsFavorites() {
        return true;
    }

    @Override
    protected boolean isFavorite(HypixelPlayer player, CollectibleDefinition definition) {
        return BedWarsCollectibleStateService.isFavorite(player, definition.id());
    }

    @Override
    protected boolean toggleFavorite(HypixelPlayer player, CollectibleDefinition definition) {
        return BedWarsCollectibleStateService.toggleFavorite(player, definition.id());
    }

    @Override
    protected boolean isPinnedDefault(HypixelPlayer player, CollectibleDefinition definition) {
        String pinned = BedWarsCollectibleCatalog.pinnedDefaultId(category);
        return pinned != null && pinned.equals(definition.id());
    }

    @Override
    protected long tokenBalance(HypixelPlayer player) {
        BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(player);
        if (dataHandler == null) {
            return 0L;
        }
        return dataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class).getValue();
    }

    @Override
    protected ItemStack.Builder renderItem(CollectibleDefinition item, int index, HypixelPlayer player) {
        if ("prestige_scheme_none".equals(item.id())) {
            return renderNoneScheme(player, item);
        }

        ItemStack.Builder stack = super.renderItem(item, index, player);
        // The base collectible view handles state and action lore; prestige adds the exact live preview.
        return stack;
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        if (!firstSlotIsItem) {
            if (state.page() == 0) {
                BedWarsCollectibleCatalog.findItemById("prestige_scheme_none")
                    .ifPresent(item -> layout.slot(10, (s, c) -> renderNoneScheme(c.player(), item),
                        (click, context) -> onItemClick(click, context, item, 0)));
            } else {
                Components.close(layout, 10);
            }
        }

        layout.slot(11, ItemStacks.item(Material.CHEST, """
                <a>Random {}
                <7>Use a random {}!

                {}""",
            itemType,
            itemType,
            actionLine(randomSelected(ctx.player(), BedWarsCollectibleStateService.RANDOM_SELECTION_ID))
        ), (click, context) -> selectSpecial(click, context, BedWarsCollectibleStateService.RANDOM_SELECTION_ID));

        layout.slot(12, ItemStacks.item(Material.ENDER_CHEST, """
                <a>Random Favorite {}
                <7>Use a Random <6>✯ Favorite <7>{}!

                {}""",
            itemType,
            itemType,
            actionLine(randomSelected(ctx.player(), BedWarsCollectibleStateService.RANDOM_FAVORITE_SELECTION_ID))
        ), (click, context) -> selectSpecial(click, context, BedWarsCollectibleStateService.RANDOM_FAVORITE_SELECTION_ID));

        super.layoutCustom(layout, state, ctx);
    }

    @Override
    protected boolean isPreviewSupported(HypixelPlayer player, CollectibleDefinition definition) {
        return true;
    }

    @Override
    protected void preview(HypixelPlayer player, CollectibleDefinition definition, State state) {
        player.sendMessage("<7>Preview: {}", preview(player, definition));
    }

    @Override
    protected List<Text> previewLore(HypixelPlayer player, CollectibleDefinition definition) {
        return List.of(Text.of("<7>Preview: {}", preview(player, definition)));
    }

    protected Text preview(HypixelPlayer player, CollectibleDefinition definition) {
        int level = bedWarsLevel(player);
        String scheme = selected(player, CollectibleCategory.PRESTIGE_SCHEMES);
        String star = selected(player, CollectibleCategory.PRESTIGE_STARS);
        String bracket = selected(player, CollectibleCategory.PRESTIGE_BRACKETS);

        if (definition.category() == CollectibleCategory.PRESTIGE_SCHEMES) {
            scheme = definition.id();
        } else if (definition.category() == CollectibleCategory.PRESTIGE_STARS) {
            star = definition.id();
        } else if (definition.category() == CollectibleCategory.PRESTIGE_BRACKETS) {
            bracket = definition.id();
        }

        return BedWarsPrestigeRenderer.renderPreview(player, level, scheme, star, bracket)
                .append(Text.of(" {}", player.getFullDisplayName()));
    }

    @Override
    protected Comparator<CollectibleDefinition> baseComparator(SortMode sortMode) {
        if (category == CollectibleCategory.PRESTIGE_SCHEMES && sortMode == SortMode.LOWEST_RARITY_FIRST) {
            return Comparator.comparingInt(CollectibleDefinition::sortIndex);
        }
        return super.baseComparator(sortMode);
    }

    @Override
    protected boolean shouldGroupOwnedFirst(SortMode sortMode) {
        return category != CollectibleCategory.PRESTIGE_SCHEMES || sortMode != SortMode.LOWEST_RARITY_FIRST;
    }

    private ItemStack.Builder renderNoneScheme(HypixelPlayer player, CollectibleDefinition item) {
        return ItemStacks.item(Material.NAME_TAG, """
                <a>None
                <8>Prestige Scheme

                <7>Select the None Prestige Scheme for
                <7>your level to display as.

                <7>Preview: {}
                {}
                <e>Shift-click to toggle favorite!""",
            preview(player, item),
            actionLine(isSelected(player, item))
        );
    }

    private static Text actionLine(boolean selected) {
        return selected ? Text.of("<a>SELECTED!") : Text.of("<e>Click to select!");
    }

    private String selected(HypixelPlayer player, CollectibleCategory category) {
        return BedWarsCollectibleStateService.getSelectedId(player, category);
    }

    private boolean randomSelected(HypixelPlayer player, String selectionId) {
        return BedWarsCollectibleStateService.isRandomModeSelected(player, category, selectionId);
    }

    private void selectSpecial(ClickContext<State> click, ViewContext context, String selectionId) {
        BedWarsCollectibleStateService.SelectionResult result =
            BedWarsCollectibleStateService.selectSpecial(click.player(), category, selectionId);
        click.player().sendMessage(result.message());
        context.session(State.class).refresh();
    }

    private int bedWarsLevel(HypixelPlayer player) {
        BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(player);
        if (dataHandler == null) {
            return 0;
        }
        long experience = dataHandler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class).getValue();
        return net.swofty.commons.bedwars.BedwarsLevelUtil.calculateLevel(experience);
    }
}
