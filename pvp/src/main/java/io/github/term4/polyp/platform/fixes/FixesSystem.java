package io.github.term4.polyp.platform.fixes;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.ScopedSystem;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.fixes.client.EquipmentSlotsFix;
import io.github.term4.polyp.platform.fixes.client.InventorySync;
import io.github.term4.polyp.platform.fixes.client.LegacyFireDouseFix;
import io.github.term4.polyp.platform.fixes.client.LegacyPlacementGhostFix;
import io.github.term4.polyp.platform.fixes.client.LegacyUseOnBlockFix;
import io.github.term4.polyp.platform.fixes.client.UseItemInterruptFix;
import io.github.term4.polyp.platform.fixes.client.LegacySelfPlacementFix;
import io.github.term4.polyp.platform.fixes.client.LegacyTabCompleteFix;
import io.github.term4.polyp.platform.fixes.visuals.VisualsConfig;
import io.github.term4.polyp.platform.fixes.visuals.legacy_1_8.LegacyArrowVisibility;
import io.github.term4.polyp.platform.fixes.visuals.legacy_1_8.LegacyArrowVisibilityConfig;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Installs the client/protocol behavior fixes from a {@link FixesConfig}; per-scope config via {@code FIXES}.
 *
 * <p>The self-meta smoothing fix is delivered by the custom player override, so it is armed by
 * {@code MetaFix.installListeners()} from {@code Polyp.init}, not here.
 */
public final class FixesSystem extends ScopedSystem<FixesConfig> {

    private final EventNode<@NotNull Event> node;
    private final LegacyArrowVisibility legacyArrowVisibility;

    public FixesSystem(Polyp polyp, FixesConfig config) {
        super(polyp, MechanicsKeys.FIXES, config);
        this.node = EventNode.all("polyp:fixes");
        this.legacyArrowVisibility = new LegacyArrowVisibility(this);
    }

    public EventNode<@NotNull Event> node() { return node; }
    public LegacyArrowVisibility legacyArrowVisibility() { return legacyArrowVisibility; }

    public @Nullable LegacyArrowVisibilityConfig legacyArrowVisibilityConfig(@Nullable Entity subject) {
        VisualsConfig v = configFor(subject).visuals();
        return v != null ? v.legacyArrowVisibility() : null;
    }

    /** Whether the legacy arrow-visibility team fix is enabled for {@code subject} (default {@code false}). */
    public boolean legacyArrowVisibilityEnabled(@Nullable Entity subject) {
        LegacyArrowVisibilityConfig c = legacyArrowVisibilityConfig(subject);
        return c != null && Boolean.TRUE.equals(c.enabled());
    }

    /** Whether the cosmetic deflect crit-trail is enabled for {@code subject} (default {@code false}). */
    public boolean legacyArrowDeflectParticles(@Nullable Entity subject) {
        LegacyArrowVisibilityConfig c = legacyArrowVisibilityConfig(subject);
        return c != null && Boolean.TRUE.equals(c.deflectParticles());
    }

    /** Installs from the GLOBAL profile's {@link FixesConfig} - set the profile before installing. */
    public static FixesSystem install(Polyp polyp) {
        FixesConfig global = polyp.profiles().resolve(null, MechanicsKeys.FIXES);
        return install(polyp, global != null ? global : FixesConfig.builder().build());
    }

    public static FixesSystem install(Polyp polyp, FixesConfig cfg) {
        FixesSystem system = new FixesSystem(polyp, cfg);
        system.legacyArrowVisibility.install(system.node);
        LegacyFireDouseFix.install(system.node, system);
        LegacyPlacementGhostFix.install(system.node);
        LegacyUseOnBlockFix.install(system.node);
        UseItemInterruptFix.install(system.node);
        BlockUpdateOrderFix.install();
        // Below ride server-wide listeners / send overrides, so they gate on the install config and cannot vary per scope.
        // Self-placement wraps the STOCK placement listener; an app that replaces that listener re-installs LAST with
        // its own as the delegate.
        if (enabled(cfg.legacySelfPlacement())) LegacySelfPlacementFix.install();
        if (enabled(cfg.equipmentFix())) EquipmentSlotsFix.install();
        if (enabled(cfg.legacyTabCompleteFix())) LegacyTabCompleteFix.install();
        if (enabled(cfg.inventorySync())) InventorySync.install(system.node);
        return polyp.installModule(system);
    }

    private static boolean enabled(@Nullable FixToggleConfig cfg) {
        return cfg != null && cfg.enabled();
    }
}
