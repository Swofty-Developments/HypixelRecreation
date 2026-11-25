package io.github.term4.polyp.config;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Base for layered, context-resolved configs: the optional {@link #subConfig} overlay plus the
 * {@link #merge}/{@link #resolve} helpers.
 */
public abstract class Config<CTX, SELF extends Config<CTX, SELF>> {

    /** Overlay applied over this config: the resolver resolves {@code subConfig.apply(ctx).fromBase(this)}. */
    @Nullable public final Function<CTX, SELF> subConfig;

    protected Config(@Nullable Function<CTX, SELF> subConfig) {
        this.subConfig = subConfig;
    }

    /** Merges this config over {@code base}: set fields win, unset fields fall back to {@code base} per resolution. */
    public abstract SELF fromBase(SELF base);

    /** This config with its {@link #subConfig} overlay applied; itself when there is none. */
    @SuppressWarnings("unchecked")
    public final SELF withOverlay(CTX ctx) {
        SELF self = (SELF) this;
        if (subConfig == null) return self;
        SELF overlay = subConfig.apply(ctx);
        return overlay != null ? overlay.fromBase(self) : self;
    }

    /** Per-type layering, highest first: {@code entry} over {@code generic} over {@code base}, then {@link #withOverlay}. */
    public static <CTX, T extends Config<CTX, T>> T layer(T base, @Nullable T generic, @Nullable T entry, CTX ctx) {
        if (generic != null) base = generic.fromBase(base);
        if (entry != null) base = entry.fromBase(base);
        return base.withOverlay(ctx);
    }

    /** {@code a} layered over {@code b}: {@code a} wins, falling back per resolution. */
    protected static <CTX, T> FieldValue<CTX, T> merge(@Nullable FieldValue<CTX, T> a,
                                                       @Nullable FieldValue<CTX, T> b) {
        return FieldValue.merge(a, b);
    }

    /** {@code null} when the field is unset. */
    protected static <CTX, T> @Nullable T resolve(@Nullable FieldValue<CTX, T> fv, CTX ctx) {
        return FieldValue.resolve(fv, ctx);
    }
}
