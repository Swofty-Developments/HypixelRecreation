package net.swofty.commons.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import net.swofty.commons.ChatUtility;

import java.util.ArrayList;
import java.util.List;

final class TextLayout {

    record Run(String text, Component special, Style style) {
    }

    private record Atom(char value, Component special, Style style) {
    }

    private static final char SPECIAL_PLACEHOLDER = '￼';

    private TextLayout() {
    }

    static List<Run> flatten(Component component) {
        List<Run> out = new ArrayList<>();
        collect(component, Style.empty(), out);
        return out;
    }

    private static void collect(Component component, Style inherited, List<Run> out) {
        Style resolved = component.style().merge(inherited, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
        if (component instanceof TextComponent text) {
            if (!text.content().isEmpty()) {
                out.add(new Run(text.content(), null, resolved));
            }
        } else {
            out.add(new Run(null, component.children(List.of()).style(Style.empty()), resolved));
        }
        for (Component child : component.children()) {
            collect(child, resolved, out);
        }
    }

    static Component fromRuns(List<Run> runs) {
        if (runs.isEmpty()) {
            return Component.empty();
        }
        List<Component> parts = new ArrayList<>(runs.size());
        for (Run run : runs) {
            parts.add(run.special() != null
                    ? run.special().style(run.style())
                    : Component.text(run.text(), run.style()));
        }
        return parts.size() == 1 ? parts.getFirst() : Component.empty().children(parts);
    }

    static List<Component> lines(Component component) {
        List<List<Run>> split = new ArrayList<>();
        List<Run> current = new ArrayList<>();
        for (Run run : flatten(component)) {
            if (run.special() != null || run.text().indexOf('\n') < 0) {
                current.add(run);
                continue;
            }
            String text = run.text();
            int start = 0;
            while (true) {
                int newline = text.indexOf('\n', start);
                if (newline < 0) {
                    if (start < text.length()) {
                        current.add(new Run(text.substring(start), null, run.style()));
                    }
                    break;
                }
                if (newline > start) {
                    current.add(new Run(text.substring(start, newline), null, run.style()));
                }
                split.add(current);
                current = new ArrayList<>();
                start = newline + 1;
            }
        }
        split.add(current);
        List<Component> out = new ArrayList<>(split.size());
        for (List<Run> line : split) {
            out.add(fromRuns(line));
        }
        return out;
    }

    static Component join(List<Component> lines) {
        if (lines.size() == 1) {
            return lines.getFirst();
        }
        List<Component> parts = new ArrayList<>(lines.size() * 2);
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                parts.add(Component.text("\n"));
            }
            parts.add(lines.get(i));
        }
        return Component.empty().children(parts);
    }

    static Component center(Component component) {
        List<Component> lines = lines(component);
        List<Component> out = new ArrayList<>(lines.size());
        for (Component line : lines) {
            String spaces = ChatUtility.FontInfo.getCenterSpaces(plain(line));
            out.add(spaces.isEmpty() ? line : Component.text(spaces).append(line));
        }
        return join(out);
    }

    static Component wrap(Component component, int width) {
        List<Atom> source = atoms(flatten(component));
        List<List<Atom>> wrapped = new ArrayList<>();
        for (List<Atom> line : split(source, '\n')) {
            if (line.isEmpty()) {
                wrapped.add(List.of());
                continue;
            }
            List<Atom> current = new ArrayList<>();
            for (List<Atom> word : split(line, ' ')) {
                if (word.isEmpty()) {
                    continue;
                }
                int extraSpace = current.isEmpty() ? 0 : 1;
                if (current.size() + extraSpace + word.size() > width && !current.isEmpty()) {
                    wrapped.add(current);
                    current = new ArrayList<>();
                }
                if (!current.isEmpty()) {
                    current.add(new Atom(' ', null, current.getLast().style()));
                }
                current.addAll(word);
            }
            wrapped.add(current);
        }
        List<Atom> joined = new ArrayList<>();
        for (int i = 0; i < wrapped.size(); i++) {
            List<Atom> line = wrapped.get(i);
            if (i > 0) {
                joined.add(new Atom('\n', null, newlineStyle(joined, line)));
            }
            joined.addAll(line);
        }
        return fromRuns(runsOf(joined));
    }

    static String plain(Component component) {
        StringBuilder builder = new StringBuilder();
        for (Run run : flatten(component)) {
            if (run.special() == null) {
                builder.append(run.text());
            } else if (run.special() instanceof TranslatableComponent translatable) {
                String key = translatable.key();
                builder.append(key.startsWith(TextTags.CONTEXT_KEY_PREFIX)
                        ? "<" + key.substring(TextTags.CONTEXT_KEY_PREFIX.length()) + ">"
                        : key);
            }
        }
        return builder.toString();
    }

    private static Style newlineStyle(List<Atom> joined, List<Atom> next) {
        if (!joined.isEmpty()) {
            return joined.getLast().style();
        }
        return next.isEmpty() ? Style.empty() : next.getFirst().style();
    }

    private static List<Atom> atoms(List<Run> runs) {
        List<Atom> out = new ArrayList<>();
        for (Run run : runs) {
            if (run.special() != null) {
                out.add(new Atom(SPECIAL_PLACEHOLDER, run.special(), run.style()));
                continue;
            }
            String text = run.text();
            for (int i = 0; i < text.length(); i++) {
                out.add(new Atom(text.charAt(i), null, run.style()));
            }
        }
        return out;
    }

    private static List<Run> runsOf(List<Atom> atoms) {
        List<Run> out = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Style style = null;
        for (Atom atom : atoms) {
            if (atom.special() != null) {
                if (!buffer.isEmpty()) {
                    out.add(new Run(buffer.toString(), null, style));
                    buffer.setLength(0);
                }
                out.add(new Run(null, atom.special(), atom.style()));
                style = null;
                continue;
            }
            if (!buffer.isEmpty() && !atom.style().equals(style)) {
                out.add(new Run(buffer.toString(), null, style));
                buffer.setLength(0);
            }
            style = atom.style();
            buffer.append(atom.value());
        }
        if (!buffer.isEmpty()) {
            out.add(new Run(buffer.toString(), null, style));
        }
        return out;
    }

    private static List<List<Atom>> split(List<Atom> atoms, char separator) {
        List<List<Atom>> out = new ArrayList<>();
        List<Atom> current = new ArrayList<>();
        for (Atom atom : atoms) {
            if (atom.special() == null && atom.value() == separator) {
                out.add(current);
                current = new ArrayList<>();
                continue;
            }
            current.add(atom);
        }
        out.add(current);
        return out;
    }
}
