package net.swofty.type.ravengardgeneric.classes;

import lombok.Getter;
import net.swofty.commons.text.Text;
import net.swofty.type.ravengardgeneric.gui.RavengardSprite;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum RavengardAbility implements RavengardSprite {
    // cooldowns marked (capture) come from the live tooltips, which disagree with the wiki
    SENTINEL(RavengardClass.KNIGHT, "Sentinel", '\uF106', 100,   // capture; wiki says 1m 28s
            "Increases physical defense by 30% for 10 seconds."),
    PROVOKE(RavengardClass.KNIGHT, "Provoke", '\uF112', 26,
            "Taunts enemies within a 7-block radius to target you."),
    SHIELD_BASH(RavengardClass.KNIGHT, "Shield Bash", '\uF117', 26,
            "Pushes back all enemies within a 5-block radius."),
    RALLY(RavengardClass.KNIGHT, "Rally", '\uF10B', 79,
            "Temporarily boosts the damage dealt by all team members within a 6-block radius by 20%."),
    WOUND(RavengardClass.KNIGHT, "Wound", '\uF11B', 17,
            "Applies a bleed that deals 10 HP of damage every second for 5 seconds."),
    RECOVERY(RavengardClass.KNIGHT, "Recovery", '\uF115', 120,   // capture; wiki says 1m 45s
            "Instantly heals +30 HP."),
    DIRECT_HIT(RavengardClass.KNIGHT, "Direct Hit", '\uF103', 35,
            "Attacks deal 20% more damage for 5 seconds."),

    WAR_CRY(RavengardClass.WARRIOR, "War Cry", '\uF11A', 40,
            "Increases damage by 20% on your next attack."),
    ARMOR_BREAK(RavengardClass.WARRIOR, "Armor Break", '\uF100', 50,
            "Reduces the defense of the enemy by 10% for 5 seconds."),
    BOLSTER(RavengardClass.WARRIOR, "Bolster", '\uF102', 60,
            "Incoming damage is reduced by 20% for 10 seconds."),
    COOL_OFF(RavengardClass.WARRIOR, "Cool Off", '\uF101', 80,
            "Heals +35 HP over 10 seconds."),
    RUSH(RavengardClass.WARRIOR, "Rush", '\uF116', 20,
            "No stamina loss for 5 seconds."),
    OVERPOWER(RavengardClass.WARRIOR, "Overpower", '\uF10E', 10,
            "Knocks back all mobs within a radius of 5 blocks."),
    CARNAGE(RavengardClass.WARRIOR, "Carnage", '\uF113', 40,
            "Increases knockback for 5 seconds."),

    EAGLE_EYE(RavengardClass.HUNTER, "Eagle Eye", '\uF105', 40,
            "Increases arrow damage by 20% for 10 seconds."),
    RAPID_FIRE(RavengardClass.HUNTER, "Rapid Fire", '\uF114', 40,
            "Loose your next 3 arrows instantly."),
    POISON_ARROW(RavengardClass.HUNTER, "Poison Arrow", '\uF231', 30,
            "Your next arrow will poison its target for 6 seconds."),
    IMMOBILIZE(RavengardClass.HUNTER, "Immobilize", '\uF109', 30,
            "Your next arrow will slow its target for 2 seconds."),
    MED_KIT(RavengardClass.HUNTER, "Med Kit", '\uF10D', 120,
            "Heals +30 HP."),
    DOUBLE_SHOT(RavengardClass.HUNTER, "Double Shot", '\uF104', 40,
            "Loose two arrows simultaneously."),
    POWER_SHOT(RavengardClass.HUNTER, "Power Shot", '\uF110', 40,
            "Loose your next arrow instantly."),

    PRECISE_ACCURACY(RavengardClass.ASSASSIN, "Precise Accuracy", '\uF111', 40,
            "Attacks deal 30% more damage for 10 seconds."),
    SHADOW_STEP(RavengardClass.ASSASSIN, "Shadow Step", '\uF118', 25,
            "Dashes forward 6 blocks."),
    HEAL_WOUNDS(RavengardClass.ASSASSIN, "Heal Wounds", '\uF108', 120,
            "Heals +35 HP over 10 seconds."),
    LACERATE(RavengardClass.ASSASSIN, "Lacerate", '\uF10C', 60,
            "Your next attack will deal 80% more damage."),
    SHADOWS(RavengardClass.ASSASSIN, "Shadows", '\uF10A', 100,
            "Become invisible for 25 seconds, but movement speed is reduced.",
            "Attacking or being attacked cancels the invisibility."),
    SWIFT(RavengardClass.ASSASSIN, "Swift", '\uF119', 180,
            "Increases movement speed by 50% with no stamina loss."),
    POISON_DAGGER(RavengardClass.ASSASSIN, "Poison Dagger", '\uF10F', 30,
            "Applies 7 damage every 6 seconds to the target."),

    LIGHTNING_STRIKE(RavengardClass.SORCERER, "Lightning Strike", '\uF240', 40,
            "Fires an electric strike towards players. On impact causes 40 damage to target and damages others close by."),
    FIREBALL(RavengardClass.SORCERER, "Fireball", '\uF241', 25,
            "Spawn a small moving fireball that deals 60 damage on impact."),
    ICE_SHARDS(RavengardClass.SORCERER, "Ice Shards", '\uF242', 40,
            "Fires a continuous stream of ice that deals 10 damage and knockbacks on each hit."),
    RUMBLE(RavengardClass.SORCERER, "Rumble", '\uF243', 30,
            "Rips up the earth around its user, dealing 20 damage to any enemy within its radius."),
    SPRING_HEALING(RavengardClass.SORCERER, "Spring Healing", '\uF244', 40,
            "Heals +45 HP over 10 seconds."),
    WIND_CUTTER(RavengardClass.SORCERER, "Wind Cutter", '\uF245', 60,
            "Spawns 3 powerful air slashes through the air, dealing 50 damage and pushing enemies back."),
    ACCELERATE(RavengardClass.SORCERER, "Accelerate", '\uF23F', 40,
            "Temporarily reduces the cooldown on Magical Items by 50%!");

    private final RavengardClass owner;
    private final String displayName;
    private final char icon;
    private final int cooldownSeconds;
    private final String[] description;

    RavengardAbility(RavengardClass owner, String displayName, char icon, int cooldownSeconds,
                     String... description) {
        this.owner = owner;
        this.displayName = displayName;
        this.icon = icon;
        this.cooldownSeconds = cooldownSeconds;
        this.description = description;
    }

    private static final String MODEL_ROOT = "hypixel_ravengard:ui/menu/button/";

    // every ability button shares one sprite size, so one base pair covers all of them
    private static final int HOVER_BASE_X = 39;
    private static final int HOVER_BASE_Y = 25;

    @Override
    public String itemModel() {
        return MODEL_ROOT + name().toLowerCase();
    }

    @Override
    public int iconCodePoint() {
        return icon;
    }

    @Override
    public int hoverBaseX() {
        return HOVER_BASE_X;
    }

    @Override
    public int hoverBaseY() {
        return HOVER_BASE_Y;
    }

    @Override
    public int slotWidth() {
        return 1;
    }

    @Override
    public int slotHeight() {
        return 1;
    }

    /**
     * The live tooltips highlight the numbers in a description rather than printing it flat:
     * percentages green, HP amounts red, durations yellow. Inferred from the captured War Cry,
     * Cool Off, Shadows and Heal Wounds tooltips, which agree on all three.
     */
    public List<Text> getHighlightedDescription() {
        List<Text> lines = new ArrayList<>(description.length);
        for (String line : description) {
            String highlighted = line
                    .replaceAll("([+-]?\\d+(?:\\.\\d+)? ?HP)", "<c>$1</c>")
                    .replaceAll("(\\d+(?:\\.\\d+)?%)", "<a>$1</a>")
                    .replaceAll("(\\d+ seconds?)", "<e>$1</e>")
                    .replaceAll("(\\d+(?:-block| blocks?))", "<a>$1</a>")
                    .replaceAll("(\\d+)(?= damage)", "<a>$1</a>")
                    .replaceAll("(Magical Items)", "<d>$1</d>");
            lines.add(Text.of("<7>" + highlighted));
        }
        return lines;
    }

    /** Glyph of this ability's ui/spell texture, drawn in the bottom hud's two spell slots. */
    public int getHudCodePoint() {
        return switch (this) {
            case SENTINEL -> 0xE034; case PROVOKE -> 0xE032; case SHIELD_BASH -> 0xE035;
            case RALLY -> 0xE031; case WOUND -> 0xE036; case RECOVERY -> 0xE033;
            case DIRECT_HIT -> 0xE030;
            case WAR_CRY -> 0xE04D; case ARMOR_BREAK -> 0xE037; case BOLSTER -> 0xE038;
            case COOL_OFF -> 0xE039; case RUSH -> 0xE04C; case OVERPOWER -> 0xE04A;
            case CARNAGE -> 0xE04B;
            case EAGLE_EYE -> 0xE02A; case RAPID_FIRE -> 0xE02F; case POISON_ARROW -> 0xE050;
            case IMMOBILIZE -> 0xE02C; case MED_KIT -> 0xE02D; case DOUBLE_SHOT -> 0xE029;
            case POWER_SHOT -> 0xE02E;
            case PRECISE_ACCURACY -> 0xE025; case SHADOW_STEP -> 0xE026; case HEAL_WOUNDS -> 0xE021;
            case LACERATE -> 0xE023; case SHADOWS -> 0xE022; case SWIFT -> 0xE027;
            case POISON_DAGGER -> 0xE024;
            case LIGHTNING_STRIKE -> 0xF247; case FIREBALL -> 0xF248; case ICE_SHARDS -> 0xF249;
            case RUMBLE -> 0xF24A; case SPRING_HEALING -> 0xF24B; case WIND_CUTTER -> 0xF24C;
            case ACCELERATE -> 0xF246;
        };
    }

    /**
     * The highlighted description wrapped the way the captured tooltips wrap, roughly
     * thirty-six visible characters to a line, each continuation reopening in gray.
     */
    public List<Text> getWrappedDescription() {
        Text joined = Text.join(Text.literal("\n"), getHighlightedDescription());
        return Text.of("<wrap:36>{}</wrap>", joined).lines();
    }

    public int getCooldownTicks() {
        return cooldownSeconds * 20;
    }

    public String getCooldownText() {
        int minutes = cooldownSeconds / 60;
        int seconds = cooldownSeconds % 60;
        if (minutes == 0) {
            return seconds + "s";
        }
        return seconds == 0 ? minutes + "m" : minutes + "m " + seconds + "s";
    }

    public static RavengardAbility fromKey(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        for (RavengardAbility value : values()) {
            if (value.name().equalsIgnoreCase(key)) {
                return value;
            }
        }
        return null;
    }
}
