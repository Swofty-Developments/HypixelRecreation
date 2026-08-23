package net.swofty.type.ravengardgeneric.item;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.item.Material;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RavengardItemType {
    private final String id;
    private Material material;
    private RavengardRarity rarity = RavengardRarity.COMMON;
    private String itemModel;
    private String displayName;
    private final net.swofty.type.ravengardgeneric.item.statistics.RavengardItemStatistics statistics =
            net.swofty.type.ravengardgeneric.item.statistics.RavengardItemStatistics.empty();
    /** Crown value, shown in shop context only. */
    private int value;
    private final List<RavengardItemComponent> components = new ArrayList<>();

    /** Set by the CLASS_RESTRICTED component; empty means any class may hold the item. */
    private final java.util.Set<RavengardClass> restrictedTo = new java.util.LinkedHashSet<>();
    private final List<String> description = new ArrayList<>();
    private String effect;
    /** Overrides the rarity-prefixed tracked id, for items whose live id carries no prefix. */
    private String trackedId;

    public RavengardItemType(String id) {
        this.id = id;
    }

    public double statistic(net.swofty.type.ravengardgeneric.item.statistics.RavengardItemStatistic statistic) {
        return statistics.get(statistic);
    }

    public boolean usableBy(RavengardClass value) {
        return restrictedTo.isEmpty() || restrictedTo.contains(value);
    }

    public <T extends RavengardItemComponent> T component(Class<T> type) {
        for (RavengardItemComponent component : components) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
        }
        return null;
    }
}
