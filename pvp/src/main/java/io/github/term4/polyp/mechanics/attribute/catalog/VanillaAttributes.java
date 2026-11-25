package io.github.term4.polyp.mechanics.attribute.catalog;

import io.github.term4.polyp.mechanics.attribute.source.Source;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.AquaAffinity;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Bane;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.DepthStrider;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Efficiency;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.FireAspect;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Smite;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Blindness;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Hunger;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.InstantDamage;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.InstantHealth;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Invisibility;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.NightVision;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Regeneration;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Slowness;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Speed;

/**
 * Vanilla attribute-{@link Source} bundles shared across presets - the version-agnostic enchants/effects both
 * {@code Vanilla18} and {@code Vanilla} register. Version-specific variants ({@code Strength.LEGACY}/{@code .MODERN},
 * Absorption, modern-only Haste/Mining Fatigue/Jump Boost) stay inline in the preset.
 */
public final class VanillaAttributes {
    private VanillaAttributes() {}

    public static Source[] enchants() {
        return new Source[]{Smite.INSTANCE, Bane.INSTANCE, Efficiency.INSTANCE, AquaAffinity.INSTANCE,
                DepthStrider.INSTANCE, FireAspect.INSTANCE};
    }

    public static Source[] effects() {
        return new Source[]{Speed.INSTANCE, Slowness.INSTANCE, Invisibility.INSTANCE, Regeneration.INSTANCE,
                InstantHealth.INSTANCE, InstantDamage.INSTANCE, Blindness.INSTANCE, NightVision.INSTANCE,
                Hunger.INSTANCE};
    }
}
