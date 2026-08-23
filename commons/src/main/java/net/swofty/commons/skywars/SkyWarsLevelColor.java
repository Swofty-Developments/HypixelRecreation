package net.swofty.commons.skywars;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;

@Getter
public enum SkyWarsLevelColor {
    NONE(0, NamedTextColor.GRAY, "⋆"),
    IRON(5, NamedTextColor.WHITE, "✦"),
    GOLD(10, NamedTextColor.GOLD, "✦"),
    DIAMOND(15, NamedTextColor.AQUA, "✦"),
    EMERALD(20, NamedTextColor.GREEN, "✦"),
    SAPPHIRE(25, NamedTextColor.DARK_AQUA, "✦"),
    RUBY(30, NamedTextColor.RED, "✦"),
    CRYSTAL(35, NamedTextColor.LIGHT_PURPLE, "✦"),
    OPAL(40, NamedTextColor.DARK_GRAY, "✦"),
    AMETHYST(45, NamedTextColor.DARK_PURPLE, "✦"),
    RAINBOW(50, NamedTextColor.WHITE, "✦");

    private static final TextColor[] RAINBOW_CYCLE = {
            NamedTextColor.RED,
            NamedTextColor.GOLD,
            NamedTextColor.YELLOW,
            NamedTextColor.GREEN,
            NamedTextColor.AQUA,
            NamedTextColor.LIGHT_PURPLE
    };

    private final int minimumLevel;
    private final TextColor color;
    private final String symbol;

    SkyWarsLevelColor(int minimumLevel, TextColor color, String symbol) {
        this.minimumLevel = minimumLevel;
        this.color = color;
        this.symbol = symbol;
    }

    public static SkyWarsLevelColor fromLevel(int level) {
        SkyWarsLevelColor result = NONE;
        for (SkyWarsLevelColor color : values()) {
            if (level >= color.minimumLevel) {
                result = color;
            }
        }
        return result;
    }

    public Text constructLevelBrackets(int level) {
        if (this == RAINBOW) {
            return constructRainbowBrackets(level);
        }
        return Text.of("<color:{}>[{}{}]", color, level, symbol);
    }

    public Text constructLevelString(int level) {
        if (this == RAINBOW) {
            return constructRainbowString(level);
        }
        return Text.of("<color:{}>{}{}", color, level, symbol);
    }

    private Text constructRainbowBrackets(int level) {
        return Text.of("<color:{}>[", RAINBOW_CYCLE[0])
                .append(rainbowDigits(level, 1))
                .append("<f>{}", symbol)
                .append("<color:{}>]", RAINBOW_CYCLE[0]);
    }

    private Text constructRainbowString(int level) {
        return rainbowDigits(level, 0).append("<f>{}", symbol);
    }

    private static Text rainbowDigits(int level, int offset) {
        String digits = String.valueOf(level);
        Text result = Text.empty();
        for (int i = 0; i < digits.length(); i++) {
            result = result.append("<color:{}>{}", RAINBOW_CYCLE[(i + offset) % RAINBOW_CYCLE.length], digits.charAt(i));
        }
        return result;
    }

    public static Text getLevelDisplay(int level) {
        return fromLevel(level).constructLevelBrackets(level);
    }
}
