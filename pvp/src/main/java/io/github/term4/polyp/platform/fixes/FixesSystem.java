package io.github.term4.polyp.platform.fixes;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.fixes.client.EquipmentSlotsFix;
import io.github.term4.polyp.platform.fixes.client.InventorySync;
import io.github.term4.polyp.platform.fixes.client.LegacyFireDouseFix;
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
public final class FixesSystem implements MechanicsModule {

    private final Polyp polyp;
    private final FixesConfig config;
    private final EventNode<@NotNull Event> node;
    private final LegacyArrowVisibility legacyArrowVisibility;

    public FixesSystem(Polyp polyp, FixesConfig config) {
        this.polyp = polyp;
        this.config = config;
        this.node = EventNode.all("polyp:fixes");
        this.legacyArrowVisibility = new LegacyArrowVisibility(this);
    }

    public EventNode<@NotNull Event> node() { return node; }
    public FixesConfig config() { return config; }
    public LegacyArrowVisibility legacyArrowVisibility() { return legacyArrowVisibility; }

    /** Effective config for {@code subject}: the scoped profile, else the install config. */
    public FixesConfig configFor(@Nullable Entity subject) {
        return polyp.profiles().resolveOr(subject, MechanicsKeys.FIXES, config);
    }

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
        polyp.register(system);
        system.legacyArrowVisibility.install(system.node);
        LegacyFireDouseFix.install(system.node, system);
        // Below ride server-wide listeners / send overrides, so they gate on the install config and cannot vary per scope.
        // Self-placement wraps the STOCK placement listener; an app that replaces that listener re-installs LAST with
        // its own as the delegate.
        if (enabled(cfg.legacySelfPlacement())) LegacySelfPlacementFix.install();
        if (enabled(cfg.equipmentFix())) EquipmentSlotsFix.install();
        if (enabled(cfg.legacyTabCompleteFix())) LegacyTabCompleteFix.install();
        if (enabled(cfg.inventorySync())) InventorySync.install(system.node);
        polyp.install(system.node);
        return system;
    }

    private static boolean enabled(@Nullable FixToggleConfig cfg) {
        return cfg != null && cfg.enabled();
    }
}
