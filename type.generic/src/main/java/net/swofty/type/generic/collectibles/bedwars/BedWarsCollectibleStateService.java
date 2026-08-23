package net.swofty.type.generic.collectibles.bedwars;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.collectibles.CollectibleCategory;
import net.swofty.type.generic.collectibles.CollectibleCurrency;
import net.swofty.type.generic.collectibles.CollectibleDefinition;
import net.swofty.type.generic.collectibles.CollectibleEvaluator;
import net.swofty.type.generic.collectibles.CollectibleSelectionCheck;
import net.swofty.type.generic.collectibles.CollectibleUnlockMethod;
import net.swofty.type.generic.data.datapoints.DatapointCollectibles;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class BedWarsCollectibleStateService {

    public static final String RANDOM_SELECTION_ID = "random";
    public static final String RANDOM_FAVORITE_SELECTION_ID = "random_favorite";

    private BedWarsCollectibleStateService() {
    }

    public static SelectionResult select(HypixelPlayer player, CollectibleDefinition definition) {
        DatapointCollectibles.CollectiblesState state = state(player);
        CollectibleSelectionCheck check = checkSelectable(player, state, definition);
        if (!check.selectable()) {
            String reason = check.reason() != null ? check.reason() : "Locked.";
            return new SelectionResult(false, "<c>" + reason);
        }

        state.setSelected(definition.category(), definition.id());
        persist(player, state);
        return new SelectionResult(true, Text.of("<a>Selected {}<a>.", definition.name()).serialize());
    }

    public static SelectionResult selectSpecial(HypixelPlayer player, CollectibleCategory category, String selectionId) {
        if (!BedWarsCollectibleCatalog.categorySupportsRandom(category)) {
            return new SelectionResult(false, "<c>Random mode is not supported for this category.");
        }

        if (!RANDOM_SELECTION_ID.equals(selectionId) && !RANDOM_FAVORITE_SELECTION_ID.equals(selectionId)) {
            return new SelectionResult(false, "<c>Invalid random selection mode.");
        }

        DatapointCollectibles.CollectiblesState state = state(player);
        state.setSelected(category, selectionId);
        persist(player, state);

        if (RANDOM_FAVORITE_SELECTION_ID.equals(selectionId)) {
            return new SelectionResult(true, "<a>Selected Random Favorite mode.");
        }
        return new SelectionResult(true, "<a>Selected Random mode.");
    }

    public static SelectionResult purchaseAndSelect(HypixelPlayer player, CollectibleDefinition definition) {
        DatapointCollectibles.CollectiblesState state = state(player);
        PurchaseCheck purchaseCheck = checkPurchase(player, state, definition);
        if (!purchaseCheck.purchasable()) {
            return new SelectionResult(false, "<c>" + purchaseCheck.reason());
        }

        BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(player);
        DatapointLeaderboardLong tokensDp = dataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class);
        long currentTokens = tokensDp.getValue();
        long cost = purchaseCheck.cost();
        if (currentTokens < cost) {
            return new SelectionResult(false, "<c>You do not have enough BedWars Tokens.");
        }

        tokensDp.setValue(currentTokens - cost);
        state.unlock(definition.category(), definition.id());
        state.setSelected(definition.category(), definition.id());
        persist(player, state);
        return new SelectionResult(true, Text.of("<6>You purchased <a>{}<6>!", definition.name()).serialize());
    }

    public static PurchaseCheck checkPurchase(HypixelPlayer player, CollectibleDefinition definition) {
        return checkPurchase(player, state(player), definition);
    }

    public static boolean isRandomModeSelected(HypixelPlayer player, CollectibleCategory category, String selectionId) {
        return selectionId.equals(state(player).getSelected(category));
    }

    public static String getSelectedId(HypixelPlayer player, CollectibleCategory category) {
        return state(player).getSelected(category);
    }

    public static Optional<CollectibleDefinition> resolveSelected(HypixelPlayer player, CollectibleCategory category) {
        return reconcileSelected(player, category);
    }

    public static CollectibleSelectionCheck checkSelectable(HypixelPlayer player, CollectibleDefinition definition) {
        return checkSelectable(player, state(player), definition);
    }

    public static Optional<CollectibleDefinition> reconcileSelected(HypixelPlayer player, CollectibleCategory category) {
        List<CollectibleDefinition> definitions = BedWarsCollectibleCatalog.getCategoryItems(category);
        if (definitions.isEmpty()) {
            return Optional.empty();
        }

        DatapointCollectibles.CollectiblesState state = state(player);
        String selectedId = state.getSelected(category);

        if ((RANDOM_SELECTION_ID.equals(selectedId) || RANDOM_FAVORITE_SELECTION_ID.equals(selectedId))
            && BedWarsCollectibleCatalog.categorySupportsRandom(category)) {
            return resolveRandomSelection(player, state, category, definitions, selectedId);
        }

        CollectibleDefinition selectedDefinition = null;
        if (selectedId != null && !selectedId.isBlank()) {
            for (CollectibleDefinition definition : definitions) {
                if (definition.id().equals(selectedId)) {
                    selectedDefinition = definition;
                    break;
                }
            }
        }

        if (selectedDefinition != null && checkSelectable(player, state, selectedDefinition).selectable()) {
            return Optional.of(selectedDefinition);
        }

        Optional<CollectibleDefinition> fallback = nextSelectable(player, state, category, definitions);
        if (fallback.isPresent()) {
            state.setSelected(category, fallback.get().id());
            persist(player, state);
            return fallback;
        }

        CollectibleDefinition first = definitions.get(0);
        state.setSelected(category, first.id());
        persist(player, state);
        return Optional.of(first);
    }

    public static boolean isSelected(HypixelPlayer player, CollectibleCategory category, String collectibleId) {
        return collectibleId.equals(state(player).getSelected(category));
    }

    public static boolean isFavorite(HypixelPlayer player, String collectibleId) {
        return state(player).isFavorite(collectibleId);
    }

    public static boolean toggleFavorite(HypixelPlayer player, String collectibleId) {
        Optional<CollectibleDefinition> definition = BedWarsCollectibleCatalog.findItemById(collectibleId);
        if (definition.isEmpty() || !BedWarsCollectibleCatalog.categorySupportsFavorites(definition.get().category())) {
            return false;
        }

        DatapointCollectibles.CollectiblesState state = state(player);
        boolean favorite = state.toggleFavorite(collectibleId);
        persist(player, state);
        return favorite;
    }

    public static void unlock(HypixelPlayer player, CollectibleCategory category, String collectibleId) {
        DatapointCollectibles.CollectiblesState state = state(player);
        state.unlock(category, collectibleId);
        persist(player, state);
    }

    private static Optional<CollectibleDefinition> resolveRandomSelection(
        HypixelPlayer player,
        DatapointCollectibles.CollectiblesState state,
        CollectibleCategory category,
        List<CollectibleDefinition> definitions,
        String selectedId
    ) {
        List<CollectibleDefinition> selectable = definitions.stream()
            .filter(definition -> checkSelectable(player, state, definition).selectable())
            .toList();

        if (selectable.isEmpty()) {
            return Optional.empty();
        }

        if (RANDOM_FAVORITE_SELECTION_ID.equals(selectedId)) {
            List<CollectibleDefinition> favorites = selectable.stream()
                .filter(definition -> state.isFavorite(definition.id()))
                .toList();

            if (!favorites.isEmpty()) {
                return Optional.of(favorites.get(ThreadLocalRandom.current().nextInt(favorites.size())));
            }
            return Optional.of(selectable.get(ThreadLocalRandom.current().nextInt(selectable.size())));
        }

        if (RANDOM_SELECTION_ID.equals(selectedId)) {
            return Optional.of(selectable.get(ThreadLocalRandom.current().nextInt(selectable.size())));
        }

        return Optional.empty();
    }

    private static Optional<CollectibleDefinition> nextSelectable(
        HypixelPlayer player,
        DatapointCollectibles.CollectiblesState state,
        CollectibleCategory category,
        List<CollectibleDefinition> definitions
    ) {
        String pinnedDefault = BedWarsCollectibleCatalog.pinnedDefaultId(category);
        if (pinnedDefault != null && !pinnedDefault.isBlank()) {
            for (CollectibleDefinition definition : definitions) {
                if (definition.id().equals(pinnedDefault) && checkSelectable(player, state, definition).selectable()) {
                    return Optional.of(definition);
                }
            }
        }

        for (CollectibleDefinition candidate : definitions) {
            if (checkSelectable(player, state, candidate).selectable()) {
                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    private static CollectibleSelectionCheck checkSelectable(
        HypixelPlayer player,
        DatapointCollectibles.CollectiblesState state,
        CollectibleDefinition definition
    ) {
        boolean manuallyUnlocked = state.isManuallyUnlocked(definition.category(), definition.id());
        return CollectibleEvaluator.checkSelectable(player, definition, manuallyUnlocked);
    }

    private static PurchaseCheck checkPurchase(
        HypixelPlayer player,
        DatapointCollectibles.CollectiblesState state,
        CollectibleDefinition definition
    ) {
        if (state.isManuallyUnlocked(definition.category(), definition.id())) {
            return new PurchaseCheck(false, 0L, null, "This collectible is already unlocked.");
        }

        if (definition.unlockRequirement().method() != CollectibleUnlockMethod.CURRENCY) {
            return new PurchaseCheck(false, 0L, null, "This collectible cannot be purchased.");
        }

        CollectibleCurrency currency = definition.unlockRequirement().currency();
        if (currency != CollectibleCurrency.BEDWARS_TOKENS) {
            return new PurchaseCheck(false, 0L, currency, "This collectible cannot be purchased with BedWars Tokens.");
        }

        Long configuredCost = definition.unlockRequirement().cost();
        long cost = configuredCost == null ? 0L : configuredCost;
        if (cost <= 0) {
            return new PurchaseCheck(false, 0L, currency, "This collectible has invalid pricing data.");
        }

        CollectibleSelectionCheck selectableCheck = checkSelectable(player, state, definition);
        if (selectableCheck.selectable()) {
            return new PurchaseCheck(false, cost, currency, "This collectible is already selectable.");
        }

        String reason = selectableCheck.reason() == null ? "This collectible is currently unavailable." : selectableCheck.reason();
        if (!isCostReason(reason)) {
            return new PurchaseCheck(false, cost, currency, reason);
        }

        BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(player);
        DatapointLeaderboardLong tokensDp = dataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class);
        long currentTokens = tokensDp.getValue();
        if (currentTokens < cost) {
            return new PurchaseCheck(false, cost, currency, "You do not have enough BedWars Tokens.");
        }

        return new PurchaseCheck(true, cost, currency, null);
    }

    private static DatapointCollectibles.CollectiblesState state(HypixelPlayer player) {
        BedWarsCollectibleCatalog.initialize();
        DatapointCollectibles.CollectiblesState state = datapoint(player).getValue();
        state.ensureCategoryKeys(BedWarsCollectibleCatalog.knownCategories());
        return state;
    }

    private static boolean isCostReason(String reason) {
        return reason != null && reason.contains("Cost:");
    }

    private static DatapointCollectibles datapoint(HypixelPlayer player) {
        BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(player);
        if (dataHandler == null) {
            throw new IllegalStateException("BedWars data handler is not loaded for player " + player.getUuid());
        }
        return dataHandler.get(BedWarsDataHandler.Data.COLLECTIBLES, DatapointCollectibles.class);
    }

    private static void persist(HypixelPlayer player, DatapointCollectibles.CollectiblesState state) {
        datapoint(player).setValue(state.copy());
    }

    public record SelectionResult(boolean success, String message) {
    }

    public record PurchaseCheck(boolean purchasable, long cost, CollectibleCurrency currency, String reason) {
    }
}
