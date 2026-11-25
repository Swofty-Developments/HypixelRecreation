package io.github.term4.polyp.mechanics.cooldown;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-item use cooldowns (vanilla ticks), enforced server-side by {@link CooldownSystem}. Unset materials have none.
 * Modern presets mirror vanilla's component cooldowns here (ender pearl 20); 1.8 presets simply don't set one.
 */
public final class CooldownConfig {

    private final Map<Material, Integer> ticks;

    private CooldownConfig(Builder b) { this.ticks = Map.copyOf(b.ticks); }

    /** Vanilla ticks, or {@code null} for none. */
    public @Nullable Integer ticks(Material material) { return ticks.get(material); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final Map<Material, Integer> ticks = new HashMap<>();

        public Builder cooldown(Material material, int vanillaTicks) { ticks.put(material, vanillaTicks); return this; }

        public CooldownConfig build() { return new CooldownConfig(this); }
    }
}
