package net.swofty.type.generic.text;

import net.kyori.adventure.text.Component;
import net.swofty.type.generic.i18n.HypixelTranslator;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RenderContext {

    @FunctionalInterface
    public interface Scope {
        @Nullable Component resolve(String tag);
    }

    private static final Map<Class<?>, Function<Object, Scope>> CONTRIBUTORS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Optional<Function<Object, Scope>>> RESOLVED = new ConcurrentHashMap<>();
    private static final RenderContext SERVER = new RenderContext(null, null, List.of());

    private final HypixelPlayer viewer;
    private final Locale locale;
    private final List<Scope> scopes;

    private RenderContext(HypixelPlayer viewer, Locale locale, List<Scope> scopes) {
        this.viewer = viewer;
        this.locale = locale;
        this.scopes = scopes;
    }

    public static RenderContext ofServer() {
        return SERVER;
    }

    public static RenderContext ofServer(Locale locale) {
        return new RenderContext(null, locale, List.of());
    }

    public static RenderContext of(HypixelPlayer viewer) {
        if (viewer == null) {
            return SERVER;
        }
        return new RenderContext(viewer, null, List.of(viewerScope(viewer)));
    }

    @SuppressWarnings("unchecked")
    public static <T> void registerScope(Class<T> type, Function<T, Scope> contributor) {
        CONTRIBUTORS.put(type, (Function<Object, Scope>) contributor);
        RESOLVED.clear();
    }

    public RenderContext withScope(Scope scope) {
        if (scope == null) {
            return this;
        }
        List<Scope> combined = new ArrayList<>(scopes.size() + 1);
        combined.addAll(scopes);
        combined.add(scope);
        return new RenderContext(viewer, locale, List.copyOf(combined));
    }

    public RenderContext withScope(Object subject) {
        if (subject == null) {
            return this;
        }
        Function<Object, Scope> contributor = contributor(subject.getClass());
        return contributor == null ? this : withScope(contributor.apply(subject));
    }

    public RenderContext with(String tag, Supplier<Component> resolver) {
        String name = tag.toLowerCase(Locale.ROOT);
        return withScope(candidate -> candidate.equals(name) ? resolver.get() : null);
    }

    public @Nullable HypixelPlayer viewer() {
        return viewer;
    }

    public Locale locale() {
        if (locale != null) {
            return locale;
        }
        if (viewer != null) {
            Locale viewerLocale = viewer.getLocale();
            if (viewerLocale != null) {
                return viewerLocale;
            }
        }
        return HypixelTranslator.defaultLocale;
    }

    public @Nullable Component resolve(String tag) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Component resolved = scopes.get(i).resolve(tag);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    private static Scope viewerScope(HypixelPlayer viewer) {
        return tag -> switch (tag) {
            case "player" -> Component.text(viewer.getUsername());
            case "rank" -> viewer.getRankPrefix();
            default -> null;
        };
    }

    private static Function<Object, Scope> contributor(Class<?> type) {
        return RESOLVED.computeIfAbsent(type, key -> Optional.ofNullable(search(key))).orElse(null);
    }

    private static Function<Object, Scope> search(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            Function<Object, Scope> direct = CONTRIBUTORS.get(current);
            if (direct != null) {
                return direct;
            }
        }
        Deque<Class<?>> queue = new ArrayDeque<>();
        Set<Class<?>> seen = new LinkedHashSet<>();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Class<?> face : current.getInterfaces()) {
                if (seen.add(face)) {
                    queue.add(face);
                }
            }
        }
        while (!queue.isEmpty()) {
            Class<?> face = queue.poll();
            Function<Object, Scope> direct = CONTRIBUTORS.get(face);
            if (direct != null) {
                return direct;
            }
            for (Class<?> parent : face.getInterfaces()) {
                if (seen.add(parent)) {
                    queue.add(parent);
                }
            }
        }
        return null;
    }
}
