package io.github.term4.polyp.mechanics.blocking;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Resolves a {@link BlockingConfig} into plain values against a {@link BlockingContext}: the per-material entry over
 * {@link BlockingConfig#defaults()}, then each knob.
 */
public final class BlockingConfigResolver {

    private BlockingConfigResolver() {}

    /**
     * What the per-item {@code FieldValue}s and the {@link BlockingBehavior} resolve against. The incoming hit rides
     * along as a {@link DamageContext}, so a behavior reads type/attacker/{@code bypassArmor} through the damage
     * domain's own resolution.
     */
    public record BlockingContext(Player defender, ItemStack item, PlayerHand hand, DamageContext damage,
                                  @Nullable Services services) {

        public DamageType type() { return damage.snap().type(); }

        public @Nullable Entity attacker() { return damage.snap().source(); }

        /** Whether the incoming damage bypasses armor (vanilla {@code ignoresArmor}) - the 1.8 sword's blockable test. */
        public boolean bypassesArmor() { return damage.typeConfig().bypassArmor(damage); }

        /** Ticks the defender has been using (holding) the blocking item - the shield block-delay reads this. */
        public int useTicks() { return (int) defender.getCurrentItemUseTime(); }

        /**
         * Horizontal angle (degrees) between the defender's facing and the direction to the attacker; {@code 0} = dead
         * ahead, {@code 180} = directly behind. {@code 0} when there's no positioned attacker (treated as blockable).
         */
        public double attackerAngleDegrees() {
            Entity atk = attacker();
            if (atk == null) return 0.0;
            Pos pos = defender.getPosition();
            Vec look = pos.direction();
            double dx = atk.getPosition().x() - pos.x();
            double dz = atk.getPosition().z() - pos.z();
            double toLen = Math.sqrt(dx * dx + dz * dz);
            double lookLen = Math.sqrt(look.x() * look.x() + look.z() * look.z());
            if (toLen < 1.0e-6 || lookLen < 1.0e-6) return 0.0;
            double dot = (look.x() * dx + look.z() * dz) / (toLen * lookLen);
            return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
        }

        /**
         * Effective per-item config: the active config's entry for this item's material layered over its generic
         * {@link BlockingConfig#defaults()}, plus any {@code subConfig} overlay. Only called for blockable materials.
         */
        public BlockingTypeConfig typeConfig(@Nullable BlockingConfig cfg) {
            return Config.layer(BlockingTypeConfig.builder().build(),
                    cfg != null ? cfg.defaults() : null,
                    cfg != null ? cfg.typeConfig(item.material()) : null,
                    this);
        }
    }

    public static ResolvedBlocking resolve(@Nullable BlockingConfig cfg, BlockingContext ctx) {
        BlockingTypeConfig tc = ctx.typeConfig(cfg);
        return new ResolvedBlocking(
                FieldValue.resolve(tc.enabled, ctx, Boolean.TRUE),
                FieldValue.resolve(tc.behavior, ctx, BlockingBehavior.SWORD),
                FieldValue.resolve(tc.reductionBase, ctx, 0.0),
                FieldValue.resolve(tc.reductionFactor, ctx, 1.0),
                FieldValue.resolve(tc.blockDelayTicks, ctx, 0),
                FieldValue.resolve(tc.blockingAngle, ctx),
                FieldValue.resolve(tc.bypassedTypes, ctx, Set.of()));
    }

    public record ResolvedBlocking(boolean enabled, BlockingBehavior behavior, double reductionBase, double reductionFactor,
                                   int blockDelayTicks, @Nullable Double blockingAngle, Set<Key> bypassedTypes) {}
}
