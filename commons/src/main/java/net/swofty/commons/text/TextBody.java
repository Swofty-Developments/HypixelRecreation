package net.swofty.commons.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Mutable, string first tree of named text sections that renders down to a flat list of {@link Text} lines.
 *
 * Sections are addressed by id and created on demand, so a body can be assembled out of order and by code
 * that does not know about its siblings. {@link #section(String, int) Explicit ordering} sorts lower first
 * and falls back to creation order for ties, while {@link Section#child(String)} nests a subsection that
 * renders inline directly after its parent's own lines.
 *
 * {@link #render()} walks the tree depth first and drops every section that is empty or whose
 * {@link Section#when(BooleanSupplier)} predicate is false, so callers never have to reason about the
 * blank lines around content that turned out not to exist. {@link Section#separated()} asks for exactly one
 * {@link Text#empty()} between a section and whatever visible content precedes it, which means a separated
 * section that ends up first contributes no leading blank and a body never ends on a trailing one. Lines
 * carrying {@code <wrap:N>} are expanded through {@link Text#lines()} as they are emitted.
 *
 * {@link #renderJoined(Text)} is the single line form of the same walk: the surviving lines are joined with
 * a caller supplied separator into one {@link Text}, for surfaces such as action bars that lay their
 * sections out horizontally rather than vertically.
 */
public final class TextBody {

    private final Map<String, Section> sections = new LinkedHashMap<>();
    private int sequence;

    public Section section(String id) {
        Objects.requireNonNull(id, "id");
        return sections.computeIfAbsent(id, _ -> new Section(0, sequence++));
    }

    public Section section(String id, int order) {
        Objects.requireNonNull(id, "id");
        Section existing = sections.get(id);
        if (existing == null) {
            Section created = new Section(order, sequence++);
            sections.put(id, created);
            return created;
        }
        existing.order = order;
        return existing;
    }

    public boolean has(String id) {
        return sections.containsKey(id);
    }

    public void remove(String id) {
        sections.remove(id);
    }

    public List<Text> render() {
        List<Text> out = new ArrayList<>();
        for (Section section : ordered(sections)) {
            section.renderInto(out);
        }
        return List.copyOf(out);
    }

    public Text renderJoined(Text separator) {
        Objects.requireNonNull(separator, "separator");
        return Text.join(separator, render());
    }

    private static List<Section> ordered(Map<String, Section> source) {
        List<Section> out = new ArrayList<>(source.values());
        out.sort(Comparator.comparingInt((Section section) -> section.order)
                .thenComparingInt(section -> section.sequence));
        return out;
    }

    public static final class Section {

        private final List<Text> lines = new ArrayList<>();
        private final Map<String, Section> children = new LinkedHashMap<>();
        private final int sequence;
        private int order;
        private int childSequence;
        private BooleanSupplier visible = () -> true;
        private boolean separated;

        private Section(int order, int sequence) {
            this.order = order;
            this.sequence = sequence;
        }

        public Section line(String markup, Object... arguments) {
            lines.add(Text.of(markup, arguments));
            return this;
        }

        public Section line(Text line) {
            lines.add(Objects.requireNonNull(line, "line"));
            return this;
        }

        public Section lines(Collection<Text> lines) {
            this.lines.addAll(Objects.requireNonNull(lines, "lines"));
            return this;
        }

        public Section keyLines(String i18nKey, Object... arguments) {
            lines.addAll(Text.keyLines(i18nKey, arguments));
            return this;
        }

        public Section when(BooleanSupplier visible) {
            this.visible = Objects.requireNonNull(visible, "visible");
            return this;
        }

        public Section separated() {
            this.separated = true;
            return this;
        }

        public Section child(String id) {
            Objects.requireNonNull(id, "id");
            return children.computeIfAbsent(id, _ -> new Section(0, childSequence++));
        }

        public Section clear() {
            lines.clear();
            children.clear();
            return this;
        }

        public boolean isEmpty() {
            if (!lines.isEmpty()) {
                return false;
            }
            for (Section child : children.values()) {
                if (child.renders()) {
                    return false;
                }
            }
            return true;
        }

        private boolean renders() {
            return visible.getAsBoolean() && !isEmpty();
        }

        private void renderInto(List<Text> out) {
            if (!renders()) {
                return;
            }
            if (separated && !out.isEmpty()) {
                out.add(Text.empty());
            }
            for (Text line : lines) {
                expand(line, out);
            }
            for (Section child : ordered(children)) {
                child.renderInto(out);
            }
        }

        private static void expand(Text line, List<Text> out) {
            if (line.plain().indexOf('\n') < 0) {
                out.add(line);
                return;
            }
            out.addAll(line.lines());
        }
    }
}
