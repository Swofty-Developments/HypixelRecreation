package net.swofty.type.generic.gui.impl.collectibles;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.collectibles.CollectibleCategory;
import net.swofty.type.generic.collectibles.CollectibleDefinition;
import net.swofty.type.generic.collectibles.CollectibleDescriptionService;
import net.swofty.type.generic.collectibles.CollectibleEvent;
import net.swofty.type.generic.collectibles.CollectibleRarity;
import net.swofty.type.generic.collectibles.CollectibleSelectionCheck;
import net.swofty.type.generic.collectibles.CollectibleUnlockMethod;
import net.swofty.type.generic.collectibles.CollectibleUnlockRequirement;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CollectibleSelectionView extends StatefulPaginatedView<CollectibleDefinition, CollectibleSelectionView.State> {

    private final String title;

    protected CollectibleSelectionView(String title) {
        this.title = title;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    protected void layoutBackground(ViewLayout<State> layout, State state, ViewContext ctx) {
        // nothing
    }

    @Override
    protected boolean shouldRenderNavBackground() {
        return false;
    }

    @Override
    public State initialState() {
        return new State(List.of(), 0, SortMode.HIGHEST_RARITY_FIRST, true);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        List<CollectibleDefinition> items = loadItems(ctx.player());
        ctx.session(State.class).update(current -> sortState(current.withItems(items).withPage(0), ctx.player()));
    }

    @Override
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
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
    protected ItemStack.Builder renderItem(CollectibleDefinition item, int index, HypixelPlayer player) {
        CollectibleSelectionCheck check = selectionCheck(player, item);
        boolean selected = isSelected(player, item);
        boolean favoriteable = isCategoryFavoriteable(item.category());
        boolean favorite = favoriteable && isFavorite(player, item);
        boolean selectable = check.selectable();
        CollectibleUnlockRequirement requirement = item.unlockRequirement();
        CollectibleEvent event = requirement.event();

        Long configuredCost = requirement.cost();
        long cost = configuredCost != null ? configuredCost : 0L;
        boolean currencyUnlock = requirement.method() == CollectibleUnlockMethod.CURRENCY && cost > 0L;
        boolean eventAvailable = event == null || event.isAvailableNow();
        boolean hasMoney = !currencyUnlock || tokenBalance(player) >= cost;
        boolean showInsufficientTokens = !selectable
            && currencyUnlock
            && eventAvailable
            && isCostReason(check.reason())
            && !hasMoney;
        boolean showRarity = item.rarity().getWeight() > 2;
        boolean hasDetailSection = event != null || showRarity;

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{}", item.category().getDisplayName()));

        List<Text> descriptionLore = CollectibleDescriptionService.resolveLore(item);
        if (!descriptionLore.isEmpty()) {
            lore.add(Text.empty());
            lore.addAll(descriptionLore);
        }

        List<Text> previewLore = previewLore(player, item);
        if (!previewLore.isEmpty()) {
            lore.add(Text.empty());
            lore.addAll(previewLore);
        }

        if (isPreviewSupported(player, item)) {
            lore.add(Text.empty());
            lore.add(Text.of("<e>Right-Click to preview!"));
        }

        if (hasDetailSection) {
            lore.add(Text.empty());

            if (event != null) {
                lore.add(Text.of("<7>Event: <b>{}", event.displayName()));
                if (showRarity) {
                    lore.add(Text.empty());
                }
            }

            if (showRarity) {
                lore.add(Text.of("<7>Rarity: {}", item.rarity().formattedName()));
            }

            lore.add(Text.empty());
        }

        if (selectable) {
            lore.add(Text.of(selected ? "<a>Selected!" : "<e>Click to select!"));

            if (favoriteable) {
                lore.add(Text.of("<e>Shift-click to toggle favorite!"));
            }
        } else {
            if (!hasDetailSection) {
                lore.add(Text.empty());
            }

            String reason = check.reason() != null ? check.reason() : "<c>Locked.";
            lore.addAll(legacyLines(reason));

            if (showInsufficientTokens) {
                lore.add(Text.empty());
                lore.addAll(Text.of("<c><wrap:38>You don't have enough tokens to buy that!</wrap>").lines());
            }
        }

        String namePrefix = check.selectable() ? "<a>" : "<c>";
        Text itemNameText = favorite
            ? Text.of("<6>✯ " + namePrefix + "{}", item.name())
            : Text.of(namePrefix + "{}", item.name());
        if (item.iconTexture() != null && !item.iconTexture().isBlank()) {
            return ItemStacks.clearAttributes(ItemStacks.head(item.iconTexture(), itemNameText, lore));
        }

        Material iconMaterial = item.iconMaterial() != null ? item.iconMaterial() : Material.BARRIER;
        return ItemStacks.item(iconMaterial, 1, itemNameText, lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, CollectibleDefinition item, int index) {
        if (isCategoryFavoriteable(item.category())
            && (click.click() instanceof Click.LeftShift || click.click() instanceof Click.RightShift)) {
            CollectibleSelectionCheck check = selectionCheck(click.player(), item);
            if (!check.selectable()) {
                String reason = check.reason() != null ? check.reason() : "Locked.";
                click.player().sendMessage(bottomLineFailureMessage(reason));
                ctx.session(State.class).refresh();
                return;
            }

            boolean favorite = toggleFavorite(click.player(), item);
            click.player().sendMessage(favorite ? "<a>Added to favorites." : "<c>Removed from favorites.");
            ctx.session(State.class).refresh();
            return;
        }

        if (click.click() instanceof Click.Right && isPreviewSupported(click.player(), item)) {
            preview(click.player(), item, click.state());
            return;
        }

        SelectionOutcome outcome = selectItem(click.player(), ctx, item);
        if (outcome.message() != null && !outcome.message().isBlank()) {
            if (outcome.success()) {
                String message = outcome.message();
                click.player().sendMessage(message);
            } else {
                click.player().sendMessage(bottomLineFailureMessage(outcome.message()));
            }
        }
        ctx.session(State.class).refresh();
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, CollectibleDefinition item) {
        return false;
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.backOrClose(layout, 48, ctx);

        layout.slot(49, (s, c) -> ItemStacks.item(Material.EMERALD, 1, """
            <7>Total {}: <2>{:,}
            <6>https://store.hypixel.net""", tokenCurrencyLabel(), tokenBalance(c.player())));

        layout.slot(50, (s, c) -> ItemStacks.item(Material.HOPPER, 1,
            "<6>Sorted by: <a>{}\n"
                + "<7>Sort order: <a>{}\n"
                + "\n"
                + "<7>Next sort: <a>{}\n"
                + "<e>Left click to use!\n"
                + "\n"
                + "<7>Owned items first: " + (s.ownedFirst() ? "<a>Yes" : "<c>No") + "\n"
                + "<e>Right click to toggle!",
            s.sortMode().displayName, s.sortMode().displayName, s.sortMode().next().displayName
        ), (click, context) -> {
            if (click.click() instanceof Click.Right) {
                context.session(State.class).update(current ->
                    sortState(current.withOwnedFirst(!current.ownedFirst()), click.player()));
                return;
            }

            context.session(State.class).update(current ->
                sortState(current.withSortMode(current.sortMode().next()), click.player()));
        });
    }

    protected abstract List<CollectibleDefinition> loadItems(HypixelPlayer player);

    protected abstract CollectibleSelectionCheck selectionCheck(HypixelPlayer player, CollectibleDefinition definition);

    protected abstract boolean isSelected(HypixelPlayer player, CollectibleDefinition definition);

    protected abstract SelectionOutcome selectItem(HypixelPlayer player, ViewContext ctx, CollectibleDefinition definition);

    protected long tokenBalance(HypixelPlayer player) {
        return 0L;
    }

    protected String tokenCurrencyLabel() {
        return "Tokens";
    }

    protected boolean isCategoryFavoriteable(CollectibleCategory category) {
        return supportsFavorites();
    }

    protected boolean supportsFavorites() {
        return false;
    }

    protected boolean isPreviewSupported(HypixelPlayer player, CollectibleDefinition definition) {
        return false;
    }

    protected List<Text> previewLore(HypixelPlayer player, CollectibleDefinition definition) {
        return List.of();
    }

    protected boolean isFavorite(HypixelPlayer player, CollectibleDefinition definition) {
        return false;
    }

    protected boolean toggleFavorite(HypixelPlayer player, CollectibleDefinition definition) {
        return false;
    }

    protected void preview(HypixelPlayer player, CollectibleDefinition definition, State state) {
        player.notImplemented();
    }

    private State sortState(State state, HypixelPlayer player) {
        List<CollectibleDefinition> sorted = sortItems(state.items(), state.sortMode(), state.ownedFirst(), player);
        return state.withItems(sorted);
    }

    private List<CollectibleDefinition> sortItems(
        List<CollectibleDefinition> items,
        SortMode sortMode,
        boolean ownedFirst,
        HypixelPlayer player
    ) {
        Comparator<CollectibleDefinition> baseComparator = baseComparator(sortMode);
        Map<String, Boolean> selectableById = new HashMap<>();
        Map<String, Boolean> pinnedById = new HashMap<>();
        for (CollectibleDefinition definition : items) {
            selectableById.put(definition.id(), selectionCheck(player, definition).selectable());
            pinnedById.put(definition.id(), isPinnedDefault(player, definition));
        }

        Comparator<CollectibleDefinition> comparator = Comparator
            .comparing((CollectibleDefinition definition) -> !pinnedById.getOrDefault(definition.id(), false));

        if (ownedFirst && shouldGroupOwnedFirst(sortMode)) {
            comparator = comparator.thenComparing((CollectibleDefinition definition) -> !selectableById.getOrDefault(definition.id(), false));
        }

        comparator = comparator.thenComparing(baseComparator);

        return items.stream().sorted(comparator).toList();
    }

    protected Comparator<CollectibleDefinition> baseComparator(SortMode sortMode) {
        return switch (sortMode) {
            case HIGHEST_RARITY_FIRST -> Comparator
                .comparingInt((CollectibleDefinition definition) -> rarityWeight(definition.rarity()))
                .reversed()
                .thenComparing(CollectibleDefinition::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(CollectibleDefinition::sortIndex);
            case LOWEST_RARITY_FIRST -> Comparator
                .comparingInt((CollectibleDefinition definition) -> rarityWeight(definition.rarity()))
                .thenComparing(CollectibleDefinition::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(CollectibleDefinition::sortIndex);
            case A_TO_Z -> Comparator
                .comparing(CollectibleDefinition::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(CollectibleDefinition::sortIndex);
        };
    }

    protected boolean shouldGroupOwnedFirst(SortMode sortMode) {
        return true;
    }

    protected boolean isPinnedDefault(HypixelPlayer player, CollectibleDefinition definition) {
        return false;
    }

    private int rarityWeight(CollectibleRarity rarity) {
        return rarity != null ? rarity.getWeight() : CollectibleRarity.COMMON.getWeight();
    }

    protected List<Text> legacyLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        List<Text> parsed = new ArrayList<>();
        for (String line : lines) {
            parsed.add(Text.read(line));
        }
        return parsed;
    }

    private boolean isCostReason(String reason) {
        return reason != null && reason.contains("Cost:");
    }

    private Text bottomLineFailureMessage(String message) {
        if (message == null || message.isBlank()) {
            return Text.of("<c>Action failed.");
        }

        String normalized = message.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.indexOf('§') >= 0) {
                return Text.legacy(line);
            }
            if (line.indexOf('<') >= 0) {
                return Text.parse(line);
            }
            return Text.of("<c>{}", line);
        }

        return Text.of("<c>Action failed.");
    }

    public record SelectionOutcome(boolean success, String message) {
        public static SelectionOutcome success(String message) {
            return new SelectionOutcome(true, message);
        }

        public static SelectionOutcome failure(String message) {
            return new SelectionOutcome(false, message);
        }
    }

    public enum SortMode {
        HIGHEST_RARITY_FIRST("Highest rarity first"),
        A_TO_Z("A to Z"),
        LOWEST_RARITY_FIRST("Lowest rarity first");

        private final String displayName;

        SortMode(String displayName) {
            this.displayName = displayName;
        }

        public SortMode next() {
            return switch (this) {
                case HIGHEST_RARITY_FIRST -> A_TO_Z;
                case A_TO_Z -> LOWEST_RARITY_FIRST;
                case LOWEST_RARITY_FIRST -> HIGHEST_RARITY_FIRST;
            };
        }
    }

    public record State(
        List<CollectibleDefinition> items,
        int page,
        SortMode sortMode,
        boolean ownedFirst
    ) implements PaginatedState<CollectibleDefinition> {

        @Override
        public State withPage(int page) {
            return new State(items, page, sortMode, ownedFirst);
        }

        @Override
        public State withItems(List<CollectibleDefinition> items) {
            return new State(items, page, sortMode, ownedFirst);
        }

        public State withSortMode(SortMode sortMode) {
            return new State(items, page, sortMode, ownedFirst);
        }

        public State withOwnedFirst(boolean ownedFirst) {
            return new State(items, page, sortMode, ownedFirst);
        }
    }
}
