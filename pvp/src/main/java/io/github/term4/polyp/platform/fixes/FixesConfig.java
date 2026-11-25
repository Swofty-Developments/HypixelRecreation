package io.github.term4.polyp.platform.fixes;

import io.github.term4.polyp.platform.fixes.visuals.VisualsConfig;
import org.jetbrains.annotations.Nullable;

/**
 * Top-level config for the lib's client/protocol behavior <b>fixes</b> - both cross-version compatibility (the 1.8
 * arrow-visibility fix) and single-version client smoothing (the self-meta echo fix). Assigned per scope via the
 * {@link io.github.term4.polyp.MechanicsProfile} {@code fixes} member.
 */
public final class FixesConfig {

    private final @Nullable VisualsConfig visuals;
    private final @Nullable FixToggleConfig legacySelfPlacement;
    private final @Nullable FixToggleConfig equipmentFix;
    private final @Nullable FixToggleConfig legacyTabCompleteFix;
    private final @Nullable FixToggleConfig legacyConsume;
    private final @Nullable FixToggleConfig legacyFireDouse;
    private final @Nullable FixToggleConfig inventorySync;

    private FixesConfig(Builder b) {
        this.visuals = b.visuals;
        this.legacySelfPlacement = b.legacySelfPlacement;
        this.equipmentFix = b.equipmentFix;
        this.legacyTabCompleteFix = b.legacyTabCompleteFix;
        this.legacyConsume = b.legacyConsume;
        this.legacyFireDouse = b.legacyFireDouse;
        this.inventorySync = b.inventorySync;
    }

    public @Nullable VisualsConfig visuals() { return visuals; }

    /** 1.8 self-placement (passable blocks into your own body, {@code LegacySelfPlacementFix}); wraps the server-wide placement listener - install-level, not per-scope. */
    public @Nullable FixToggleConfig legacySelfPlacement() { return legacySelfPlacement; }

    /** Strips empty slots from outgoing equipment packets, vanilla parity for every version ({@code EquipmentSlotsFix}); install-level. */
    public @Nullable FixToggleConfig equipmentFix() { return equipmentFix; }

    /** Command-name tab completion for legacy clients ({@code LegacyTabCompleteFix}); replaces the packet listener - install-level. */
    public @Nullable FixToggleConfig legacyTabCompleteFix() { return legacyTabCompleteFix; }

    /**
     * The legacy 1.8/Via consume fix (eating under lag): a 1.8 client neither gates its own consumption nor learns
     * the eaten count, so {@code ConsumableSystem} refuses a re-use mid-use, decrements the held slot silently, and
     * confirms each finish. Per-scope; legacy clients only.
     */
    public @Nullable FixToggleConfig legacyConsume() { return legacyConsume; }

    /** 1.8 face douse - dig-starts extinguish fire on the clicked face ({@code LegacyFireDouseFix}). Per-scope. */
    public @Nullable FixToggleConfig legacyFireDouse() { return legacyFireDouse; }

    /** Remote-slot echo suppression ({@code InventorySync}); EXPERIMENTAL; server-wide - install config only. */
    public @Nullable FixToggleConfig inventorySync() { return inventorySync; }

    /** Merges this config over {@code base} (each member: this if set, else base; both set -&gt; member-merged). */
    public FixesConfig fromBase(FixesConfig base) {
        VisualsConfig v = visuals == null ? base.visuals
                : base.visuals == null ? visuals : visuals.fromBase(base.visuals);
        return new Builder().visuals(v)
                .legacySelfPlacement(merge(legacySelfPlacement, base.legacySelfPlacement))
                .equipmentFix(merge(equipmentFix, base.equipmentFix))
                .legacyTabCompleteFix(merge(legacyTabCompleteFix, base.legacyTabCompleteFix))
                .legacyConsume(merge(legacyConsume, base.legacyConsume))
                .legacyFireDouse(merge(legacyFireDouse, base.legacyFireDouse))
                .inventorySync(merge(inventorySync, base.inventorySync))
                .build();
    }

    private static @Nullable FixToggleConfig merge(@Nullable FixToggleConfig over, @Nullable FixToggleConfig base) {
        return over != null ? over : base;
    }

    public Builder toBuilder() { return new Builder(this); }
    public static Builder builder() { return new Builder(); }
    public static Builder builder(@Nullable FixesConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static final class Builder {
        private @Nullable VisualsConfig visuals;
        private @Nullable FixToggleConfig legacySelfPlacement;
        private @Nullable FixToggleConfig equipmentFix;
        private @Nullable FixToggleConfig legacyTabCompleteFix;
        private @Nullable FixToggleConfig legacyConsume;
        private @Nullable FixToggleConfig legacyFireDouse;
        private @Nullable FixToggleConfig inventorySync;

        Builder() {}
        Builder(FixesConfig c) { visuals = c.visuals; legacySelfPlacement = c.legacySelfPlacement; equipmentFix = c.equipmentFix; legacyTabCompleteFix = c.legacyTabCompleteFix; legacyConsume = c.legacyConsume; legacyFireDouse = c.legacyFireDouse; inventorySync = c.inventorySync; }

        public Builder visuals(@Nullable VisualsConfig v) { this.visuals = v; return this; }
        public Builder legacySelfPlacement(@Nullable FixToggleConfig v) { this.legacySelfPlacement = v; return this; }
        public Builder equipmentFix(@Nullable FixToggleConfig v) { this.equipmentFix = v; return this; }
        public Builder legacyTabCompleteFix(@Nullable FixToggleConfig v) { this.legacyTabCompleteFix = v; return this; }
        public Builder legacyConsume(@Nullable FixToggleConfig v) { this.legacyConsume = v; return this; }
        public Builder legacyFireDouse(@Nullable FixToggleConfig v) { this.legacyFireDouse = v; return this; }
        public Builder inventorySync(@Nullable FixToggleConfig v) { this.inventorySync = v; return this; }

        public FixesConfig build() { return new FixesConfig(this); }
    }
}
