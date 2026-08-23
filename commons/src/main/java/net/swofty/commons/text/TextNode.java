package net.swofty.commons.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;

sealed interface TextNode {

    Component render(Object[] arguments);

    static Component renderAll(List<TextNode> nodes, Object[] arguments) {
        if (nodes.isEmpty()) {
            return Component.empty();
        }
        if (nodes.size() == 1) {
            return nodes.getFirst().render(arguments);
        }
        return Component.empty().children(renderEach(nodes, arguments));
    }

    static List<Component> renderEach(List<TextNode> nodes, Object[] arguments) {
        List<Component> out = new ArrayList<>(nodes.size());
        for (TextNode node : nodes) {
            out.add(node.render(arguments));
        }
        return out;
    }

    static String plainOf(List<TextNode> nodes, Object[] arguments) {
        return TextLayout.plain(renderAll(nodes, arguments));
    }

    static String constant(List<TextNode> nodes) {
        StringBuilder builder = new StringBuilder();
        for (TextNode node : nodes) {
            if (!(node instanceof Literal literal)) {
                return null;
            }
            builder.append(literal.value());
        }
        return builder.toString();
    }

    static Component argument(Object value) {
        return switch (value) {
            case null -> Component.text("null");
            case Text text -> text.asComponent();
            case Component component -> component;
            case TextColor colour -> Component.text(TextTags.colorToken(colour));
            case ComponentLike like -> like.asComponent();
            case String string -> Component.text(string);
            case Number number -> Component.text(String.valueOf(number));
            case Character character -> Component.text(String.valueOf(character));
            case Boolean flag -> Component.text(String.valueOf(flag));
            default -> TextArgRenderers.render(value);
        };
    }

    record Literal(String value) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return Component.text(value);
        }
    }

    record Arg(int index, TextFormats.Spec format, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            if (index >= arguments.length) {
                return Component.text(source);
            }
            Object value = arguments[index];
            return format == null ? argument(value) : Component.text(TextFormats.apply(format, value));
        }
    }

    record Group(List<TextNode> children) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return renderAll(children, arguments);
        }
    }

    record Styled(Style style, List<TextNode> colour, List<TextNode> children, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            Style resolved = colour == null
                    ? style
                    : style.color(TextDomain.colour(plainOf(colour, arguments), source));
            if (children.size() == 1 && children.getFirst() instanceof Literal literal) {
                return Component.text(literal.value(), resolved);
            }
            return Component.empty().style(resolved).children(renderEach(children, arguments));
        }
    }

    record Hovered(List<TextNode> hover, List<TextNode> children) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return renderAll(children, arguments).hoverEvent(HoverEvent.showText(renderAll(hover, arguments)));
        }
    }

    record Clicked(String action, List<TextNode> value, List<TextNode> children, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            String target = plainOf(value, arguments);
            ClickEvent<?> event = switch (action) {
                case "run" -> ClickEvent.runCommand(target);
                case "suggest" -> ClickEvent.suggestCommand(target);
                case "copy" -> ClickEvent.copyToClipboard(target);
                case "url" -> ClickEvent.openUrl(target);
                default -> throw new TextParseException(source, 0, "Unknown click action '" + action + "'");
            };
            return renderAll(children, arguments).clickEvent(event);
        }
    }

    record Centered(List<TextNode> children) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return TextLayout.center(renderAll(children, arguments));
        }
    }

    record Wrapped(List<TextNode> width, List<TextNode> children, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return TextLayout.wrap(renderAll(children, arguments),
                    TextDomain.length(plainOf(width, arguments), source));
        }
    }

    record Context(String name) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return Component.translatable(TextTags.CONTEXT_KEY_PREFIX + name);
        }
    }

    record Translate(List<TextNode> key, List<List<TextNode>> parameters) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            List<Component> resolved = new ArrayList<>(parameters.size());
            for (List<TextNode> parameter : parameters) {
                resolved.add(renderAll(parameter, arguments));
            }
            return Component.translatable(plainOf(key, arguments), resolved);
        }
    }

    record Glyph(List<TextNode> name, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return TextDomain.glyph(plainOf(name, arguments), source);
        }
    }

    record Stat(List<TextNode> name, List<TextNode> value, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return TextDomain.stat(plainOf(name, arguments),
                    value == null ? null : plainOf(value, arguments), source);
        }
    }

    record Custom(String name, List<TextNode> argument, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return TextDomain.custom(name, plainOf(argument, arguments), source);
        }
    }

    record RarityDisplay(List<TextNode> name, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return TextDomain.rarity(plainOf(name, arguments), source);
        }
    }

    record Separator(List<TextNode> colour, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            return TextDomain.separator(colour == null ? null : plainOf(colour, arguments), source);
        }
    }

    record Bar(List<List<TextNode>> parameters, String source) implements TextNode {
        @Override
        public Component render(Object[] arguments) {
            List<String> resolved = new ArrayList<>(parameters.size());
            for (List<TextNode> parameter : parameters) {
                resolved.add(plainOf(parameter, arguments));
            }
            return TextDomain.bar(resolved, source);
        }
    }
}
