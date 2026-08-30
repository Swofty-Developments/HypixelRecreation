package io.github.term4.polyp.config;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A single configurable value resolved against a context {@code CTX}: a constant, a context-aware function, or a
 * function with a constant fallback. Values built from {@link #constant(Object)} stay readable without a context via
 * {@link #constantOrNull()}.
 */
public record FieldValue<CTX, T>(Function<CTX, T> fn, @Nullable T constant) {

    public static <CTX, T> FieldValue<CTX, T> constant(T v) {
        return new FieldValue<>(ctx -> v, v);
    }

    public static <CTX, T> FieldValue<CTX, T> of(Function<CTX, T> f) {
        return new FieldValue<>(f, null);
    }

    /** Falls back to {@code fallback} when the function returns {@code null}. */
    public static <CTX, T> FieldValue<CTX, T> ofWithFallback(T fallback, Function<CTX, T> fn) {
        return new FieldValue<>(ctx -> {
            T r = fn.apply(ctx);
            return r != null ? r : fallback;
        }, null);
    }

    public T resolve(CTX ctx) {
        return fn.apply(ctx);
    }

    /** {@code null} when the field is unset or resolves to {@code null}. */
    public static <CTX, T> @Nullable T resolve(@Nullable FieldValue<CTX, T> field, CTX ctx) {
        return field != null ? field.resolve(ctx) : null;
    }

    /** An unset field or a {@code null} resolution falls back to {@code def}. */
    public static <CTX, T> T resolve(@Nullable FieldValue<CTX, T> field, CTX ctx, T def) {
        T v = field != null ? field.resolve(ctx) : null;
        return v != null ? v : def;
    }

    /** {@code null} when this value is context-dependent. */
    public @Nullable T constantOrNull() {
        return constant;
    }

    /** {@code a} layered over {@code b} ({@code a} wins, falling back per resolution); either side may be {@code null}. */
    public static <CTX, T> FieldValue<CTX, T> merge(FieldValue<CTX, T> a, FieldValue<CTX, T> b) {
        if (b == null) return a;
        if (a == null) return b;
        return a.or(b);
    }

    /** Uses {@code fallback} when this one resolves to {@code null}. */
    public FieldValue<CTX, T> or(FieldValue<CTX, T> fallback) {
        return new FieldValue<>(ctx -> {
            T r = fn.apply(ctx);
            return r != null ? r : fallback.fn.apply(ctx);
        }, null);
    }
}
