package io.github.term4.polyp.platform.fixes;

import io.github.term4.polyp.platform.fixes.visuals.VisualsConfig;
import io.github.term4.polyp.platform.fixes.visuals.legacy_1_8.LegacyArrowVisibilityConfig;

/**
 * Ready-made {@link FixesConfig} presets; pass to {@code FixesSystem.install} or a {@code MechanicsProfile.fixes}
 * scope, or override with the builders for a subset.
 */
public final class Fixes {

    private Fixes() {}

    /**
     * Any-version QOL/parity set (no legacy-client dependency): the empty-slot equipment strip and the EXPERIMENTAL
     * inventory sync. The self-meta echo fix is the {@code Polyp.metaFix} init option (it wraps the player provider),
     * not a member here.
     */
    public static FixesConfig qol() {
        return FixesConfig.builder()
                .equipmentFix(FixToggleConfig.on())
                .inventorySync(FixToggleConfig.on()) // EXPERIMENTAL
                .build();
    }

    /** The recommended 1.8-client fixes; the cosmetic deflect crit-trail stays off (a flourish, not a fix). */
    public static FixesConfig legacy18() {
        return FixesConfig.builder()
                .visuals(VisualsConfig.builder()
                        .legacyArrowVisibility(LegacyArrowVisibilityConfig.builder().enabled(true).build())
                        .build())
                .legacyFireDouse(FixToggleConfig.on())
                .build();
    }
}
