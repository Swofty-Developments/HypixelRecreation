package net.swofty.type.skyblockgeneric.utility;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.ItemQuantifiable;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.crafting.CraftingRequirement;
import net.swofty.type.skyblockgeneric.item.crafting.CraftingRequirements;
import net.swofty.type.skyblockgeneric.item.crafting.ShapedRecipe;
import net.swofty.type.skyblockgeneric.item.crafting.ShapelessRecipe;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecipeParser {
    private RecipeParser() {
    }

    public static SkyBlockRecipe<?> parseRecipe(Map<String, Object> config) {
        if (config == null) throw new IllegalArgumentException("Recipe configuration cannot be null");

        String type = requiredString(config, "type", "recipe");
        SkyBlockRecipe.RecipeType craftingType = parseRecipeType(config.get("recipe-type"));
        SkyBlockItem result = parseResult(asMap(config.get("result"), "result"));
        CraftingRequirements requirements = parseRequirements(config.get("requirements"));

        SkyBlockRecipe<?> recipe = switch (type.toLowerCase()) {
            case "shapeless" -> parseShapelessRecipe(config, craftingType, result);
            case "shaped" -> parseShapedRecipe(config, craftingType, result);
            default -> throw new IllegalArgumentException("Invalid recipe type: " + type);
        };
        recipe.setCraftingRequirements(requirements);
        return recipe;
    }

    public static CraftingRequirements parseRequirements(Object requirementsConfig) {
        if (requirementsConfig == null) return CraftingRequirements.none();

        Map<String, Object> map = requirementsConfig instanceof Map<?, ?>
                ? asMap(requirementsConfig, "requirements")
                : null;
        if (map != null && isRequirementDefinition(map)) {
            return CraftingRequirements.allOf(parseRequirement("requirement", map));
        }

        List<CraftingRequirement> requirements = parseRequirementEntries(requirementsConfig, "requirements");
        return CraftingRequirements.allOf(requirements);
    }

    private static List<CraftingRequirement> parseRequirementEntries(Object value, String path) {
        if (value instanceof List<?> list) {
            List<CraftingRequirement> requirements = new ArrayList<>();
            for (int index = 0; index < list.size(); index++) {
                Object entry = list.get(index);
                if (entry instanceof Map<?, ?> entryMap) {
                    Map<String, Object> map = asMap(entryMap, path + "[" + index + "]");
                    if (isRequirementDefinition(map)) {
                        String id = string(map, "id", string(map, "name", "requirement_" + index));
                        requirements.add(parseRequirement(id, map));
                    } else {
                        requirements.addAll(parseRequirementEntries(map, path + "[" + index + "]"));
                    }
                } else {
                    throw new IllegalArgumentException("Expected a requirement map at " + path + "[" + index + "]");
                }
            }
            return requirements;
        }

        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Expected a requirement map or list at " + path);
        }

        Map<String, Object> map = asMap(value, path);
        if (isRequirementDefinition(map)) {
            return List.of(parseRequirement(path, map));
        }

        List<CraftingRequirement> requirements = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String id = entry.getKey();
            if (id.equalsIgnoreCase("all") || id.equalsIgnoreCase("any")) {
                List<CraftingRequirement> children = parseRequirementEntries(entry.getValue(), path + "." + id);
                requirements.add(id.equalsIgnoreCase("all")
                        ? CraftingRequirement.all(id, children)
                        : CraftingRequirement.any(id, children));
                continue;
            }
            if (id.equalsIgnoreCase("not")) {
                List<CraftingRequirement> children = parseRequirementEntries(entry.getValue(), path + ".not");
                if (children.size() != 1) {
                    throw new IllegalArgumentException("The not requirement at " + path + " must contain one child");
                }
                requirements.add(CraftingRequirement.not(id, children.getFirst(), null));
                continue;
            }

            requirements.add(parseRequirement(id, asMap(entry.getValue(), path + "." + id)));
        }
        return requirements;
    }

    private static CraftingRequirement parseRequirement(String id, Map<String, Object> config) {
        String failureMessage = failureMessage(config, id);
        String type = string(config, "type", null);
        if (type == null || type.equalsIgnoreCase("expression") || type.equalsIgnoreCase("template")) {
            String left = requiredString(config, "left", id);
            String operation = string(config, "operation", string(config, "operator", null));
            String right = requiredValue(config, "right", id);
            return CraftingRequirement.expression(
                    id,
                    left,
                    CraftingRequirement.Operator.parse(operation),
                    right,
                    failureMessage
            );
        }

        String normalizedType = type.toUpperCase().replace('-', '_').replace(' ', '_');
        String parameter;
        String left;
        String right;
        String defaultOperation;

        switch (normalizedType) {
            case "SKILL" -> {
                parameter = requiredParameter(config, "skill", id);
                left = "%player.skill_level:" + parameter + "%";
                right = requiredMetricValue(config, "level", id);
                defaultOperation = ">=";
            }
            case "SKILL_XP" -> {
                parameter = requiredParameter(config, "skill", id);
                left = "%player.skill_xp:" + parameter + "%";
                right = requiredMetricValue(config, "xp", id);
                defaultOperation = ">=";
            }
            case "COLLECTION" -> {
                parameter = requiredParameter(config, "collection", id);
                left = "%player.collection:" + parameter + "%";
                right = requiredMetricValue(config, "amount", id);
                defaultOperation = ">=";
            }
            case "COINS", "BITS", "GEMS", "SKYBLOCK_LEVEL", "SKYBLOCK_XP", "FAIRY_SOULS",
                 "MAGICAL_POWER", "RUNE_LEVEL", "EMPTY_SLOTS", "COOP_MEMBERS" -> {
                left = "%player." + normalizedType.toLowerCase() + "%";
                right = requiredMetricValue(config, "amount", id);
                defaultOperation = ">=";
            }
            case "REGION", "ARMOR_SET" -> {
                left = "%player." + normalizedType.toLowerCase() + "%";
                right = requiredValue(config, "value", id);
                defaultOperation = "=";
            }
            case "IS_COOP" -> {
                left = "%player.is_coop%";
                right = requiredValue(config, "value", id);
                defaultOperation = "=";
            }
            case "TOGGLE" -> {
                parameter = requiredParameter(config, "toggle", id);
                left = "%player.toggle:" + parameter + "%";
                right = requiredValue(config, "value", id);
                defaultOperation = "=";
            }
            default ->
                    throw new IllegalArgumentException("Unsupported crafting requirement type '" + type + "' for " + id);
        }

        String customLeft = string(config, "left", null);
        String customRight = firstValue(config, "right", "value", "amount", "level", "xp");
        String operation = string(config, "operation", string(config, "operator", defaultOperation));
        return CraftingRequirement.expression(
                id,
                customLeft == null ? left : customLeft,
                CraftingRequirement.Operator.parse(operation),
                customRight == null ? right : customRight,
                failureMessage
        );
    }

    private static boolean isRequirementDefinition(Map<String, Object> map) {
        return map.containsKey("left") || map.containsKey("type") || map.containsKey("operator")
                || map.containsKey("operation");
    }

    private static String failureMessage(Map<String, Object> config, String id) {
        String message = string(config, "fail-message", null);
        if (message == null) message = string(config, "failure-message", null);
        if (message == null) message = string(config, "message", null);
        return message == null ? "<c>Requirement not met: " + id : message;
    }

    private static String requiredParameter(Map<String, Object> config, String key, String id) {
        String value = string(config, key, null);
        if (value == null) value = string(config, "value", null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + key + " for crafting requirement " + id);
        }
        return value.toUpperCase();
    }

    private static String requiredMetricValue(Map<String, Object> config, String key, String id) {
        String value = firstValue(config, key, "amount", "value", "right");
        if (value == null) {
            throw new IllegalArgumentException("Missing " + key + " for crafting requirement " + id);
        }
        return value;
    }

    private static String requiredValue(Map<String, Object> config, String key, String id) {
        String value = firstValue(config, key, "right");
        if (value == null) {
            throw new IllegalArgumentException("Missing " + key + " for crafting requirement " + id);
        }
        return value;
    }

    private static String requiredString(Map<String, Object> config, String key, String path) {
        String value = string(config, key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + key + " for " + path);
        }
        return value;
    }

    private static String firstValue(Map<String, Object> config, String... keys) {
        for (String key : keys) {
            Object value = config.get(key);
            if (value != null) return String.valueOf(value);
        }
        return null;
    }

    private static String string(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private static SkyBlockRecipe.RecipeType parseRecipeType(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return SkyBlockRecipe.RecipeType.NONE;
        return SkyBlockRecipe.RecipeType.valueOf(String.valueOf(value).toUpperCase());
    }

    private static ShapelessRecipe parseShapelessRecipe(Map<String, Object> config,
                                                        SkyBlockRecipe.RecipeType craftingType,
                                                        SkyBlockItem result) {
        List<Map<String, Object>> ingredients = mapList(config.get("ingredients"), "ingredients");
        int amount = integer(asMap(config.get("result"), "result"), "amount", 1);
        result.setAmount(amount);

        ShapelessRecipe recipe = new ShapelessRecipe(craftingType, result, amount,
                player -> new SkyBlockRecipe.CraftingResult(true, null));

        for (Map<String, Object> ingredient : ingredients) {
            String materialType = requiredString(ingredient, "type", "ingredient");
            recipe.add(parseItemType(materialType), integer(ingredient, "amount", 1));
        }
        return recipe;
    }

    private static ShapedRecipe parseShapedRecipe(Map<String, Object> config,
                                                  SkyBlockRecipe.RecipeType craftingType,
                                                  SkyBlockItem result) {
        Map<String, Object> ingredients = asMap(config.get("ingredients"), "ingredients");
        List<String> pattern = stringList(config.get("pattern"), "pattern");
        Map<Character, ItemQuantifiable> ingredientMap = new java.util.HashMap<>();

        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            if (entry.getKey().isEmpty()) throw new IllegalArgumentException("Empty shaped recipe ingredient key");
            Map<String, Object> ingredient = asMap(entry.getValue(), "ingredient " + entry.getKey());
            String materialType = requiredString(ingredient, "type", "ingredient " + entry.getKey());
            ingredientMap.put(entry.getKey().charAt(0),
                    new ItemQuantifiable(parseItemType(materialType), integer(ingredient, "amount", 1)));
        }

        result.setAmount(integer(asMap(config.get("result"), "result"), "amount", 1));
        return new ShapedRecipe(craftingType, result, ingredientMap, pattern,
                player -> new SkyBlockRecipe.CraftingResult(true, null));
    }

    private static SkyBlockItem parseResult(Map<String, Object> resultConfig) {
        String type = requiredString(resultConfig, "type", "result");
        return new SkyBlockItem(parseItemType(type), integer(resultConfig, "amount", 1));
    }

    private static ItemType parseItemType(String value) {
        String normalized = value.toUpperCase();
        if (normalized.startsWith("ITEM_TYPE_")) normalized = normalized.substring("ITEM_TYPE_".length());
        return ItemType.valueOf(normalized);
    }

    private static int integer(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Expected an integer for '" + key + "'");
        }
    }

    private static List<Map<String, Object>> mapList(Object value, String path) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected a list at " + path);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object entry : list) result.add(asMap(entry, path));
        return result;
    }

    private static List<String> stringList(Object value, String path) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected a list at " + path);
        }
        return list.stream().map(String::valueOf).toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value, String path) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected a map at " + path);
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        map.forEach((key, mapValue) -> result.put(String.valueOf(key), mapValue));
        return result;
    }
}
