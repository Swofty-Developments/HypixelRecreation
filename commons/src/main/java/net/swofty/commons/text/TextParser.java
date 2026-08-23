package net.swofty.commons.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hand written parser for the HypixelSkyBlock text markup.
 *
 * <pre>
 * markup   := ( literal | escape | tag | placeholder )*
 * escape   := '\\' ( '&lt;' | '{' | '\\' )
 * tag      := '&lt;' name ( ':' segment )* '&gt;' | '&lt;/' name '&gt;'
 * segment  := bare | '\'' markup '\''
 * holder   := '{' [ index ] [ ':' format ] '}'
 * </pre>
 *
 * Colours are the sixteen legacy letters ({@code <0>} - {@code <f>}), their MiniMessage long names
 * ({@code <red>}, {@code <dark_purple>}), {@code <color:NAME>} or {@code <#rrggbb>}. Decorations are
 * {@code <l> <o> <n> <m> <k>} and the long names {@code <bold> <italic> <underlined> <strikethrough>
 * <obfuscated>}. {@code <r>} pops every open style, {@code </x>} pops one frame back to the enclosing
 * style, and closing tags may be omitted at the end of the string.
 *
 * Domain tags expand while parsing: {@code <glyph:name>} / {@code <sbglyph:name>},
 * {@code <stat:name[:value]>} / {@code <sbstat:...>}, {@code <rarity:name>}, {@code <sep[:colour]>},
 * {@code <bar:fraction>} / {@code <bar:value:max[:length[:colour]]>}, {@code <center>...</center>} and
 * {@code <wrap:N>...</wrap>}. {@code <key:'i18n.key'[:'arg']*>} (aliases {@code <lang:>}, {@code <tr:>})
 * becomes a translatable component, and the context tags {@code <player> <rank> <purse> <bank> <island>
 * <profile>} plus any {@code <ctx:name>} become inert {@code hsb.ctx.*} translatable placeholders that the
 * render boundary resolves. {@code <hover:'markup'>} and {@code <click:run|suggest|copy|url:'value'>}
 * carry interactivity. Downstream modules add their own single argument domain tags through
 * {@link Text#registerTag(String, java.util.function.Function)}; those expand while parsing just like the
 * built in domain tags, and an argument the resolver rejects is a parse error in strict mode.
 *
 * Placeholders insert arguments as literal text, never as markup. {@code {}} auto indexes, {@code {0}}
 * indexes explicitly, and {@code {:,} {:short} {:roman} {:time} {:+} {:%} {:.N}} format numbers.
 *
 * Strict parsing rejects anything it does not recognise with a {@link TextParseException}; lenient
 * parsing leaves unrecognised or malformed tags in place as literal text.
 */
public final class TextParser {

    record Compiled(TextNode node, int argumentCount) {
    }

    private static final int CACHE_LIMIT = 10_000;
    private static final Map<String, Compiled> STRICT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Compiled> LENIENT_CACHE = new ConcurrentHashMap<>();

    private TextParser() {
    }

    public static Component parse(String markup, Object... arguments) {
        return Text.of(markup, arguments).asComponent();
    }

    public static Component parseLenient(String markup, Object... arguments) {
        return compile(markup, true).node().render(arguments == null ? new Object[0] : arguments);
    }

    static void invalidate() {
        STRICT_CACHE.clear();
        LENIENT_CACHE.clear();
    }

    static Compiled compile(String markup, boolean lenient) {
        Map<String, Compiled> cache = lenient ? LENIENT_CACHE : STRICT_CACHE;
        Compiled cached = cache.get(markup);
        if (cached != null) {
            return cached;
        }
        Compiled compiled = new Parser(markup, markup, 0, lenient, false).run();
        if (cache.size() >= CACHE_LIMIT) {
            cache.clear();
        }
        cache.put(markup, compiled);
        return compiled;
    }

    private enum Kind {
        ROOT,
        STYLE,
        HOVER,
        CLICK,
        CENTER,
        WRAP
    }

    private static final class Frame {

        private final String id;
        private final Kind kind;
        private final List<TextNode> children = new ArrayList<>();
        private Style style;
        private List<TextNode> colour;
        private List<TextNode> payload;
        private String action;
        private String source;

        private Frame(String id, Kind kind) {
            this.id = id;
            this.kind = kind;
        }
    }

    private static final class Parser {

        private final String source;
        private final String origin;
        private final int offset;
        private final boolean lenient;
        private final boolean nested;
        private final StringBuilder pending = new StringBuilder();
        private final List<Frame> stack = new ArrayList<>();
        private int position;
        private int autoIndex;
        private int maxIndex = -1;

        private Parser(String source, String origin, int offset, boolean lenient, boolean nested) {
            this.source = source;
            this.origin = origin;
            this.offset = offset;
            this.lenient = lenient;
            this.nested = nested;
            this.stack.add(new Frame(null, Kind.ROOT));
        }

        private Compiled run() {
            while (position < source.length()) {
                char current = source.charAt(position);
                if (current == '\\') {
                    escape();
                } else if (current == '<') {
                    tag();
                } else if (current == '{') {
                    placeholder();
                } else {
                    pending.append(current);
                    position++;
                }
            }
            flush();
            while (stack.size() > 1) {
                closeTop();
            }
            return new Compiled(new TextNode.Group(List.copyOf(stack.getFirst().children)), maxIndex + 1);
        }

        private TextParseException error(int at, String reason) {
            return new TextParseException(origin, nested ? offset : offset + at, reason);
        }

        private Frame current() {
            return stack.getLast();
        }

        private void flush() {
            if (pending.isEmpty()) {
                return;
            }
            current().children.add(new TextNode.Literal(pending.toString()));
            pending.setLength(0);
        }

        private void escape() {
            if (position + 1 >= source.length()) {
                if (lenient) {
                    pending.append('\\');
                    position++;
                    return;
                }
                throw error(position, "Dangling escape character");
            }
            char next = source.charAt(position + 1);
            if (next != '<' && next != '{' && next != '\\') {
                if (lenient) {
                    pending.append('\\');
                    position++;
                    return;
                }
                throw error(position, "Unsupported escape sequence '\\" + next + "'");
            }
            pending.append(next);
            position += 2;
        }

        private void placeholder() {
            int start = position;
            int end = source.indexOf('}', start + 1);
            if (end < 0) {
                if (lenient) {
                    pending.append('{');
                    position++;
                    return;
                }
                throw error(start, "Unclosed placeholder");
            }
            String body = source.substring(start + 1, end);
            int savedAuto = autoIndex;
            try {
                int colon = body.indexOf(':');
                String indexPart = colon < 0 ? body : body.substring(0, colon);
                String formatPart = colon < 0 ? null : body.substring(colon + 1);
                int index;
                if (indexPart.isEmpty()) {
                    index = autoIndex++;
                } else {
                    index = readIndex(indexPart, start);
                }
                TextFormats.Spec spec = formatPart == null
                        ? null
                        : TextFormats.parse(formatPart, origin, nested ? offset : offset + start);
                maxIndex = Math.max(maxIndex, index);
                flush();
                current().children.add(new TextNode.Arg(index, spec, source.substring(start, end + 1)));
                position = end + 1;
            } catch (TextParseException exception) {
                if (!lenient) {
                    throw exception;
                }
                autoIndex = savedAuto;
                pending.append('{');
                position = start + 1;
            }
        }

        private int readIndex(String raw, int start) {
            int index;
            try {
                index = Integer.parseInt(raw);
            } catch (NumberFormatException exception) {
                throw error(start, "Invalid argument index '" + raw + "'");
            }
            if (index < 0) {
                throw error(start, "Negative argument index '" + raw + "'");
            }
            return index;
        }

        private void tag() {
            int start = position;
            int mark = stack.size();
            int savedAuto = autoIndex;
            int savedMax = maxIndex;
            try {
                boolean closing = start + 1 < source.length() && source.charAt(start + 1) == '/';
                int end = findEnd(start);
                String body = source.substring(start + (closing ? 2 : 1), end);
                flush();
                position = end + 1;
                if (closing) {
                    closeNamed(body.trim().toLowerCase(Locale.ROOT), start);
                } else {
                    open(body, start, source.substring(start, end + 1));
                }
            } catch (TextParseException exception) {
                if (!lenient) {
                    throw exception;
                }
                while (stack.size() > mark) {
                    stack.removeLast();
                }
                autoIndex = savedAuto;
                maxIndex = savedMax;
                pending.append('<');
                position = start + 1;
            }
        }

        private int findEnd(int start) {
            boolean quoted = false;
            for (int i = start + 1; i < source.length(); i++) {
                char current = source.charAt(i);
                if (quoted) {
                    if (current == '\\') {
                        i++;
                    } else if (current == '\'') {
                        quoted = false;
                    }
                    continue;
                }
                if (current == '\'') {
                    quoted = true;
                } else if (current == '>') {
                    return i;
                } else if (current == '<') {
                    throw error(start, "Unclosed tag");
                }
            }
            throw error(start, quoted ? "Unclosed quote in tag" : "Unclosed tag");
        }

        private List<String> segments(String body, int start) {
            List<String> out = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            boolean quoted = false;
            for (int i = 0; i < body.length(); i++) {
                char current = body.charAt(i);
                if (quoted) {
                    if (current == '\\' && i + 1 < body.length()) {
                        char next = body.charAt(i + 1);
                        if (next == '\'') {
                            builder.append('\'');
                            i++;
                            continue;
                        }
                        if (next == '\\') {
                            builder.append("\\\\");
                            i++;
                            continue;
                        }
                        builder.append(current);
                        continue;
                    }
                    if (current == '\'') {
                        quoted = false;
                        continue;
                    }
                    builder.append(current);
                    continue;
                }
                if (current == '\'') {
                    quoted = true;
                } else if (current == ':') {
                    out.add(builder.toString());
                    builder.setLength(0);
                } else {
                    builder.append(current);
                }
            }
            if (quoted) {
                throw error(start, "Unclosed quote in tag");
            }
            out.add(builder.toString());
            return out;
        }

        private List<TextNode> sub(String segment, int start) {
            Parser parser = new Parser(segment, origin, nested ? offset : offset + start, lenient, true);
            parser.autoIndex = autoIndex;
            parser.maxIndex = maxIndex;
            Compiled compiled = parser.run();
            autoIndex = parser.autoIndex;
            maxIndex = parser.maxIndex;
            return ((TextNode.Group) compiled.node()).children();
        }

        private void open(String body, int start, String raw) {
            List<String> parts = segments(body, start);
            String name = parts.getFirst().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                throw error(start, "Empty tag");
            }
            if (parts.size() == 1) {
                if (name.equals("r") || name.equals("reset")) {
                    reset();
                    return;
                }
                TextColor colour = TextTags.color(name);
                if (colour != null) {
                    push(styleFrame(TextTags.colorTag(colour), Style.style(colour), null, raw));
                    return;
                }
                TextDecoration decoration = TextTags.decoration(name);
                if (decoration != null) {
                    push(styleFrame(TextTags.decorationTag(decoration),
                            Style.style().decoration(decoration, true).build(), null, raw));
                    return;
                }
                if (TextTags.CONTEXT_TAGS.contains(name)) {
                    current().children.add(new TextNode.Context(name));
                    return;
                }
                if (name.equals("sep")) {
                    current().children.add(new TextNode.Separator(null, raw));
                    return;
                }
                if (name.equals("center")) {
                    push(new Frame("center", Kind.CENTER));
                    return;
                }
            }
            switch (name) {
                case "color" -> openColour(parts, start, raw);
                case "ctx" -> openContext(parts, start);
                case "hover" -> openHover(parts, start);
                case "click" -> openClick(parts, start, raw);
                case "wrap" -> openWrap(parts, start, raw);
                case "sep" -> {
                    require(parts, 2, start, "<sep> or <sep:colour>");
                    List<TextNode> colour = sub(parts.get(1), start);
                    validateColour(colour, raw);
                    current().children.add(new TextNode.Separator(colour, raw));
                }
                case "glyph", "sbglyph" -> {
                    require(parts, 2, start, "<glyph:name>");
                    List<TextNode> glyph = sub(parts.get(1), start);
                    String constant = TextNode.constant(glyph);
                    if (constant != null) {
                        TextDomain.glyph(constant, raw);
                    }
                    current().children.add(new TextNode.Glyph(glyph, raw));
                }
                case "stat", "sbstat" -> openStat(parts, start, raw);
                case "rarity" -> {
                    require(parts, 2, start, "<rarity:name>");
                    List<TextNode> rarity = sub(parts.get(1), start);
                    String constant = TextNode.constant(rarity);
                    if (constant != null) {
                        TextDomain.rarity(constant, raw);
                    }
                    current().children.add(new TextNode.RarityDisplay(rarity, raw));
                }
                case "bar" -> openBar(parts, start, raw);
                case "center" -> throw error(start, "Expected <center>");
                case "key", "lang", "tr" -> openKey(parts, start);
                default -> openRegistered(name, parts, start, raw);
            }
        }

        private void openRegistered(String name, List<String> parts, int start, String raw) {
            if (parts.size() != 2 || !TextDomain.registered(name)) {
                throw error(start, "Unknown tag '" + name + "'");
            }
            List<TextNode> argument = sub(parts.get(1), start);
            String constant = TextNode.constant(argument);
            if (constant != null) {
                TextDomain.custom(name, constant, raw);
            }
            current().children.add(new TextNode.Custom(name, argument, raw));
        }

        private void require(List<String> parts, int count, int start, String usage) {
            if (parts.size() != count) {
                throw error(start, "Expected " + usage);
            }
        }

        private void validateColour(List<TextNode> nodes, String raw) {
            String constant = TextNode.constant(nodes);
            if (constant != null) {
                TextDomain.colour(constant, raw);
            }
        }

        private void openColour(List<String> parts, int start, String raw) {
            require(parts, 2, start, "<color:name>");
            List<TextNode> nodes = sub(parts.get(1), start);
            String constant = TextNode.constant(nodes);
            if (constant != null) {
                TextColor colour = TextDomain.colour(constant, raw);
                push(styleFrame(TextTags.colorTag(colour), Style.style(colour), null, raw));
                return;
            }
            push(styleFrame("color", Style.empty(), nodes, raw));
        }

        private void openContext(List<String> parts, int start) {
            require(parts, 2, start, "<ctx:name>");
            String name = parts.get(1).trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty() || !name.chars().allMatch(TextParser::isContextChar)) {
                throw error(start, "Invalid context tag name '" + parts.get(1) + "'");
            }
            current().children.add(new TextNode.Context(name));
        }

        private void openHover(List<String> parts, int start) {
            require(parts, 2, start, "<hover:'markup'>");
            Frame frame = new Frame("hover", Kind.HOVER);
            frame.payload = sub(parts.get(1), start);
            push(frame);
        }

        private void openClick(List<String> parts, int start, String raw) {
            require(parts, 3, start, "<click:run|suggest|copy|url:'value'>");
            String action = parts.get(1).trim().toLowerCase(Locale.ROOT);
            if (!action.equals("run") && !action.equals("suggest") && !action.equals("copy") && !action.equals("url")) {
                throw error(start, "Unknown click action '" + parts.get(1) + "'");
            }
            Frame frame = new Frame("click", Kind.CLICK);
            frame.action = action;
            frame.payload = sub(parts.get(2), start);
            frame.source = raw;
            push(frame);
        }

        private void openWrap(List<String> parts, int start, String raw) {
            require(parts, 2, start, "<wrap:width>");
            List<TextNode> width = sub(parts.get(1), start);
            String constant = TextNode.constant(width);
            if (constant != null) {
                TextDomain.length(constant, raw);
            }
            Frame frame = new Frame("wrap", Kind.WRAP);
            frame.payload = width;
            frame.source = raw;
            push(frame);
        }

        private void openStat(List<String> parts, int start, String raw) {
            if (parts.size() < 2 || parts.size() > 3) {
                throw error(start, "Expected <stat:name> or <stat:name:value>");
            }
            List<TextNode> name = sub(parts.get(1), start);
            String constant = TextNode.constant(name);
            if (constant != null) {
                TextDomain.statistic(constant, raw);
            }
            List<TextNode> value = parts.size() == 3 ? sub(parts.get(2), start) : null;
            current().children.add(new TextNode.Stat(name, value, raw));
        }

        private void openBar(List<String> parts, int start, String raw) {
            if (parts.size() < 2 || parts.size() > 5) {
                throw error(start, "Expected <bar:fraction> or <bar:value:max[:length[:colour]]>");
            }
            List<List<TextNode>> parameters = new ArrayList<>(parts.size() - 1);
            List<String> constants = new ArrayList<>(parts.size() - 1);
            boolean allConstant = true;
            for (int i = 1; i < parts.size(); i++) {
                List<TextNode> nodes = sub(parts.get(i), start);
                parameters.add(nodes);
                String constant = TextNode.constant(nodes);
                allConstant &= constant != null;
                constants.add(constant);
            }
            if (allConstant) {
                TextDomain.bar(constants, raw);
            }
            current().children.add(new TextNode.Bar(List.copyOf(parameters), raw));
        }

        private void openKey(List<String> parts, int start) {
            if (parts.size() < 2) {
                throw error(start, "Expected <key:'i18n.key'>");
            }
            List<TextNode> key = sub(parts.get(1), start);
            String constant = TextNode.constant(key);
            if (constant != null && constant.isBlank()) {
                throw error(start, "Empty translation key");
            }
            List<List<TextNode>> parameters = new ArrayList<>(parts.size() - 2);
            for (int i = 2; i < parts.size(); i++) {
                parameters.add(sub(parts.get(i), start));
            }
            current().children.add(new TextNode.Translate(key, List.copyOf(parameters)));
        }

        private Frame styleFrame(String id, Style style, List<TextNode> colour, String raw) {
            Frame frame = new Frame(id, Kind.STYLE);
            frame.style = style;
            frame.colour = colour;
            frame.source = raw;
            return frame;
        }

        private void push(Frame frame) {
            stack.add(frame);
        }

        private void reset() {
            while (stack.size() > 1 && current().kind == Kind.STYLE) {
                closeTop();
            }
        }

        private void closeTop() {
            Frame frame = stack.removeLast();
            List<TextNode> children = List.copyOf(frame.children);
            TextNode node = switch (frame.kind) {
                case STYLE -> new TextNode.Styled(frame.style, frame.colour, children, frame.source);
                case HOVER -> new TextNode.Hovered(frame.payload, children);
                case CLICK -> new TextNode.Clicked(frame.action, frame.payload, children, frame.source);
                case CENTER -> new TextNode.Centered(children);
                case WRAP -> new TextNode.Wrapped(frame.payload, children, frame.source);
                case ROOT -> new TextNode.Group(children);
            };
            current().children.add(node);
        }

        private void closeNamed(String name, int start) {
            int target = locate(name);
            if (target <= 0) {
                throw error(start, "No open tag matching '</" + name + ">'");
            }
            while (stack.size() > target) {
                closeTop();
            }
        }

        private int locate(String name) {
            if (name.equals("color")) {
                for (int i = stack.size() - 1; i > 0; i--) {
                    Frame frame = stack.get(i);
                    if (frame.kind == Kind.STYLE && (frame.colour != null || TextTags.color(frame.id) != null)) {
                        return i;
                    }
                }
                return -1;
            }
            String id = name;
            TextColor colour = TextTags.color(name);
            if (colour != null) {
                id = TextTags.colorTag(colour);
            } else {
                TextDecoration decoration = TextTags.decoration(name);
                if (decoration != null) {
                    id = TextTags.decorationTag(decoration);
                }
            }
            for (int i = stack.size() - 1; i > 0; i--) {
                if (id.equals(stack.get(i).id)) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static boolean isContextChar(int codePoint) {
        return (codePoint >= 'a' && codePoint <= 'z')
                || (codePoint >= '0' && codePoint <= '9')
                || codePoint == '_' || codePoint == '.' || codePoint == '-';
    }
}
