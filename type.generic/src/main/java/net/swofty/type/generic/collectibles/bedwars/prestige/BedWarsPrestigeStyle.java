package net.swofty.type.generic.collectibles.bedwars.prestige;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record BedWarsPrestigeStyle(
    TextPaint openBracket,
    DigitPaint digits,
    TextPaint star,
    TextPaint closeBracket
) {
    public BedWarsPrestigeStyle {
        openBracket = openBracket == null ? TextPaint.none() : openBracket;
        digits = digits == null ? DigitPaint.none() : digits;
        star = star == null ? TextPaint.none() : star;
        closeBracket = closeBracket == null ? TextPaint.none() : closeBracket;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BedWarsPrestigeStyle solid(TextColor color) {
        return builder()
            .openBracket(color)
            .digits(color)
            .star(color)
            .closeBracket(color)
            .build();
    }

    public static BedWarsPrestigeStyle colors(TextColor openColor, List<TextColor> digitColors, TextColor starColor, TextColor closeColor) {
        return BedWarsPrestigeStyle.builder()
            .openBracket(openColor)
            .digits(digitColors.toArray(TextColor[]::new))
            .star(starColor)
            .closeBracket(closeColor)
            .build();
    }

    public String render(String level, String starSymbol, BedWarsPrestigeDefinitions.Bracket bracket, boolean includeBrackets) {
        StringBuilder rendered = new StringBuilder();
        if (includeBrackets) {
            rendered.append(openBracket.apply(escape(bracket.open())));
        }
        for (int i = 0; i < level.length(); i++) {
            rendered.append(digits.apply(escape(String.valueOf(level.charAt(i))), i, level.length()));
        }
        rendered.append(star.apply(escape(starSymbol)));
        if (includeBrackets) {
            rendered.append(closeBracket.apply(escape(bracket.close())));
        }
        return rendered.toString();
    }

    private static String tag(TextColor color) {
        if (color == null) {
            return "";
        }
        return color instanceof NamedTextColor named
            ? "<color:" + NamedTextColor.NAMES.keyOrThrow(named) + ">"
            : "<" + color.asHexString() + ">";
    }

    private static String escape(String literal) {
        if (literal == null || literal.isEmpty()) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(literal.length() + 2);
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            if (c == '<' || c == '{' || c == '\\') {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    public static final class Builder {
        private TextPaint openBracket = TextPaint.none();
        private DigitPaint digits = DigitPaint.none();
        private TextPaint star = TextPaint.none();
        private TextPaint closeBracket = TextPaint.none();

        public Builder openBracket(TextColor color) {
            this.openBracket = TextPaint.color(color);
            return this;
        }

        public Builder digits(TextColor... colors) {
            this.digits = DigitPaint.gradient(colors);
            return this;
        }

        public Builder star(TextColor color) {
            this.star = TextPaint.color(color);
            return this;
        }

        public Builder closeBracket(TextColor color) {
            this.closeBracket = TextPaint.color(color);
            return this;
        }

        public Builder all(TextColor color) {
            return openBracket(color).digits(color).star(color).closeBracket(color);
        }

        public BedWarsPrestigeStyle build() {
            return new BedWarsPrestigeStyle(openBracket, digits, star, closeBracket);
        }
    }

    public record TextPaint(TextColor color) {
        public static TextPaint none() {
            return new TextPaint(null);
        }

        public static TextPaint color(TextColor color) {
            return new TextPaint(color);
        }

        public String apply(String text) {
            return tag(color) + text;
        }
    }

    public record DigitPaint(List<TextColor> colors) {
        public DigitPaint {
            colors = colors == null || colors.isEmpty()
                ? Collections.singletonList(null)
                : Collections.unmodifiableList(new ArrayList<>(colors));
        }

        public static DigitPaint none() {
            return new DigitPaint(Collections.singletonList(null));
        }

        public static DigitPaint gradient(TextColor... colors) {
            if (colors == null || colors.length == 0) {
                return none();
            }
            return new DigitPaint(Arrays.asList(colors));
        }

        public String apply(String digit, int index, int totalDigits) {
            return tag(colorAt(index, totalDigits)) + digit;
        }

        private TextColor colorAt(int index, int totalDigits) {
            if (colors.size() == 1 || totalDigits <= 1) {
                return colors.getFirst();
            }
            int mappedIndex = Math.min(colors.size() - 1, Math.round(index * (colors.size() - 1) / (float) (totalDigits - 1)));
            return colors.get(mappedIndex);
        }
    }

}
