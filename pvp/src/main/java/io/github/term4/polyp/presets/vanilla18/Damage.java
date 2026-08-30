package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.breathing.BreathingConfig;
import io.github.term4.polyp.mechanics.damage.types.breathing.DrowningDamage;
import io.github.term4.polyp.mechanics.damage.types.breathing.SuffocationDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.BurningConfig;
import io.github.term4.polyp.mechanics.damage.types.burning.BurningDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.InFireDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.LavaDamage;
import io.github.term4.polyp.mechanics.damage.types.cactus.CactusDamage;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDamageConfig;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamageConfig;
import io.github.term4.polyp.mechanics.damage.types.starvation.StarvationDamage;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

/** Vanilla 1.8 damage config; also the {@code DamageSystem} fallback. */
public final class Damage {

    private Damage() {}

    public static DamageConfig config() {
        return DamageConfig.builder()
                .invulTicks(10)
                .enableOverdamage(true)
                .syncHurtVelocity(true)
                .hurtKnockback(Knockback.hurt())
                .typeConfigs(
                        fallDamage(),
                        inFireDamage(),
                        lavaDamage(),
                        burningDamage(),
                        cactusDamage(),
                        drownDamage(),
                        suffocationDamage(),
                        starvationDamage(),
                        playerAttackDamage()
                )
                .build();
    }

    /** Damages at air {@code <= -20}; 1.8 air snaps back to max instantly out of water. */
    private static BreathingConfig drownDamage() {
        return BreathingConfig.builder()
                .key(DrowningDamage.KEY)
                .baseAmount(2.0)
                .airRefill(BreathingConfig.AirRefill.LEGACY)
                .bypassArmor(true) // 1.8 DROWN.setIgnoreArmor; also makes it unblockable
                .build();
    }

    /** Per tick while the head is in a solid block. */
    private static DamageTypeConfig suffocationDamage() {
        // 1.8 STUCK.setIgnoreArmor; also makes it unblockable
        return DamageTypeConfig.builder(SuffocationDamage.KEY).baseAmount(1.0).bypassArmor(true).build();
    }

    private static FallDamageConfig fallDamage() {
        return FallDamageConfig.builder()
                .formula(FallDamageConfig.Formula.LEGACY_CEIL)
                .bypassArmor(true) // 1.8 DamageSource.FALL ignores armor; only Feather Falling (EPF) reduces it. Also unblockable.
                // 1.8 has no SAFE_FALL_DISTANCE attribute: Jump Boost subtracts (amp+1) blocks directly (EntityLiving.e)
                .threshold(ctx -> {
                    if (ctx.snap().target() instanceof LivingEntity le) {
                        int amp = le.getEffectLevel(PotionEffect.JUMP_BOOST);
                        if (amp >= 0) return 3.0 + (amp + 1);
                    }
                    return 3.0;
                })
                .build();
    }

    private static BurningConfig inFireDamage() {
        return BurningConfig.builder()
                .key(InFireDamage.KEY)
                .baseAmount(1.0)
                .igniteTicks(160)
                .igniteWarmupInvulMult(2)
                .contactIntervalTicks(1)
                .exhaustion(0.3f) // on_fire (burn ticks) is free
                .build();
    }

    private static BurningConfig lavaDamage() {
        return BurningConfig.builder()
                .key(LavaDamage.KEY)
                .baseAmount(4.0)
                .igniteTicks(300)
                .igniteWarmupInvulMult(2)
                .contactIntervalTicks(1)
                .exhaustion(0.3f)
                .build();
    }

    private static BurningConfig burningDamage() {
        return BurningConfig.builder()
                .key(BurningDamage.KEY)
                .baseAmount(1.0)
                .intervalTicks(20)
                .bypassArmor(true) // 1.8 BURN.setIgnoreArmor; in_fire/lava don't. Also unblockable.
                .build();
    }

    private static DamageTypeConfig cactusDamage() {
        return DamageTypeConfig.builder(CactusDamage.KEY).baseAmount(1.0).exhaustion(0.3f).build();
    }

    /** Produced by the hunger food tick; STARVE ignores armor and charges no exhaustion. */
    private static DamageTypeConfig starvationDamage() {
        return DamageTypeConfig.builder(StarvationDamage.KEY).baseAmount(1.0).bypassArmor(true).build();
    }

    private static MeleeDamageConfig playerAttackDamage() {
        return MeleeDamageConfig.builder().critMultiplier(1.5).exhaustion(0.3f).build();
    }
}
