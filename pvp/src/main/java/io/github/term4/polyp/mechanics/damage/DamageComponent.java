package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.api.event.damage.DamageEvent;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import org.jetbrains.annotations.Nullable;

/**
 * A pluggable damage transform applied after the {@link DamageEvent} fires. Each component runs in order and may
 * return a replacement amount; {@code null} leaves it unchanged. Self-gates from the {@link DamageContext}/{@link DamageEvent}.
 */
@FunctionalInterface
public interface DamageComponent {

    /**
     * @param overdamage {@code true} when transforming the invul-window replacement delta;
     *                   {@code false} for a fresh hit
     * @return replacement amount, or {@code null} to leave unchanged
     */
    @Nullable Float apply(DamageContext ctx, DamageEvent event, float amount, boolean overdamage);
}
