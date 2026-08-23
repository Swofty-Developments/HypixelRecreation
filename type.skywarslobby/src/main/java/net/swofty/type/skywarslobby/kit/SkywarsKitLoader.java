package net.swofty.type.skywarslobby.kit;

import lombok.Data;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Loader for SkyWars kit configuration from YAML files.
 */
public class SkywarsKitLoader {
    private static final File KITS_DIR = new File("./configuration/skywars");

    @Data
    public static class KitsConfig {
        public Map<String, KitEntry> kits;
    }

    @Data
    public static class KitEntry {
        public String name;
        public String material;
        public String texture;  // Optional custom head texture
        public String rarity = "COMMON";
        public int cost = 0;
        public int opalCost = 0;
        public boolean soulWellDrop = true;
        public boolean isDefault = false;
        public String unlockMethod;
        public String specialAbility;
        public Map<String, ModeEntry> modes;
    }

    @Data
    public static class ModeEntry {
        public List<ItemEntry> items;
    }

    @Data
    public static class ItemEntry {
        public String material;
        public int amount = 1;
        public String customName;
        public String texture;  // For player heads
        public String color;    // For leather armor (hex color)
        public String potionType;
        public int potionDuration = 0;
        public int potionAmplifier = 0;
        public List<EnchantmentEntry> enchantments;
    }

    @Data
    public static class EnchantmentEntry {
        public String type;
        public int level = 1;
    }

    /**
     * Load SkyWars kits from the YAML file
     */
    public static List<SkywarsKit> loadFromFile() {
        List<SkywarsKit> kits = new ArrayList<>();

        try {
            if (!YamlFileUtils.ensureDirectoryExists(KITS_DIR)) {
                throw new IOException("Failed to create kits directory");
            }

            File kitsFile = new File(KITS_DIR, "kits.yml");

            if (!kitsFile.exists()) {
                Logger.warn("Kits file not found at: " + kitsFile.getAbsolutePath());
                return getDefaultKits();
            }

            Yaml yaml = new Yaml();
            KitsConfig config = yaml.loadAs(new FileReader(kitsFile), KitsConfig.class);

            if (config == null || config.kits == null) {
                Logger.warn("Invalid kits.yml structure");
                return getDefaultKits();
            }

            for (Map.Entry<String, KitEntry> entry : config.kits.entrySet()) {
                String id = entry.getKey();
                KitEntry kitEntry = entry.getValue();

                SkywarsKit kit = parseKit(id, kitEntry);
                if (kit != null) {
                    kits.add(kit);
                }
            }

            Logger.info("Loaded " + kits.size() + " SkyWars kits from configuration");

        } catch (Exception e) {
            Logger.error(e, "Failed to load SkyWars kits from file");
            return getDefaultKits();
        }

        return kits;
    }

    /**
     * Parse a single kit entry
     */
    private static SkywarsKit parseKit(String id, KitEntry entry) {
        if (entry == null || entry.name == null) {
            return null;
        }

        Material iconMaterial = parseMaterial(entry.material);
        SkywarsKitRarity rarity = parseRarity(entry.rarity);

        Map<String, List<ItemStack>> modeItems = new HashMap<>();
        if (entry.modes != null) {
            for (Map.Entry<String, ModeEntry> modeEntry : entry.modes.entrySet()) {
                String mode = modeEntry.getKey();
                ModeEntry modeData = modeEntry.getValue();
                if (modeData.items != null) {
                    modeItems.put(mode, parseItems(modeData.items));
                }
            }
        }

        return new SkywarsKit(
                id,
                entry.name,
                iconMaterial,
                entry.texture,
                rarity,
                entry.cost,
                entry.opalCost,
                entry.soulWellDrop,
                entry.isDefault,
                entry.unlockMethod,
                entry.specialAbility,
                modeItems
        );
    }

    /**
     * Parse items list from YAML config
     */
    private static List<ItemStack> parseItems(List<ItemEntry> itemEntries) {
        List<ItemStack> items = new ArrayList<>();

        if (itemEntries == null) {
            return items;
        }

        for (ItemEntry entry : itemEntries) {
            ItemStack item = parseItem(entry);
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }

    /**
     * Parse a single item entry
     */
    private static ItemStack parseItem(ItemEntry entry) {
        if (entry == null || entry.material == null) {
            return null;
        }

        Material material = parseMaterial(entry.material);
        ItemStack.Builder builder = ItemStack.builder(material)
                .amount(entry.amount);

        // Add custom name if specified
        if (entry.customName != null && !entry.customName.isEmpty()) {
            ItemStacks.customName(builder, Text.read(entry.customName));
        }

        // Add enchantments if specified
        if (entry.enchantments != null && !entry.enchantments.isEmpty()) {
            EnchantmentList enchantmentList = EnchantmentList.EMPTY;
            for (EnchantmentEntry enchEntry : entry.enchantments) {
                RegistryKey<Enchantment> enchantment = parseEnchantment(enchEntry.type);
                if (enchantment != null) {
                    enchantmentList = enchantmentList.with(enchantment, enchEntry.level);
                }
            }
            if (!enchantmentList.enchantments().isEmpty()) {
                builder.set(net.minestom.server.component.DataComponents.ENCHANTMENTS, enchantmentList);
            }
        }

        return builder.build();
    }

    /**
     * Parse material string to Material enum
     */
    private static Material parseMaterial(String materialStr) {
        if (materialStr == null) {
            return Material.STONE;
        }

        // Minestom uses key().value() which returns lowercase like "oak_planks"
        String normalizedStr = materialStr.toLowerCase().replace("-", "_");
        return Material.values().stream()
                .filter(m -> m.key().value().equalsIgnoreCase(normalizedStr))
                .findFirst()
                .orElse(Material.STONE);
    }

    /**
     * Parse rarity string to enum
     */
    private static SkywarsKitRarity parseRarity(String rarityStr) {
        if (rarityStr == null) {
            return SkywarsKitRarity.COMMON;
        }

        try {
            return SkywarsKitRarity.valueOf(rarityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SkywarsKitRarity.COMMON;
        }
    }

    /**
     * Parse enchantment string to Enchantment key
     */
    private static RegistryKey<Enchantment> parseEnchantment(String enchantmentStr) {
        if (enchantmentStr == null) {
            return null;
        }

        String normalizedName = enchantmentStr.toLowerCase();

        // Map common enchantment names to their Minecraft keys
        return switch (normalizedName) {
            case "protection" -> Enchantment.PROTECTION;
            case "fire_protection" -> Enchantment.FIRE_PROTECTION;
            case "feather_falling" -> Enchantment.FEATHER_FALLING;
            case "blast_protection" -> Enchantment.BLAST_PROTECTION;
            case "projectile_protection" -> Enchantment.PROJECTILE_PROTECTION;
            case "respiration" -> Enchantment.RESPIRATION;
            case "aqua_affinity" -> Enchantment.AQUA_AFFINITY;
            case "thorns" -> Enchantment.THORNS;
            case "depth_strider" -> Enchantment.DEPTH_STRIDER;
            case "frost_walker" -> Enchantment.FROST_WALKER;
            case "sharpness" -> Enchantment.SHARPNESS;
            case "smite" -> Enchantment.SMITE;
            case "bane_of_arthropods" -> Enchantment.BANE_OF_ARTHROPODS;
            case "knockback" -> Enchantment.KNOCKBACK;
            case "fire_aspect" -> Enchantment.FIRE_ASPECT;
            case "looting" -> Enchantment.LOOTING;
            case "efficiency" -> Enchantment.EFFICIENCY;
            case "silk_touch" -> Enchantment.SILK_TOUCH;
            case "unbreaking" -> Enchantment.UNBREAKING;
            case "fortune" -> Enchantment.FORTUNE;
            case "power" -> Enchantment.POWER;
            case "punch" -> Enchantment.PUNCH;
            case "flame" -> Enchantment.FLAME;
            case "infinity" -> Enchantment.INFINITY;
            case "luck_of_the_sea" -> Enchantment.LUCK_OF_THE_SEA;
            case "lure" -> Enchantment.LURE;
            case "mending" -> Enchantment.MENDING;
            case "sweeping_edge", "sweeping" -> Enchantment.SWEEPING_EDGE;
            default -> null;
        };
    }

    /**
     * Get default kits if file loading fails
     */
    private static List<SkywarsKit> getDefaultKits() {
        List<SkywarsKit> defaults = new ArrayList<>();

        // Default kit only
        Map<String, List<ItemStack>> defaultModeItems = new HashMap<>();
        defaultModeItems.put("NORMAL", List.of(
                ItemStack.of(Material.WOODEN_PICKAXE),
                ItemStack.of(Material.WOODEN_AXE),
                ItemStack.of(Material.WOODEN_SHOVEL),
                ItemStack.of(Material.WOODEN_SWORD),
                ItemStack.of(Material.LEATHER_CHESTPLATE)
        ));
        defaultModeItems.put("INSANE", List.of(
                ItemStack.of(Material.IRON_PICKAXE),
                ItemStack.of(Material.IRON_AXE),
                ItemStack.of(Material.IRON_SHOVEL),
                ItemStack.of(Material.IRON_SWORD),
                ItemStack.of(Material.IRON_CHESTPLATE)
        ));

        defaults.add(new SkywarsKit(
                "default",
                "Default",
                Material.WOODEN_PICKAXE,
                null,
                SkywarsKitRarity.COMMON,
                0,
                0,
                false,
                true,
                null,
                null,
                defaultModeItems
        ));

        return defaults;
    }
}
