package io.github.term4.polyp.mechanics.attribute.catalog;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.potion.TimedPotion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vanilla base-potion → effects table - a mirror of vanilla's {@code minecraft:potion} registry (26 {@code Potions.java}),
 * needed because Minestom's {@link PotionType} is only a key and doesn't carry its effects. Consumers resolve a potion
 * item's base {@code potion} through this (the tipped-arrow framework, the drinkable-potion consumable); a potion's own
 * {@code customEffects} are used directly and don't need a row.
 *
 * <p><b>Version-invariant.</b> The recipe is the same in 1.8 and 26 for every shared potion; what differs is each
 * <em>effect's mechanic</em>, which lives in the {@code LEGACY}/{@code MODERN} effect sources. The 26-only potions
 * (Turtle Master, Slow Falling, Luck, Strong Slowness) are unreachable by a 1.8/Via client, so one table serves both.
 * The 1/8 tipped-arrow scaling is NOT baked in - it's the arrow's {@code potion_duration_scale}.
 */
public final class VanillaPotions {
    private VanillaPotions() {}

    /** {@code amplifier} is 0-based (level I = 0); instant potions use duration {@code 1}. Ambient off, particles + icon on. */
    private static CustomPotionEffect effect(PotionEffect id, int amplifier, int duration) {
        return new CustomPotionEffect(id, amplifier, duration, false, true, true);
    }

    private static final Map<PotionType, List<CustomPotionEffect>> TABLE = Map.ofEntries(
            Map.entry(PotionType.NIGHT_VISION, List.of(effect(PotionEffect.NIGHT_VISION, 0, 3600))),
            Map.entry(PotionType.LONG_NIGHT_VISION, List.of(effect(PotionEffect.NIGHT_VISION, 0, 9600))),
            Map.entry(PotionType.INVISIBILITY, List.of(effect(PotionEffect.INVISIBILITY, 0, 3600))),
            Map.entry(PotionType.LONG_INVISIBILITY, List.of(effect(PotionEffect.INVISIBILITY, 0, 9600))),
            Map.entry(PotionType.LEAPING, List.of(effect(PotionEffect.JUMP_BOOST, 0, 3600))),
            Map.entry(PotionType.LONG_LEAPING, List.of(effect(PotionEffect.JUMP_BOOST, 0, 9600))),
            Map.entry(PotionType.STRONG_LEAPING, List.of(effect(PotionEffect.JUMP_BOOST, 1, 1800))),
            Map.entry(PotionType.FIRE_RESISTANCE, List.of(effect(PotionEffect.FIRE_RESISTANCE, 0, 3600))),
            Map.entry(PotionType.LONG_FIRE_RESISTANCE, List.of(effect(PotionEffect.FIRE_RESISTANCE, 0, 9600))),
            Map.entry(PotionType.SWIFTNESS, List.of(effect(PotionEffect.SPEED, 0, 3600))),
            Map.entry(PotionType.LONG_SWIFTNESS, List.of(effect(PotionEffect.SPEED, 0, 9600))),
            Map.entry(PotionType.STRONG_SWIFTNESS, List.of(effect(PotionEffect.SPEED, 1, 1800))),
            Map.entry(PotionType.SLOWNESS, List.of(effect(PotionEffect.SLOWNESS, 0, 1800))),
            Map.entry(PotionType.LONG_SLOWNESS, List.of(effect(PotionEffect.SLOWNESS, 0, 4800))),
            Map.entry(PotionType.STRONG_SLOWNESS, List.of(effect(PotionEffect.SLOWNESS, 3, 400))),
            Map.entry(PotionType.TURTLE_MASTER, List.of(effect(PotionEffect.SLOWNESS, 3, 400), effect(PotionEffect.RESISTANCE, 2, 400))),
            Map.entry(PotionType.LONG_TURTLE_MASTER, List.of(effect(PotionEffect.SLOWNESS, 3, 800), effect(PotionEffect.RESISTANCE, 2, 800))),
            Map.entry(PotionType.STRONG_TURTLE_MASTER, List.of(effect(PotionEffect.SLOWNESS, 5, 400), effect(PotionEffect.RESISTANCE, 3, 400))),
            Map.entry(PotionType.WATER_BREATHING, List.of(effect(PotionEffect.WATER_BREATHING, 0, 3600))),
            Map.entry(PotionType.LONG_WATER_BREATHING, List.of(effect(PotionEffect.WATER_BREATHING, 0, 9600))),
            Map.entry(PotionType.HEALING, List.of(effect(PotionEffect.INSTANT_HEALTH, 0, 1))),
            Map.entry(PotionType.STRONG_HEALING, List.of(effect(PotionEffect.INSTANT_HEALTH, 1, 1))),
            Map.entry(PotionType.HARMING, List.of(effect(PotionEffect.INSTANT_DAMAGE, 0, 1))),
            Map.entry(PotionType.STRONG_HARMING, List.of(effect(PotionEffect.INSTANT_DAMAGE, 1, 1))),
            Map.entry(PotionType.POISON, List.of(effect(PotionEffect.POISON, 0, 900))),
            Map.entry(PotionType.LONG_POISON, List.of(effect(PotionEffect.POISON, 0, 1800))),
            Map.entry(PotionType.STRONG_POISON, List.of(effect(PotionEffect.POISON, 1, 432))),
            Map.entry(PotionType.REGENERATION, List.of(effect(PotionEffect.REGENERATION, 0, 900))),
            Map.entry(PotionType.LONG_REGENERATION, List.of(effect(PotionEffect.REGENERATION, 0, 1800))),
            Map.entry(PotionType.STRONG_REGENERATION, List.of(effect(PotionEffect.REGENERATION, 1, 450))),
            Map.entry(PotionType.STRENGTH, List.of(effect(PotionEffect.STRENGTH, 0, 3600))),
            Map.entry(PotionType.LONG_STRENGTH, List.of(effect(PotionEffect.STRENGTH, 0, 9600))),
            Map.entry(PotionType.STRONG_STRENGTH, List.of(effect(PotionEffect.STRENGTH, 1, 1800))),
            Map.entry(PotionType.WEAKNESS, List.of(effect(PotionEffect.WEAKNESS, 0, 1800))),
            Map.entry(PotionType.LONG_WEAKNESS, List.of(effect(PotionEffect.WEAKNESS, 0, 4800))),
            Map.entry(PotionType.LUCK, List.of(effect(PotionEffect.LUCK, 0, 6000))),
            Map.entry(PotionType.SLOW_FALLING, List.of(effect(PotionEffect.SLOW_FALLING, 0, 1800))),
            Map.entry(PotionType.LONG_SLOW_FALLING, List.of(effect(PotionEffect.SLOW_FALLING, 0, 4800)))
    );

    /** Empty for an effectless base (water / mundane / thick / awkward) or an unmapped type. */
    public static List<CustomPotionEffect> effects(PotionType potion) {
        return TABLE.getOrDefault(potion, List.of());
    }

    /**
     * The full effect payload of a potion item's {@code potion_contents}: custom effects plus the base potion's rows.
     * Empty for a non-potion item or a water bottle.
     */
    public static List<CustomPotionEffect> payload(@Nullable ItemStack item) {
        PotionContents pc = item != null ? item.get(DataComponents.POTION_CONTENTS) : null;
        if (pc == null) return List.of();
        List<CustomPotionEffect> effects = new ArrayList<>(pc.customEffects());
        if (pc.potion() != null) effects.addAll(effects(pc.potion()));
        return effects;
    }

    /**
     * Vanilla add-or-combine (1.8 {@code MobEffect.a}, same shape in 26): a higher amplifier replaces amplifier +
     * duration, an equal amplifier only extends a shorter remaining duration, and a weaker or shorter effect is
     * ignored - a plain {@code addEffect} would let any potion override any active effect of the same type.
     */
    public static void addEffect(LivingEntity living, Potion potion) {
        TimedPotion existing = null;
        for (TimedPotion t : living.getActiveEffects()) {
            if (t.potion().effect() == potion.effect()) { existing = t; break; }
        }
        if (existing != null) {
            long remaining = existing.potion().duration() - (living.getAliveTicks() - existing.startingTicks());
            boolean upgrade = potion.amplifier() > existing.potion().amplifier()
                    || (potion.amplifier() == existing.potion().amplifier() && potion.duration() > remaining);
            if (!upgrade) return;
        }
        living.addEffect(potion);
    }

    /** 1.8 drinkable potion damage values (its wire encoding of potion identity); splash = {@code + 8192}. */
    private static final Map<PotionType, Integer> LEGACY_VALUES = Map.ofEntries(
            Map.entry(PotionType.NIGHT_VISION, 8198), Map.entry(PotionType.LONG_NIGHT_VISION, 8262),
            Map.entry(PotionType.INVISIBILITY, 8206), Map.entry(PotionType.LONG_INVISIBILITY, 8270),
            Map.entry(PotionType.LEAPING, 8203), Map.entry(PotionType.LONG_LEAPING, 8267), Map.entry(PotionType.STRONG_LEAPING, 8235),
            Map.entry(PotionType.FIRE_RESISTANCE, 8195), Map.entry(PotionType.LONG_FIRE_RESISTANCE, 8259),
            Map.entry(PotionType.SWIFTNESS, 8194), Map.entry(PotionType.LONG_SWIFTNESS, 8258), Map.entry(PotionType.STRONG_SWIFTNESS, 8226),
            Map.entry(PotionType.SLOWNESS, 8202), Map.entry(PotionType.LONG_SLOWNESS, 8266),
            Map.entry(PotionType.WATER_BREATHING, 8205), Map.entry(PotionType.LONG_WATER_BREATHING, 8269),
            Map.entry(PotionType.HEALING, 8261), Map.entry(PotionType.STRONG_HEALING, 8229),
            Map.entry(PotionType.HARMING, 8204), Map.entry(PotionType.STRONG_HARMING, 8236),
            Map.entry(PotionType.POISON, 8196), Map.entry(PotionType.LONG_POISON, 8260), Map.entry(PotionType.STRONG_POISON, 8228),
            Map.entry(PotionType.REGENERATION, 8193), Map.entry(PotionType.LONG_REGENERATION, 8257), Map.entry(PotionType.STRONG_REGENERATION, 8225),
            Map.entry(PotionType.STRENGTH, 8201), Map.entry(PotionType.LONG_STRENGTH, 8265), Map.entry(PotionType.STRONG_STRENGTH, 8233),
            Map.entry(PotionType.WEAKNESS, 8200), Map.entry(PotionType.LONG_WEAKNESS, 8264));

    /**
     * The 1.8 SPLASH damage value - what a real 1.8 server carries on the wire; the client reads it straight from level
     * event 2002, which Via passes through untranslated. Splash water for an unmapped type.
     */
    public static int legacySplashValue(@Nullable PotionType potion) {
        Integer drinkable = potion != null ? LEGACY_VALUES.get(potion) : null;
        return drinkable != null ? drinkable + 8192 : 16384;
    }
}
