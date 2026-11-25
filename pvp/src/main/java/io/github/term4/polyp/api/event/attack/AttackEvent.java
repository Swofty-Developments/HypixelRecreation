package io.github.term4.polyp.api.event.attack;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.attack.AttackConfig;
import io.github.term4.polyp.mechanics.attack.AttackConfigResolver;
import io.github.term4.polyp.mechanics.attack.AttackSnapshot;
import io.github.term4.polyp.tracking.motion.MotionTracker;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

/**
 * The main attack phase: fired when a hit is detected and its config resolved, before the ruleset runs. Bracketed by
 * {@link PreAttackEvent} (raw detection) and {@link AttackAppliedEvent} (after the ruleset).
 */
// TODO: allow mobs/other entities, or manual firing
public final class AttackEvent extends CancellableMechanicsEvent<AttackSnapshot> {

    private boolean process = true;
    private @Nullable AttackRule.Ruleset ruleset;

    private @Nullable Boolean overrideCritical;
    private @Nullable ItemStack overrideItem;

    public AttackEvent(AttackSnapshot snapshot, Services services) {
        super(snapshot, services);
    }

    /** {@code null} = defaults. */
    public @Nullable AttackConfig config() { return finalSnap().config(); }
    public void config(AttackConfig config) { finalSnap(finalSnap().withConfig(config)); }

    /** Re-resolved from {@link #finalSnap()} each call, so it reflects listener changes. */
    public AttackConfigResolver.ResolvedAttackConfig resolvedConfig() {
        AttackSnapshot s = finalSnap();
        return s.config() != null
                ? AttackConfigResolver.resolve(s.config(), AttackConfigResolver.AttackContext.of(s, services()))
                : AttackConfigResolver.ResolvedAttackConfig.defaults();
    }

    /** Whether the attack proceeds to the ruleset ({@code false} = detected but not processed). */
    public boolean process() { return process; }
    public void process(boolean process) { this.process = process; }

    /** Per-hit ruleset override ({@code null} = the config's). */
    public @Nullable AttackRule.Ruleset processor() { return ruleset; }
    public void processor(AttackRule.Ruleset ruleset) { this.ruleset = ruleset; }

    /** Per-hit crit override ({@code null} = let the {@link CriticalRule} decide). */
    public @Nullable Boolean overrideCritical() { return overrideCritical; }
    public void overrideCritical(@Nullable Boolean b) { this.overrideCritical = b; }

    /** Per-hit item override ({@code null} = the attacker's main hand). */
    public @Nullable ItemStack overrideItem() { return overrideItem; }
    public void overrideItem(@Nullable ItemStack item) { this.overrideItem = item; }

    public Entity attacker() { return finalSnap().attacker(); }
    public @Nullable Entity target() { return finalSnap().target(); }
    /** The attacker's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(attacker()); }

    /** Off the ground and descending. */
    public boolean attackerFalling() {
        return MotionTracker.isFalling(attacker());
    }

    public boolean attackerFlying() {
        return attacker() instanceof Player p && p.isFlying();
    }

    /** Vanilla blindness suppresses crits. */
    public boolean attackerBlind() {
        return attacker().getEffectLevel(PotionEffect.BLINDNESS) >= 0;
    }

    /** Whether this is a crit (per {@link CriticalRule}; {@link #overrideCritical(Boolean)} wins). */
    public boolean critical() {
        if (overrideCritical != null) return overrideCritical;
        CriticalRule rule = resolvedConfig().criticalRule();
        return rule != null && rule.isCritical(this);
    }

    /** The attacking item: {@link #overrideItem(ItemStack) override} else the attacker's main hand. */
    public @Nullable ItemStack item() {
        if (overrideItem != null) return overrideItem;
        return attacker() instanceof LivingEntity le ? le.getItemInMainHand() : ItemStack.AIR;
    }

    /**
     * Decides whether a hit is critical (the melee type applies the multiplier). Must not call
     * {@link AttackEvent#critical()} - infinite recursion.
     */
    @FunctionalInterface
    public interface CriticalRule {

        /** Used when a config sets none. */
        CriticalRule DEFAULT = vanilla();

        boolean isCritical(AttackEvent event);

        /** Vanilla: falling (or flying) and not blinded. */
        static CriticalRule vanilla() { return e -> (e.attackerFalling() || e.attackerFlying()) && !e.attackerBlind(); }

        static CriticalRule never() { return e -> false; }

        static CriticalRule always() { return e -> true; }
    }

    /** Applies the combat ruleset (damage, knockback, ...) to a detected hit. */
    @FunctionalInterface
    public interface AttackRule {

        /** Target may be null for swing/raytraced emulated attacks. */
        void processAttack(AttackEvent event);

        @FunctionalInterface
        interface Ruleset {
            AttackRule create(Services services);
        }
    }
}
