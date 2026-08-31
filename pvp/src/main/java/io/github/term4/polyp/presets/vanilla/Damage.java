package io.github.term4.polyp.presets.vanilla;

import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.breathing.BreathingConfig;
import io.github.term4.polyp.mechanics.damage.types.breathing.DrowningDamage;
import io.github.term4.polyp.mechanics.damage.types.breathing.SuffocationDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.BurningConfig;
import io.github.term4.polyp.mechanics.damage.types.burning.BurningDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.InFireDamage;
import io.github.term4.polyp.mechanics.damage.types.burning.LavaDamage;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDamageConfig;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamageConfig;
import io.github.term4.polyp.mechanics.damage.types.starvation.StarvationDamage;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;

/** Modern (26.1) damage config: the modern fall formula ({@code MODERN_FLOOR}) and 26.1 burning behaviour. */
public final class Damage {

    private Damage() {}

    public static DamageConfig config() {
        return DamageConfig.builder()
                .typeConfigs(
                        fallDamage(),
                        inFireDamage(),
                        lavaDamage(),
                        burningDamage(),
                        drownDamage(),
                        suffocationDamage(),
                        starvationDamage(),
                        playerAttackDamage()
                )
                .build();
    }

    /** Produced by the hunger food tick; starve is in BYPASSES_ARMOR and charges no exhaustion. */
    private static DamageTypeConfig starvationDamage() {
        return DamageTypeConfig.builder(StarvationDamage.KEY).baseAmount(1.0).bypassArmor(true).build();
    }

    private static MeleeDamageConfig playerAttackDamage() {
        return MeleeDamageConfig.builder().critMultiplier(1.5).exhaustion(0.1f).build();
    }

    /** Damages at air {@code <= -20}; modern air recovers {@code +4}/tick. */
    private static BreathingConfig drownDamage() {
        return BreathingConfig.builder()
                .key(DrowningDamage.KEY)
                .baseAmount(2.0)
                .airRefill(BreathingConfig.AirRefill.MODERN)
                .build();
    }

    /** Per tick while the head is in a solid block. */
    private static DamageTypeConfig suffocationDamage() {
        return DamageTypeConfig.builder(SuffocationDamage.KEY).baseAmount(1.0).build();
    }

    private static FallDamageConfig fallDamage() {
        return FallDamageConfig.builder()
                .formula(FallDamageConfig.Formula.MODERN_FLOOR)
                .bypassArmor(true) // 26 BYPASSES_ARMOR tag; only Feather Falling (EPF) reduces it
                // Jump Boost pushes +1/level onto SAFE_FALL_DISTANCE
                .threshold(ctx -> ctx.snap().target() instanceof LivingEntity le
                        ? le.getAttributeValue(Attribute.SAFE_FALL_DISTANCE) : 3.0)
                .damageModifier(1.0)
                .fallDamageMultiplier(1.0)
                .build();
    }

    private static BurningConfig inFireDamage() {
        return BurningConfig.builder()
                .key(InFireDamage.KEY)
                .baseAmount(1.0)
                .igniteTicks(160)
                .igniteWarmupInvulMult(3)
                .contactIntervalTicks(1)
                .exhaustion(0.1f) // DamageTypes registry; on_fire (burn ticks) is 0.0
                .build();
    }

    private static BurningConfig lavaDamage() {
        return BurningConfig.builder()
                .key(LavaDamage.KEY)
                .baseAmount(4.0)
                .igniteTicks(300)
                .igniteWarmupInvulMult(3)
                .contactIntervalTicks(1)
                .exhaustion(0.1f)
                .build();
    }

    private static BurningConfig burningDamage() {
        return BurningConfig.builder()
                .key(BurningDamage.KEY)
                .baseAmount(1.0)
                .intervalTicks(20)
                .skipBurnWhileInLava(true)
                .bypassArmor(true) // 26 BYPASSES_ARMOR tag; in_fire/lava aren't
                .build();
    }
}
