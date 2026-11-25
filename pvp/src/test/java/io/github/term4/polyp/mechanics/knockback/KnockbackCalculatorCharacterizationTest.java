package io.github.term4.polyp.mechanics.knockback;

import io.github.term4.polyp.presets.vanilla18.Knockback;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Characterization pins for {@link KnockbackCalculator#compute} under {@link Knockback#melee()}. Values are b/s (the
 * compute output is already {@code × tps}). Knockback resistance is NOT read here - it lives in
 * {@code DamageSystem.applyHurtKnockback}. See docs/attributes-design.md.
 */
class KnockbackCalculatorCharacterizationTest extends HeadlessServerTest {

    private static final double EPS = 1e-9;

    private KnockbackCalculator calc() {
        return new KnockbackCalculator(services, Knockback.melee());
    }

    private static void assertVec(Vec expected, Vec actual) {
        assertEquals(expected.x(), actual.x(), EPS, "x");
        assertEquals(expected.y(), actual.y(), EPS, "y");
        assertEquals(expected.z(), actual.z(), EPS, "z");
    }

    /** Melee hit, attacker not sprinting: base 0.4 horizontal away from the source + 0.4 vertical (capped), × 20 tps. */
    @Test
    void sourceRelative_noSprint() {
        LivingEntity target = zombie(new Pos(0, 64, 0));
        LivingEntity source = zombie(new Pos(0, 64, -1, 0f, 0f)); // due south, looking +Z
        source.setSprinting(false);
        Vec kb = calc().compute(new KnockbackSnapshot(target, true, source, null, null, Knockback.melee()));
        assertVec(new Vec(0.0, 8.0, 8.0), kb);
    }

    /** Same geometry but sprinting: the extra (sprint) knockback folds in - bigger horizontal + vertical. */
    @Test
    void sourceRelative_sprint() {
        LivingEntity target = zombie(new Pos(10, 64, 0));
        LivingEntity source = zombie(new Pos(10, 64, -1, 0f, 0f));
        source.setSprinting(true);
        Vec kb = calc().compute(new KnockbackSnapshot(target, true, source, null, null, Knockback.melee()));
        assertVec(new Vec(0.0, 10.0, 18.0), kb);
    }

    /** Explicit origin + direction (no source entity): the {@code dir != null} dirCtx branch. */
    @Test
    void explicitDirection_noSource() {
        LivingEntity target = looseZombie(); // instance-less; position is the origin
        Vec kb = calc().compute(new KnockbackSnapshot(target, false, null,
                new Pos(0, 64, -1), new Vec(0, 0, 1), Knockback.melee()));
        assertVec(new Vec(0.0, 8.0, 8.0), kb);
    }

    /** Origin set, direction null: the origin is made to look at the target. */
    @Test
    void originLooksAtTarget() {
        LivingEntity target = zombie(new Pos(0, 64, 5));
        Vec kb = calc().compute(new KnockbackSnapshot(target, false, null,
                new Pos(0, 64, 0), null, Knockback.melee()));
        assertVec(new Vec(0.0, 8.0, 8.0), kb);
    }

    /** Source diagonally SW of the target: the horizontal direction splits across both axes (0.4 mag / √2 each, × 20). */
    @Test
    void diagonalSplitsHorizontal() {
        LivingEntity target = zombie(new Pos(20, 64, 0));
        LivingEntity source = zombie(new Pos(19, 64, -1, 0f, 0f));
        source.setSprinting(false);
        Vec kb = calc().compute(new KnockbackSnapshot(target, true, source, null, null, Knockback.melee()));
        assertVec(new Vec(5.65685424949238, 8.0, 5.65685424949238), kb);
    }

    /** The friction fold: a target's tracked velocity (b/s) bleeds into the result via the 1/2 divisor coefficient. */
    @Test
    void velocityFrictionFold() {
        LivingEntity target = zombie(new Pos(30, 64, 0));
        target.setVelocity(new Vec(8, 0, 4)); // b/s -> 0.4/0 /0.2 b/t -> × 0.5 fold -> +0.2 / +0.1 onto the impulse
        LivingEntity source = zombie(new Pos(30, 64, -1, 0f, 0f));
        source.setSprinting(false);
        Vec kb = calc().compute(new KnockbackSnapshot(target, true, source, null, null, Knockback.melee()));
        assertVec(new Vec(4.0, 8.0, 10.0), kb);
    }
}
