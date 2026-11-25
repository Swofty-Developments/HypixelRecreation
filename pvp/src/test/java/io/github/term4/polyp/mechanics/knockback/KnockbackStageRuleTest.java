package io.github.term4.polyp.mechanics.knockback;

import io.github.term4.polyp.presets.vanilla18.Knockback;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The replaceable knockback stages: a config rule swaps the built-in math, absent rules keep vanilla byte-identical. */
class KnockbackStageRuleTest extends HeadlessServerTest {

    private static final double TPS = ServerFlag.SERVER_TICKS_PER_SECOND;

    private KnockbackCalculator calc() { return new KnockbackCalculator(services, Knockback.melee()); }

    private KnockbackSnapshot snap(KnockbackConfig config) {
        LivingEntity attacker = zombie(new Pos(0, 64, 700));
        LivingEntity victim = zombie(new Pos(0, 64, 702));
        return new KnockbackSnapshot(victim, true, attacker, null, null, config);
    }

    @Test
    void frictionRuleReplacesTheFold() {
        Vec plain = calc().compute(snap(Knockback.melee()));
        assertNotNull(plain);
        // marker rule: vanilla fold + a +1 b/t x tag proves the stage ran with the right inputs (y would hit the 0.4 vertical cap)
        Vec tagged = calc().compute(snap(Knockback.melee().toBuilder()
                .frictionRule((ctx, vel, kb, cfg) -> KnockbackCalculator.vanillaFriction(vel, kb, cfg).add(1, 0, 0))
                .build()));
        assertNotNull(tagged);
        assertEquals(plain.x() + TPS, tagged.x(), 1e-9);
        assertEquals(plain.y(), tagged.y(), 1e-9);
    }

    @Test
    void boundsRuleReplacesTheClamps() {
        Vec floored = calc().compute(snap(Knockback.melee().toBuilder()
                .boundsRule((ctx, kb, afterExtra, cfg) -> {
                    double h = Math.hypot(kb.x(), kb.z());
                    return h < 2.0 ? kb.mul(2.0 / h, 1, 2.0 / h) : kb; // hard floor no Bounds clamp can express
                })
                .build()));
        assertNotNull(floored);
        assertTrue(Math.hypot(floored.x(), floored.z()) >= 2.0 * TPS - 1e-6,
                "horizontal floored to 2 b/t: " + floored);
    }
}
