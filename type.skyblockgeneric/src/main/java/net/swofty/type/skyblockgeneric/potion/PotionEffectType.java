package net.swofty.type.skyblockgeneric.potion;

import lombok.Getter;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.potion.handler.DebuffHandler;
import net.swofty.type.skyblockgeneric.potion.handler.InstantDamageHandler;
import net.swofty.type.skyblockgeneric.potion.handler.InstantHealingHandler;
import net.swofty.type.skyblockgeneric.potion.handler.NoOpHandler;
import net.swofty.type.skyblockgeneric.potion.handler.PotionEffectHandler;
import net.swofty.type.skyblockgeneric.potion.handler.StatBuffHandler;
import net.swofty.type.skyblockgeneric.potion.handler.UtilityEffectHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@Getter
public enum PotionEffectType {
    // === BUFF POTIONS ===
    SPEED("Speed", PotionEffectCategory.BUFF, 8, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.SPEED, 5.0 * level) // +5 to +40 Speed
                    .build(),
            PotionEffect.SPEED),

    STRENGTH("Strength", PotionEffectCategory.BUFF, 8, 180,
            level -> {
                // Level 1-3: +5 per level, Level 4+: scales up to 75
                double strength = level <= 3 ? 5.0 * level : 15.0 + (level - 3) * 10.0;
                return ItemStatistics.builder()
                        .withBase(ItemStatistic.STRENGTH, Math.min(strength, 75.0))
                        .build();
            },
            PotionEffect.STRENGTH),

    CRITICAL("Critical", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.CRITICAL_CHANCE, 10.0 + (level - 1) * 5.0) // +10 to +25% Crit Chance
                    .withBase(ItemStatistic.CRITICAL_DAMAGE, 10.0 + (level - 1) * 10.0) // +10 to +40% Crit Damage
                    .build(),
            null),

    HASTE("Haste", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.MINING_SPEED, 50.0 * level) // +50 to +200 Mining Speed
                    .build(),
            PotionEffect.HASTE),

    REGENERATION("Regeneration", PotionEffectCategory.BUFF, 9, 45,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.HEALTH_REGENERATION, 5.0 + (level - 1) * 5.0) // +5 to +45 Health Regen
                    .build(),
            PotionEffect.REGENERATION),

    RESISTANCE("Resistance", PotionEffectCategory.BUFF, 8, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.DEFENSE, 5.0 + (level - 1) * 7.5) // +5 to +60 Defense
                    .build(),
            PotionEffect.RESISTANCE),

    MANA("Mana", PotionEffectCategory.BUFF, 8, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.INTELLIGENCE, (double) level) // +1 to +8 Mana per second (tracked separately)
                    .build(),
            null),

    ABSORPTION("Absorption", PotionEffectCategory.BUFF, 8, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.HEALTH, 20.0 + (level - 1) * 40.0) // +20 to +300 Absorption HP
                    .build(),
            PotionEffect.ABSORPTION),

    ARCHERY("Archery", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withMultiplicativePercentage(ItemStatistic.DAMAGE, 12.5 * level) // +12.5% to +50% bow damage
                    .build(),
            null),

    SPELUNKER("Spelunker", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.MINING_FORTUNE, 5.0 + (level - 1) * 6.67) // +5 to +25 Mining Fortune
                    .build(),
            null),

    MAGIC_FIND("Magic Find", PotionEffectCategory.BUFF, 4, 180,
            level -> {
                // +10, +25, +50, +75 Magic Find
                double[] values = {10.0, 25.0, 50.0, 75.0};
                return ItemStatistics.builder()
                        .withBase(ItemStatistic.MAGIC_FIND, values[Math.min(level - 1, 3)])
                        .build();
            },
            null),

    PET_LUCK("Pet Luck", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.PET_LUCK, 5.0 * level) // +5 to +20 Pet Luck
                    .build(),
            null),

    TRUE_RESISTANCE("True Resistance", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.TRUE_DEFENSE, 5.0 * level) // +5 to +20 True Defense
                    .build(),
            null),

    SPIRIT("Spirit", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.SPEED, 10.0 * level) // +10 to +40 Speed
                    .withBase(ItemStatistic.CRITICAL_DAMAGE, 10.0 * level) // +10 to +40 Crit Damage
                    .build(),
            null),

    ADRENALINE("Adrenaline", PotionEffectCategory.BUFF, 8, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.HEALTH, 20.0 + (level - 1) * 40.0) // +20 to +300 Absorption
                    .withBase(ItemStatistic.SPEED, 5.0 * level) // +5 to +40 Speed
                    .build(),
            null),

    AGILITY("Agility", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.SPEED, 10.0 * level) // +10 to +40 Speed
                    // Dodge chance handled separately
                    .build(),
            null),

    DODGE("Dodge", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder().build(), // 10-40% dodge chance, handled by combat system
            null),

    EXPERIENCE("Experience", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder().build(), // +5-20% XP gain, handled separately
            null),

    BURNING("Burning", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder().build(), // +5-20% fire duration, handled separately
            null),

    RABBIT("Rabbit", PotionEffectCategory.BUFF, 6, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.SPEED, 10.0 * level) // +10 to +60 Speed
                    .build(),
            PotionEffect.JUMP_BOOST),

    KNOCKBACK("Knockback", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder().build(), // +20-80% knockback, handled separately
            null),

    STUN("Stun", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder().build(), // 10-40% stun chance, handled separately
            null),

    // === INSTANT POTIONS ===
    HEALING("Healing", PotionEffectCategory.INSTANT, 8, 0,
            level -> ItemStatistics.builder().build(), // Instant heal handled separately
            PotionEffect.INSTANT_HEALTH),

    DAMAGE("Harming", PotionEffectCategory.INSTANT, 8, 0,
            level -> ItemStatistics.builder().build(), // Instant damage handled separately
            PotionEffect.INSTANT_DAMAGE),

    STAMINA("Stamina", PotionEffectCategory.INSTANT, 4, 0,
            level -> ItemStatistics.builder().build(), // Instant heal + mana handled separately
            null),

    // === DEBUFF POTIONS ===
    POISON("Poison", PotionEffectCategory.DEBUFF, 4, 45,
            level -> ItemStatistics.builder().build(), // Damage over time, handled separately
            PotionEffect.POISON),

    WEAKNESS("Weakness", PotionEffectCategory.DEBUFF, 8, 90,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.DAMAGE, -5.0 - (level - 1) * 6.25) // -5 to -50 Damage
                    .build(),
            PotionEffect.WEAKNESS),

    SLOWNESS("Slowness", PotionEffectCategory.DEBUFF, 8, 90,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.SPEED, -5.0 * level) // -5 to -40 Speed
                    .build(),
            PotionEffect.SLOWNESS),

    BLINDNESS("Blindness", PotionEffectCategory.DEBUFF, 3, 45,
            level -> ItemStatistics.builder().build(),
            PotionEffect.BLINDNESS),

    VENOMOUS("Venomous", PotionEffectCategory.DEBUFF, 4, 45,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.SPEED, -5.0 * level) // -5 to -20 Speed
                    .build(),
            null),

    WOUNDED("Wounded", PotionEffectCategory.DEBUFF, 4, 45,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.HEALTH_REGENERATION, -25.0 * level) // -25 to -100% healing
                    .build(),
            null),

    // === UTILITY POTIONS ===
    FIRE_RESISTANCE("Fire Resistance", PotionEffectCategory.UTILITY, 1, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.HEAT_RESISTANCE, 10.0) // 10% reduced fire/lava damage
                    .build(),
            PotionEffect.FIRE_RESISTANCE),

    NIGHT_VISION("Night Vision", PotionEffectCategory.UTILITY, 1, 180,
            level -> ItemStatistics.builder().build(),
            PotionEffect.NIGHT_VISION),

    WATER_BREATHING("Water Breathing", PotionEffectCategory.UTILITY, 6, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.RESPIRATION, 15.0 + (level - 1) * 17.0) // 15-100% no drowning
                    .build(),
            PotionEffect.WATER_BREATHING),

    JUMP_BOOST("Jump Boost", PotionEffectCategory.UTILITY, 4, 180,
            level -> ItemStatistics.builder().build(), // Jump height handled by Minestom effect
            PotionEffect.JUMP_BOOST),

    INVISIBILITY("Invisibility", PotionEffectCategory.UTILITY, 1, 180,
            level -> ItemStatistics.builder().build(),
            PotionEffect.INVISIBILITY),

    // === WISDOM POTIONS ===
    ALCHEMY_XP_BOOST("Alchemy XP Boost", PotionEffectCategory.BUFF, 3, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.ALCHEMY_WISDOM, 5.0 + (level - 1) * 7.5) // +5 to +20 Alchemy Wisdom
                    .build(),
            null),

    COMBAT_XP_BOOST("Combat XP Boost", PotionEffectCategory.BUFF, 3, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.COMBAT_WISDOM, 5.0 + (level - 1) * 7.5)
                    .build(),
            null),

    ENCHANTING_XP_BOOST("Enchanting XP Boost", PotionEffectCategory.BUFF, 3, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.ENCHANTING_WISDOM, 5.0 + (level - 1) * 7.5)
                    .build(),
            null),

    FARMING_XP_BOOST("Farming XP Boost", PotionEffectCategory.BUFF, 3, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.FARMING_WISDOM, 5.0 + (level - 1) * 7.5)
                    .build(),
            null),

    FISHING_XP_BOOST("Fishing XP Boost", PotionEffectCategory.BUFF, 3, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.FISHING_WISDOM, 5.0 + (level - 1) * 7.5)
                    .build(),
            null),

    FORAGING_XP_BOOST("Foraging XP Boost", PotionEffectCategory.BUFF, 3, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.FORAGING_WISDOM, 5.0 + (level - 1) * 7.5)
                    .build(),
            null),

    MINING_XP_BOOST("Mining XP Boost", PotionEffectCategory.BUFF, 3, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.MINING_WISDOM, 5.0 + (level - 1) * 7.5)
                    .build(),
            null),

    // === SPECIAL POTIONS ===
    COLD_RESISTANCE("Cold Resistance", PotionEffectCategory.BUFF, 4, 180,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.COLD_RESISTANCE, 2.5 * level) // +2.5 to +10 Cold Resistance
                    .build(),
            null),

    HARVEST_HARBINGER("Harvest Harbinger", PotionEffectCategory.BUFF, 5, 1800,
            level -> ItemStatistics.builder()
                    .withBase(ItemStatistic.FARMING_FORTUNE, 50.0) // +50 Farming Fortune (level 5 only)
                    .build(),
            null),

    // === BASE POTIONS ===
    AWKWARD("Awkward", PotionEffectCategory.BASE, 1, 0,
            level -> ItemStatistics.builder().build(),
            null),

    THICK("Thick", PotionEffectCategory.BASE, 1, 0,
            level -> ItemStatistics.builder().build(),
            null),

    MUNDANE("Mundane", PotionEffectCategory.BASE, 1, 0,
            level -> ItemStatistics.builder().build(),
            null),

    WATER("Water", PotionEffectCategory.BASE, 1, 0,
            level -> ItemStatistics.builder().build(),
            null),
    ;

    private final String displayName;
    private final PotionEffectCategory category;
    private final int maxLevel;
    private final int baseSecondsDuration; // Base duration in seconds (before modifiers/alchemy bonus)
    private final Function<Integer, ItemStatistics> statsFunction;
    private final @Nullable PotionEffect minestomEffect; // The vanilla Minestom effect to apply (if any)

    PotionEffectType(String displayName, PotionEffectCategory category, int maxLevel, int baseSecondsDuration,
                     Function<Integer, ItemStatistics> statsFunction, @Nullable PotionEffect minestomEffect) {
        this.displayName = displayName;
        this.category = category;
        this.maxLevel = maxLevel;
        this.baseSecondsDuration = baseSecondsDuration;
        this.statsFunction = statsFunction;
        this.minestomEffect = minestomEffect;
    }

    /**
     * Get the ItemStatistics for this potion effect at a given level
     */
    public ItemStatistics getStatistics(int level) {
        if (statsFunction == null) return ItemStatistics.empty();
        return statsFunction.apply(Math.min(level, maxLevel));
    }

    /**
     * Get display name with roman numeral level
     */
    public String getLevelDisplay(int level) {
        if (maxLevel == 1) return displayName;
        return displayName + " " + StringUtility.getAsRomanNumeral(level);
    }

    /**
     * Get the color code for this effect's category
     */
    public String getColor() {
        return category.getColor();
    }

    /**
     * Get the description for this effect at the given level
     */
    public String getDescription(int level) {
        ItemStatistics stats = getStatistics(level);
        return switch (this) {
            case SPEED -> Text.of("Increases <a>Speed <7>by <a>{}<7>.", stats.getOverall(ItemStatistic.SPEED).intValue()).serialize();
            case STRENGTH -> Text.of("Increases <c>Strength <7>by <c>{}<7>.", stats.getOverall(ItemStatistic.STRENGTH).intValue()).serialize();
            case CRITICAL -> Text.of("Grants <9>+{}% Crit Chance <7>and <9>+{}% Crit Damage<7>.",
                    stats.getOverall(ItemStatistic.CRITICAL_CHANCE).intValue(),
                    stats.getOverall(ItemStatistic.CRITICAL_DAMAGE).intValue()).serialize();
            case HASTE -> Text.of("Increases <6>Mining Speed <7>by <6>{}<7>.", stats.getOverall(ItemStatistic.MINING_SPEED).intValue()).serialize();
            case REGENERATION -> Text.of("Increases <c>Health Regen <7>by <c>{}<7>.", stats.getOverall(ItemStatistic.HEALTH_REGENERATION).intValue()).serialize();
            case RESISTANCE -> Text.of("Increases <a>Defense <7>by <a>{}<7>.", stats.getOverall(ItemStatistic.DEFENSE).intValue()).serialize();
            case MANA -> Text.of("Regenerate <b>{} Mana <7>per second.", level).serialize();
            case ABSORPTION -> Text.of("Grants <6>{} Absorption Hearts<7>.", stats.getOverall(ItemStatistic.HEALTH).intValue()).serialize();
            case ARCHERY -> Text.of("Increases <c>Bow Damage <7>by <c>{}%<7>.", (int) (12.5 * level)).serialize();
            case SPELUNKER -> Text.of("Increases <6>Mining Fortune <7>by <6>{}<7>.", stats.getOverall(ItemStatistic.MINING_FORTUNE).intValue()).serialize();
            case MAGIC_FIND -> Text.of("Increases <b>Magic Find <7>by <b>{}<7>.", stats.getOverall(ItemStatistic.MAGIC_FIND).intValue()).serialize();
            case PET_LUCK -> Text.of("Increases <d>Pet Luck <7>by <d>{}<7>.", stats.getOverall(ItemStatistic.PET_LUCK).intValue()).serialize();
            case TRUE_RESISTANCE -> Text.of("Increases <f>True Defense <7>by <f>{}<7>.", stats.getOverall(ItemStatistic.TRUE_DEFENSE).intValue()).serialize();
            case HEALING -> Text.of("Instantly heals <c>{} Health<7>.", (int) getInstantHealAmount(level)).serialize();
            case DAMAGE -> Text.of("Deals <c>{} damage <7>to enemies.", (int) getInstantDamageAmount(level)).serialize();
            case STAMINA -> Text.of("Instantly restores <c>{} Health <7>and <b>{} Mana<7>.",
                    (int) getStaminaHealAmount(level), (int) getStaminaManaBoost(level)).serialize();
            case POISON -> Text.of("Deals <2>{} damage <7>per second.", (int) getPoisonDamagePerSecond(level)).serialize();
            case WEAKNESS -> Text.of("Decreases <c>Damage <7>by <c>{}<7>.", Math.abs(stats.getOverall(ItemStatistic.DAMAGE).intValue())).serialize();
            case SLOWNESS -> Text.of("Decreases <a>Speed <7>by <c>{}<7>.", Math.abs(stats.getOverall(ItemStatistic.SPEED).intValue())).serialize();
            case FIRE_RESISTANCE -> "Reduces fire and lava damage.";
            case NIGHT_VISION -> "Allows you to see in the dark.";
            case WATER_BREATHING -> "Allows you to breathe underwater.";
            case JUMP_BOOST -> "Increases jump height.";
            case INVISIBILITY -> "Makes you invisible to mobs.";
            case SPIRIT -> Text.of("Increases <c>Crit Damage <7>by <c>{}<7>.", stats.getOverall(ItemStatistic.CRITICAL_DAMAGE).intValue()).serialize();
            case DODGE, AGILITY -> Text.of("Grants <a>{}% <7>chance to dodge attacks.", (int) getDodgeChance(level)).serialize();
            case STUN -> Text.of("Grants <e>{}% <7>chance to stun enemies.", (int) getStunChance(level)).serialize();
            case AWKWARD, THICK, MUNDANE, WATER -> "No effect.";
            default -> "";
        };
    }

    /**
     * Check if this effect has a Minestom (vanilla) visual effect
     */
    public boolean hasMinestomEffect() {
        return minestomEffect != null;
    }

    /**
     * Get the instant healing amount for healing potions
     */
    public double getInstantHealAmount(int level) {
        if (this != HEALING) return 0;
        // +20 HP per level, up to 160 at level 8
        return 20.0 * level;
    }

    /**
     * Get the instant damage amount for damage potions
     */
    public double getInstantDamageAmount(int level) {
        if (this != DAMAGE) return 0;
        // 20 damage per level, up to 160 at level 8
        return 20.0 * level;
    }

    /**
     * Get the poison damage per second
     */
    public double getPoisonDamagePerSecond(int level) {
        if (this != POISON) return 0;
        // 10 + 12.5 per level above 1
        return 10.0 + (level - 1) * 12.5;
    }

    /**
     * Get the stamina potion heal amount
     */
    public double getStaminaHealAmount(int level) {
        if (this != STAMINA) return 0;
        // 150 + 121.67 per level
        return 150.0 + (level - 1) * 121.67;
    }

    /**
     * Get the stamina potion mana boost
     */
    public double getStaminaManaBoost(int level) {
        if (this != STAMINA) return 0;
        // 50 + 50 per level
        return 50.0 * level;
    }

    /**
     * Get the dodge chance percentage for dodge/agility potions
     */
    public double getDodgeChance(int level) {
        if (this != DODGE && this != AGILITY) return 0;
        return 10.0 * level; // 10-40%
    }

    /**
     * Get the stun chance percentage for stun potions
     */
    public double getStunChance(int level) {
        if (this != STUN) return 0;
        return 10.0 * level; // 10-40%
    }

    /**
     * Get a PotionEffectType from its name (case-insensitive)
     */
    public static @Nullable PotionEffectType fromName(String name) {
        if (name == null || name.isEmpty()) return null;

        // Handle common aliases from YAML/vanilla naming
        String normalizedName = name.toUpperCase().replace(" ", "_");
        normalizedName = switch (normalizedName) {
            case "INSTANT_HEALTH" -> "HEALING";
            case "INSTANT_DAMAGE", "HARMING" -> "DAMAGE";
            case "JUMP" -> "JUMP_BOOST";
            case "SLOWNESS" -> "SLOWNESS";
            case "SWIFTNESS" -> "SPEED";
            default -> normalizedName;
        };

        try {
            return valueOf(normalizedName);
        } catch (IllegalArgumentException e) {
            // Try matching by display name
            for (PotionEffectType type : values()) {
                if (type.displayName.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Check if this effect affects mobs when splashed
     */
    public boolean affectsMobs() {
        return switch (this) {
            case HEALING, DAMAGE, POISON, WEAKNESS, SLOWNESS, STUN -> true;
            default -> false;
        };
    }

    /**
     * Check if this effect can be applied to other players when splashed
     */
    public boolean affectsOtherPlayers() {
        return getHandler().affectsOtherPlayers();
    }

    /**
     * Get the handler for this effect type.
     * Handlers encapsulate the logic for applying effects.
     */
    public PotionEffectHandler getHandler() {
        return switch (this) {
            // Instant effects
            case HEALING -> new InstantHealingHandler();
            case DAMAGE -> new InstantDamageHandler();
            case STAMINA -> new InstantHealingHandler(); // Similar to healing

            // Debuffs
            case POISON, WEAKNESS, SLOWNESS, BLINDNESS, VENOMOUS, WOUNDED -> new DebuffHandler(this);

            // Utility effects
            case FIRE_RESISTANCE, NIGHT_VISION, WATER_BREATHING, JUMP_BOOST, INVISIBILITY -> new UtilityEffectHandler(this);

            // Base potions (no effect)
            case AWKWARD, THICK, MUNDANE, WATER -> NoOpHandler.INSTANCE;

            // All other buffs
            default -> new StatBuffHandler(this);
        };
    }

    /**
     * Get the vanilla PotionType for this effect, if one exists.
     * Used for setting the potion_contents component on items.
     * @param level The potion level
     * @param isExtended Whether the potion has extended duration
     * @return The vanilla PotionType or null for SkyBlock-only effects
     */
    public @Nullable PotionType getVanillaPotionType(int level, boolean isExtended) {
        return switch (this) {
            case WATER -> PotionType.WATER;
            case AWKWARD -> PotionType.AWKWARD;
            case THICK -> PotionType.THICK;
            case MUNDANE -> PotionType.MUNDANE;
            case SPEED -> isExtended ? PotionType.LONG_SWIFTNESS : (level >= 2 ? PotionType.STRONG_SWIFTNESS : PotionType.SWIFTNESS);
            case STRENGTH -> isExtended ? PotionType.LONG_STRENGTH : (level >= 2 ? PotionType.STRONG_STRENGTH : PotionType.STRENGTH);
            case REGENERATION -> isExtended ? PotionType.LONG_REGENERATION : (level >= 2 ? PotionType.STRONG_REGENERATION : PotionType.REGENERATION);
            case HEALING -> level >= 2 ? PotionType.STRONG_HEALING : PotionType.HEALING;
            case DAMAGE -> level >= 2 ? PotionType.STRONG_HARMING : PotionType.HARMING;
            case POISON -> isExtended ? PotionType.LONG_POISON : (level >= 2 ? PotionType.STRONG_POISON : PotionType.POISON);
            case WEAKNESS -> isExtended ? PotionType.LONG_WEAKNESS : PotionType.WEAKNESS;
            case SLOWNESS -> isExtended ? PotionType.LONG_SLOWNESS : (level >= 2 ? PotionType.STRONG_SLOWNESS : PotionType.SLOWNESS);
            case FIRE_RESISTANCE -> isExtended ? PotionType.LONG_FIRE_RESISTANCE : PotionType.FIRE_RESISTANCE;
            case NIGHT_VISION -> isExtended ? PotionType.LONG_NIGHT_VISION : PotionType.NIGHT_VISION;
            case WATER_BREATHING -> isExtended ? PotionType.LONG_WATER_BREATHING : PotionType.WATER_BREATHING;
            case JUMP_BOOST -> isExtended ? PotionType.LONG_LEAPING : (level >= 2 ? PotionType.STRONG_LEAPING : PotionType.LEAPING);
            case INVISIBILITY -> isExtended ? PotionType.LONG_INVISIBILITY : PotionType.INVISIBILITY;
            default -> null; // SkyBlock-only effects have no vanilla equivalent
        };
    }

    /**
     * Get a custom color for this potion effect.
     * Used when there's no vanilla PotionType or for SkyBlock-specific effects.
     * Colors are based on the effect's theme.
     */
    public RGBLike getPotionColor() {
        return switch (this) {
            // Buff potions - generally positive/bright colors
            case SPEED -> new Color(124, 175, 198); // Light blue (swiftness)
            case STRENGTH -> new Color(147, 36, 35); // Dark red
            case CRITICAL -> new Color(220, 20, 60); // Crimson
            case HASTE -> new Color(217, 192, 67); // Gold/yellow
            case REGENERATION -> new Color(205, 92, 171); // Pink
            case RESISTANCE -> new Color(153, 69, 58); // Brown-red
            case MANA -> new Color(33, 150, 243); // Bright blue
            case ABSORPTION -> new Color(37, 82, 165); // Dark blue
            case ARCHERY -> new Color(255, 127, 80); // Coral/orange
            case SPELUNKER -> new Color(139, 90, 43); // Brown (mining)
            case MAGIC_FIND -> new Color(85, 255, 255); // Cyan
            case PET_LUCK -> new Color(255, 85, 255); // Magenta
            case TRUE_RESISTANCE -> new Color(255, 255, 255); // White
            case SPIRIT -> new Color(170, 170, 255); // Light purple
            case ADRENALINE -> new Color(255, 85, 85); // Light red
            case AGILITY, DODGE -> new Color(144, 238, 144); // Light green
            case EXPERIENCE -> new Color(127, 255, 0); // Chartreuse
            case BURNING -> new Color(255, 100, 0); // Orange-red
            case RABBIT -> new Color(85, 255, 85); // Bright green
            case KNOCKBACK -> new Color(210, 180, 140); // Tan
            case STUN -> new Color(255, 215, 0); // Gold

            // Instant effects
            case HEALING -> new Color(248, 36, 35); // Red (instant health)
            case DAMAGE -> new Color(67, 10, 9); // Dark red (harming)
            case STAMINA -> new Color(255, 128, 0); // Orange

            // Debuffs - darker/muted colors
            case POISON -> new Color(78, 147, 49); // Green
            case WEAKNESS -> new Color(72, 77, 72); // Gray
            case SLOWNESS -> new Color(90, 108, 129); // Slate blue
            case BLINDNESS -> new Color(31, 31, 35); // Near black
            case VENOMOUS -> new Color(50, 100, 50); // Dark green
            case WOUNDED -> new Color(139, 0, 0); // Dark red

            // Utility effects
            case FIRE_RESISTANCE -> new Color(227, 154, 56); // Orange
            case NIGHT_VISION -> new Color(31, 31, 161); // Dark blue
            case WATER_BREATHING -> new Color(46, 82, 153); // Ocean blue
            case JUMP_BOOST -> new Color(34, 255, 76); // Bright green (leaping)
            case INVISIBILITY -> new Color(127, 131, 146); // Gray

            // Wisdom/XP potions
            case ALCHEMY_XP_BOOST, ENCHANTING_XP_BOOST -> new Color(148, 0, 211); // Purple
            case COMBAT_XP_BOOST -> new Color(255, 0, 0); // Red
            case FARMING_XP_BOOST -> new Color(0, 128, 0); // Green
            case FISHING_XP_BOOST -> new Color(0, 191, 255); // Deep sky blue
            case FORAGING_XP_BOOST -> new Color(139, 69, 19); // Saddle brown
            case MINING_XP_BOOST -> new Color(192, 192, 192); // Silver

            // Special potions
            case COLD_RESISTANCE -> new Color(173, 216, 230); // Light blue
            case HARVEST_HARBINGER -> new Color(34, 139, 34); // Forest green

            // Base potions
            case AWKWARD -> new Color(40, 40, 255); // Blue-ish
            case THICK -> new Color(100, 100, 100); // Gray
            case MUNDANE -> new Color(120, 100, 180); // Dull purple
            case WATER -> new Color(55, 93, 198); // Water blue
        };
    }
}
