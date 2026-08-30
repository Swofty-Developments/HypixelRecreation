package io.github.term4.polyp.mechanics.damage.types.magic;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

/**
 * Instant potion effects (vanilla {@code applyInstantEffect}, identical 1.8-26): healing restores
 * {@code (int)(intensity * (4 << amp) + 0.5)} health, harming deals {@code (int)(intensity * (6 << amp) + 0.5)} as
 * {@link MagicDamage}. {@code intensity} is {@code 1.0} drunk, the distance falloff splashed. The undead inversion is
 * out of scope (player-scoped lib; test mobs stand in for players).
 */
public final class HealOrHarm {

    private HealOrHarm() {}

    /** Applies {@code effect} instantly if it's an instant one (healing/harming); {@code false} = not instant, add it timed. */
    public static boolean apply(@Nullable Services services, LivingEntity target, @Nullable Entity source,
                                @Nullable Point at, CustomPotionEffect effect, double intensity) {
        if (effect.id() == PotionEffect.INSTANT_HEALTH) {
            target.setHealth(target.getHealth() + (int) (intensity * (4 << effect.amplifier()) + 0.5));
            return true;
        }
        if (effect.id() == PotionEffect.INSTANT_DAMAGE) {
            float amount = (int) (intensity * (6 << effect.amplifier()) + 0.5);
            if (services != null && services.damage() != null) {
                services.damage().apply(DamageSnapshot.of(target, MagicDamage.INSTANCE)
                        .withSource(source).withPoint(at).withAmount(amount));
            }
            return true;
        }
        return false;
    }
}
