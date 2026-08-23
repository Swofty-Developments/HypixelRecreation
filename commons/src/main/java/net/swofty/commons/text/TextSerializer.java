package net.swofty.commons.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class TextSerializer {

    private static final String COLOR = "color";
    private static final String HOVER = "hover";
    private static final String CLICK = "click";

    private record Entry(Object key, Object value, String open, String close) {
    }

    private TextSerializer() {
    }

    static String serialize(Component component) {
        StringBuilder out = new StringBuilder();
        List<Entry> stack = new ArrayList<>();
        for (TextLayout.Run run : TextLayout.flatten(component)) {
            adjust(out, stack, run.style());
            if (run.special() == null) {
                escape(out, run.text());
            } else {
                out.append(special(run.special()));
            }
        }
        return out.toString();
    }

    private static void adjust(StringBuilder out, List<Entry> stack, Style target) {
        while (!stack.isEmpty() && !covers(stack, target)) {
            pop(out, stack);
        }
        int colourPops = colourPops(stack, target.color());
        for (int i = 0; i < colourPops; i++) {
            pop(out, stack);
        }
        ClickEvent<?> click = target.clickEvent();
        if (click != null && eventOf(stack, CLICK) == null) {
            String tag = clickTag(click);
            if (tag != null) {
                push(out, stack, new Entry(CLICK, click, tag, "</click>"));
            }
        }
        HoverEvent<?> hover = target.hoverEvent();
        if (hover != null && eventOf(stack, HOVER) == null) {
            String tag = hoverTag(hover);
            if (tag != null) {
                push(out, stack, new Entry(HOVER, hover, tag, "</hover>"));
            }
        }
        TextColor colour = target.color();
        if (colour != null && !colour.equals(colourOf(stack))) {
            String name = TextTags.colorTag(colour);
            push(out, stack, new Entry(COLOR, colour, "<" + name + ">", "</" + name + ">"));
        }
        for (TextDecoration decoration : TextTags.DECORATION_ORDER) {
            if (target.decoration(decoration) == TextDecoration.State.TRUE && !decorated(stack, decoration)) {
                String name = TextTags.decorationTag(decoration);
                push(out, stack, new Entry(decoration, Boolean.TRUE, "<" + name + ">", "</" + name + ">"));
            }
        }
    }

    private static void push(StringBuilder out, List<Entry> stack, Entry entry) {
        out.append(entry.open());
        stack.add(entry);
    }

    private static void pop(StringBuilder out, List<Entry> stack) {
        out.append(stack.removeLast().close());
    }

    private static boolean covers(List<Entry> stack, Style target) {
        if (colourOf(stack) != null && target.color() == null) {
            return false;
        }
        for (TextDecoration decoration : TextTags.DECORATION_ORDER) {
            if (decorated(stack, decoration) && target.decoration(decoration) != TextDecoration.State.TRUE) {
                return false;
            }
        }
        Object click = eventOf(stack, CLICK);
        if (click != null && !click.equals(target.clickEvent())) {
            return false;
        }
        Object hover = eventOf(stack, HOVER);
        return hover == null || hover.equals(target.hoverEvent());
    }

    private static int colourPops(List<Entry> stack, TextColor target) {
        int index = stack.size();
        while (index > 0 && COLOR.equals(stack.get(index - 1).key())) {
            index--;
            if (Objects.equals(colourOf(stack.subList(0, index)), target)) {
                return stack.size() - index;
            }
        }
        return 0;
    }

    private static TextColor colourOf(List<Entry> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (COLOR.equals(stack.get(i).key())) {
                return (TextColor) stack.get(i).value();
            }
        }
        return null;
    }

    private static boolean decorated(List<Entry> stack, TextDecoration decoration) {
        for (Entry entry : stack) {
            if (decoration.equals(entry.key())) {
                return true;
            }
        }
        return false;
    }

    private static Object eventOf(List<Entry> stack, String key) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (key.equals(stack.get(i).key())) {
                return stack.get(i).value();
            }
        }
        return null;
    }

    private static String clickTag(ClickEvent<?> event) {
        String action = switch (event.action().name()) {
            case "run_command" -> "run";
            case "suggest_command" -> "suggest";
            case "copy_to_clipboard" -> "copy";
            case "open_url" -> "url";
            default -> null;
        };
        if (action == null || !(event.payload() instanceof ClickEvent.Payload.Text payload)) {
            return null;
        }
        return "<click:" + action + ":'" + quote(escape(payload.value())) + "'>";
    }

    private static String hoverTag(HoverEvent<?> event) {
        if (!(event.value() instanceof Component value)) {
            return null;
        }
        return "<hover:'" + quote(serialize(value)) + "'>";
    }

    private static String special(Component component) {
        if (!(component instanceof TranslatableComponent translatable)) {
            return "";
        }
        String key = translatable.key();
        List<TranslationArgument> parameters = translatable.arguments();
        if (parameters.isEmpty() && key.startsWith(TextTags.CONTEXT_KEY_PREFIX)) {
            return "<ctx:" + key.substring(TextTags.CONTEXT_KEY_PREFIX.length()) + ">";
        }
        StringBuilder builder = new StringBuilder("<key:'").append(quote(escape(key))).append('\'');
        for (TranslationArgument parameter : parameters) {
            builder.append(":'").append(quote(serialize(parameter.asComponent()))).append('\'');
        }
        return builder.append('>').toString();
    }

    private static String quote(String markup) {
        return markup.replace("'", "\\'");
    }

    private static String escape(String raw) {
        StringBuilder out = new StringBuilder(raw.length());
        escape(out, raw);
        return out.toString();
    }

    private static void escape(StringBuilder out, String raw) {
        for (int i = 0; i < raw.length(); i++) {
            char current = raw.charAt(i);
            if (current == '\\' || current == '<' || current == '{') {
                out.append('\\');
            }
            out.append(current);
        }
    }
}
