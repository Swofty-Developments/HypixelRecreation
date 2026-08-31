package test.presets;

import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.projectile.ProjectileDamage;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.junit.jupiter.api.Test;
import io.github.term4.polyp.presets.mmc18.Explosion;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.presets.mmc18.Tnt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fireball-on-TNT (captures 2026-07-07 minemen.csv vs stom.csv): the push is 1.3167 * (1-d3/4) * feet-radial,
 * downward delivered, no contact KB (vanilla: damage/knockback live on EntityLivingBase - a non-living hit is a
 * pure trigger). The live capture read M=1.00: {@code fireballFight()}'s toBuilder() round-trip dropped every
 * generated knob - the Builder copy-ctors were missing {@code copyKnobs} (six configs, all fixed).
 */
class Mmc18FireballTntPushTest extends HeadlessServerTest {

    @Test
    void fireballStyleBlastPushesTntPureRadialAtFullScale() {
        ExplosionSystem explosions = new ExplosionSystem(polyp, Explosion.fireballFight().toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build()); // push test on the shared instance: no terrain edits // the toBuilder-round-tripped config
        PrimedTnt victim = Tnt.spawn(explosions, instance, new BlockVec(8, 64, 8)); // mid-chunk: the -x sail must clear the x=0 unloaded-chunk wall
        victim.setVelocity(new Vec(0, 0.2, 0).mul(20));
        for (int i = 0; i < 25; i++) victim.tick(0);
        Pos feet = victim.getPosition();

        Vec fbPos = new Vec(feet.x() + 1.2, feet.y() + 0.7, feet.z() + 0.16); // elevated side impact
        DamageSystem.DamageOutcome contact = services.damage().apply(
                DamageSnapshot.of(victim, ProjectileDamage.INSTANCE).withPoint(fbPos).withAmount(6.0f));
        assertFalse(contact.landed(), "non-living contact never lands (no contact KB, vanilla parity)");

        Entity source = new Entity(EntityType.ARMOR_STAND);
        Vec pre = victim.getVelocity().div(20);
        explosions.explode(instance, fbPos, 2.0f, source, victim);
        Vec dv = victim.getVelocity().div(20).sub(pre);

        double dx = feet.x() - fbPos.x(), dy = feet.y() - fbPos.y(), dz = feet.z() - fbPos.z();
        double d3 = Math.sqrt(dx * dx + dy * dy + dz * dz);
        Vec expected = new Vec(dx / d3, dy / d3, dz / d3).mul(1.3167 * (1 - d3 / 4));
        assertEquals(expected.x(), dv.x(), 1e-6, "radial x at full scale");
        assertEquals(expected.y(), dv.y(), 1e-6, "downward DELIVERED (no clamp)");
        assertEquals(expected.z(), dv.z(), 1e-6, "radial z at full scale");

        // the downward component bounces off the ground (their vy -0.31 hit skipped 4.4 blocks airborne);
        // the old grounded-rest response killed it in ~2 blocks of sliding
        boolean airborne = false;
        for (int i = 0; i < 4; i++) {
            victim.tick(0);
            airborne |= !victim.isOnGround();
        }
        assertTrue(airborne, "downward blast impulse on grounded TNT bounces it airborne");
        for (int i = 0; i < 26; i++) victim.tick(0);
        assertTrue(feet.x() - victim.getPosition().x() > 3.0,
                "bounced TNT sails (travelled " + (feet.x() - victim.getPosition().x()) + ")");
        victim.remove();
        source.remove();
    }
}
