package net.swofty.type.ravengardgeneric.classes;

import lombok.Getter;
import net.swofty.commons.text.Text;

import java.util.Arrays;
import java.util.List;

@Getter
public enum RavengardClass {
    KNIGHT("Knight", '\uE226'),
    WARRIOR("Warrior", '\uE228'),
    HUNTER("Hunter", '\uE227'),
    ASSASSIN("Assassin", '\uE225'),
    SORCERER("Sorcerer", '\uE229');

    private final String displayName;
    private final char icon;

    RavengardClass(String displayName, char icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    /** The class's small tag glyph from the pack's font/tags textures, used on item tooltips. */
    public int getTagGlyph() {
        return switch (this) {
            case KNIGHT -> 0xE210;
            case WARRIOR -> 0xE21E;
            case HUNTER -> 0xE221;
            case ASSASSIN -> 0xE200;
            case SORCERER -> 0xF23D;
        };
    }

    /** Class description and primary weapon, verbatim from the captured Select Class menu. */
    public List<Text> selectLore() {
        return switch (this) {
            case KNIGHT -> List.of(
                    Text.of("<7>A tank class specializing in defense."),
                    Text.of("<7>Knights can support themselves and"),
                    Text.of("<7>teammates against incoming damage."),
                    Text.empty(),
                    Text.of("<7>Primary weapon: <f>Sword and Shield"));
            case WARRIOR -> List.of(
                    Text.of("<7>A heavy damage dealer."),
                    Text.of("<7>Uses two-handed weapons to deal"),
                    Text.of("<7>devastating damage."),
                    Text.empty(),
                    Text.of("<7>Primary weapon: <f>Great Axe"));
            case HUNTER -> List.of(
                    Text.of("<7>Specializes in ranged combat."),
                    Text.of("<7>Hunters are known for using traps"),
                    Text.of("<7>to keep enemies at bay."),
                    Text.empty(),
                    Text.of("<7>Primary weapon: <f>Bow"));
            case ASSASSIN -> List.of(
                    Text.of("<7>A stealth-focused class adept at"),
                    Text.of("<7>sneaking."),
                    Text.empty(),
                    Text.of("<7>Primary weapon: <f>Daggers"));
            case SORCERER -> List.of(
                    Text.of("<7>A spellcaster focused on ability"),
                    Text.of("<7>damage."),
                    Text.of("<7>Sorcerers unleash elemental magic"),
                    Text.of("<7>to blast foes from a distance."),
                    Text.empty(),
                    Text.of("<7>Primary weapons: <f>Staves and Orbs"));
        };
    }

    /**
     * Base stats as the captured profile tooltips list them: health, protection, damage. Only the
     * Assassin and Sorcerer screens were captured, so the rest return null and their stats block
     * is left out rather than invented.
     */
    public int[] baseStats() {
        return switch (this) {
            case ASSASSIN -> new int[]{160, 0, 20};
            case SORCERER -> new int[]{170, 0, 20};
            default -> null;
        };
    }

    /**
     * The two abilities unlocked by default, bound to the drop and swap-hand keys.
     *
     * <p>Knight, Warrior and Assassin come from captures. Hunter has not been captured; every
     * captured pair is one offensive/utility ability plus one heal, so it follows that shape.
     */
    public List<RavengardAbility> defaultAbilities() {
        return switch (this) {
            case KNIGHT -> List.of(RavengardAbility.SENTINEL, RavengardAbility.RECOVERY);
            case WARRIOR -> List.of(RavengardAbility.WAR_CRY, RavengardAbility.COOL_OFF);
            case ASSASSIN -> List.of(RavengardAbility.SHADOWS, RavengardAbility.HEAL_WOUNDS);
            case HUNTER -> List.of(RavengardAbility.EAGLE_EYE, RavengardAbility.MED_KIT);
            case SORCERER -> List.of(RavengardAbility.FIREBALL, RavengardAbility.SPRING_HEALING);
        };
    }

    public List<RavengardAbility> getAbilities() {
        return Arrays.stream(RavengardAbility.values())
                .filter(ability -> ability.getOwner() == this)
                .toList();
    }

    public static RavengardClass fromKey(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        for (RavengardClass value : values()) {
            if (value.name().equalsIgnoreCase(key)) {
                return value;
            }
        }
        return null;
    }
}
