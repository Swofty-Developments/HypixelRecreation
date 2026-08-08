package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.attribute.AttributeConfig;
import io.github.term4.polyp.mechanics.attribute.catalog.VanillaAttributes;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Absorption;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Strength;
import io.github.term4.polyp.mechanics.attribute.catalog.effect.Weakness;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Sharpness;
import io.github.term4.polyp.mechanics.attribute.defense.ArmorConfig;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionConfig;

/**
 * Vanilla 1.8 attribute config: the LEGACY source variants + LEGACY defense stages the {@code AttributeSystem.mitigate}
 * pipeline runs. A MODERN preset registers the {@code .MODERN} variants instead - the version lives in what's
 * registered, not a flag.
 */
public final class Attributes {

    private Attributes() {}

    public static AttributeConfig config() {
        return AttributeConfig.builder()
                .sources(Strength.LEGACY, Weakness.LEGACY, Sharpness.LEGACY, Absorption.LEGACY)
                .sources(VanillaAttributes.enchants())
                .sources(VanillaAttributes.effects())
                .armor(ArmorConfig.builder().formula(ArmorConfig.Formula.LEGACY_LINEAR).build())
                .protection(ProtectionConfig.builder().formula(ProtectionConfig.Formula.LEGACY_RANDOMIZED).build())
                .build();
    }
}
