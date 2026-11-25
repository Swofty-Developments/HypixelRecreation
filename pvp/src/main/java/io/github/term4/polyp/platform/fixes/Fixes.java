package io.github.term4.polyp.platform.fixes;

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
}
