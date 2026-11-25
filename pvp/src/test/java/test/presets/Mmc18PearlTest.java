package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.projectile.PearlTeleportEvent;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MineMen pearl teleport law (the replay tests pin the captured arenas): walk back from the ray contact to
 * the first spot the player box fits - lateral 0.4 off the face, floor y = plane, ceiling plane - 2, nothing
 * fits = refusal echo; entity hits land one block off the victim toward the pearl.
 */
class Mmc18PearlTest extends HeadlessServerTest {

    private static Pos pearlLanding(FakePlayer shooter, Pos from) {
        shooter.player.teleport(from).join();
        ProjectileConfig config = Projectiles.config();
        var snap = ProjectileSnapshot.of(shooter.player, Pearl.INSTANCE).withConfig(config);
        ProjectileEntity pearl = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(pearl);
        awaitSpawn(pearl);
        for (int tick = 1; tick <= 200 && !pearl.isRemoved(); tick++) pearl.tick(tick * 50L);
        assertTrue(pearl.isRemoved(), "pearl never impacted");
        return shooter.player.getPosition();
    }

    /** Feet land exactly on the floor plane; x/z stay the pearl's continuous position (no block snap). */
    @Test
    void floorHitLandsFeetOnTheFace() {
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(400.3, 80, 400.3, 0f, 90f), "MmcPearlDn");
        try {
            Pos landed = pearlLanding(shooter, new Pos(400.3, 80, 400.3, 0f, 90f));
            assertEquals(64.0, landed.y(), 1e-9, "feet exactly on the floor plane");
            assertTrue(Math.abs(landed.x() - 400.3) < 0.25, "x stays continuous: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** A wall hit with a clear column lands EXACTLY 0.4 off the face - the walk-back's first candidate, the
     *  flight point where the hit axis sits 0.4 clear (staircase capture: every row snapped plane - 0.4). */
    @Test
    void wallHitLandsExactlyPointFourOffTheFace() {
        int px = 420, pz = 420; // pillar columns x=420..421
        for (int x = px; x <= px + 1; x++)
            for (int y = 64; y <= 70; y++)
                for (int z = pz - 1; z <= pz + 1; z++) instance.setBlock(x, y, z, Block.STONE);
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(425.5, 65, 420.5, 90f, 0f), "MmcPearlWall");
        try {
            Pos east = pearlLanding(shooter, new Pos(425.5, 65, 420.5, 90f, 0f));   // yaw 90 = -x into the east face at 422.0
            assertEquals(422.4, east.x(), 1e-6, "exactly 0.4 off the face: " + east);
            Pos west = pearlLanding(shooter, new Pos(416.5, 65, 420.5, -90f, 0f));  // mirrored into the west face at 420.0
            assertEquals(419.6, west.x(), 1e-6, "mirrored: " + west);
            assertTrue(east.y() % 1 != 0.0 && west.y() % 1 != 0.0, "y stays the pearl's continuous flight height");
        } finally {
            shooter.player.remove();
        }
    }

    /** The free horizontal axis follows the flight line: mirrored-drift throws land symmetric around the
     *  spawn line (drift preserved, no artificial shove). */
    @Test
    void wallHitFreeAxisKeepsTheTickStartDrift() {
        int px = 430, pz = 430; // pillar columns x=430..431
        for (int x = px; x <= px + 1; x++)
            for (int y = 64; y <= 70; y++)
                for (int z = pz - 2; z <= pz + 2; z++) instance.setBlock(x, y, z, Block.STONE);
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(435.5, 65, 430.5, 88f, 0f), "MmcPearlFree");
        try {
            Pos plusZ = pearlLanding(shooter, new Pos(435.5, 65, 430.5, 88f, 0f));   // toward -x, drifting +z
            Pos minusZ = pearlLanding(shooter, new Pos(435.5, 65, 430.5, 92f, 0f));  // mirrored, drifting -z
            // mirror center = throw line shifted by the vanilla sideways spawn offset (-sin(yaw)*0.16)
            assertEquals(430.34, (plusZ.z() + minusZ.z()) / 2, 0.01, "mirrored drifts around the spawn line");
            assertTrue(plusZ.z() >= minusZ.z(), "drift preserved, not shoved across: " + plusZ + " vs " + minusZ);
        } finally {
            shooter.player.remove();
        }
    }

    /** A ceiling hit places the feet two below the underside - the player fits, unlike Hypixel's clip-in. */
    @Test
    void ceilingHitFitsTwoBelowTheFace() {
        int cx = 440, cz = 440, ceilY = 75;
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) instance.setBlock(cx + dx, ceilY, cz + dz, Block.STONE);
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(cx + 0.3, 65, cz + 0.3, 0f, -90f), "MmcPearlUp");
        try {
            Pos landed = pearlLanding(shooter, new Pos(cx + 0.3, 65, cz + 0.3, 0f, -90f));
            assertEquals(73.0, landed.y(), 1e-9, "feet = ceiling face - 2: " + landed);
            assertTrue(Math.abs(landed.x() - (cx + 0.3)) < 0.25, "x stays continuous: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** 1.8 rays fences at SELECTION height 1.0, not collision 1.5: a pearl crossing in the 1.0-1.5 band flies
     *  OVER the fence (mmcfencespearl: gap-column pearls at fence-top height sailed on to the structure). */
    @Test
    void fenceTopBandFliesOverLikeThe18Ray() {
        int fx = 470, fz = 470;
        for (int z = fz - 2; z <= fz + 2; z++) instance.setBlock(fx, 64, z, Block.OAK_FENCE); // ray top 65.0, shape top 65.5
        Pos from = new Pos(fx - 3.5, 64, fz + 0.34, -90f, 0f); // flat throw crosses the fence line at ~65.45 (the band)
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlBand");
        try {
            Pos landed = pearlLanding(shooter, from);
            assertTrue(landed.x() > fx + 0.7, "flew over the fence, landed beyond: " + landed);
            assertEquals(64.0, landed.y(), 1e-9, "floor law on the far side: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** Water never blocks a teleport: a pearl dropped into a pool lands feet on the pool floor (shapeless
     *  fluids don't contact the fit test). */
    @Test
    void waterPoolLandsFeetOnThePoolFloor() {
        int px = 560, pz = 460;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) instance.setBlock(px + dx, 63, pz + dz, Block.WATER);
        Pos from = new Pos(px + 0.3, 70, pz + 0.3, 0f, 90f); // straight down into the pool
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlPool");
        try {
            Pos landed = pearlLanding(shooter, from);
            assertEquals(63.0, landed.y(), 1e-9, "feet on the pool floor, standing in the water: " + landed);
            assertTrue(Math.abs(landed.x() - from.x()) < 0.25, "not refused or displaced: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** Standing under a low roof with nowhere to walk back to: MineMen keeps the thrower's x/z and drops y to
     *  the contact's block level rather than refusing (mmctunnelpearl / mmcwaterpearlvariiedwater, 0 refusals). */
    @Test
    void spawnUnderTheRoofSnapsToTheContactLevel() {
        int wz = 525, roofY = 66;
        for (int x = 516; x <= 524; x++) {
            for (int y = 64; y <= 70; y++) instance.setBlock(x, y, wz, Block.STONE); // wall
            for (int z = 521; z < wz; z++) instance.setBlock(x, roofY, z, Block.STONE); // roof in front of it
        }
        Pos from = new Pos(520.5, 64, 523.5, 0f, 0f); // STANDING under the roof, flat throw into the wall
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlRoof");
        try {
            Pos landed = pearlLanding(shooter, from);
            assertEquals(520.5, landed.x(), 1e-6, "x/z stay the thrower's: " + landed);
            assertEquals(523.5, landed.z(), 1e-6, "x/z stay the thrower's: " + landed);
            assertEquals(Math.floor(landed.y()), landed.y(), 1e-9, "y is the contact's block level: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** Acceptance is the plain player-box fit: a ceiling target whose box overlaps the side wall REFUSES
     *  (mmcwallAGAIN - hug-climb spam refuses at hug distance < 0.3, teleports at >= 0.3; 0/27 tps overlap).
     *  A ceiling hit never takes the boxed-in lift - it already lands two below the plane. */
    @Test
    void wallHuggingCeilingTargetRefusesTheOverlap() {
        int cx = 540, cz = 540, ceilY = 75;
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) instance.setBlock(cx + dx, ceilY, cz + dz, Block.STONE);
        for (int y = 64; y < ceilY; y++) instance.setBlock(cx + 1, y, cz, Block.STONE); // side wall beside the throw column
        Pos from = new Pos(cx + 0.9, 65, cz + 0.3, 0f, -90f); // straight up, box 0.2 into the wall the whole flight
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlHug");
        try {
            Pos landed = pearlLanding(shooter, from);
            assertEquals(65.0, landed.y(), 1e-9, "no candidate fits - refusal echo: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** A fence side hit is a lateral hit like any wall: lands short of the line, never refuses
     *  (mmcfencespearl - fence-line tps scatter short of the posts). */
    @Test
    void fenceSideHitBacksUpShortOfTheFence() {
        int fx = 464, fz = 464;
        for (int z = fz - 2; z <= fz + 2; z++) {
            instance.setBlock(fx, 64, z, Block.OAK_FENCE);
            instance.setBlock(fx, 65, z, Block.OAK_FENCE); // two high, like the capture wall
        }
        // unconnected fences are bare 0.25 posts; aim through the post center (sideways spawn offset +0.16 on z)
        Pos from = new Pos(fx - 3.5, 64, fz + 0.34, -90f, 0f); // flat throw +x into the fence side
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlFence");
        try {
            Pos landed = pearlLanding(shooter, from);
            assertTrue(landed.x() > from.x() + 0.5, "not refused, moved toward the fence: " + landed);
            assertTrue(landed.x() < fx + 0.375, "pre-move position: short of the fence face: " + landed);
            assertTrue(landed.y() > 64.9 && landed.y() < 66.0, "y stays the pearl's continuous height: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** A ceiling hit grazing a side wall still teleports (stomceilingclip pearl 497): the walk lands the
     *  last spot the box fits, beside the wall at face - 2. */
    @Test
    void ceilingCornerWallGrazeStillTeleports() {
        int wx = 474, cz = 470, ceilY = 70;
        for (int y = 64; y <= ceilY; y++)
            for (int z = cz - 2; z <= cz + 2; z++) instance.setBlock(wx, y, z, Block.STONE); // wall
        for (int x = wx - 4; x <= wx; x++)
            for (int z = cz - 2; z <= cz + 2; z++) instance.setBlock(x, ceilY, z, Block.STONE); // ceiling
        Pos from = new Pos(wx - 0.5, 64, cz + 0.5, -90f, -87f); // near-vertical, drifting into the wall corner
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlCorner");
        try {
            Pos landed = pearlLanding(shooter, from);
            assertEquals(68.0, landed.y(), 1e-9, "ceiling rule: feet = face - 2: " + landed);
            assertTrue(landed.x() > wx - 1 && landed.x() < wx, "continuous x beside the wall: " + landed);
        } finally {
            shooter.player.remove();
        }
    }

    /** A refusal is NOT wire-silent: minemen teleports you to your own location, so the client gets a real
     *  position echo (re-syncing its prediction) even though nobody moves. */
    @Test
    void refusalEchoesThePositionOnTheWire() {
        int wz = 565, roofY = 67;
        for (int x = 556; x <= 564; x++) {
            for (int y = 64; y <= 70; y++) instance.setBlock(x, y, wz, Block.STONE); // wall
            for (int z = 561; z < wz; z++) instance.setBlock(x, roofY, z, Block.STONE); // roof in front of it
        }
        // AIRBORNE under the roof: mid-jump is what actually refuses (73/73 corpus refusals are fractional feet.y)
        Pos from = new Pos(560.5, 64.5, 563.5, 0f, 0f);
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlEcho");
        try {
            shooter.player.teleport(from).join();
            ProjectileConfig config = Projectiles.config();
            var snap = ProjectileSnapshot.of(shooter.player, Pearl.INSTANCE).withConfig(config);
            ProjectileEntity pearl = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
            assertNotNull(pearl);
            awaitSpawn(pearl);
            shooter.sent.clear();
            for (int tick = 1; tick <= 200 && !pearl.isRemoved(); tick++) pearl.tick(tick * 50L);
            assertTrue(pearl.isRemoved(), "pearl never impacted");
            var echoes = shooter.sent.stream()
                    .filter(p -> p instanceof PlayerPositionAndLookPacket)
                    .map(p -> (PlayerPositionAndLookPacket) p).toList();
            assertEquals(1, echoes.size(), "the refusal sends one position echo: " + echoes);
            assertEquals(from.withView(0f, 0f), echoes.getFirst().position().asPos().withView(0f, 0f), "echo = the unmoved position");
            assertEquals(from.x(), shooter.player.getPosition().x(), 1e-6, "nobody moves");
        } finally {
            shooter.player.remove();
        }
    }

    /** A cancelled {@link PearlTeleportEvent} consumes the pearl and moves nobody - the game-layer seam. */
    @Test
    void cancelledTeleportMovesNothing() {
        int wz = 505;
        for (int x = 496; x <= 504; x++)
            for (int y = 64; y <= 70; y++) instance.setBlock(x, y, wz, Block.STONE); // wall
        var boundary = EventListener.of(PearlTeleportEvent.class, e -> e.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(boundary);
        Pos from = new Pos(500.5, 64, 500.5, 0f, 0f); // yaw 0 = +z, flat throw into the wall
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlClip");
        try {
            Pos landed = pearlLanding(shooter, from);
            assertEquals(500.5, landed.x(), 1e-6, "unmoved: " + landed);
            assertEquals(64.0, landed.y(), 1e-6, "unmoved: " + landed);
            assertEquals(500.5, landed.z(), 1e-6, "unmoved: " + landed);
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(boundary);
            shooter.player.remove();
        }
    }

    /** An entity hit lands ONE block off the struck entity, horizontally toward the pearl, at the entity's y. */
    @Test
    void entityHitLandsOneBlockOffTheVictimTowardThePearl() {
        var victim = zombie(new Pos(460.5, 64, 462.5));
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(460.5, 64, 458.5, 0f, 0f), "MmcPearlEnt");
        try {
            Pos landed = pearlLanding(shooter, new Pos(460.5, 64, 458.5, 0f, 0f)); // yaw 0 = +z, into the zombie
            Pos v = victim.getPosition();
            assertEquals(v.y(), landed.y(), 1e-9, "the victim's y: " + landed);
            double horizontal = Math.hypot(landed.x() - v.x(), landed.z() - v.z());
            assertEquals(1.0, horizontal, 1e-9, "one block off the victim: " + landed);
            assertTrue(landed.z() < v.z(), "displaced toward the pearl's approach side: " + landed);
        } finally {
            shooter.player.remove();
            victim.remove();
        }
    }

    /** The fireball catch: pearl straight up, own fireball thrown after it - the shooter lands one block off
     *  the fireball toward the pearl's column (the wire-proven self-catch sideways step), at the fireball's y. */
    @Test
    void fireballCatchTeleportsToTheFireballMidAir() {
        Pos from = new Pos(580.5, 64, 580.5, 0f, -90f);
        FakePlayer shooter = FakePlayer.connect(instance, from, "MmcPearlCatch");
        try {
            shooter.player.teleport(from).join();
            ProjectileConfig config = Projectiles.config();
            var system = new ProjectileSystem(Polyp.getInstance(), config);
            ProjectileEntity pearl = system.launch(ProjectileSnapshot.of(shooter.player, Pearl.INSTANCE).withConfig(config));
            assertNotNull(pearl);
            awaitSpawn(pearl);
            for (int tick = 1; tick <= 3; tick++) pearl.tick(tick * 50L);
            ProjectileEntity fireball = system.launch(ProjectileSnapshot.of(shooter.player,
                    io.github.term4.polyp.mechanics.projectile.types.Fireball.INSTANCE).withConfig(config));
            assertNotNull(fireball);
            awaitSpawn(fireball);
            Pos fbAtCatch = null;
            for (int tick = 4; tick <= 120 && !pearl.isRemoved(); tick++) {
                pearl.tick(tick * 50L);
                fbAtCatch = fireball.getPosition();
                if (!fireball.isRemoved()) fireball.tick(tick * 50L);
            }
            assertTrue(pearl.isRemoved(), "the pearl never met the fireball");
            Pos landed = shooter.player.getPosition();
            assertTrue(landed.y() > 70, "mid-air catch, not the ground: " + landed);
            assertNotNull(fbAtCatch);
            assertEquals(fbAtCatch.y(), landed.y(), 1.2, "at the fireball's height: " + landed + " vs fb " + fbAtCatch);
            // pearl column = 0.16 sideways (yaw 0 -> -x) of the fb column: the unit offset resolves to a full -x block
            assertEquals(fbAtCatch.x() - 1.0, landed.x(), 0.15, "one block toward the pearl's column: " + landed + " vs fb " + fbAtCatch);
            if (!fireball.isRemoved()) fireball.remove();
        } finally {
            shooter.player.remove();
        }
    }
}
