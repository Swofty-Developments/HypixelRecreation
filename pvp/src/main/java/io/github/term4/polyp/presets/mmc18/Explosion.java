package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.mechanics.explosion.BlockBreaking;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfig;
import io.github.term4.polyp.mechanics.explosion.ExplosionExposure;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.projectile.entities.FireballEntity;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;

/**
 * mmc18 explosion, closed-form from the 2026-07-01 captures (point-blank sweep fits to ≤0.0013 b/t): vanilla-1.8
 * falloff DAMAGE (exact: 9 @2.42m, 3 @3.38m, power 2 - NOT Hypixel's flat 2.0) + the vanilla two-impulse KB with one
 * global scale {@link #KB_SCALE}: the melee hurt-KB ({@link Knockback#explosionHurt()}) folded before the radial push.
 * Velocity-delivered; the explosion packet stays motion-less (captures: all-zero).
 */
public final class Explosion {

    /** Fireball blast radius (mmc18 {@code Projectiles.POWER}); anything bigger is TNT-scale. */
    private static final double FIREBALL_POWER = 2.0;

    private Explosion() {}

    // radial push, wire-exact from the point-blank sweep; near but NOT exactly melee B/0.4 = 1.3185
    static final double KB_SCALE = 1.3167;
    // TNT: flat-pad capture fit (counts + P(r) curves within ~2%): the vanilla ray algorithm at a sparse 8-shell,
    // paying resistance x0.0775 once per block
    static final double CHARGE_SCALE = 0.0775;
    static final int RAY_GRID = 8;
    private static final BlockBreaking TNT_RAYS =
            io.github.term4.polyp.presets.vanilla18.Explosion.blockBreaking().toBuilder()
                    .rayGrid(RAY_GRID)
                    .charge(r -> r * CHARGE_SCALE)
                    .charging(BlockBreaking.Charging.PER_BLOCK)
                    .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS) // MineMen explosions never drop
                    .build();

    // Fireball DEFAULT: threshold rays, one 0.6-1.4x roll per horizontal heading, lifted origin. MineMen's true
    // system is fireballFrozenTable(), whose same-spot craters repeat their core cell-for-cell; that measured
    // consistency reads as repetitive in play, so the default re-rolls per shot - a feel choice, not a parity gap.
    // End stone's 2.66 gate exceeds the 2.575 a ray can carry to it (max roll minus one decay step): fireball-proof.
    private static final BlockBreaking FIREBALL_RAYS =
            io.github.term4.polyp.presets.vanilla18.Explosion.blockBreaking().toBuilder()
                    .charging(BlockBreaking.Charging.THRESHOLD)
                    .intensityRoll(0.6, 1.4)
                    .rollPerHeading(true)
                    .originLift(0.25) // 52-shot wool corpus: footprints shrink faster with standoff than a feet-origin walk
                    .charge(r -> 0.322 + 0.260 * r)
                    .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS)
                    .build();

    // MineMen's capture-measured mechanism: frozen per-ray table + per-shot noise. TNT is NOT frozen (re-rolls per
    // shot) - the freeze is a fireball-path quirk. Max table entry + noise stays under end stone's gate minus one
    // decay step, so the table cannot break the fireball-proof invariant either.
    private static final BlockBreaking FIREBALL_FROZEN =
            FIREBALL_RAYS.toBuilder()
                    .intensityTable(loadFireballTable())
                    .intensityNoise(0.05)
                    .originLift(0) // the table's extraction frame
                    .build();

    /** The default fireball config (per-shot rolls). */
    public static BlockBreaking fireballBlockBreaking() { return FIREBALL_RAYS; }

    /** MineMen's measured system (frozen table + per-ray noise): same-spot craters keep their core, rims flicker.
     *  Swap in via {@code config().toBuilder().blockBreaking(...)} for capture-faithful behavior. */
    public static BlockBreaking fireballFrozenTable() { return FIREBALL_FROZEN; }

    private static float[] loadFireballTable() {
        try (var in = new java.io.DataInputStream(new java.io.BufferedInputStream(
                Explosion.class.getResourceAsStream("fireball-intensity.bin")))) {
            float[] table = new float[1352];
            for (int i = 0; i < table.length; i++) table[i] = in.readFloat();
            return table;
        } catch (java.io.IOException | NullPointerException e) {
            throw new IllegalStateException("mmc18 fireball-intensity.bin missing or truncated", e);
        }
    }

    public static ExplosionConfig config() {
        ExplosionConfig base = Vanilla18.explosion();
        return ExplosionConfig.builder(base)
                .knockbackMultiplier(KB_SCALE)
                .blockBreaking(ctx -> ctx.source() instanceof FireballEntity ? FIREBALL_RAYS : TNT_RAYS)
                .damageKnockback(Knockback.explosionHurt())
                .packetPush(false)
                .pushEye(Explosion::pushEye)
                .exposure(ExplosionExposure.Rays.LEGACY_1_8_FULL_CUBE) // MineMen gates off-flat blasts (full-cube), unlike singleplayer 1.8
                .fire(false) // MineMen fireballs never ignite (overrides vanilla18's fireball incendiary); fireballFight() inherits this
                // ground loot: captured 3 fireballs / 2 TNT to clear, distance-independent, against vanilla
                // health 5 (mmcfbitemdestroy 10/10 at n=3, mmctntdestroy 12/12 at n=2). Keyed on the blast's
                // POWER, not the source class, so a sourceless or app-triggered blast is priced the same
                .itemDamage(ctx -> ctx.power() != null
                        ? (ctx.power() <= FIREBALL_POWER ? 2.0 : 3.0)
                        : (ctx.source() instanceof FireballEntity ? 2.0 : 3.0))
                // ...and it never SHOVES that loot: a MineMen blast either destroys a dropped stack or leaves it
                // exactly where it lay. Vanilla rides items out on the radial push like any other physics entity
                .knockbackTargets(e -> !(e instanceof ItemEntity) && ExplosionSystem.defaultKnockbackTarget(e))
                .build();
    }

    // 1.8 getHeadHeight (eye − 0.08 sneaking); the −1e-6 is the captured knife-edge: a blast exactly at the sneak eye
    // pushes DOWN, at the standing eye UP
    private static double pushEye(Entity e) {
        double eye = e.getEntityType().registry().eyeHeight();
        return e instanceof Player p && p.isSneaking() ? eye - 0.08 - 1.0e-6 : eye;
    }

    // measured: the vanilla FLOORED falloff × 0.05 (2026-07-01 leather captures)
    static final double FBF_DAMAGE_SCALE = 0.05;

    /**
     * Fireball-Fight variant: same KB as {@link #config()}, blast damage scaled off the vanilla floored curve; armor
     * applies normally. Direct hits deal the fireball's vanilla 6.0 CONTACT damage, and the splash after it is
     * overdamage-blocked.
     */
    public static ExplosionConfig fireballFight() {
        return config().toBuilder().damageScale(FBF_DAMAGE_SCALE).build();
    }
}
