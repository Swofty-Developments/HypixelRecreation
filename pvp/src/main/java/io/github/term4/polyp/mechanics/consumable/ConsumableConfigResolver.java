package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.consumable.ConsumableTypeConfig.ParticleVisibility;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves a {@link ConsumableConfig} + {@link Consumable} type into plain values against a {@link ConsumableContext}:
 * per-type override -&gt; {@link ConsumableConfig#defaults()} -&gt; the type's {@code defaultConfig()} -&gt; hard
 * fallbacks, then resolve each knob.
 */
public final class ConsumableConfigResolver {

    private ConsumableConfigResolver() {}

    /**
     * What the per-consumable {@code FieldValue}s resolve against, and what the {@link ConsumableBehavior} hooks
     * receive; {@link Services} lets a behavior reach hunger / attributes / etc.
     */
    public record ConsumableContext(Player user, ItemStack item, PlayerHand hand, Consumable consumable, @Nullable Services services) {

        /**
         * Effective per-consumable config, layered highest-first: {@code cfg}'s per-type override -&gt; its generic
         * {@link ConsumableConfig#defaults()} -&gt; the type's {@code defaultConfig()}, plus any {@code subConfig} overlay.
         */
        public ConsumableTypeConfig typeConfig(@Nullable ConsumableConfig cfg) {
            return Config.layer(consumable.defaultConfig(),
                    cfg != null ? cfg.defaults() : null,
                    cfg != null ? cfg.typeConfig(consumable.key()) : null,
                    this);
        }

        /**
         * The resolved {@code particles} knob, default {@link ParticleVisibility#SHOWN}. Resolve once per behavior -
         * it re-reads the config.
         */
        public ParticleVisibility particles() {
            ConsumableSystem sys = services != null ? services.consumables() : null;
            ConsumableTypeConfig tc = typeConfig(sys != null ? sys.configFor(user) : null);
            return FieldValue.resolve(tc.particles, this, ParticleVisibility.SHOWN);
        }
    }

    public static ResolvedConsumable resolve(@Nullable ConsumableConfig cfg, ConsumableContext ctx) {
        ConsumableTypeConfig tc = ctx.typeConfig(cfg);
        return new ResolvedConsumable(
                FieldValue.resolve(tc.enabled, ctx, Boolean.TRUE),
                FieldValue.resolve(tc.canConsume, ctx, Boolean.TRUE),
                // scaled HERE so the arm, the remaining countdown and the sound cadence all speak server ticks
                TickScaler.duration(ctx.user(), FieldValue.resolve(tc.consumeTicks, ctx, Consumable.VANILLA_CONSUME_TICKS),
                        ConsumableSystem.KEY),
                FieldValue.resolve(tc.behavior, ctx, ConsumableBehavior.NONE));
    }

    public record ResolvedConsumable(boolean enabled, boolean canConsume, int consumeTicks, ConsumableBehavior behavior) {}
}
