package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.presets.hypixel.Projectiles;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hypixel (BedWars) pearl teleport, captured 2026-07-27 over 130 teleports: the shooter lands at the CENTRE of the
 * block ABOVE the pearl's pre-move block - x/z always .5, y always floor+1 - with no solidity search (ceiling hits
 * clip you into the ceiling). Vanilla teleports to the continuous pearl position instead.
 */
class HypixelPearlTest extends HeadlessServerTest {

    /** Throws a pearl from {@code from} and returns the shooter's position after the impact tick. */
    private static Pos pearlLanding(FakePlayer shooter, Pos from, ProjectileConfig config) {
        shooter.player.teleport(from).join();
        var snap = ProjectileSnapshot.of(shooter.player, Pearl.INSTANCE).withConfig(config);
        ProjectileEntity pearl = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(pearl);
        awaitSpawn(pearl);
        for (int tick = 1; tick <= 200 && !pearl.isRemoved(); tick++) pearl.tick(tick * 50L);
        assertTrue(pearl.isRemoved(), "pearl never impacted");
        return shooter.player.getPosition();
    }

    @Test
    void floorHitSnapsToBlockCentreAbove() {
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(300.3, 80, 300.3, 0f, 90f), "PearlBw");
        try {
            Pos landed = pearlLanding(shooter, new Pos(300.5, 80, 300.5, 0f, 90f), Projectiles.config());
            // straight down onto the stone surface at y=64 -> that column's centre, floor+1. The column itself is
            // not pinned: the 1.8 launch spread drifts the pearl, so assert the snap, not which block it snapped to
            assertEquals(0.5, Math.abs(landed.x() % 1), 1e-9);
            assertEquals(0.5, Math.abs(landed.z() % 1), 1e-9);
            assertEquals(65.0, landed.y(), 1e-9);
            assertTrue(Math.abs(landed.x() - 300.5) <= 1.0 && Math.abs(landed.z() - 300.5) <= 1.0,
                    "a straight-down pearl lands in or beside the thrower's column, got " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    @Test
    void vanillaPearlKeepsTheContinuousImpactPosition() {
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(320.3, 80, 320.3, 0f, 90f), "PearlVan");
        try {
            Pos landed = pearlLanding(shooter, new Pos(320.3, 80, 320.3, 0f, 90f), Vanilla18.projectiles());
            assertTrue(Math.abs(landed.x() % 1) != 0.5 || Math.abs(landed.z() % 1) != 0.5 || landed.y() % 1 != 0.0,
                    "vanilla teleports to the continuous pearl position, not a block snap: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** No solidity search: a ceiling hit places the feet INSIDE the ceiling layer (the client clips out). */
    @Test
    void ceilingHitClipsIntoTheCeiling() {
        int cx = 340, cz = 340, ceilY = 75;
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                instance.setBlock(cx + dx, ceilY, cz + dz, Block.STONE);
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(cx + 0.5, 65, cz + 0.5, 0f, -90f), "PearlUp");
        try {
            Pos landed = pearlLanding(shooter, new Pos(cx + 0.5, 65, cz + 0.5, 0f, -90f), Projectiles.config());
            assertEquals(ceilY, landed.y(), 1e-9, "feet inside the ceiling layer - no safe-spot search");
            // the 1.8 launch spread drifts the pearl, so pin the block snap, not which block it snapped to
            assertEquals(0.5, Math.abs(landed.x() % 1), 1e-9);
            assertEquals(0.5, Math.abs(landed.z() % 1), 1e-9);
            assertTrue(Math.abs(landed.x() - (cx + 0.5)) <= 1.0 && Math.abs(landed.z() - (cz + 0.5)) <= 1.0,
                    "a straight-up pearl lands in or beside the thrower's column, got " + landed);
        } finally {
            shooter.player.remove();
        }
    }
}
