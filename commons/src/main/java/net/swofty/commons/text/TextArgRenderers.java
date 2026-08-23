package net.swofty.commons.text;

import net.kyori.adventure.text.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class TextArgRenderers {

    private static final Map<Class<?>, Function<Object, Component>> RENDERERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Optional<Function<Object, Component>>> RESOLVED = new ConcurrentHashMap<>();

    private TextArgRenderers() {
    }

    @SuppressWarnings("unchecked")
    public static <T> void register(Class<T> type, Function<T, Component> renderer) {
        RENDERERS.put(type, (Function<Object, Component>) renderer);
        RESOLVED.clear();
    }

    static Component render(Object value) {
        Function<Object, Component> renderer = lookup(value.getClass());
        return renderer != null ? renderer.apply(value) : Component.text(String.valueOf(value));
    }

    private static Function<Object, Component> lookup(Class<?> type) {
        return RESOLVED.computeIfAbsent(type, key -> Optional.ofNullable(search(key))).orElse(null);
    }

    private static Function<Object, Component> search(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            Function<Object, Component> direct = RENDERERS.get(current);
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
            Function<Object, Component> direct = RENDERERS.get(face);
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
