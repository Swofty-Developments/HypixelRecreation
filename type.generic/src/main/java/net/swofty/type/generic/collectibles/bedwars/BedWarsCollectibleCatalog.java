package net.swofty.type.generic.collectibles.bedwars;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.YamlFileUtils;
import net.swofty.type.generic.collectibles.*;
import net.swofty.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeDefinitions;
import net.swofty.type.generic.collectibles.bedwars.prestige.PrestigeCollectibleFactory;
import net.swofty.type.generic.user.categories.Rank;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class BedWarsCollectibleCatalog extends CollectibleCatalog {

    private static final File CATALOG_FILE = new File("./configuration/bedwars/cosmetics.yml");
    private static final BedWarsCollectibleCatalog INSTANCE = new BedWarsCollectibleCatalog();

    @Getter
    private static boolean initialized = false;

    private final Map<CollectibleCategory, CategorySettings> categorySettings = new EnumMap<>(CollectibleCategory.class);

    private static final String DEFAULT_WOOD_ID = "oak_plank";
    private static final String DEFAULT_SHOPKEEPER_ID = "blacksmith";

    private BedWarsCollectibleCatalog() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        INSTANCE.loadInternal();
        initialized = true;
    }

    public static synchronized void reload() {
        INSTANCE.loadInternal();
        initialized = true;
    }

    public static List<CollectibleDefinition> getCategoryItems(CollectibleCategory category) {
        initialize();
        return INSTANCE.getByCategory(category);
    }

    public static Optional<CollectibleDefinition> findItemById(String id) {
        initialize();
        return INSTANCE.findById(id);
    }

    public static Set<CollectibleCategory> knownCategories() {
        initialize();
        return INSTANCE.getKnownCategories();
    }

    public static boolean categorySupportsFavorites(CollectibleCategory category) {
        initialize();
        return INSTANCE.categorySettings.getOrDefault(category, CategorySettings.DEFAULT).favoriteable();
    }

    public static boolean categorySupportsRandom(CollectibleCategory category) {
        initialize();
        return INSTANCE.categorySettings.getOrDefault(category, CategorySettings.DEFAULT).random();
    }

    public static String pinnedDefaultId(CollectibleCategory category) {
        initialize();
        return INSTANCE.categorySettings.getOrDefault(category, CategorySettings.DEFAULT).defaultSelectionId();
    }

    public static String categoryDescriptionKey(CollectibleCategory category) {
        initialize();
        return INSTANCE.categorySettings.getOrDefault(category, CategorySettings.DEFAULT).descriptionKey();
    }

    private void loadInternal() {
        clear();
        categorySettings.clear();
        for (CollectibleCategory category : CollectibleCategory.bedWarsCategories()) {
            registerCategory(category);
            categorySettings.put(category, CategorySettings.DEFAULT);
        }
        categorySettings.put(CollectibleCategory.PRESTIGE_SCHEMES, new CategorySettings(
            true, true, BedWarsPrestigeDefinitions.DEFAULT_SCHEME_ID, null));
        categorySettings.put(CollectibleCategory.PRESTIGE_STARS, new CategorySettings(
            true, true, BedWarsPrestigeDefinitions.DEFAULT_STAR_ID, null));
        categorySettings.put(CollectibleCategory.PRESTIGE_BRACKETS, new CategorySettings(
            true, true, BedWarsPrestigeDefinitions.DEFAULT_BRACKET_ID, null));

        if (!CATALOG_FILE.exists()) {
            Logger.warn("BedWars collectibles configuration not found: {}", CATALOG_FILE.getAbsolutePath());
            sortAll();
            return;
        }

        try {
            Map<String, Object> yaml = YamlFileUtils.loadYaml(CATALOG_FILE);
            if (yaml != null) {
                parseCategories(yaml.get("categories"));
            }
        } catch (IOException exception) {
            Logger.error(exception, "Failed loading BedWars collectibles from {}", CATALOG_FILE.getAbsolutePath());
        } catch (Exception exception) {
            Logger.error(exception, "Failed parsing BedWars collectibles from {}", CATALOG_FILE.getAbsolutePath());
        }

        registerPrestigeDefinitions();
        sortAll();
        Logger.info("Loaded {} BedWars collectibles from {}", getAll().size(), CATALOG_FILE.getAbsolutePath());
    }

    private void registerPrestigeDefinitions() {
        for (CollectibleDefinition definition : PrestigeCollectibleFactory.definitions()) {
            try {
                register(definition);
            } catch (IllegalArgumentException exception) {
                Logger.warn(exception, "Skipping duplicate BedWars prestige collectible '{}'", definition.id());
            }
        }
    }

    private void parseCategories(Object categoriesRaw) {
        if (!(categoriesRaw instanceof Map<?, ?> categories)) {
            return;
        }

        for (Map.Entry<?, ?> categoryEntry : categories.entrySet()) {
            String categoryKey = String.valueOf(categoryEntry.getKey());
            Optional<CollectibleCategory> parsedCategory = CollectibleCategory.fromKey(categoryKey);
            if (parsedCategory.isEmpty()) {
                Logger.warn("Unknown BedWars collectible category '{}' in {}", categoryKey, CATALOG_FILE.getName());
                continue;
            }

            CollectibleCategory category = parsedCategory.get();
            registerCategory(category);

            if (!(categoryEntry.getValue() instanceof Map<?, ?> categoryData)) {
                continue;
            }

            categorySettings.put(category, parseCategorySettings(category, categoryData));

            Object itemsRaw = categoryData.get("items");
            if (!(itemsRaw instanceof List<?> items)) {
                continue;
            }

            int autoSortIndex = 0;
            for (Object itemRaw : items) {
                if (!(itemRaw instanceof Map<?, ?> itemDataRaw)) {
                    continue;
                }
                CollectibleDefinition definition = parseDefinition(category, itemDataRaw, autoSortIndex++);
                if (definition == null) {
                    continue;
                }
                try {
                    register(definition);
                } catch (IllegalArgumentException exception) {
                    Logger.warn(exception, "Skipping duplicate BedWars collectible '{}'", definition.id());
                }
            }
        }
    }

    private CollectibleDefinition parseDefinition(CollectibleCategory category, Map<?, ?> rawData, int autoSortIndex) {
        String id = stringValue(rawData.get("id"));
        if (id == null || id.isBlank()) {
            return null;
        }

        String name = stringValue(rawData.get("name"));
        if (name == null || name.isBlank()) {
            name = id;
        }

        Material iconMaterial = parseMaterial(stringValue(rawData.containsKey("iconMaterial")
                ? rawData.get("iconMaterial") : rawData.get("material")));
        String iconTexture = stringValue(rawData.containsKey("iconTexture")
                ? rawData.get("iconTexture") : rawData.get("texture"));

        List<String> description = stringList(rawData.get("description"));
        String categoryDescriptionKey = categorySettings.getOrDefault(category, CategorySettings.DEFAULT).descriptionKey();
        Map<String, String> customData = new HashMap<>(stringMap(rawData.get("custom")));
        String mapData = stringValue(rawData.get("item"));
        if (mapData != null && !mapData.isBlank()) customData.put("item", mapData);
        CollectibleRarity rarity = CollectibleRarity.fromString(stringValue(rawData.get("rarity")), CollectibleRarity.COMMON);
        int sortIndex = intValue(rawData.get("sortIndex"), autoSortIndex);
        String selectionValue = stringValue(rawData.get("selectionValue"));

        CollectibleUnlockRequirement unlockRequirement = parseUnlockRequirement(rawData.get("unlock"));

        return new CollectibleDefinition(
            id,
            CollectibleGamemode.BEDWARS,
            category,
            name,
            iconMaterial,
            iconTexture,
            description,
            categoryDescriptionKey,
            customData,
            rarity,
            sortIndex,
            selectionValue,
            unlockRequirement
        );
    }

    private CategorySettings parseCategorySettings(CollectibleCategory category, Map<?, ?> categoryData) {
        boolean favoriteable = booleanValue(categoryData.get("favoriteable"), false);
        boolean random = booleanValue(categoryData.get("random"), false);
        String defaultSelectionId = stringValue(categoryData.get("defaultSelectionId"));
        String descriptionKey = stringValue(categoryData.get("descriptionKey"));

        if (defaultSelectionId == null) {
            if (category == CollectibleCategory.WOOD_SKINS) {
                defaultSelectionId = DEFAULT_WOOD_ID;
            } else if (category == CollectibleCategory.SHOPKEEPER_SKINS) {
                defaultSelectionId = DEFAULT_SHOPKEEPER_ID;
            }
        }

        return new CategorySettings(favoriteable, random, defaultSelectionId, descriptionKey);
    }

    private CollectibleUnlockRequirement parseUnlockRequirement(Object raw) {
        if (!(raw instanceof Map<?, ?> unlockMap)) {
            return CollectibleUnlockRequirement.free();
        }

        CollectibleUnlockMethod method = CollectibleUnlockMethod.fromString(
            stringValue(unlockMap.get("method")),
            CollectibleUnlockMethod.FREE
        );
        Rank requiredRank = parseRank(stringValue(unlockMap.get("requiredRank")));
        CollectibleCurrency currency = CollectibleCurrency.fromString(
            stringValue(unlockMap.get("currency")),
            CollectibleCurrency.BEDWARS_TOKENS
        );
        Long cost = longValue(unlockMap.get("cost"));
        String customResolverKey = stringValue(unlockMap.get("customResolverKey"));
        String customDisplayText = stringValue(unlockMap.get("customDisplayText"));
        CollectibleEvent event = CollectibleEvent.fromString(stringValue(unlockMap.get("event"))).orElse(null);

        return new CollectibleUnlockRequirement(
            method,
            requiredRank,
            currency,
            cost,
            customResolverKey,
            customDisplayText,
            event
        );
    }

    private Rank parseRank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Rank.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static Material parseMaterial(String rawMaterial) {
        if (rawMaterial == null || rawMaterial.isBlank()) {
            return null;
        }

        String normalized = rawMaterial.trim().toLowerCase(Locale.ROOT);
        Material material = Material.fromKey(normalized);
        if (material != null) {
            return material;
        }

        if (!normalized.contains(":")) {
            material = Material.fromKey("minecraft:" + normalized);
            if (material != null) {
                return material;
            }
        }

        Logger.warn("Unknown material '{}' in {}", rawMaterial, CATALOG_FILE.getName());
        return null;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String string = String.valueOf(value);
        return string.isBlank() ? null : string;
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (Object entry : list) {
            if (entry == null) {
                continue;
            }
            values.add(String.valueOf(entry));
        }
        return values;
    }

    private Map<String, String> stringMap(Object value) {
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String key = String.valueOf(entry.getKey());
            if (key.isBlank()) {
                continue;
            }

            Object rawValue = entry.getValue();
            values.put(key, rawValue == null ? "" : String.valueOf(rawValue));
        }
        return values;
    }

    private int intValue(Object value, int fallback) {
        if (!(value instanceof Number number)) {
            return fallback;
        }
        return number.intValue();
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return fallback;
    }

    private record CategorySettings(boolean favoriteable, boolean random, String defaultSelectionId,
                                    String descriptionKey) {
        private static final CategorySettings DEFAULT = new CategorySettings(false, false, null, null);
    }
}
