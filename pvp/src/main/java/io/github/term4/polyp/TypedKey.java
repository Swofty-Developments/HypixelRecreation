package io.github.term4.polyp;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared implementation of the typed key classes ({@link ConfigKey}, {@code ClientKey}) - not a key type itself.
 * Keys are equal by {@link #id()} within their own class, so a re-created key matches.
 */
public abstract class TypedKey<T> {

    /** One id table per key class: the id namespaces stay separate, so an id may be claimed in each. */
    public static final class Namespace {
        private final ConcurrentHashMap<String, Class<?>> types = new ConcurrentHashMap<>();
    }

    private final String id;
    private final Class<T> type;

    protected TypedKey(String id, Class<T> type, Namespace namespace) {
        Class<?> previous = namespace.types.putIfAbsent(id, type);
        if (previous != null && previous != type) {
            throw new IllegalStateException(getClass().getSimpleName() + " id collision: \"" + id
                    + "\" already registered as " + previous.getName() + ", now requested as " + type.getName());
        }
        this.id = id;
        this.type = type;
    }

    public final String id() { return id; }
    public final Class<T> type() { return type; }

    @Override public final boolean equals(Object o) {
        return o != null && o.getClass() == getClass() && id.equals(((TypedKey<?>) o).id);
    }
    @Override public final int hashCode() { return id.hashCode(); }
    @Override public final String toString() { return getClass().getSimpleName() + "[" + id + "]"; }
}
