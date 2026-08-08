package io.github.term4.polyp.mechanics.damage.types.burning;

import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.EnvironmentalDamageTicker;
import io.github.term4.polyp.mechanics.damage.EnvironmentalTickProducer;
import io.github.term4.polyp.util.BlockContact;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared producer for the burning family: one per-tick bounding-box pass detects fire/lava/water contact, then
 * extinguishes wet entities, steps the lava/in-fire ignite warmup + emits contact damage on its interval, and emits
 * {@code on_fire} damage while fire ticks remain.
 */
final class BurningTicker implements EnvironmentalTickProducer {

    static final BurningTicker INSTANCE = new BurningTicker();
    /** Consecutive ticks standing in the current fire/lava contact (reset on leave). */
    private static final Tag<Integer> CONTACT_TICKS = Tag.Transient("polyp:burn-contact-ticks");
    private static final int DEFAULT_BURN_INTERVAL = 20;
    private static final int DEFAULT_CONTACT_INTERVAL = 1;

    private final Set<Key> active = ConcurrentHashMap.newKeySet();
    private boolean registered;

    private BurningTicker() {}

    static void activate(Key key, DamageSystem system) {
        INSTANCE.add(key, system);
    }

    static void deactivate(Key key) {
        INSTANCE.remove(key);
    }

    private synchronized void add(Key key, DamageSystem system) {
        EnvironmentalDamageTicker.instance().bind(system);
        active.add(key);
        if (!registered) {
            EnvironmentalDamageTicker.instance().register(this);
            registered = true;
        }
    }

    private synchronized void remove(Key key) {
        active.remove(key);
        if (active.isEmpty() && registered) {
            EnvironmentalDamageTicker.instance().unregister(this);
            registered = false;
        }
    }

    @Override
    public void tick(LivingEntity living, DamageSystem sys) {
        boolean[] contact = new boolean[3]; // water, lava, fire
        BlockContact.scan(living, block -> {
            if (block.compare(Block.WATER)) contact[0] = true;
            else if (block.compare(Block.LAVA)) contact[1] = true;
            else if (block.compare(Block.FIRE) || block.compare(Block.SOUL_FIRE)) contact[2] = true;
            return contact[0] && contact[1] && contact[2];
        });
        boolean wet = contact[0]; // water contact only: vanilla's isWet also counts rain, which nothing here models
        boolean inHazard = contact[1] || contact[2];

        if (wet && living.getFireTicks() > 0) {
            living.setFireTicks(0);
            // viewers only, like 1.8's makeSound: the doused player hears nothing (their client forces fireTicks 0)
            Fx.play(sys.services(), Fx.FIRE_EXTINGUISH, FxContext.of(living));
        }

        if (!inHazard) {
            living.removeTag(CONTACT_TICKS);
        } else {
            Integer prev = living.getTag(CONTACT_TICKS);
            int contactTicks = (prev != null ? prev : 0) + 1;
            living.setTag(CONTACT_TICKS, contactTicks);
            if (contact[1] && active.contains(LavaDamage.KEY)) {
                contact(sys, living, LavaDamage.INSTANCE, wet, contactTicks);
            } else if (contact[2] && active.contains(InFireDamage.KEY)) {
                contact(sys, living, InFireDamage.INSTANCE, wet, contactTicks);
            }
        }

        if (active.contains(BurningDamage.KEY)) burnTick(sys, living, contact[1]);
    }

    private static void contact(DamageSystem sys, LivingEntity living, DamageType type, boolean wet, int contactTicks) {
        DamageSnapshot snap = DamageSnapshot.of(living, type);
        DamageContext ctx = sys.contextFor(snap);
        DamageTypeConfig cfg = ctx.typeConfig();
        if (!cfg.enabled(ctx) || !(cfg instanceof BurningConfig bc)) return;

        if (!wet) {
            Integer ignite = bc.igniteTicks(ctx);
            if (ignite != null && ignite > 0) {
                int invul = resolvedInvul(sys, ctx, bc);
                int warmup = TickScaler.duration(living, bc.resolveIgniteWarmup(ctx, invul), DamageSystem.KEY);
                boolean pin = living.getFireTicks() > 0 || contactTicks >= warmup;
                // every vanilla ignition routes through setOnFire, so Fire Protection cuts this one too
                if (pin) Ignite.ignite(living, ignite, DamageSystem.KEY);
            }
        }

        int interval = TickScaler.duration(living, interval(bc.contactIntervalTicks(ctx), DEFAULT_CONTACT_INTERVAL), DamageSystem.KEY);
        if ((contactTicks - 1) % interval != 0) return;

        if (DamageSystem.absorbedByWindow(living, ctx.baseAmount())) return;
        sys.apply(snap);
    }

    private static void burnTick(DamageSystem sys, LivingEntity living, boolean inLava) {
        int fireTicks = living.getFireTicks();
        if (fireTicks <= 0) return;

        DamageSnapshot snap = DamageSnapshot.of(living, BurningDamage.INSTANCE);
        DamageContext ctx = sys.contextFor(snap);
        DamageTypeConfig cfg = ctx.typeConfig();
        if (!cfg.enabled(ctx)) return;

        if (cfg instanceof BurningConfig bc && Boolean.TRUE.equals(bc.skipBurnWhileInLava(ctx)) && inLava) {
            return;
        }

        int interval = TickScaler.duration(living, cfg instanceof BurningConfig bc
                ? interval(bc.intervalTicks(ctx), DEFAULT_BURN_INTERVAL)
                : DEFAULT_BURN_INTERVAL, DamageSystem.KEY);
        if (fireTicks % interval != 0) return;

        if (DamageSystem.absorbedByWindow(living, ctx.baseAmount())) return;
        sys.apply(snap);
    }

    private static int interval(@Nullable Integer ticks, int fallback) {
        return ticks != null && ticks > 0 ? ticks : fallback;
    }

    private static int resolvedInvul(DamageSystem sys, DamageContext ctx, BurningConfig bc) {
        Integer typeInvul = bc.invulTicks(ctx);
        if (typeInvul != null) return typeInvul;
        DamageConfig cfg = ctx.snap().config();
        if (cfg == null) cfg = sys.config();
        Integer global = DamageConfigResolver.resolve(cfg, ctx).invulTicks();
        return global != null ? global : DamageSystem.DEFAULT_INVUL_TICKS;
    }
}
