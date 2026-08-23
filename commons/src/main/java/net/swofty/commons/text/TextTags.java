package net.swofty.commons.text;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class TextTags {

    static final String CONTEXT_KEY_PREFIX = "hsb.ctx.";
    static final String SEPARATOR_LINE = "-".repeat(53);
    static final int DEFAULT_BAR_LENGTH = 20;

    static final Set<String> CONTEXT_TAGS = Set.of("player", "rank", "purse", "bank", "island", "profile");

    static final TextDecoration[] DECORATION_ORDER = {
            TextDecoration.BOLD,
            TextDecoration.ITALIC,
            TextDecoration.UNDERLINED,
            TextDecoration.STRIKETHROUGH,
            TextDecoration.OBFUSCATED
    };

    private static final Map<String, NamedTextColor> COLOR_BY_TOKEN = new HashMap<>();
    private static final Map<Integer, Character> LETTER_BY_COLOR_VALUE = new HashMap<>();
    private static final Map<String, TextDecoration> DECORATION_BY_TOKEN = new HashMap<>();
    private static final Map<TextDecoration, Character> LETTER_BY_DECORATION = new EnumMap<>(TextDecoration.class);

    static {
        color('0', "black", NamedTextColor.BLACK);
        color('1', "dark_blue", NamedTextColor.DARK_BLUE);
        color('2', "dark_green", NamedTextColor.DARK_GREEN);
        color('3', "dark_aqua", NamedTextColor.DARK_AQUA);
        color('4', "dark_red", NamedTextColor.DARK_RED);
        color('5', "dark_purple", NamedTextColor.DARK_PURPLE);
        color('6', "gold", NamedTextColor.GOLD);
        color('7', "gray", NamedTextColor.GRAY);
        color('8', "dark_gray", NamedTextColor.DARK_GRAY);
        color('9', "blue", NamedTextColor.BLUE);
        color('a', "green", NamedTextColor.GREEN);
        color('b', "aqua", NamedTextColor.AQUA);
        color('c', "red", NamedTextColor.RED);
        color('d', "light_purple", NamedTextColor.LIGHT_PURPLE);
        color('e', "yellow", NamedTextColor.YELLOW);
        color('f', "white", NamedTextColor.WHITE);

        COLOR_BY_TOKEN.put("grey", NamedTextColor.GRAY);
        COLOR_BY_TOKEN.put("dark_grey", NamedTextColor.DARK_GRAY);

        decoration('l', "bold", TextDecoration.BOLD);
        decoration('o', "italic", TextDecoration.ITALIC);
        decoration('n', "underlined", TextDecoration.UNDERLINED);
        decoration('m', "strikethrough", TextDecoration.STRIKETHROUGH);
        decoration('k', "obfuscated", TextDecoration.OBFUSCATED);

        DECORATION_BY_TOKEN.put("underline", TextDecoration.UNDERLINED);
        DECORATION_BY_TOKEN.put("strike", TextDecoration.STRIKETHROUGH);
        DECORATION_BY_TOKEN.put("magic", TextDecoration.OBFUSCATED);
    }

    private TextTags() {
    }

    private static void color(char letter, String name, NamedTextColor value) {
        COLOR_BY_TOKEN.put(String.valueOf(letter), value);
        COLOR_BY_TOKEN.put(name, value);
        LETTER_BY_COLOR_VALUE.put(value.value(), letter);
    }

    private static void decoration(char letter, String name, TextDecoration value) {
        DECORATION_BY_TOKEN.put(String.valueOf(letter), value);
        DECORATION_BY_TOKEN.put(name, value);
        LETTER_BY_DECORATION.put(value, letter);
    }

    static TextColor color(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        NamedTextColor named = COLOR_BY_TOKEN.get(lower);
        if (named != null) {
            return named;
        }
        if (lower.length() == 7 && lower.charAt(0) == '#') {
            return TextColor.fromHexString(lower);
        }
        return null;
    }

    static TextDecoration decoration(String token) {
        return DECORATION_BY_TOKEN.get(token.toLowerCase(Locale.ROOT));
    }

    static String colorId(String token) {
        TextColor color = color(token);
        return color == null ? null : colorTag(color);
    }

    static String colorTag(TextColor color) {
        Character letter = LETTER_BY_COLOR_VALUE.get(color.value());
        return letter != null ? String.valueOf(letter) : String.format(Locale.ROOT, "#%06x", color.value());
    }

    static String colorToken(TextColor color) {
        return color instanceof NamedTextColor named ? NamedTextColor.NAMES.keyOrThrow(named) : color.asHexString();
    }

    static String decorationTag(TextDecoration decoration) {
        return String.valueOf(LETTER_BY_DECORATION.get(decoration));
    }

    static boolean isLegacyColorCode(char code) {
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
    }

    static NamedTextColor legacyColor(char code) {
        return COLOR_BY_TOKEN.get(String.valueOf(code));
    }

    static TextDecoration legacyDecoration(char code) {
        return switch (code) {
            case 'k' -> TextDecoration.OBFUSCATED;
            case 'l' -> TextDecoration.BOLD;
            case 'm' -> TextDecoration.STRIKETHROUGH;
            case 'n' -> TextDecoration.UNDERLINED;
            case 'o' -> TextDecoration.ITALIC;
            default -> null;
        };
    }
}
