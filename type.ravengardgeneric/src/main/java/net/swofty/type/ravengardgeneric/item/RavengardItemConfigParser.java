package net.swofty.type.ravengardgeneric.item;

import net.minestom.server.item.Material;
import net.swofty.type.ravengardgeneric.item.components.ClassRestrictedComponent;
import net.swofty.type.ravengardgeneric.item.components.EquippableComponent;
import net.swofty.type.ravengardgeneric.item.components.PlaceholderSlotComponent;
import net.swofty.type.ravengardgeneric.item.components.StandardItemComponent;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class RavengardItemConfigParser {
    private static final Map<String, Supplier<RavengardItemComponent>> COMPONENTS = Map.of(
            "STANDARD_ITEM", StandardItemComponent::new,
            "EQUIPPABLE", EquippableComponent::new,
            "CLASS_RESTRICTED", ClassRestrictedComponent::new,
            "PLACEHOLDER_SLOT", PlaceholderSlotComponent::new);

    private RavengardItemConfigParser() {
    }

    @SuppressWarnings("unchecked")
    public static RavengardItemType parse(Map<String, Object> config) {
        String id = String.valueOf(config.get("id")).toUpperCase();
        RavengardItemType type = new RavengardItemType(id);

        type.setMaterial(Material.fromKey(String.valueOf(config.get("material"))));
        if (config.get("item_model") != null) {
            type.setItemModel(String.valueOf(config.get("item_model")));
        }
        type.setRarity(RavengardRarity.fromKey((String) config.get("rarity")));
        if (config.get("description") instanceof List<?> descriptionLines) {
            descriptionLines.forEach(line -> type.getDescription().add(String.valueOf(line)));
        }
        if (config.get("effect") != null) {
            type.setEffect(String.valueOf(config.get("effect")));
        }
        if (config.get("display_name") != null) {
            type.setDisplayName(String.valueOf(config.get("display_name")));
        }
        if (config.get("tracked_id") != null) {
            type.setTrackedId(String.valueOf(config.get("tracked_id")).toUpperCase());
        }

        if (config.get("value") != null) {
            type.setValue(Integer.parseInt(config.get("value").toString()));
        }
        Map<String, Object> statistics = (Map<String, Object>) config.get("default_statistics");
        if (statistics != null) {
            statistics.forEach((key, value) -> {
                if ("value".equals(key)) {
                    type.setValue((int) Double.parseDouble(value.toString()));
                    return;
                }
                var statistic = net.swofty.type.ravengardgeneric.item.statistics.RavengardItemStatistic.fromKey(key);
                if (statistic != null) {
                    type.getStatistics().withBase(statistic, Double.parseDouble(value.toString()));
                }
            });
        }

        List<Map<String, Object>> components = (List<Map<String, Object>>) config.get("components");
        if (components != null) {
            for (Map<String, Object> componentConfig : components) {
                String componentId = String.valueOf(componentConfig.get("id")).toUpperCase();
                Supplier<RavengardItemComponent> supplier = COMPONENTS.get(componentId);
                if (supplier == null) {
                    Logger.warn("Unknown Ravengard item component '{}' on item {}", componentId, id);
                    continue;
                }
                RavengardItemComponent component = supplier.get();
                component.configure(componentConfig);
                type.getComponents().add(component);

                if (component instanceof ClassRestrictedComponent restricted) {
                    type.getRestrictedTo().addAll(restricted.getRestrictedTo());
                }
            }
        }

        return type;
    }
}
