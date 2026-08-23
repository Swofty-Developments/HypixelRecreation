package net.swofty.commons.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.swofty.commons.ChatUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Immutable, thread safe value type holding a markup template plus its interpolation arguments.
 *
 * The markup grammar is documented on {@link TextParser}. A {@code Text} parses eagerly and caches the
 * resulting parse tree by template string, so malformed markup fails where the {@code Text} is built
 * rather than where it is rendered. Context tags stay in the tree as inert {@code hsb.ctx.*} translatable
 * placeholders until the render boundary resolves them.
 *
 * {@link #serialize()} produces canonical markup which is the wire format between services and which
 * round-trips: {@code Text.parse(text.serialize()).serialize().equals(text.serialize())}. Equality,
 * hashing and {@code toString} are all defined on that canonical form.
 */
public final class Text implements ComponentLike {

    public static final String CONTEXT_KEY_PREFIX = TextTags.CONTEXT_KEY_PREFIX;

    private static final Object[] NO_ARGUMENTS = new Object[0];
    private static final Text EMPTY = new Text(Component.empty());

    private static volatile Predicate<String> translationKeyLookup = _ -> false;

    private final Object[] arguments;
    private final TextNode node;
    private volatile Component component;
    private volatile String serialized;

    private Text(Object[] arguments, TextNode node) {
        this.arguments = arguments;
        this.node = node;
    }

    private Text(Component component) {
        this.arguments = NO_ARGUMENTS;
        this.node = null;
        this.component = component;
    }

    public static Text of(String markup, Object... arguments) {
        Objects.requireNonNull(markup, "markup");
        TextParser.Compiled compiled = TextParser.compile(markup, false);
        Object[] copy = arguments == null || arguments.length == 0 ? NO_ARGUMENTS : arguments.clone();
        if (copy.length < compiled.argumentCount()) {
            throw new TextParseException(markup, markup.length(),
                    "Markup needs " + compiled.argumentCount() + " argument(s) but got " + copy.length);
        }
        return new Text(copy, compiled.node());
    }

    public static Text parse(String markup) {
        return of(markup);
    }

    public static Text parseLenient(String markup) {
        Objects.requireNonNull(markup, "markup");
        return new Text(NO_ARGUMENTS, TextParser.compile(markup, true).node());
    }

    public static Text key(String i18nKey, Object... arguments) {
        Objects.requireNonNull(i18nKey, "i18nKey");
        if (arguments == null || arguments.length == 0) {
            return new Text(Component.translatable(i18nKey));
        }
        List<Component> parameters = new ArrayList<>(arguments.length);
        for (Object argument : arguments) {
            parameters.add(TextNode.argument(argument));
        }
        return new Text(Component.translatable(i18nKey, parameters));
    }

    public static void translationKeyLookup(Predicate<String> lookup) {
        translationKeyLookup = Objects.requireNonNull(lookup, "lookup");
    }

    public static void registerTag(String name, Function<String, Component> resolver) {
        TextDomain.register(name, resolver);
    }

    public static List<Text> keyLines(String key, Object... arguments) {
        Objects.requireNonNull(key, "key");
        Predicate<String> lookup = translationKeyLookup;
        List<Text> lines = new ArrayList<>();
        int index = 1;
        while (true) {
            String numberedKey = key + "." + index;
            if (!lookup.test(numberedKey)) {
                break;
            }
            lines.add(key(numberedKey, arguments));
            index++;
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("Missing dialogue translation key in en_US: " + key + ".1");
        }
        return List.copyOf(lines);
    }

    public static Text legacy(String legacyString) {
        Objects.requireNonNull(legacyString, "legacyString");
        return new Text(TextLegacy.parse(legacyString));
    }

    public static Text read(String raw) {
        Objects.requireNonNull(raw, "raw");
        return raw.indexOf('§') >= 0 ? legacy(raw) : of(raw);
    }

    public static Text literal(String raw) {
        Objects.requireNonNull(raw, "raw");
        return raw.isEmpty() ? EMPTY : new Text(Component.text(raw));
    }

    public static Text component(Component component) {
        Objects.requireNonNull(component, "component");
        return new Text(component);
    }

    public static Text empty() {
        return EMPTY;
    }

    public static String colorTag(TextColor colour) {
        Objects.requireNonNull(colour, "colour");
        return "<" + TextTags.colorTag(colour) + ">";
    }

    public static Text join(Text separator, Collection<Text> parts) {
        Objects.requireNonNull(separator, "separator");
        if (parts.isEmpty()) {
            return EMPTY;
        }
        List<Component> components = new ArrayList<>(parts.size() * 2);
        boolean first = true;
        for (Text part : parts) {
            if (!first) {
                components.add(separator.asComponent());
            }
            components.add(part.asComponent());
            first = false;
        }
        return new Text(Component.empty().children(components));
    }

    public Text append(Text other) {
        Objects.requireNonNull(other, "other");
        if (other.isEmpty()) {
            return this;
        }
        return new Text(Component.empty().children(List.of(asComponent(), other.asComponent())));
    }

    public Text append(String markup, Object... arguments) {
        return append(of(markup, arguments));
    }

    public Text appendIf(boolean condition, String markup, Object... arguments) {
        return condition ? append(markup, arguments) : this;
    }

    public String serialize() {
        String local = serialized;
        if (local == null) {
            local = TextSerializer.serialize(asComponent());
            serialized = local;
        }
        return local;
    }

    public String plain() {
        return TextLayout.plain(asComponent());
    }

    public String plain(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        return TextLayout.plain(GlobalTranslator.render(asComponent(), locale));
    }

    public int pixelWidth() {
        return ChatUtility.FontInfo.getLength(plain());
    }

    public List<Text> lines() {
        List<Component> split = TextLayout.lines(asComponent());
        List<Text> out = new ArrayList<>(split.size());
        for (Component line : split) {
            out.add(new Text(line));
        }
        return out;
    }

    public boolean isEmpty() {
        return plain().isEmpty();
    }

    @Override
    public Component asComponent() {
        Component local = component;
        if (local == null) {
            local = node.render(arguments);
            component = local;
        }
        return local;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Text text && serialize().equals(text.serialize()));
    }

    @Override
    public int hashCode() {
        return serialize().hashCode();
    }

    @Override
    public String toString() {
        return serialize();
    }
}
