package net.swofty.commons.rates;

import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.config.Settings;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class GameRates {
    public static final double MIN_VALUE = 0;
    public static final double MAX_VALUE = 10_000;

    private static volatile Map<RateCategory, Double> overrides = Map.of();

    private GameRates() {
    }

    public static double get(RateCategory category) {
        Double override = overrides.get(category);
        return override != null ? override : defaultValue(category);
    }

    public static double multiplier(RateCategory category) {
        if (category.isGlobal()) return get(category);
        return get(category.global()) * get(category);
    }

    public static double chance(RateCategory category, double basePercent) {
        if (basePercent <= 0 || basePercent >= 100) return basePercent;
        return Math.min(100, basePercent * multiplier(category));
    }

    public static double fraction(RateCategory category, double baseFraction) {
        return chance(category, baseFraction * 100) / 100;
    }

    public static boolean isOverridden(RateCategory category) {
        return overrides.containsKey(category);
    }

    public static double defaultValue(RateCategory category) {
        Settings.RateSettings settings = ConfigProvider.settings().getRates();
        return switch (category) {
            case ALL_DROPS -> settings.getDropChanceMultiplier();
            case ALL_XP -> settings.getSkillXpMultiplier();
            default -> 1.0;
        };
    }

    public static synchronized void set(RateCategory category, double value) {
        EnumMap<RateCategory, Double> updated = copy();
        updated.put(category, clamp(value));
        overrides = Collections.unmodifiableMap(updated);
    }

    public static synchronized void reset(RateCategory category) {
        EnumMap<RateCategory, Double> updated = copy();
        updated.remove(category);
        overrides = Collections.unmodifiableMap(updated);
    }

    public static synchronized void resetAll() {
        overrides = Map.of();
    }

    public static synchronized void apply(Map<RateCategory, Double> values) {
        EnumMap<RateCategory, Double> updated = new EnumMap<>(RateCategory.class);
        values.forEach((category, value) -> updated.put(category, clamp(value)));
        overrides = Collections.unmodifiableMap(updated);
    }

    public static Map<RateCategory, Double> snapshot() {
        return overrides;
    }

    private static EnumMap<RateCategory, Double> copy() {
        EnumMap<RateCategory, Double> map = new EnumMap<>(RateCategory.class);
        map.putAll(overrides);
        return map;
    }

    private static double clamp(double value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }
}
