package io.github.term4.polyp.mechanics.damage.types.breathing;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.item.Enchants;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.EnvironmentalDamageTicker;
import io.github.term4.polyp.mechanics.damage.EnvironmentalTickProducer;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import io.github.term4.polyp.mechanics.damage.types.breathing.BreathingConfig.AirRefill;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.tag.Tag;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Drowning ({@code minecraft:drown}). Vanilla 1.8 ({@code EntityLiving}) and 26 ({@code LivingEntity}) are identical: while
 * the head block is water and the entity can't breathe (no Water Breathing, not invulnerable - creative/spectator are
 * already excluded by the {@link EnvironmentalDamageTicker}), air drains by 1/tick; Respiration on armor gives a
 * {@code level/(level+1)} chance to skip each drain (1.8 {@code getOxygenEnchantmentLevel} = max across armor; 26's
 * {@code OXYGEN_BONUS} attribute carries the same number). At air {@code <= -20} it deals {@code baseAmount} (2.0) and
 * resets air to 0. Out of water it refills - the one version difference ({@link AirRefill}): 1.8 snaps to max instantly
 * (out of water only), 26 recovers {@code +4}/tick (in water too while breathing). Air is a per-entity tag.
 */
public final class DrowningDamage extends DamageType implements EnvironmentalTickProducer {

    public static final Key KEY = Key.key("minecraft:drown");
    public static final DrowningDamage INSTANCE = new DrowningDamage();

    private static final int MAX_AIR = 300;
    private static final int DROWN_AT = -20;
    private static final Key RESPIRATION = Key.key("minecraft:respiration");
    private static final Tag<Integer> AIR = Tag.Transient("polyp:air");
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS};

    private boolean registered;

    private DrowningDamage() {
        super(KEY, "Drowning", VanillaTypes.DROWN, BreathingConfig.builder()
                .key(KEY).baseAmount(2.0).maxAir(MAX_AIR).airRefill(AirRefill.MODERN).build());
    }

    @Override
    public void enable(DamageSystem system, Polyp polyp) {
        EnvironmentalDamageTicker.instance().bind(system);
        if (!registered) {
            EnvironmentalDamageTicker.instance().register(this);
            registered = true;
        }
    }

    @Override
    public void disable() {
        if (registered) {
            EnvironmentalDamageTicker.instance().unregister(this);
            registered = false;
        }
    }

    /** Clears the tracked air so it defaults back to full next tick, and refills the HUD bubbles. */
    public static void resetAir(LivingEntity entity) {
        entity.removeTag(AIR);
        entity.getEntityMeta().setAirTicks(MAX_AIR);
    }

    /** Mirrors the tracked air to {@code AIR_TICKS} metadata - Minestom never ticks air, so without this the bubbles never move (same-value writes are deduped there). */
    private static void setAir(LivingEntity living, int air) {
        living.setTag(AIR, air);
        living.getEntityMeta().setAirTicks(air);
    }

    @Override
    public void tick(LivingEntity living, DamageSystem sys) {
        if (living.getInstance() == null) return;

        DamageSnapshot snap = DamageSnapshot.of(living, this);
        DamageContext ctx = sys.contextFor(snap);
        DamageTypeConfig tc = ctx.typeConfig();
        if (!tc.enabled(ctx)) return;

        int maxAir = MAX_AIR;
        AirRefill refill = AirRefill.MODERN;
        if (tc instanceof BreathingConfig bc) {
            Integer m = bc.maxAir(ctx);
            if (m != null) maxAir = m;
            AirRefill r = bc.airRefill(ctx);
            if (r != null) refill = r;
        }

        Integer stored = living.getTag(AIR);
        int air = stored != null ? stored : maxAir;

        boolean inWater = headInWater(living);
        boolean canBreathe = !inWater || living.hasEffect(PotionEffect.WATER_BREATHING); // identical 1.8/26; invulnerable already filtered

        if (!canBreathe) {
            air = drainAir(living, air);
            if (air <= DROWN_AT) {
                setAir(living, 0);
                if (DamageSystem.absorbedByWindow(living, ctx.baseAmount())) return;
                sys.apply(snap);
                return;
            }
        } else if (air < maxAir) {
            air = refill == AirRefill.LEGACY
                    ? (inWater ? air : maxAir)       // 1.8: snap to max, out of water only
                    : Math.min(air + 4, maxAir);     // 26: +4/tick, in water too while breathing
        }
        setAir(living, air);
    }

    /** Respiration-gated drain (vanilla {@code j} / {@code decreaseAirSupply}): max Respiration across armor gives a {@code level/(level+1)} chance to keep this tick's air. */
    private static int drainAir(LivingEntity living, int air) {
        int resp = 0;
        for (EquipmentSlot slot : ARMOR) resp = Math.max(resp, Enchants.level(living.getEquipment(slot), RESPIRATION));
        if (resp > 0 && ThreadLocalRandom.current().nextInt(resp + 1) > 0) return air;
        return air - 1;
    }

    /** Whether the head (eye position) block is water; {@code false} if its chunk is unloaded. */
    private static boolean headInWater(LivingEntity living) {
        Point eye = living.getPosition().add(0, living.getEyeHeight(), 0);
        MechanicsWorld world = MechanicsWorld.viewed(living);
        return world.isChunkLoaded(eye.chunkX(), eye.chunkZ()) && world.getBlock(eye).compare(Block.WATER);
    }
}
