package io.github.term4.polyp.mechanics.damage.types.melee;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.item.ItemRegistry;
import io.github.term4.polyp.item.ItemStat;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Config for {@link MeleeDamage}. Adds the melee-only {@code critMultiplier} on top of the common
 * {@link DamageTypeConfig} tunables; like them it is a {@link FieldValue} (constant or per-hit lambda).
 */
@GenerateBuilder
public final class MeleeDamageConfig extends DamageTypeConfig {

    /** Bare-hand melee damage (the 1.8 base attack-damage attribute); the fallback for fist/unlisted items. */
    public static final double FIST_DAMAGE = 1.0;

    public final @Nullable FieldValue<DamageContext, Double> critMultiplier;

    private MeleeDamageConfig(Builder b) {
        super(b.common);
        this.critMultiplier = b.critMultiplier;
    }

    /** Damage multiplier applied on a critical melee hit (vanilla 1.8 = 1.5x), or {@code null} (treated as 1x). */
    public @Nullable Double critMultiplier(DamageContext ctx) { return resolve(critMultiplier, ctx); }

    /**
     * Default melee {@code baseAmount}: the held item's {@code ATTACK_DAMAGE} from the holder's resolved
     * {@link ItemRegistry} ({@code MechanicsProfile.items}, version per preset), falling back to {@link #FIST_DAMAGE}
     * for fist/unlisted items or when no registry is set.
     */
    static double weaponBaseAmount(DamageContext ctx) {
        ItemStack item = ctx.item();
        LivingEntity holder = ctx.snap().source() instanceof LivingEntity le ? le : null;
        if ((item == null || item.isAir()) && holder != null) item = holder.getItemInMainHand();
        ItemRegistry items = ctx.services() != null ? ctx.services().profiles().resolve(holder, MechanicsKeys.ITEMS) : null;
        return items != null ? items.value(item, holder, ItemStat.ATTACK_DAMAGE, FIST_DAMAGE) : FIST_DAMAGE;
    }

    @Override
    public DamageTypeConfig fromBase(DamageTypeConfig base) {
        DamageTypeConfig mergedCommon = super.fromBase(base);
        Builder b = new Builder();
        b.common.copyFrom(mergedCommon);
        if (base instanceof MeleeDamageConfig p) b.mergeKnobs(this, p);
        else b.copyKnobs(this);
        return b.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends MeleeDamageConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        // invul/overdamage stay unset to inherit the global config
        private final DamageTypeConfig.Builder common = new DamageTypeConfig.Builder().key(MeleeDamage.KEY)
                .baseAmount(MeleeDamageConfig::weaponBaseAmount);

        public Builder enabled(Boolean v) { common.enabled(v); return this; }
        public Builder enabled(Function<DamageContext, Boolean> fn) { common.enabled(fn); return this; }
        public Builder baseAmount(Double v) { common.baseAmount(v); return this; }
        public Builder baseAmount(Function<DamageContext, Double> fn) { common.baseAmount(fn); return this; }
        public Builder baseAmount(Double fallback, Function<DamageContext, Double> fn) { common.baseAmount(fallback, fn); return this; }
        public Builder invulTicks(Integer v) { common.invulTicks(v); return this; }
        public Builder invulTicks(Function<DamageContext, Integer> fn) { common.invulTicks(fn); return this; }
        public Builder invulTicks(Integer fallback, Function<DamageContext, Integer> fn) { common.invulTicks(fallback, fn); return this; }
        public Builder overdamage(Boolean v) { common.overdamage(v); return this; }
        public Builder overdamage(Function<DamageContext, Boolean> fn) { common.overdamage(fn); return this; }
        public Builder overdamage(Boolean fallback, Function<DamageContext, Boolean> fn) { common.overdamage(fallback, fn); return this; }
        public Builder silent(Boolean v) { common.silent(v); return this; }
        public Builder silent(Function<DamageContext, Boolean> fn) { common.silent(fn); return this; }
        public Builder silent(Boolean fallback, Function<DamageContext, Boolean> fn) { common.silent(fallback, fn); return this; }
        public Builder overdamageSilent(Boolean v) { common.overdamageSilent(v); return this; }
        public Builder overdamageSilent(Function<DamageContext, Boolean> fn) { common.overdamageSilent(fn); return this; }
        public Builder overdamageSilent(Boolean fallback, Function<DamageContext, Boolean> fn) { common.overdamageSilent(fallback, fn); return this; }
        public Builder triggersInvul(Boolean v) { common.triggersInvul(v); return this; }
        public Builder triggersInvul(Function<DamageContext, Boolean> fn) { common.triggersInvul(fn); return this; }
        public Builder bypassInvul(Boolean v) { common.bypassInvul(v); return this; }
        public Builder bypassInvul(Function<DamageContext, Boolean> fn) { common.bypassInvul(fn); return this; }
        public Builder bypassImmune(Boolean v) { common.bypassImmune(v); return this; }
        public Builder bypassImmune(Function<DamageContext, Boolean> fn) { common.bypassImmune(fn); return this; }
        public Builder ownsVelocityBroadcast(Boolean v) { common.ownsVelocityBroadcast(v); return this; }
        public Builder ownsVelocityBroadcast(Function<DamageContext, Boolean> fn) { common.ownsVelocityBroadcast(fn); return this; }
        public Builder exhaustion(Float v) { common.exhaustion(v); return this; }
        public Builder exhaustion(Function<DamageContext, Float> fn) { common.exhaustion(fn); return this; }
        public Builder subConfig(Function<DamageContext, DamageTypeConfig> fn) { common.subConfig(fn); return this; }


        public MeleeDamageConfig build() { return new MeleeDamageConfig(this); }
    }
}
