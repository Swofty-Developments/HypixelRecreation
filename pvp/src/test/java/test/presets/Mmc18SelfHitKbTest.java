package test.presets;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import org.junit.jupiter.api.Test;
import io.github.term4.polyp.presets.mmc18.Knockback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mmc18 projectile SELF-hit knockback (capture-solved: snowball+rod selfhit sessions 2026-07-17, 30 hits): horizontal
 * {@code (B/2)(diagonal + look)} - the fixed yaw-135 degenerate diagonal blended 50/50 with the live look - vertical at
 * the melee cap. Golden yaws: look 135 = aligned -> the full B diagonal (the known (-2983,-2983) shorts); look 45 =
 * perpendicular -> pure -X at {@code B*cos(45)}; look 315 = opposed -> near-cancellation.
 */
class Mmc18SelfHitKbTest extends HeadlessServerTest {

    private static final double B = 0.5274;

    /** The self-hit wire velocity in b/t. */
    private static double[] selfHitBt(float yaw, String tag) {
        return selfHitBt(yaw, tag, Knockback.projectile(), 0);
    }

    private static double[] selfHitBt(float yaw, String tag, KnockbackConfig kb, int punch) {
        FakePlayer p = FakePlayer.connect(instance, new Pos(8.5, 64, 8.5, yaw, 0f), "Self" + tag);
        p.sent.clear();
        services.knockback().apply(new KnockbackSnapshot(p.player, false, p.player, null, null, kb, punch));
        EntityVelocityPacket v = p.sent.stream().filter(EntityVelocityPacket.class::isInstance)
                .map(EntityVelocityPacket.class::cast).filter(x -> x.entityId() == p.player.getEntityId())
                .findFirst().orElseThrow(() -> new AssertionError("no self velocity"));
        return new double[]{ v.velocity().x(), v.velocity().y(), v.velocity().z() };
    }

    private static final double D = -Math.sqrt(0.5); // the yaw-135 diagonal's per-axis component

    @Test
    void lookAlignedWithDiagonalGivesFullDiagonal() {
        double[] v = selfHitBt(135f, "A");
        assertEquals(B * D, v[0], 1e-3); // the known (-2983,-2983)-shorts case
        assertEquals(B * D, v[2], 1e-3);
        assertEquals(0.3614, v[1], 1e-3, "vertical at the melee cap");
    }

    @Test
    void lookPerpendicularGivesBisectorAtCos45() {
        double[] v = selfHitBt(45f, "B"); // look (-.707, +.707) + diag (-.707, -.707) -> pure -X at B*cos(45)
        assertEquals(-B * Math.cos(Math.toRadians(45)), v[0], 1e-3);
        assertEquals(0, v[2], 1e-3);
    }

    @Test
    void lookOpposedCancelsHorizontal() {
        double[] v = selfHitBt(315f, "C");
        assertEquals(0, v[0], 1e-3);
        assertEquals(0, v[2], 1e-3);
        assertEquals(0.3614, v[1], 1e-3, "the vertical survives the horizontal cancellation");
    }

    /** The bow's arrows push 100% along the live look - no diagonal, full B at any yaw. */
    @Test
    void arrowSelfHitIsPureLook() {
        double[] v = selfHitBt(90f, "D", Knockback.arrow(), 0); // look = (-1, 0)
        assertEquals(-B, v[0], 1e-3);
        assertEquals(0, v[2], 1e-3);
        double[] w = selfHitBt(315f, "E", Knockback.arrow(), 0); // the blend's cancellation yaw: still full B for the bow
        assertEquals(B, Math.hypot(w[0], w[2]), 1e-3);
        assertEquals(0.3614, w[1], 1e-3);
    }

    /** Punch rides the self look too: +0.6/level onto the same direction. */
    @Test
    void arrowSelfHitCarriesPunch() {
        double[] v = selfHitBt(90f, "F", Knockback.arrow(), 2);
        assertEquals(-(B + 1.2), v[0], 1e-3);
        assertEquals(0, v[2], 1e-3);
    }

    /** A punched throwable/rod self-hit scales the whole blend: look 135 = aligned -> the full (B + 0.6) diagonal. */
    @Test
    void blendSelfHitCarriesPunch() {
        double[] v = selfHitBt(135f, "G", Knockback.projectile(), 1);
        double d = -(B + 0.6) * Math.sqrt(0.5);
        assertEquals(d, v[0], 1e-3);
        assertEquals(d, v[2], 1e-3);
    }

    /** The LIB's own self-hit contract (no components): a degenerate relative position falls back to a RANDOM horizontal
     *  at full strength - vanilla 1.8's {@code d0*d0+d1*d1 < 1e-4} random loop ({@code Directions.horizontalBetween}). */
    @Test
    void vanillaSelfHitIsRandomDirectionAtFullStrength() {
        double[] v = selfHitBt(0f, "H",
                io.github.term4.polyp.presets.vanilla18.Knockback.projectile(), 0);
        assertEquals(0.4, Math.hypot(v[0], v[2]), 1e-3, "full base strength in some horizontal direction");
        assertTrue(v[1] > 0.3, "vanilla vertical (minus the estimated-velocity gravity fold)");
    }
}
