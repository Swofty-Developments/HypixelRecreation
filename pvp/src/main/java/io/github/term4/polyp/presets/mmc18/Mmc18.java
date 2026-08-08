package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.tracking.motion.VelocityConfig;
import io.github.term4.polyp.tracking.motion.VelocityRule;

/**
 * <b>MMC 1.8</b> preset - replicates MineMenClub's 1.8 PvP feel, built on the {@link Vanilla18} 1.8 baseline with the MMC
 * deltas in this package ({@link Attack} hit-queue, {@link Damage} silent overdamage, {@link Knockback} components,
 * {@link Explosion} two-impulse KB (melee hurt-KB + scaled push), {@link Projectiles} fireball).
 *
 * <p>Carries mechanics only - cross-version compat ({@code Compat18}) and fixes install separately.
 */
public final class Mmc18 {

    private Mmc18() {}

    public static MechanicsProfile profile() {
        return Vanilla18.profile().toBuilder()
                .set(MechanicsKeys.ATTACK, Attack.config())
                .set(MechanicsKeys.DAMAGE, Damage.config())
                .set(MechanicsKeys.KNOCKBACK, Knockback.melee())
                .set(MechanicsKeys.PLAYER, Player.config())
                // MineMen quirk (user-measured): a lag-frozen victim's motY holds until its next move packet
                .set(MechanicsKeys.VELOCITY, VelocityRule.simulated(
                        VelocityConfig.builder().motYOnMovePacket(true).build()))
                .set(MechanicsKeys.EXPLOSION, Explosion.fireballFight())
                .set(MechanicsKeys.ITEM_DAMAGE, Items.damage())
                .set(MechanicsKeys.PROJECTILES, Projectiles.config())
                // arrow hit-marker ding to the shooter; vanilla presets don't
                .set(MechanicsKeys.FX, Fx.vanilla18().register(Fx.ARROW_HIT_PLAYER, Fx.arrowHitMarker()))
                .build();
    }
}
