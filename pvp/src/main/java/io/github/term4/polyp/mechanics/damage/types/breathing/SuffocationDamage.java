package io.github.term4.polyp.mechanics.damage.types.breathing;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.DamageProducers;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.EnvironmentalDamageTicker;
import io.github.term4.polyp.mechanics.damage.EnvironmentalTickProducer;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import io.github.term4.polyp.util.BlockContact;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;

/**
 * Suffocation ({@code minecraft:in_wall}). Vanilla 1.8 ({@code inBlock} -&gt; {@code STUCK}) and 26 ({@code isInWall} -&gt;
 * {@code inWall()}) are behaviourally identical: while the head sits in a full occluding cube, deal {@code baseAmount}
 * (1.0) per tick, gated by the invul window (so it lands at the hurt cadence). Self-driven via
 * {@link EnvironmentalDamageTicker} (creative/spectator excluded).
 */
public final class SuffocationDamage extends DamageType implements EnvironmentalTickProducer {

    public static final Key KEY = Key.key("minecraft:in_wall");
    public static final SuffocationDamage INSTANCE = new SuffocationDamage();

    private boolean registered;

    private SuffocationDamage() {
        super(KEY, "Suffocation", VanillaTypes.IN_WALL, DamageTypeConfig.builder(KEY).baseAmount(1.0).build());
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

    @Override
    public void tick(LivingEntity living, DamageSystem sys) {
        if (living.getInstance() == null || !inWall(living)) return;
        DamageProducers.emit(sys, living, this);
    }

    /**
     * Vanilla 26 {@code Entity.isInWall} - cheaper than 1.8's 8-point ring. Build a flat eye-plane box ({@code width*0.8}
     * wide, ~0 tall) centred on the eye and suffocate if any overlapped cell is a full occluding cube. Pose-aware via
     * {@link LivingEntity#getEyeHeight()} (crawling/swimming drop the eye, so a standing-head-height wall won't suffocate a crawler).
     *
     * <p>{@link BlockContact#isFullCube} approximates vanilla's per-block {@code isSuffocating} flag, which Minestom doesn't
     * expose: a transparent full cube (glass) wrongly suffocates. Only a per-block {@code isSuffocating} override would
     * close the gap - {@code blocksMotion} wouldn't (glass blocks motion and is a full cube).
     */
    private static boolean inWall(LivingEntity living) {
        double eye = living.getEyeHeight();
        double half = living.getBoundingBox().width() * 0.4; // width * 0.8, split either side of the eye centre
        BoundingBox eyePlane = new BoundingBox(
                new Vec(-half, eye - 5.0E-7, -half),
                new Vec(half, eye + 5.0E-7, half));
        return BlockContact.scanCells(MechanicsWorld.viewed(living), living.getPosition(), eyePlane, 0, BlockContact::isFullCube);
    }
}
