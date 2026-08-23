package net.swofty.commons.skyblock.statistics;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.PackSprite;
import org.jetbrains.annotations.NotNull;

@Getter
public enum ItemStatistic {
    // Combat Stats
    HEALTH("Health", NamedTextColor.GREEN, NamedTextColor.RED,
            false, PackSprite.STAT_HEALTH, 100D, 1D),
    DEFENSE("Defense", NamedTextColor.GREEN, NamedTextColor.GREEN, false, PackSprite.STAT_DEFENSE),
    STRENGTH("Strength", NamedTextColor.RED, NamedTextColor.RED, false, PackSprite.STAT_STRENGTH),
    INTELLIGENCE("Intelligence", NamedTextColor.GREEN, NamedTextColor.AQUA, false, PackSprite.STAT_INTELLIGENCE),
    CRITICAL_CHANCE("Crit Chance", NamedTextColor.RED, NamedTextColor.BLUE,
            true, PackSprite.STAT_CRIT_CHANCE, 30D, 1D),
    CRITICAL_DAMAGE("Crit Damage", NamedTextColor.RED, NamedTextColor.BLUE,
            true, PackSprite.STAT_CRIT_DAMAGE, 50D, 1D),
    BONUS_ATTACK_SPEED("Bonus Attack Speed", NamedTextColor.RED, NamedTextColor.YELLOW, true, PackSprite.STAT_ATTACK_SPEED, 100D),
    ABILITY_DAMAGE("Ability Damage", NamedTextColor.RED, NamedTextColor.RED, true, PackSprite.STAT_ABILITY_DAMAGE, 0D, 1D, null, false),
    TRUE_DEFENSE("True Defense", NamedTextColor.GREEN, NamedTextColor.WHITE, false, PackSprite.STAT_TRUE_DEFENSE),
    FEROCITY("Ferocity", NamedTextColor.GREEN, NamedTextColor.RED, false, PackSprite.STAT_FEROCITY, 500D),
    HEALTH_REGENERATION("Health Regen", NamedTextColor.GREEN, NamedTextColor.RED,
            false, PackSprite.STAT_HEALTH_REGEN, 100D, 1D),
    VITALITY("Vitality", NamedTextColor.GREEN, NamedTextColor.DARK_RED,
            false, PackSprite.STAT_VITALITY, 100D, 1D),
    MENDING("Mending", NamedTextColor.GREEN, NamedTextColor.GREEN, false, PackSprite.STAT_MENDING,
            100D, 1D),
    SWING_RANGE("Swing Range", NamedTextColor.YELLOW, NamedTextColor.YELLOW, false, PackSprite.STAT_SWING_RANGE, 3D, 1D, 15D),
    RIFT_DAMAGE("Rift Damage", NamedTextColor.DARK_PURPLE, NamedTextColor.LIGHT_PURPLE, false, PackSprite.STAT_RIFT_DAMAGE),

    // Gathering Stats
    MINING_SPEED("Mining Speed", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_MINING_SPEED),
    MINING_FORTUNE("Mining Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_MINING_FORTUNE),
    FARMING_FORTUNE("Farming Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    FORAGING_FORTUNE("Foraging Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FORAGING_FORTUNE),
    BREAKING_POWER("Breaking Power", NamedTextColor.GREEN, NamedTextColor.DARK_GREEN, false, PackSprite.STAT_BREAKING_POWER),
    PRISTINE("Pristine", NamedTextColor.GREEN, NamedTextColor.DARK_PURPLE, false, PackSprite.STAT_PRISTINE),
    MINING_SPREAD("Mining Spread", NamedTextColor.GREEN, NamedTextColor.YELLOW, false, PackSprite.STAT_MINING_SPREAD, 10_000D),
    GEMSTONE_SPREAD("Gemstone Spread", NamedTextColor.GREEN, NamedTextColor.YELLOW, false, PackSprite.STAT_GEMSTONE_SPREAD),
    HUNTER_FORTUNE("Hunter Fortune", NamedTextColor.GREEN, NamedTextColor.LIGHT_PURPLE, false, PackSprite.STAT_HUNTER_FORTUNE),
    SWEEP("Sweep", NamedTextColor.GREEN, NamedTextColor.DARK_GREEN, false, PackSprite.STAT_SWEEP),
    SOULFLOW("Soulflow", NamedTextColor.DARK_AQUA, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_SOULFLOW),

    // Sub-Gathering Stats
    ORE_FORTUNE("Ore Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_ORE_FORTUNE),
    BLOCK_FORTUNE("Block Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_BLOCK_FORTUNE),
    DWARVEN_METAL_FORTUNE("Dwarven Metal Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_DWARVEN_METAL_FORTUNE),
    GEMSTONE_FORTUNE("Gemstone Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_GEMSTONE_FORTUNE),

    WHEAT_FORTUNE("Wheat Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    POTATO_FORTUNE("Potato Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    CARROT_FORTUNE("Carrot Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    PUMPKIN_FORTUNE("Pumpkin Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    MELON_FORTUNE("Melon Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    CACTUS_FORTUNE("Cactus Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    NETHER_WART_FORTUNE("Nether Wart Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    COCOA_BEANS_FORTUNE("Cocoa Beans Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    MUSHROOM_FORTUNE("Mushroom Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    SUGAR_CANE_FORTUNE("Sugar Cane Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    SUNFLOWER_FORTUNE("Sunflower Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    MOONFLOWER_FORTUNE("Moonflower Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),
    WILD_ROSE_FORTUNE("Wild Rose Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FARMING_FORTUNE),

    FIG_FORTUNE("Fig Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FORAGING_FORTUNE),
    MANGROVE_FORTUNE("Mangrove Fortune", NamedTextColor.GREEN, NamedTextColor.GOLD, false, PackSprite.STAT_FORAGING_FORTUNE),

    // Wisdom Stats
    ALCHEMY_WISDOM("Alchemy Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    CARPENTRY_WISDOM("Carpentry Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    COMBAT_WISDOM("Combat Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    ENCHANTING_WISDOM("Enchanting Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    FARMING_WISDOM("Farming Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    FISHING_WISDOM("Fishing Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    FORAGING_WISDOM("Foraging Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    MINING_WISDOM("Mining Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    RUNE_CRAFTING_WISDOM("Runecrafting Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    SOCIAL_WISDOM("Social Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    TAMING_WISDOM("Taming Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),
    HUNTING_WISDOM("Hunting Wisdom", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_WISDOM),

    // Misc Stats
    SPEED("Speed", NamedTextColor.GREEN, NamedTextColor.WHITE, false, PackSprite.STAT_SPEED, 100D, 1D, 400D),
    MAGIC_FIND("Magic Find", NamedTextColor.GREEN, NamedTextColor.AQUA, false, PackSprite.STAT_MAGIC_FIND, 900D),
    PET_LUCK("Pet Luck", NamedTextColor.GREEN, NamedTextColor.LIGHT_PURPLE, false, PackSprite.STAT_PET_LUCK),
    SEA_CREATURE_CHANCE("Sea Creature Chance", NamedTextColor.RED, NamedTextColor.BLUE, true, PackSprite.STAT_SEA_CREATURE_CHANCE, 2D, 1D),
    FISHING_SPEED("Fishing Speed", NamedTextColor.GREEN, NamedTextColor.AQUA, false, PackSprite.STAT_FISHING_SPEED, 300D),
    TREASURE_CHANCE("Treasure Chance", NamedTextColor.GREEN, NamedTextColor.GOLD, true, PackSprite.STAT_TREASURE_CHANCE, 100D),
    TROPHY_FISH_CHANCE("Trophy Fish Chance", NamedTextColor.GREEN, NamedTextColor.GOLD, true, PackSprite.STAT_TROPHY_CHANCE),
    DOUBLE_HOOK_CHANCE("Double Hook Chance", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, true, PackSprite.STAT_DOUBLE_HOOK_CHANCE),
    BONUS_PEST_CHANCE("Bonus Pest Chance", NamedTextColor.GREEN, NamedTextColor.DARK_GREEN, true, PackSprite.STAT_BONUS_PEST_CHANCE),
    OVERBLOOM("Overbloom", NamedTextColor.GREEN, NamedTextColor.YELLOW, false, PackSprite.STAT_OVERBLOOM),
    HEAT_RESISTANCE("Heat Resistance", NamedTextColor.GREEN, NamedTextColor.RED, false, PackSprite.STAT_HEAT_RESISTANCE),
    COLD_RESISTANCE("Cold Resistance", NamedTextColor.GREEN, NamedTextColor.AQUA, false, PackSprite.STAT_COLD_RESISTANCE),
    FEAR("Fear", NamedTextColor.GREEN, NamedTextColor.DARK_PURPLE, false, PackSprite.STAT_FEAR),
    PULL("Pull", NamedTextColor.GREEN, NamedTextColor.AQUA, false, PackSprite.STAT_PULL),
    RESPIRATION("Respiration", NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, false, PackSprite.STAT_RESPIRATION),
    PRESSURE_RESISTANCE("Pressure Resistance", NamedTextColor.GREEN, NamedTextColor.BLUE, false, PackSprite.STAT_PRESSURE_RESISTANCE),
    TRACKING("Tracking", NamedTextColor.GREEN, NamedTextColor.LIGHT_PURPLE, false, PackSprite.STAT_TRACKING),
    BREWER("Brewer", NamedTextColor.GREEN, NamedTextColor.LIGHT_PURPLE, true, PackSprite.GUI_DENY), // TODO: what is this?

    // Other Stats
    DAMAGE("Damage", NamedTextColor.RED, NamedTextColor.RED, false, PackSprite.STAT_ABILITY_DAMAGE,
            5D, 1D),
    ;

    private final @NonNull String displayName;
    private final @NonNull TextColor loreColor;
    private final @NonNull TextColor displayColor;
    private final @NonNull Boolean isPercentage;
    private final @NonNull PackSprite symbol;
    private final Double baseAdditiveValue;
    private final Double baseMultiplicativeValue;
    private final Double cap;
    private final boolean isRendered;

    ItemStatistic(@NotNull String displayName, @NotNull TextColor loreColor, @NotNull TextColor displayColor,
                  @NonNull Boolean isPercentage, @NotNull PackSprite symbol, @NotNull Double baseAdditiveValue,
                  @NotNull Double baseMultiplicativeValue) {
        this(displayName, loreColor, displayColor, isPercentage, symbol,
                baseAdditiveValue, baseMultiplicativeValue, null);
    }

    ItemStatistic(@NotNull String displayName, @NotNull TextColor loreColor, @NotNull TextColor displayColor,
                  @NonNull Boolean isPercentage, @NotNull PackSprite symbol, @NotNull Double baseAdditiveValue,
                  @NotNull Double baseMultiplicativeValue, Double cap) {
        this(displayName, loreColor, displayColor, isPercentage, symbol,
                baseAdditiveValue, baseMultiplicativeValue, cap, true);
    }

    ItemStatistic(@NotNull String displayName, @NotNull TextColor loreColor, @NotNull TextColor displayColor,
                  @NonNull Boolean isPercentage, @NotNull PackSprite symbol, @NotNull Double baseAdditiveValue,
                  @NotNull Double baseMultiplicativeValue, Double cap, boolean isRendered) {
        this.displayName = displayName;
        this.loreColor = loreColor;
        this.displayColor = displayColor;
        this.isPercentage = isPercentage;
        this.symbol = symbol;
        this.baseAdditiveValue = baseAdditiveValue;
        this.baseMultiplicativeValue = baseMultiplicativeValue;
        this.cap = cap;
        this.isRendered = isRendered;
    }

    ItemStatistic(@NotNull String displayName, @NotNull TextColor loreColor, @NotNull TextColor displayColor,
                  @NonNull Boolean isPercentage, @NotNull PackSprite symbol) {
        this(displayName, loreColor, displayColor, isPercentage, symbol, 0D, 1D, null);
    }

    ItemStatistic(@NotNull String displayName, @NotNull TextColor loreColor, @NotNull TextColor displayColor,
                  @NonNull Boolean isPercentage, @NotNull PackSprite symbol, Double cap) {
        this(displayName, loreColor, displayColor, isPercentage, symbol, 0D, 1D, cap);
    }

    public String getSuffix() {
        return isPercentage ? "%" : "";
    }

    public String getPrefix() {
        return isPercentage ? "" : "+";
    }


    public Component getCompleteDisplayName() {
        return symbol.getSprite().appendSpace().append(Component.text(displayName)).color(displayColor);
    }

    public Component getColouredSymbol() {
        return symbol.getSprite().color(displayColor);
    }

    // it's a method cuz it's in common, so it doesn't require minestom to be imported.
    // or that's how I think that works
    public Material getIconMaterial() {
        return switch (this) {
            case HEALTH -> Material.GOLDEN_APPLE;
            case DEFENSE -> Material.IRON_CHESTPLATE;
            case STRENGTH -> Material.BLAZE_POWDER;
            case INTELLIGENCE -> Material.ENCHANTED_BOOK;
            case BONUS_ATTACK_SPEED -> Material.GOLDEN_AXE;
            case ABILITY_DAMAGE -> Material.BEACON;
            case TRUE_DEFENSE -> Material.BONE_MEAL;
            case FEROCITY -> Material.RED_DYE;
            case HEALTH_REGENERATION -> Material.POTION;
            case VITALITY -> Material.GLISTERING_MELON_SLICE;
            case MENDING -> Material.GHAST_TEAR;
            case SWING_RANGE -> Material.STONE_SWORD;
            case BREAKING_POWER -> Material.EMERALD;
            case MINING_SPEED -> Material.DIAMOND_PICKAXE;
            case MINING_SPREAD, GEMSTONE_SPREAD -> Material.GOLDEN_PICKAXE;
            case SWEEP -> Material.DIAMOND_AXE;
            case WHEAT_FORTUNE -> Material.WHEAT;
            case CARROT_FORTUNE -> Material.CARROT;
            case POTATO_FORTUNE -> Material.POTATO;
            case PUMPKIN_FORTUNE -> Material.CARVED_PUMPKIN;
            case MELON_FORTUNE -> Material.MELON_SLICE;
            case MUSHROOM_FORTUNE -> Material.RED_MUSHROOM;
            case CACTUS_FORTUNE -> Material.CACTUS;
            case SUGAR_CANE_FORTUNE -> Material.SUGAR_CANE;
            case NETHER_WART_FORTUNE -> Material.NETHER_WART;
            case COCOA_BEANS_FORTUNE -> Material.COCOA_BEANS;
            case SUNFLOWER_FORTUNE -> Material.SUNFLOWER;
            case MOONFLOWER_FORTUNE -> Material.BLUE_ORCHID;
            case WILD_ROSE_FORTUNE -> Material.ROSE_BUSH;
            case FIG_FORTUNE, MANGROVE_FORTUNE -> Material.PAPER;
            case HUNTER_FORTUNE -> Material.PRISMARINE_SHARD;
            case PULL -> Material.COBWEB;
            case FISHING_SPEED -> Material.FISHING_ROD;
            case SEA_CREATURE_CHANCE -> Material.PRISMARINE_CRYSTALS;
            case DOUBLE_HOOK_CHANCE -> Material.COOKED_COD;
            case TREASURE_CHANCE -> Material.CHEST;
            case BONUS_PEST_CHANCE -> Material.DIRT;
            case OVERBLOOM -> Material.CACTUS_FLOWER;
            case SPEED -> Material.SUGAR;
            case MAGIC_FIND -> Material.STICK;
            case HEAT_RESISTANCE -> Material.LAVA_BUCKET;
            case COLD_RESISTANCE -> Material.ICE;
            case FEAR -> Material.CAULDRON;
            case RESPIRATION, PRESSURE_RESISTANCE -> Material.GLASS_BOTTLE;
            case TRACKING -> Material.COMPASS;
            case ALCHEMY_WISDOM, CARPENTRY_WISDOM, COMBAT_WISDOM, ENCHANTING_WISDOM,
                 FARMING_WISDOM, FISHING_WISDOM, FORAGING_WISDOM, MINING_WISDOM,
                 RUNE_CRAFTING_WISDOM, SOCIAL_WISDOM, TAMING_WISDOM, HUNTING_WISDOM -> Material.WRITABLE_BOOK;
            default -> Material.PAPER;
        };
    }

    // same case here than above
    public String getIconTexture() {
        return switch (this) {
            case CRITICAL_CHANCE -> "3e4f49535a276aacc4dc84133bfe81be5f2a4799a4c04d9a4ddb72d819ec2b2b";
            case CRITICAL_DAMAGE -> "ddafb23efc57f251878e5328d11cb0eef87b79c87b254a7ec72296f9363ef7c";
            case PRISTINE -> "d886e0f41185b18a3afd89488d2ee4caa0735009247cccf039ced6aed752ff1a";
            case MINING_FORTUNE, ORE_FORTUNE, BLOCK_FORTUNE, DWARVEN_METAL_FORTUNE, GEMSTONE_FORTUNE ->
                    "b73579575ca88b3a8afe1ed18907b3125fe0987b02a88ef0e8a01087c3d024c4";
            case FORAGING_FORTUNE -> "4e44e2a8dff90f5b005e76e6f5db7c12ae59cbbc56d8bc8050f3e3dbf0c3b734";
            case FARMING_FORTUNE -> "220ee7741ff1b958dbb9fa7cddad9c3cce93373f470f9b834da02da67c8202a4";
            case TROPHY_FISH_CHANCE -> "afe7bfb403d9c8c0cdc539c147035869a23e8810a0f3c74a767140180abd00a7";
            case PET_LUCK -> "bc78314255d8864a753fe95622564046f0dee2a82c6e4e2e7f452fcb95af318c";
            default -> null;
        };
    }

    public static ItemStatistics getOfAllBaseValues() {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic stat : ItemStatistic.values()) {
            builder.withBase(stat, stat.baseAdditiveValue);
            builder.withMultiplicative(stat, stat.baseMultiplicativeValue);
        }
        return builder.build();
    }
}
