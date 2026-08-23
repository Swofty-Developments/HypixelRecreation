package net.swofty.commons.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.PackSprite;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class TextDomain {

    private static final Map<String, Function<String, Component>> REGISTERED = new ConcurrentHashMap<>();

    private TextDomain() {
    }

    static void register(String name, Function<String, Component> resolver) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(resolver, "resolver");
        String tag = name.trim().toLowerCase(Locale.ROOT);
        if (tag.isEmpty()) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        REGISTERED.put(tag, resolver);
        TextParser.invalidate();
    }

    static boolean registered(String name) {
        return REGISTERED.containsKey(name);
    }

    static Component custom(String name, String argument, String source) {
        Function<String, Component> resolver = REGISTERED.get(name);
        if (resolver == null) {
            throw new TextParseException(source, 0, "Unknown tag '" + name + "'");
        }
        Component resolved = resolver.apply(argument);
        if (resolved == null) {
            throw new TextParseException(source, 0, "Unknown " + name + " '" + argument + "'");
        }
        return resolved;
    }

    private static String normalise(String name) {
        return name.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    static Component glyph(String name, String source) {
        try {
            return PackSprite.valueOf(normalise(name)).getSprite();
        } catch (IllegalArgumentException exception) {
            throw new TextParseException(source, 0, "Unknown SkyBlock glyph '" + name + "'");
        }
    }

    static ItemStatistic statistic(String name, String source) {
        try {
            return ItemStatistic.valueOf(normalise(name));
        } catch (IllegalArgumentException exception) {
            throw new TextParseException(source, 0, "Unknown SkyBlock statistic '" + name + "'");
        }
    }

    static Component stat(String name, String value, String source) {
        ItemStatistic statistic = statistic(name, source);
        Component display = statistic.getCompleteDisplayName();
        if (value != null) {
            display = Component.text(value).appendSpace().append(display).color(statistic.getDisplayColor());
        }
        return display;
    }

    static Component rarity(String name, String source) {
        Rarity rarity = Rarity.getRarity(normalise(name));
        if (rarity == null) {
            throw new TextParseException(source, 0, "Unknown SkyBlock rarity '" + name + "'");
        }
        return rarity.getDisplay();
    }

    static Component separator(String colour, String source) {
        return Component.text(TextTags.SEPARATOR_LINE, colour == null ? NamedTextColor.BLUE : colour(colour, source),
                TextDecoration.STRIKETHROUGH);
    }

    static TextColor colour(String token, String source) {
        TextColor colour = TextTags.color(token.trim());
        if (colour == null) {
            throw new TextParseException(source, 0, "Unknown colour '" + token + "'");
        }
        return colour;
    }

    static int length(String token, String source) {
        int value = number(token, source).intValue();
        if (value < 1) {
            throw new TextParseException(source, 0, "Expected a positive length, got '" + token + "'");
        }
        return value;
    }

    static Double number(String token, String source) {
        try {
            return Double.valueOf(token.trim());
        } catch (NumberFormatException exception) {
            throw new TextParseException(source, 0, "Expected a number, got '" + token + "'");
        }
    }

    static Component bar(List<String> arguments, String source) {
        if (arguments.isEmpty() || arguments.size() > 4) {
            throw new TextParseException(source, 0, "Expected <bar:fraction> or <bar:value:max[:length[:colour]]>");
        }
        int length = TextTags.DEFAULT_BAR_LENGTH;
        TextColor progress = NamedTextColor.DARK_GREEN;
        if (arguments.size() == 1) {
            return dashes(length, progress, number(arguments.getFirst(), source), "");
        }
        double current = number(arguments.get(0), source);
        double max = number(arguments.get(1), source);
        if (arguments.size() >= 3) {
            length = length(arguments.get(2), source);
        }
        if (arguments.size() == 4) {
            progress = colour(arguments.get(3), source);
        }
        double fraction = max == 0.0D ? 0.0D : Math.min(current, max) / max;
        return dashes(length, progress, fraction, " ")
                .append(Component.text(StringUtility.commaify(current), NamedTextColor.YELLOW))
                .append(Component.text("/", NamedTextColor.GOLD))
                .append(Component.text(StringUtility.commaify(max), NamedTextColor.YELLOW));
    }

    private static Component dashes(int length, TextColor progress, double fraction, String suffix) {
        int completed = (int) Math.clamp(Math.round(length * fraction), 0L, length);
        return Component.empty()
                .append(Component.text("-".repeat(completed), progress))
                .append(Component.text("-".repeat(length - completed) + suffix, NamedTextColor.WHITE));
    }
}
