package net.swofty.type.skyblockgeneric.museum;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum MuseumableItemCategory {
    WEAPONS("Weapons", Material.DIAMOND_SWORD, "<6>"),
    ARMOR_SETS("Armor Sets", Material.CHAINMAIL_CHESTPLATE, "<9>"),
    RARITIES("Rarities", Material.EMERALD_BLOCK, "<5>"),
    SPECIAL("Special", Material.CAKE, "<d>")
    ;

    private final static Map<MuseumableItemCategory, List<ItemType>> ITEMS = new HashMap<>();

    private final String category;
    private final Material material;
    private final String color;

    MuseumableItemCategory(String category, Material material, String color) {
        this.category = category;
        this.material = material;
        this.color = color;
    }

    public List<ItemType> getItems() {
        return ITEMS.getOrDefault(this, new ArrayList<>());
    }

    @Override
    public String toString() {
        return StringUtility.toNormalCase(name().replace("_", " "));
    }

    public boolean contains(ItemType item) {
        return ITEMS.getOrDefault(this, new ArrayList<>()).contains(item);
    }

    public static MuseumableItemCategory getFromItem(ItemType item) {
        for (MuseumableItemCategory category : ITEMS.keySet()) {
            if (ITEMS.getOrDefault(category, new ArrayList<>()).contains(item)) {
                return category;
            }
        }
        return null;
    }

    public static Integer getMuseumableItemCategorySize(MuseumableItemCategory category) {
        return ITEMS.getOrDefault(category, new ArrayList<>()).size();
    }

    public static Integer getMuseumableItemCategorySize() {
        return ITEMS.values().stream().mapToInt(List::size).sum();
    }

    public static Map<MuseumableItemCategory, List<SkyBlockItem>> sortAsMuseumItems(List<SkyBlockItem> items) {
        Map<MuseumableItemCategory, List<SkyBlockItem>> sortedItems = new HashMap<>();
        for (MuseumableItemCategory category : ITEMS.keySet()) {
            sortedItems.put(category, new ArrayList<>());
        }

        for (SkyBlockItem item : items) {
            for (MuseumableItemCategory category : ITEMS.keySet()) {
                if (ITEMS.get(category).contains(item.getAttributeHandler().getPotentialType())) {
                    sortedItems.get(category).add(item);
                    break;
                }
            }
        }

        return sortedItems;
    }

    public static void addItem(MuseumableItemCategory category, ItemType item) {
        if (!ITEMS.containsKey(category)) {
            ITEMS.put(category, new ArrayList<>());
        }
        ITEMS.get(category).add(item);
    }
}
