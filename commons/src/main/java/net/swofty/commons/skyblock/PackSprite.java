package net.swofty.commons.skyblock;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// this is named as PackSprite even though they are font glyphs as of now. I think they're going to change this at some point, so that's why I made it like this.
@AllArgsConstructor
public enum PackSprite {
    // All things found in the font from hypixel_skyblock:gui/icons.png
    GUI_LOCATION("\uE067"),
    GUI_ARROW_LEFT("\uE060"),
    GUI_ARROW_RIGHT("\uE061"),
    GUI_ARROW_UP("\uE062"),
    GUI_ARROW_DOWN("\uE063"),
    GUI_CHECKMARK("✔"),
    GUI_DENY("✖"),
    GUI_LOCK("\uE066"),
    GUI_ARROW_UPWARDS("\uE068"),

    // gui/skills
    SKILL_SWORD("\uE050"),

    // gui/stats - combat
    STAT_HEALTH("\uE010"),
    STAT_DEFENSE("\uE008"),
    STAT_TRUE_DEFENSE("\uE027"),
    STAT_STRENGTH("\uE00D"),
    STAT_CRIT_DAMAGE("\uE007"),
    STAT_CRIT_CHANCE("\uE02C"),
    STAT_ATTACK_SPEED("\uE001"),
    STAT_SWING_RANGE("\uE024"),
    STAT_INTELLIGENCE("\uE003"),
    STAT_FEROCITY("\uE00B"),
    STAT_ABILITY_DAMAGE("\uE002"),
    STAT_HEALTH_REGEN("\uE011"),
    STAT_VITALITY("\uE028"),
    STAT_MENDING("\uE014"),

    // gui/stats - mining
    STAT_BREAKING_POWER("\uE005"),
    STAT_MINING_SPEED("\uE015"),
    STAT_MINING_SPREAD("\uE016"),
    STAT_GEMSTONE_SPREAD("\uE00F"),
    STAT_PRISTINE("\uE01C"),
    STAT_MINING_FORTUNE("\uE053"),
    STAT_ORE_FORTUNE("\uE053"),
    STAT_BLOCK_FORTUNE("\uE053"),
    STAT_DWARVEN_METAL_FORTUNE("\uE053"),
    STAT_GEMSTONE_FORTUNE("\uE053"),

    // gui/stats - farming
    STAT_BONUS_PEST_CHANCE("\uE019"),
    STAT_OVERBLOOM("\uE02B"),
    STAT_FARMING_FORTUNE("\uE051"),

    // gui/stats - foraging
    STAT_SWEEP("\uE023"),
    STAT_FORAGING_FORTUNE("\uE054"),
    STAT_FIG_FORTUNE("\uE054"),
    STAT_MANGROVE_FORTUNE("\uE054"),

    // gui/stats - fishing
    STAT_FISHING_SPEED("\uE00C"),
    STAT_SEA_CREATURE_CHANCE("\uE021"),
    STAT_DOUBLE_HOOK_CHANCE("\uE009"),
    STAT_TROPHY_CHANCE("\uE02A"),
    STAT_TREASURE_CHANCE("\uE025"),

    // gui/stats - hunting
    STAT_PULL("\uE02D"),
    STAT_HUNTER_FORTUNE("\uE05B"),

    // gui/stats - miscellaneous
    STAT_SPEED("\uE022"),
    STAT_MAGIC_FIND("\uE01A"),
    STAT_TRACKING("\uE077"),
    STAT_PET_LUCK("\uE013"),
    STAT_HEAT_RESISTANCE("\uE012"),
    STAT_COLD_RESISTANCE("\uE006"),
    STAT_RESPIRATION("\uE01D"),
    STAT_PRESSURE_RESISTANCE("\uE01B"),
    STAT_FEAR("\uE00A"),

    // gui/stats - wisdom
    STAT_WISDOM("☯"),

    // gui/stats - rift
    STAT_RIFT_TIME("\uE020"),
    STAT_RIFT_DAMAGE("\uE01E"),
    STAT_RIFT_MANA_REGEN("\uE004"),
    STAT_RIFT_HEARTS("\uE01F"),
    STAT_RIFT_SPEED("\uE022"),

    // gui/stats - other
    STAT_EFFECTIVE_HEALTH("\uE010"),
    STAT_ABSORPTION("\uE010"),
    STAT_MANA("\uE003"),
    STAT_SOULFLOW("⸎"),
    STAT_OVERFLOW_MANA("\uE017"),
    STAT_TRUE_DAMAGE("✷"),
    STAT_HEAT("\uE012"),
    STAT_COLD("\uE006"),
    STAT_PRESSURE("\uE01B"),
    STAT_CROP_FORTUNE("\uE051"),

    // gui/mobs
    MOBS_UNDEAD("\uE084"),
    MOBS_SKELETAL("\uE081"),
    MOBS_ENDER("\uE078"),
    MOBS_ARTHROPOD("\uE074"),
    MOBS_HUMANOID("\uE07B"),
    MOBS_INFERNAL("\uE07C"),
    MOBS_CUBIC("\uE076"),
    MOBS_FROZEN("\uE079"),
    MOBS_SPOOKY("\uE082"),
    MOBS_MYTHOLOGICAL("\uE07E"),
    MOBS_WITHER("\uE085"),
    MOBS_SUBTERRANEAN("\uE083"),
    MOBS_AQUATIC("\uE072"),
    MOBS_PEST("\uE07F"),
    MOBS_ANIMAL("\uE071"),
    MOBS_MAGMATIC("\uE07D"),
    MOBS_ELUSIVE("\uE077"),
    MOBS_CONSTRUCT("\uE075"),
    MOBS_ARCANE("\uE073"),
    MOBS_SHIELDED("\uE080"),
    MOBS_AIRBORNE("\uE070"),
    MOBS_GLACIAL("\uE07A"),
    MOBS_WOODLAND("\uE086"),

    // icons/staff
    ICONS_STAFF("ዞ");

    // this is hidden like this, so you never accidentally even imagine using it as a String. Use the Component.
    // Hypixel will probably move to sprites, and then we have to use Components.
    private final String sprite;

    public Component getSprite() {
        return Component.text(sprite);
    }

    private static final Map<String, PackSprite> BY_TAG_NAME =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(
                    sprite -> sprite.name().toLowerCase(Locale.ROOT),
                    Function.identity()
            ));
}
