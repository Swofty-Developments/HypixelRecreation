package io.github.term4.polyp.mechanics.attribute.catalog;

import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionEffect;
import net.kyori.adventure.util.RGBLike;

import java.util.List;
import java.util.Map;

/**
 * FxHandler-color table (vanilla 26 {@code MobEffects} registrations) + the potion color blend - Minestom's
 * {@link PotionEffect} carries no color. Drives the splash-potion level event (2002), whose data is the RGB color
 * (ViaBackwards maps it back to a legacy potion id for old clients).
 */
public final class PotionColors {

    /** Vanilla {@code PotionContents.BASE_POTION_COLOR}. */
    public static final int BASE_POTION_COLOR = -13083194;

    private PotionColors() {}

    private static final Map<PotionEffect, Integer> TABLE = Map.ofEntries(
            Map.entry(PotionEffect.SPEED, 3402751),
            Map.entry(PotionEffect.SLOWNESS, 9154528),
            Map.entry(PotionEffect.HASTE, 14270531),
            Map.entry(PotionEffect.MINING_FATIGUE, 4866583),
            Map.entry(PotionEffect.STRENGTH, 16762624),
            Map.entry(PotionEffect.INSTANT_HEALTH, 16262179),
            Map.entry(PotionEffect.INSTANT_DAMAGE, 11101546),
            Map.entry(PotionEffect.JUMP_BOOST, 16646020),
            Map.entry(PotionEffect.NAUSEA, 5578058),
            Map.entry(PotionEffect.REGENERATION, 13458603),
            Map.entry(PotionEffect.RESISTANCE, 9520880),
            Map.entry(PotionEffect.FIRE_RESISTANCE, 16750848),
            Map.entry(PotionEffect.WATER_BREATHING, 10017472),
            Map.entry(PotionEffect.INVISIBILITY, 16185078),
            Map.entry(PotionEffect.BLINDNESS, 2039587),
            Map.entry(PotionEffect.NIGHT_VISION, 12779366),
            Map.entry(PotionEffect.HUNGER, 5797459),
            Map.entry(PotionEffect.WEAKNESS, 4738376),
            Map.entry(PotionEffect.POISON, 8889187),
            Map.entry(PotionEffect.WITHER, 7561558),
            Map.entry(PotionEffect.HEALTH_BOOST, 16284963),
            Map.entry(PotionEffect.ABSORPTION, 2445989),
            Map.entry(PotionEffect.SATURATION, 16262179),
            Map.entry(PotionEffect.GLOWING, 9740385),
            Map.entry(PotionEffect.LEVITATION, 13565951),
            Map.entry(PotionEffect.LUCK, 5882118),
            Map.entry(PotionEffect.UNLUCK, 12624973),
            Map.entry(PotionEffect.SLOW_FALLING, 15978425));

    /** 1.8 {@code MobEffectList} colors where they differ; effects without a row fall back to the modern table. */
    private static final Map<PotionEffect, Integer> LEGACY_TABLE = Map.ofEntries(
            Map.entry(PotionEffect.SPEED, 8171462),
            Map.entry(PotionEffect.SLOWNESS, 5926017),
            Map.entry(PotionEffect.STRENGTH, 9643043),
            Map.entry(PotionEffect.INSTANT_DAMAGE, 4393481),
            Map.entry(PotionEffect.JUMP_BOOST, 2293580),
            Map.entry(PotionEffect.REGENERATION, 13458603),
            Map.entry(PotionEffect.RESISTANCE, 10044730),
            Map.entry(PotionEffect.FIRE_RESISTANCE, 14981690),
            Map.entry(PotionEffect.WATER_BREATHING, 3035801),
            Map.entry(PotionEffect.INVISIBILITY, 8356754),
            Map.entry(PotionEffect.NIGHT_VISION, 2039713),
            Map.entry(PotionEffect.WEAKNESS, 4738376),
            Map.entry(PotionEffect.POISON, 5149489),
            Map.entry(PotionEffect.WITHER, 3484199));

    /** {@code custom_color} if set, else the amplifier-weighted blend ({@code PotionContents.getColorOptional}). */
    public static int color(@org.jetbrains.annotations.Nullable PotionContents contents, List<CustomPotionEffect> effects) {
        return color(contents, effects, false);
    }

    /** {@link #color} with the 1.8 palette + unweighted blend ({@code PotionHelper}), for 1.8-parity particles on modern clients. */
    public static int legacyColor(@org.jetbrains.annotations.Nullable PotionContents contents, List<CustomPotionEffect> effects) {
        return color(contents, effects, true);
    }

    private static int color(@org.jetbrains.annotations.Nullable PotionContents contents, List<CustomPotionEffect> effects, boolean legacy) {
        RGBLike custom = contents != null ? contents.customColor() : null;
        if (custom != null) return (custom.red() << 16) | (custom.green() << 8) | custom.blue();
        return blend(effects, legacy);
    }

    private static int blend(List<CustomPotionEffect> effects, boolean legacy) {
        int r = 0, g = 0, b = 0, weight = 0;
        for (CustomPotionEffect e : effects) {
            if (!e.showParticles()) continue;
            int color = colorOf(e.id(), legacy);
            int amp = legacy ? 1 : e.amplifier() + 1; // 1.8 averages unweighted
            r += amp * ((color >> 16) & 0xFF);
            g += amp * ((color >> 8) & 0xFF);
            b += amp * (color & 0xFF);
            weight += amp;
        }
        if (weight == 0) return BASE_POTION_COLOR;
        return ((r / weight) << 16) | ((g / weight) << 8) | (b / weight);
    }

    private static int colorOf(PotionEffect id, boolean legacy) {
        Integer color = legacy ? LEGACY_TABLE.getOrDefault(id, TABLE.get(id)) : TABLE.get(id);
        return (color != null ? color : BASE_POTION_COLOR) & 0xFFFFFF;
    }
}
