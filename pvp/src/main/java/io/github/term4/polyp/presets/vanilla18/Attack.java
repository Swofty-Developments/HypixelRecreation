package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.attack.AttackEvent;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.item.Enchants;
import io.github.term4.polyp.mechanics.attack.AttackConfig;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Knockback;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.tracking.SprintTracker;
import io.github.term4.polyp.tracking.motion.MotionTracker;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;

/** Vanilla 1.8 attack config + the legacy (pre-1.9) combat ruleset; also the {@code AttackConfigResolver} fallback. */
public final class Attack {

    private Attack() {}

    public static AttackConfig config() {
        return AttackConfig.builder()
                .enabled(true)
                .ruleset(ruleset())
                .criticalRule(AttackEvent.CriticalRule.vanilla())
                .build();
    }

    public static AttackEvent.AttackRule.Ruleset ruleset() {
        return LegacyAttack::new;
    }

    private record LegacyAttack(Services services) implements AttackEvent.AttackRule {
        @Override public void processAttack(AttackEvent event) {
            DamageSystem.DamageOutcome result = DamageSystem.DamageOutcome.FRESH_DAMAGE; // no damage system -> nothing absorbs the hit
            DamageSystem dmg = services.damage();
            if (dmg != null && event.target() != null) {
                DamageSnapshot snap = MeleeDamage.INSTANCE.snapshot(
                        event.attacker(), event.target(), event.critical(), event.item(), services);
                result = dmg.apply(snap);
                // vanilla onCriticalHit / onEnchantmentCritical, independent
                if (result == DamageSystem.DamageOutcome.FRESH_DAMAGE) {
                    boolean fake = event.finalSnap().aim() != null;
                    FxContext fx = FxContext.of(event.attacker(), event.target());
                    if (event.critical()) Fx.play(services, fake ? Fx.FAKE_CRIT : Fx.CRIT, fx);
                    if (MeleeDamage.enchantCritical(event.attacker(), event.target(), event.item(), services)) {
                        Fx.play(services, fake ? Fx.FAKE_MAGIC_CRIT : Fx.MAGIC_CRIT, fx);
                    }
                }
            }
            // weapon frozen for buffered hits; the sprint +1 is added in the calculator
            KnockbackSystem kb = services.knockback();
            if (result == DamageSystem.DamageOutcome.FRESH_DAMAGE && kb != null) {
                int extra = Enchants.level(event.item(), Knockback.KEY);
                Pos aim = event.finalSnap().aim(); // swing-filled hit: KB follows the intersecting ray, not the live look
                var kbSnap = new KnockbackSnapshot(event.target(), true, event.attacker(), null,
                        aim != null ? aim.direction() : null, null, extra);
                kb.apply(kbSnap);
            }
            if (result.landed() && event.attacker() instanceof LivingEntity le) {
                double scale = event.resolvedConfig().fullHitScale();
                if (scale != 1.0 && sprintingForKb(le)) MotionTracker.scaleHorizontalResidual(le, scale);
                if (le instanceof OptimizedPlayer op) {
                    op.suppressSelf(() -> op.setSprinting(false));
                } else {
                    le.setSprinting(false);
                }
            }
        }

        /** Vanilla's {@code i > 0} sprint-KB gate. */
        private boolean sprintingForKb(LivingEntity le) {
            if (!(le instanceof Player p)) return false;
            return services.sprintTracker() != null
                    ? SprintTracker.wasRecentlySprinting(services.sprintTracker(), p, 0)
                    : p.isSprinting();
        }
    }
}
