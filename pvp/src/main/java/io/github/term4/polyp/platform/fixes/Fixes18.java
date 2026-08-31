package io.github.term4.polyp.platform.fixes;

import io.github.term4.polyp.platform.fixes.visuals.VisualsConfig;
import io.github.term4.polyp.platform.fixes.visuals.legacy_1_8.LegacyArrowVisibilityConfig;

/**
 * The full 1.8/Via legacy-client {@link FixesConfig} - {@link Fixes#qol()} plus every legacy fix; install with
 * {@code FixesSystem.install(polyp, Fixes18.config())}.
 */
public final class Fixes18 {

    private Fixes18() {}

    public static FixesConfig config() {
        return FixesConfig.builder(Fixes.qol())
                .visuals(VisualsConfig.builder()
                        .legacyArrowVisibility(LegacyArrowVisibilityConfig.builder().enabled(true).deflectParticles(true).build())
                        .build())
                .legacySelfPlacement(FixToggleConfig.on())
                .legacyTabCompleteFix(FixToggleConfig.on())
                .legacyConsume(FixToggleConfig.on())
                .legacyFireDouse(FixToggleConfig.on())
                .build();
    }
}
