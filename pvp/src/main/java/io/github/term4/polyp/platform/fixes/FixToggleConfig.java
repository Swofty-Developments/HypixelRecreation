package io.github.term4.polyp.platform.fixes;

/** Shared config for the enabled-only fix toggles; a {@code null} member = unset (falls through the scope chain). */
public final class FixToggleConfig {

    private static final FixToggleConfig ON = new FixToggleConfig(true);
    private static final FixToggleConfig OFF = new FixToggleConfig(false);

    private final boolean enabled;

    private FixToggleConfig(boolean enabled) { this.enabled = enabled; }

    public static FixToggleConfig on() { return ON; }
    public static FixToggleConfig of(boolean enabled) { return enabled ? ON : OFF; }

    public boolean enabled() { return enabled; }
}
