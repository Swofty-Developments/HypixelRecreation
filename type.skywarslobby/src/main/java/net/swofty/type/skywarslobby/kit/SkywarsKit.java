package net.swofty.type.skywarslobby.kit;

import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data class for SkyWars kits loaded from YAML configuration.
 */
@Getter
public class SkywarsKit {
    private final String id;
    private final String name;
    private final Material iconMaterial;
    private final String iconTexture;  // For custom head icons
    private final SkywarsKitRarity rarity;
    private final int cost;
    private final int opalCost;
    private final boolean soulWellDrop;
    private final boolean isDefault;
    private final String unlockMethod;
    private final String specialAbility;
    private final Map<String, List<ItemStack>> modeItems;

    public SkywarsKit(String id, String name, Material iconMaterial, String iconTexture,
                      SkywarsKitRarity rarity, int cost, int opalCost, boolean soulWellDrop,
                      boolean isDefault, String unlockMethod, String specialAbility,
                      Map<String, List<ItemStack>> modeItems) {
        this.id = id;
        this.name = name;
        this.iconMaterial = iconMaterial;
        this.iconTexture = iconTexture;
        this.rarity = rarity;
        this.cost = cost;
        this.opalCost = opalCost;
        this.soulWellDrop = soulWellDrop;
        this.isDefault = isDefault;
        this.unlockMethod = unlockMethod;
        this.specialAbility = specialAbility;
        this.modeItems = modeItems;
    }

    /**
     * Get the starting items for a specific game mode
     * @param mode The game mode (e.g., "NORMAL", "INSANE")
     * @return List of starting items, or empty list if mode not found
     */
    public List<ItemStack> getStartingItems(String mode) {
        return modeItems.getOrDefault(mode, new ArrayList<>());
    }

    /**
     * Get the starting items for Normal mode
     */
    public List<ItemStack> getNormalItems() {
        return getStartingItems("NORMAL");
    }

    /**
     * Get the starting items for Insane mode
     */
    public List<ItemStack> getInsaneItems() {
        return getStartingItems("INSANE");
    }

    /**
     * Check if this kit is available for a specific mode
     */
    public boolean isAvailableFor(String mode) {
        return modeItems.containsKey(mode);
    }

    /**
     * Check if this kit has a custom head texture
     */
    public boolean hasCustomTexture() {
        return iconTexture != null && !iconTexture.isEmpty();
    }

    /**
     * Check if this kit costs coins
     */
    public boolean costsCoin() {
        return cost > 0;
    }

    /**
     * Check if this kit costs opals
     */
    public boolean costsOpal() {
        return opalCost > 0;
    }

    /**
     * Check if this kit is free
     */
    public boolean isFree() {
        return cost == 0 && opalCost == 0;
    }

    /**
     * Get formatted cost string for display
     */
    public Text getFormattedCost() {
        if (isDefault) {
            return Text.of("<a>FREE");
        } else if (opalCost > 0) {
            return Text.of("<9>{} Opal{}", opalCost, opalCost > 1 ? "s" : "");
        } else if (cost > 0) {
            return Text.of("<6>{:,}", cost);
        } else if (unlockMethod != null) {
            return Text.of("<5>{}", formatUnlockMethod(unlockMethod));
        }
        return Text.of("<a>FREE");
    }

    private String formatUnlockMethod(String method) {
        return switch (method) {
            case "ANGELS_DESCENT" -> "Unlocked in Angel's Descent";
            default -> method;
        };
    }

    /**
     * Generate lore lines describing the kit's items for a specific mode
     */
    public List<Text> getItemsLore(String mode) {
        List<Text> lore = new ArrayList<>();
        List<ItemStack> items = getStartingItems(mode);

        for (ItemStack item : items) {
            String itemName = formatMaterial(item.material());

            // Check for custom name
            if (item.get(net.minestom.server.component.DataComponents.CUSTOM_NAME) != null) {
                itemName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                        .plainText().serialize(item.get(net.minestom.server.component.DataComponents.CUSTOM_NAME));
            }

            lore.add(item.amount() > 1
                    ? Text.of("<7>{} x{}", itemName, item.amount())
                    : Text.of("<7>{}", itemName));

            // Add enchantments if present
            var enchantments = item.get(net.minestom.server.component.DataComponents.ENCHANTMENTS);
            if (enchantments != null && !enchantments.enchantments().isEmpty()) {
                for (var entry : enchantments.enchantments().entrySet()) {
                    String enchantName = formatEnchantmentName(entry.getKey().name());
                    lore.add(Text.of("<7>   <8>∙ {} {:roman}", enchantName, entry.getValue()));
                }
            }
        }

        return lore;
    }

    private String formatMaterial(Material material) {
        // Use key().value() to get material name without namespace prefix (e.g., "stone" not "minecraft:stone")
        String name = material.key().value().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String formatEnchantmentName(String name) {
        // Remove namespace prefix if present
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        // Format the enchantment name: replace underscores with spaces and capitalize
        String formatted = name.replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : formatted.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

}
