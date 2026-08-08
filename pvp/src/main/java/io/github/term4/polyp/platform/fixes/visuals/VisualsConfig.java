package io.github.term4.polyp.platform.fixes.visuals;

import io.github.term4.polyp.platform.fixes.visuals.legacy_1_8.LegacyArrowVisibilityConfig;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code visuals} member of {@link io.github.term4.polyp.platform.fixes.FixesConfig}: fixes for how a
 * client renders something, not gameplay.
 */
public final class VisualsConfig {

    private final @Nullable LegacyArrowVisibilityConfig legacyArrowVisibility;

    private VisualsConfig(Builder b) { this.legacyArrowVisibility = b.legacyArrowVisibility; }

    public @Nullable LegacyArrowVisibilityConfig legacyArrowVisibility() { return legacyArrowVisibility; }

    /** Merges this config over {@code base} (each member: this if set, else base; both set -&gt; member-merged). */
    public VisualsConfig fromBase(VisualsConfig base) {
        LegacyArrowVisibilityConfig lav = legacyArrowVisibility == null ? base.legacyArrowVisibility
                : base.legacyArrowVisibility == null ? legacyArrowVisibility
                : legacyArrowVisibility.fromBase(base.legacyArrowVisibility);
        return new Builder().legacyArrowVisibility(lav).build();
    }

    public Builder toBuilder() { return new Builder(this); }
    public static Builder builder() { return new Builder(); }
    public static Builder builder(@Nullable VisualsConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static final class Builder {
        private @Nullable LegacyArrowVisibilityConfig legacyArrowVisibility;

        Builder() {}
        Builder(VisualsConfig c) { legacyArrowVisibility = c.legacyArrowVisibility; }

        public Builder legacyArrowVisibility(@Nullable LegacyArrowVisibilityConfig v) { this.legacyArrowVisibility = v; return this; }

        public VisualsConfig build() { return new VisualsConfig(this); }
    }
}
