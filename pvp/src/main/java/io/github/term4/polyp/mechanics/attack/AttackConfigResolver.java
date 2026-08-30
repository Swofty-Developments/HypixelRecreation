package io.github.term4.polyp.mechanics.attack;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.presets.vanilla18.Attack;
import io.github.term4.polyp.api.event.attack.AttackEvent;
import io.github.term4.polyp.config.FieldValue;
import org.jetbrains.annotations.Nullable;

/** Resolves AttackConfig with context into plain values. */
public final class AttackConfigResolver {

    private AttackConfigResolver() {}

    public record AttackContext(AttackSnapshot snap, Services services) {
        public static AttackContext of(AttackSnapshot snap, Services services) {
            return new AttackContext(snap, services);
        }
    }

    public static ResolvedAttackConfig resolve(AttackConfig config, AttackContext ctx) {
        if (config == null) return ResolvedAttackConfig.defaults();

        AttackConfig cfg = config.withOverlay(ctx);

        // no attack-level invul/buffering: the damage/knockback systems gate themselves
        return new ResolvedAttackConfig(
                FieldValue.resolve(cfg.enabled, ctx, true),
                FieldValue.resolve(cfg.ruleset, ctx, Attack.ruleset()),
                cfg.criticalRule != null ? cfg.criticalRule : AttackEvent.CriticalRule.DEFAULT,
                FieldValue.resolve(cfg.fullHitScale, ctx, AttackConfig.VANILLA_FULL_HIT_SCALE)
        );
    }

    public record ResolvedAttackConfig(
            boolean enabled,
            @Nullable AttackEvent.AttackRule.Ruleset ruleset,
            @Nullable AttackEvent.CriticalRule criticalRule,
            double fullHitScale
    ) {
        public static ResolvedAttackConfig defaults() {
            return new ResolvedAttackConfig(
                    true,
                    Attack.ruleset(),
                    AttackEvent.CriticalRule.DEFAULT,
                    AttackConfig.VANILLA_FULL_HIT_SCALE
            );
        }
    }
}
