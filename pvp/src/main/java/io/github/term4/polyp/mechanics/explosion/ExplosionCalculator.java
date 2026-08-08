package io.github.term4.polyp.mechanics.explosion;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Pure per-entity explosion math (1.8/26.1 differ only in {@code damageConstant}: 8.0 floored vs 7.0). Returns the falloff push + damage. */
public final class ExplosionCalculator {

    private ExplosionCalculator() {}

    /** {@code knockback} = the falloff push (straight up at point-blank); {@code null} only outside the radius. */
    public record Hit(@Nullable Vec knockback, float damage) {}

    /** Vanilla effect on one entity, or {@code null} if outside the {@code 2·power} radius. */
    public static @Nullable Hit compute(@NotNull Point center, float power, @NotNull Point eyeOrigin, double distance,
                                        float exposure, double damageConstant, boolean floorDamage, double knockbackMultiplier) {
        final double doubleRadius = power * 2.0;
        if (doubleRadius <= 0.0) return null;
        final double dist = normalizedDistance(distance, doubleRadius);
        if (dist > 1.0) return null;

        final double impact = (1.0 - dist) * exposure;
        final double raw = (impact * impact + impact) / 2.0 * damageConstant * doubleRadius + 1.0;
        final float damage = floorDamage ? (int) raw : (float) raw;

        final double dx = eyeOrigin.x() - center.x();
        final double dy = eyeOrigin.y() - center.y();
        final double dz = eyeOrigin.z() - center.z();
        final double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        // point-blank (eye AT center = no direction): push straight UP, not nowhere (Hypixel; vanilla skips)
        final Vec dir = len < 1.0e-7 ? new Vec(0, 1, 0) : new Vec(dx / len, dy / len, dz / len);
        return new Hit(dir.mul(impact * knockbackMultiplier), damage);
    }

    /** The falloff impact {@code (1 − dist/2power)·exposure} (0 outside the radius): the KB magnitude, and what Hypixel gates on. */
    public static double impact(double distance, float power, float exposure) {
        final double doubleRadius = power * 2.0;
        if (doubleRadius <= 0.0) return 0.0;
        final double dist = normalizedDistance(distance, doubleRadius);
        return dist > 1.0 ? 0.0 : (1.0 - dist) * exposure;
    }

    // micro-block rounding: world-absolute coords leave sub-ULP noise at high |Y| that flips on-grid KB by a wire unit between heights (1.8 is height-independent)
    private static double normalizedDistance(double distance, double doubleRadius) {
        return Math.rint(distance * 1.0e6) / 1.0e6 / doubleRadius;
    }
}
