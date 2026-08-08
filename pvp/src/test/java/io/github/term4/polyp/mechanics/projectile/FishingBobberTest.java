package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.entities.FishingBobberEntity;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.shootables.FishingRod;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.presets.mmc18.PseudoHook;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.tracking.motion.MotionTracker;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.util.tick.TickScalingConfig;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fishing bobber: flight lockstep against the vanilla integrators (1.8 {@code EntityFishingHook.t_()}: move, gravity
 * 0.04F, drag 0.92F; 26.1 {@code FishingHook.tick()}: gravity 0.03, move, drag 0.92), hook/pin/reel per source, the
 * 1.8 tracker wire (updateFrequency 5, velocity updates on), and rod cast/retract through the {@code FishingRod} launcher.
 */
class FishingBobberTest extends HeadlessServerTest {

    private static final int FLIGHT_TICKS = 20;
    private static final double DRAG_092F = 0.92f;
    private static final double GRAVITY_004F = 0.04f;

    @BeforeAll
    static void loadFlightArea() {
        for (int x = 0; x <= 7; x++)
            for (int z = 0; z <= 2; z++)
                instance.loadChunk(x, z).join();
    }

    private static LivingEntity angler(Pos at) {
        LivingEntity shooter = looseZombie();
        shooter.setItemInMainHand(ItemStack.of(Material.FISHING_ROD)); // the per-tick holding-a-rod check
        shooter.setInstance(instance, at).join();
        return shooter;
    }

    private static ProjectileEntity launch(ProjectileConfig config, LivingEntity shooter) {
        var snap = ProjectileSnapshot.of(shooter, FishingBobber.INSTANCE).withConfig(config);
        ProjectileEntity entity = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(entity);
        awaitSpawn(entity);
        return entity;
    }

    @Test
    void vanilla18FlightMatchesThe18Integration() {
        LivingEntity shooter = angler(new Pos(72.5, 150, 8.5, 37.0f, 12.5f));
        ProjectileEntity bobber = launch(Vanilla18.projectiles(), shooter);
        Pos spawn = bobber.getSpawnPosition();
        assertNotNull(spawn);
        Vec v = bobber.velocityBt();
        assertTrue(v.lengthSquared() > 0);
        // wireLockstep on the tracker wire: the sim starts from the 1.8 client's decoded spawn state
        assertEquals((int) (spawn.x() * 32) / 32.0, spawn.x());
        assertEquals((int) (spawn.y() * 32) / 32.0, spawn.y());
        assertEquals((int) (spawn.z() * 32) / 32.0, spawn.z());
        Vec lp = lpRoundTrip(v);
        assertEquals(new Vec(legacyShortAxis(lp.x()), legacyShortAxis(lp.y()), legacyShortAxis(lp.z())), v,
                "sim velocity must equal the 1.8 client's decode of its own wire");

        // 1.8 EntityFishingHook.t_(): move, then motY -= 0.04F, then all axes *= 0.92F (floats widened per tick)
        double px = spawn.x(), py = spawn.y(), pz = spawn.z();
        double vx = v.x(), vy = v.y(), vz = v.z();
        for (int tick = 1; tick <= FLIGHT_TICKS; tick++) {
            px += vx; py += vy; pz += vz;
            vy -= GRAVITY_004F;
            vx *= DRAG_092F; vy *= DRAG_092F; vz *= DRAG_092F;
            bobber.tick(tick * 50L);
            assertEquals(px, bobber.getPosition().x(), "x @ tick " + tick);
            assertEquals(py, bobber.getPosition().y(), "y @ tick " + tick);
            assertEquals(pz, bobber.getPosition().z(), "z @ tick " + tick);
        }
        bobber.remove();
        shooter.remove();
    }

    @Test
    void modernFlightMatchesThe26Integration() {
        LivingEntity shooter = angler(new Pos(104.5, 150, 8.5, 37.0f, 12.5f));
        ProjectileEntity bobber = launch(
                io.github.term4.polyp.presets.vanilla.Projectiles.config(), shooter);
        Pos spawn = bobber.getSpawnPosition();
        assertNotNull(spawn);
        Vec v = bobber.velocityBt();
        assertTrue(v.lengthSquared() > 0);

        // 26.1 FishingHook.tick(): motY -= 0.03, move, then all axes *= 0.92 (plain doubles)
        double px = spawn.x(), py = spawn.y(), pz = spawn.z();
        double vx = v.x(), vy = v.y(), vz = v.z();
        for (int tick = 1; tick <= FLIGHT_TICKS; tick++) {
            vy -= 0.03;
            px += vx; py += vy; pz += vz;
            vx *= 0.92; vy *= 0.92; vz *= 0.92;
            bobber.tick(tick * 50L);
            assertEquals(px, bobber.getPosition().x(), "x @ tick " + tick);
            assertEquals(py, bobber.getPosition().y(), "y @ tick " + tick);
            assertEquals(pz, bobber.getPosition().z(), "z @ tick " + tick);
        }
        bobber.remove();
        shooter.remove();
    }

    /**
     * ABOVE the server rate the flight runs vanilla's own model: {@code /tick rate} never stretches a step, it
     * runs the native tick more often ({@code TickRateManager} only changes the wall-clock period). At clientTps
     * 40 on a 20 server that is TWO native steps per server tick - native drag, native gravity, native launch
     * velocity - so the arc matches a 40 TPS client exactly instead of drifting (~0.5 blocks/s when stretched).
     */
    @Test
    void aboveServerRateTheFlightSubStepsAtNativeConstants() {
        TickScaler.setGlobal(TickScalingConfig.builder().clientTps(40).build());
        try {
            LivingEntity shooter = angler(new Pos(88.5, 150, 8.5, 37.0f, 12.5f));
            ProjectileEntity bobber = launch(Vanilla18.projectiles(), shooter);
            Pos spawn = bobber.getSpawnPosition();
            assertNotNull(spawn);
            Vec v = bobber.velocityBt(); // native: the speed comes from the step COUNT, not a scaled value
            double px = spawn.x(), py = spawn.y(), pz = spawn.z();
            double vx = v.x(), vy = v.y(), vz = v.z();
            for (int tick = 1; tick <= 5; tick++) {
                for (int step = 0; step < 2; step++) { // 40/20 = two vanilla ticks per server tick
                    px += vx; py += vy; pz += vz;
                    vx *= DRAG_092F; vy = (vy - GRAVITY_004F) * DRAG_092F; vz *= DRAG_092F;
                }
                bobber.tick(tick * 50L);
                assertEquals(px, bobber.getPosition().x(), "x @ tick " + tick);
                assertEquals(py, bobber.getPosition().y(), "y @ tick " + tick);
                assertEquals(pz, bobber.getPosition().z(), "z @ tick " + tick);
            }
            bobber.remove();
            shooter.remove();
        } finally {
            TickScaler.setGlobal(null);
        }
    }

    /** AT OR BELOW the server rate nothing sub-steps - the single stretched step stands, so 20 and under are untouched. */
    @Test
    void belowServerRateKeepsTheStretchedStep() {
        TickScaler.setGlobal(TickScalingConfig.builder().clientTps(10).build());
        try {
            LivingEntity shooter = angler(new Pos(88.5, 150, 8.5, 37.0f, 12.5f));
            ProjectileEntity bobber = launch(Vanilla18.projectiles(), shooter);
            Pos spawn = bobber.getSpawnPosition();
            assertNotNull(spawn);
            Vec v = bobber.velocityBt();
            double drag = Math.pow(DRAG_092F, 0.5);      // drag^s
            double gravity = GRAVITY_004F * 0.5 * 0.5;   // gravity x s²
            double px = spawn.x(), py = spawn.y(), pz = spawn.z();
            double vx = v.x(), vy = v.y(), vz = v.z();
            for (int tick = 1; tick <= 5; tick++) {
                px += vx; py += vy; pz += vz;
                vx *= drag; vy = (vy - gravity) * drag; vz *= drag;
                bobber.tick(tick * 50L);
                assertEquals(px, bobber.getPosition().x(), "x @ tick " + tick);
                assertEquals(py, bobber.getPosition().y(), "y @ tick " + tick);
                assertEquals(pz, bobber.getPosition().z(), "z @ tick " + tick);
            }
            bobber.remove();
            shooter.remove();
        } finally {
            TickScaler.setGlobal(null);
        }
    }

    @Test
    void hooksPinsAndReelsTheVictim() {
        LivingEntity shooter = angler(new Pos(16.5, 64, 16.5, 0.0f, 0.0f));
        LivingEntity victim = zombie(new Pos(16.5, 64, 20.5));
        FishingBobberEntity bobber = (FishingBobberEntity) launch(Vanilla18.projectiles(), shooter);

        for (int tick = 1; tick <= 10 && bobber.getHookedEntity() == null; tick++) bobber.tick(tick * 50L);
        assertEquals(victim, bobber.getHookedEntity());
        // hooked METADATA stays unset on vanilla18: 1.8 has none, modern viewers see only the pin (real-Via look)
        assertNull(((FishingHookMeta) bobber.getEntityMeta()).getHookedEntity());
        assertFalse(bobber.isRemoved(), "the bobber stays after hooking");

        bobber.tick(999 * 50L); // the hooked tick pins to the victim at 0.8 body height
        Pos pin = victim.getPosition().add(0, victim.getBoundingBox().height() * 0.8, 0);
        assertEquals(pin.x(), bobber.getPosition().x(), 1e-9);
        assertEquals(pin.y(), bobber.getPosition().y(), 1e-9);
        assertEquals(pin.z(), bobber.getPosition().z(), 1e-9);

        // reel: motion += (angler - bobber) * 0.1, plus sqrt(dist) * 0.08 up (1.8); durability 3
        Pos anglerPos = shooter.getPosition();
        Pos bobberPos = bobber.getPosition();
        Vec toAngler = new Vec(anglerPos.x() - bobberPos.x(), anglerPos.y() - bobberPos.y(), anglerPos.z() - bobberPos.z());
        Vec expected = victim.getVelocity().add(toAngler.mul(0.1)
                .add(0, Math.sqrt(toAngler.length()) * 0.08, 0).mul(ServerFlag.SERVER_TICKS_PER_SECOND));
        assertEquals(3, bobber.retrieve());
        assertTrue(bobber.isRemoved());
        assertEquals(expected.x(), victim.getVelocity().x(), 1e-9);
        assertEquals(expected.y(), victim.getVelocity().y(), 1e-9);
        assertEquals(expected.z(), victim.getVelocity().z(), 1e-9);
        shooter.remove();
        victim.remove();
    }

    /** A wall hit never sticks: 1.8 ray-holds + damps then falls along the face; 26.1 stops dead then falls from rest. */
    @Test
    void wallContactSlidesDownInsteadOfSticking() {
        for (int y = 70; y <= 90; y++)
            for (int x = 38; x <= 42; x++)
                instance.setBlock(x, y, 12, Block.STONE);
        LivingEntity shooter = angler(new Pos(40.5, 80, 6.5, 0.0f, 0.0f));
        try {
            FishingBobberEntity legacy = (FishingBobberEntity) launch(Vanilla18.projectiles(), shooter);
            for (int tick = 1; tick <= 120 && !legacy.isRemoved(); tick++) legacy.tick(tick * 50L);
            assertTrue(legacy.getPosition().z() < 12, "1.8: never crosses the wall plane: " + legacy.getPosition());
            assertTrue(legacy.getPosition().y() < 66, "1.8: falls the whole face to the ground: " + legacy.getPosition());
            legacy.remove();

            FishingBobberEntity modern = (FishingBobberEntity) launch(
                    io.github.term4.polyp.presets.vanilla.Projectiles.config(), shooter);
            for (int tick = 1; tick <= 120 && !modern.isRemoved(); tick++) modern.tick(tick * 50L);
            assertFalse(modern.isStuck(), "26.1 has no stuck state");
            assertTrue(modern.getPosition().z() < 12, "26.1: never crosses the wall plane: " + modern.getPosition());
            assertTrue(modern.getPosition().y() < 66, "26.1: stops dead then falls from rest to the ground: " + modern.getPosition());
            modern.remove();
        } finally {
            shooter.remove();
            for (int y = 70; y <= 90; y++)
                for (int x = 38; x <= 42; x++)
                    instance.setBlock(x, y, 12, Block.AIR);
        }
    }

    /** hookStick(true) makes the bobber freeze into the wall (arrow-like) instead of the vanilla slide - the config seam. */
    @Test
    void hookStickFreezesTheBobberIntoTheWall() {
        for (int y = 60; y <= 80; y++)
            for (int x = 58; x <= 62; x++)
                instance.setBlock(x, y, 12, Block.STONE);
        LivingEntity shooter = angler(new Pos(60.5, 70, 6.5, 0.0f, 0.0f));
        var config = ProjectileConfig.builder(Vanilla18.projectiles())
                .typeConfigs(ProjectileTypeConfig
                        .builder(Vanilla18.projectiles().typeConfig(FishingBobber.KEY))
                        .hookStick(true).build())
                .build();
        FishingBobberEntity bobber = (FishingBobberEntity) launch(config, shooter);
        try {
            for (int tick = 1; tick <= 120 && !bobber.isStuck(); tick++) bobber.tick(tick * 50L);
            assertTrue(bobber.isStuck(), "hookStick freezes into the block");
            double stuckY = bobber.getPosition().y();
            for (int tick = 121; tick <= 200; tick++) bobber.tick(tick * 50L);
            assertTrue(bobber.isStuck(), "stays stuck (no fall/despawn while the block is there)");
            assertEquals(stuckY, bobber.getPosition().y(), 1e-9, "frozen in place, doesn't slide down the face");
            assertTrue(bobber.getPosition().z() < 12, "stuck short of the wall plane: " + bobber.getPosition());
        } finally {
            bobber.remove();
            shooter.remove();
            for (int y = 60; y <= 80; y++)
                for (int x = 58; x <= 62; x++)
                    instance.setBlock(x, y, 12, Block.AIR);
        }
    }

    /** 1.8 hooks fly through: the hook tick's move completes and motion is kept (no halt/zero); mmc18 opts into the halt. */
    @Test
    void vanilla18HookKeepsFlyingWhileMmc18Halts() {
        LivingEntity shooter = angler(new Pos(96.5, 64, 8.5, 0.0f, 0.0f));
        LivingEntity victim = zombie(new Pos(96.5, 64, 12.5));
        FishingBobberEntity bobber = (FishingBobberEntity) launch(Vanilla18.projectiles(), shooter);
        try {
            for (int tick = 1; tick <= 10 && bobber.getHookedEntity() == null; tick++) bobber.tick(tick * 50L);
            assertEquals(victim, bobber.getHookedEntity());
            assertTrue(bobber.velocityBt().lengthSquared() > 1e-6, "1.8 keeps the motion on hook (stale until release)");
        } finally {
            bobber.remove();
            shooter.remove();
            victim.remove();
        }

        LivingEntity shooter2 = angler(new Pos(104.5, 64, 8.5, 0.0f, 0.0f));
        LivingEntity victim2 = zombie(new Pos(104.5, 64, 12.5));
        FishingBobberEntity halted = (FishingBobberEntity) launch(Projectiles.config(), shooter2);
        try {
            for (int tick = 1; tick <= 10 && halted.getHookedEntity() == null; tick++) halted.tick(tick * 50L);
            assertEquals(victim2, halted.getHookedEntity());
            assertEquals(0.0, halted.velocityBt().lengthSquared(), 1e-12, "mmc18 hookHalt zeroes on the hook tick");
        } finally {
            halted.remove();
            shooter2.remove();
            victim2.remove();
        }
    }

    /** mmc18 bobber synchronizes differently hooked vs flying: flight is fully client-predicted (silent wire), but the
     *  hook pin broadcasts an explicit teleport on the same silent wire (event-driven, only when the pin moves). */
    @Test
    void mmc18BobberSyncsTheHookButNotTheFlight() {
        var config = Projectiles.config();
        // flight: client-predicted, no position sync on the wire (loaded flight area = chunks x 0-7, z 0-2)
        LivingEntity flightShooter = angler(new Pos(110.5, 90, 40.5, 0.0f, -30.0f));
        FakePlayer flightViewer = FakePlayer.connect(instance, new Pos(110.5, 90, 38.5), "MmFlightView");
        FishingBobberEntity flying = (FishingBobberEntity) launch(config, flightShooter);
        try {
            flightViewer.sent.clear();
            for (int t = 1; t <= 6; t++) flying.tick(t * 50L);
            assertEquals(0, bobberTeleports(flightViewer, flying), "mmc18 flight is client-predicted (no position sync)");
        } finally {
            flying.remove();
            flightShooter.remove();
            flightViewer.player.remove();
        }

        // hook: the pin broadcasts (differs from silent flight). Viewer sits BESIDE the shooter (out of the +z flight
        // path, else the bobber would hook the viewer instead of the zombie).
        LivingEntity shooter = angler(new Pos(116.5, 64, 8.5, 0.0f, 0.0f));
        FakePlayer viewer = FakePlayer.connect(instance, new Pos(113.5, 64, 8.5), "MmHookView");
        LivingEntity victim = zombie(new Pos(116.5, 64, 12.5));
        FishingBobberEntity hooker = (FishingBobberEntity) launch(config, shooter);
        try {
            for (int t = 1; t <= 10 && hooker.getHookedEntity() == null; t++) hooker.tick(t * 50L);
            assertEquals(victim, hooker.getHookedEntity(), "mmc18 bobber hooks the victim");
            viewer.sent.clear();
            for (int t = 11; t <= 20; t++) { // drag the hooked victim: each pin move broadcasts
                victim.teleport(new Pos(116.5, 64, 12.5 + (t - 10) * 0.3)).join();
                hooker.tick(t * 50L);
            }
            assertTrue(bobberTeleports(viewer, hooker) > 0, "the hook pin broadcasts on the wire (syncs differently than flight)");
        } finally {
            hooker.remove();
            shooter.remove();
            viewer.player.remove();
            victim.remove();
        }
    }

    private static long bobberTeleports(FakePlayer viewer, FishingBobberEntity bobber) {
        return viewer.sent.stream()
                .map(sp -> net.minestom.server.network.packet.server.SendablePacket
                        .extractServerPacket(net.minestom.server.network.ConnectionState.PLAY, sp))
                .filter(pk -> pk instanceof EntityTeleportPacket tp && tp.entityId() == bobber.getEntityId())
                .count();
    }

    /** 1.8 rod pull on a player is wire-silent (no {@code velocityChanged}): the pull only enters the server-tracked motion. */
    @Test
    void vanilla18RodPullOnPlayersIsWireSilent() {
        LivingEntity shooter = angler(new Pos(88.5, 64, 8.5, 0.0f, 0.0f));
        FakePlayer victim = FakePlayer.connect(instance, new Pos(88.5, 64, 12.5), "RodVictim");
        FishingBobberEntity bobber = (FishingBobberEntity) launch(Vanilla18.projectiles(), shooter);
        try {
            for (int tick = 1; tick <= 10 && bobber.getHookedEntity() == null; tick++) bobber.tick(tick * 50L);
            assertEquals(victim.player, bobber.getHookedEntity());

            Pos anglerPos = shooter.getPosition();
            Pos bobberPos = bobber.getPosition();
            Vec toAngler = new Vec(anglerPos.x() - bobberPos.x(), anglerPos.y() - bobberPos.y(), anglerPos.z() - bobberPos.z());
            Vec pull = toAngler.mul(0.1).add(0, Math.sqrt(toAngler.length()) * 0.08, 0); // b/t
            victim.sent.clear();
            assertEquals(3, bobber.retrieve());
            assertTrue(victim.sent(EntityVelocityPacket.class).isEmpty(), "no velocity packet reaches the hooked player");
            Vec tracked = MotionTracker.horizontalMot(victim.player, 0);
            assertEquals(pull.z(), tracked.z(), 1e-9, "the pull folds into the tracked motion the next hit reads");
        } finally {
            shooter.remove();
            victim.player.remove();
        }
    }

    @Test
    void landsWithTheVanillaDampSettleAndRetrievesAtGroundCost() {
        LivingEntity shooter = angler(new Pos(24.5, 66, 24.5, 0.0f, 90.0f)); // aimed straight down
        FishingBobberEntity bobber = (FishingBobberEntity) launch(Vanilla18.projectiles(), shooter);
        Pos beforeContact = bobber.getPosition();
        int tick = 1;
        for (; tick <= 10 && !bobber.isOnGround(); tick++) {
            beforeContact = bobber.getPosition();
            bobber.tick(tick * 50L);
        }
        assertTrue(bobber.isOnGround());
        assertFalse(bobber.isStuck(), "1.8 never freezes the bobber (xTile is never set)");
        // the contact tick skips the move: still at the pre-move position (the client predicts the same halt)
        assertEquals(beforeContact.x(), bobber.getPosition().x());
        assertEquals(beforeContact.y(), bobber.getPosition().y());
        assertEquals(beforeContact.z(), bobber.getPosition().z());

        for (; tick <= 70; tick++) bobber.tick(tick * 50L); // damp cycles pull it down from the halt height
        assertFalse(bobber.isRemoved(), "no ground despawn in 1.8 (the 1200-tick counter is dead code)");
        assertEquals(64.0, bobber.getPosition().y(), 0.15, "settles onto the surface");

        for (; !bobber.isOnGround(); tick++) bobber.tick(tick * 50L); // onGround flickers between micro-bounces
        assertEquals(2, bobber.retrieve());
        assertTrue(bobber.isRemoved());
        shooter.remove();
    }

    /** The modern preset publishes the hooked metadata (the native 26.1 glued-bobber visual); vanilla18 must not. */
    @Test
    void modernPresetPublishesHookedMetadata() {
        LivingEntity shooter = angler(new Pos(48.5, 64, 40.5, 0.0f, 0.0f));
        LivingEntity victim = zombie(new Pos(48.5, 64, 44.5));
        FishingBobberEntity bobber = (FishingBobberEntity) launch(
                io.github.term4.polyp.presets.vanilla.Projectiles.config(), shooter);
        for (int tick = 1; tick <= 10 && bobber.getHookedEntity() == null; tick++) bobber.tick(tick * 50L);
        assertEquals(victim, bobber.getHookedEntity());
        assertEquals(victim, ((FishingHookMeta) bobber.getEntityMeta()).getHookedEntity());
        bobber.remove();
        victim.remove();
        shooter.remove();
    }

    @Test
    void lineSnapDiscardsPastTheConfiguredDistance() {
        ProjectileConfig base = Vanilla18.projectiles();
        ProjectileConfig config = ProjectileConfig.builder(base)
                .typeConfigs(base.typeConfig(FishingBobber.KEY).toBuilder().lineSnapDistance(2.0).build())
                .build();
        LivingEntity shooter = angler(new Pos(32.5, 150, 8.5, 0.0f, -30.0f));
        ProjectileEntity bobber = launch(config, shooter);
        for (int tick = 1; tick <= 10 && !bobber.isRemoved(); tick++) bobber.tick(tick * 50L);
        assertTrue(bobber.isRemoved(), "past the line-snap distance the bobber discards");
        shooter.remove();
    }

    @Test
    void notHoldingARodDiscards() {
        LivingEntity shooter = angler(new Pos(48.5, 150, 8.5, 0.0f, -30.0f));
        ProjectileEntity bobber = launch(Vanilla18.projectiles(), shooter);
        bobber.tick(50L);
        assertFalse(bobber.isRemoved());
        shooter.setItemInMainHand(ItemStack.AIR);
        bobber.tick(100L);
        assertTrue(bobber.isRemoved(), "letting go of the rod discards the bobber");
        shooter.remove();
    }

    @Test
    void floatsOnWater() {
        for (int x = 87; x <= 90; x++)
            for (int z = 23; z <= 26; z++) {
                instance.setBlock(x, 63, z, Block.WATER);
                instance.setBlock(x, 64, z, Block.WATER);
            }
        LivingEntity shooter = angler(new Pos(88.5, 70, 24.5, 0.0f, 90.0f));
        var snap = ProjectileSnapshot.of(shooter, FishingBobber.INSTANCE).withConfig(Vanilla18.projectiles())
                .withSpawnPos(new Pos(88.5, 65.5, 24.5)).withVelocity(new Vec(0, -0.5, 0)); // gentle drop into the pool
        ProjectileEntity bobber = new ProjectileSystem(Polyp.getInstance(), Vanilla18.projectiles()).launch(snap);
        assertNotNull(bobber);
        awaitSpawn(bobber);
        for (int tick = 1; tick <= 60; tick++) bobber.tick(tick * 50L);
        assertFalse(bobber.isRemoved());
        assertFalse(bobber.isStuck(), "buoyancy keeps it off the pool floor");
        double surface = 64 + 8.0 / 9;
        assertEquals(surface, bobber.getPosition().y(), 0.25, "settles at the water surface");
        bobber.remove();
        shooter.remove();
    }

    @Test
    void rodUseCastsAndRetracts() {
        var config = Vanilla18.projectiles();
        var system = new ProjectileSystem(Polyp.getInstance(), config);
        new FishingRod().install(system.node(), system);
        MinecraftServer.getGlobalEventHandler().addChild(system.node());
        try {
            FakePlayer caster = FakePlayer.connect(instance, new Pos(8.5, 65, 8.5), "RodCaster");
            ItemStack rod = ItemStack.of(Material.FISHING_ROD);
            caster.player.setItemInMainHand(rod);
            EventDispatcher.call(new PlayerUseItemEvent(caster.player, PlayerHand.MAIN, rod, 0));
            FishingBobberEntity bobber = caster.player.getTag(FishingBobberEntity.ACTIVE_BOBBER);
            assertNotNull(bobber, "a rod use casts a bobber");
            awaitSpawn(bobber);

            // a use in the same tick as the cast is ignored (the use_item_on/use_item pair, click spam)
            EventDispatcher.call(new PlayerUseItemEvent(caster.player, PlayerHand.MAIN, rod, 0));
            assertFalse(bobber.isRemoved(), "a same-tick second use must not retract");
            assertEquals(bobber, caster.player.getTag(FishingBobberEntity.ACTIVE_BOBBER));

            bobber.tick(50L);
            EventDispatcher.call(new PlayerUseItemEvent(caster.player, PlayerHand.MAIN, rod, 0));
            assertTrue(bobber.isRemoved(), "a later use retracts");
            assertNull(caster.player.getTag(FishingBobberEntity.ACTIVE_BOBBER));
            caster.player.remove();
        } finally {
            MinecraftServer.getGlobalEventHandler().removeChild(system.node());
        }
    }

    /** The 1.8 tracker wire (addEntity 64, 5, true): spawn CARRIES the launch velocity + the owner id (the client
     *  predicts + draws the line from it), the m=0 velocity after the first tick, then a correction teleport
     *  carrying live velocity every 5 ticks. */
    @Test
    void trackerWireCarriesSpawnVelocityAndFiveTickCorrections() {
        var viewer = FakePlayer.connect(instance, new Pos(60.5, 150, 24.5), "BobberWire");
        LivingEntity shooter = angler(new Pos(60.5, 150, 26.5, 0.0f, 20.0f));
        viewer.sent.clear();
        ProjectileEntity bobber = launch(Vanilla18.projectiles(), shooter);
        int id = bobber.getEntityId();
        for (int tick = 1; tick <= 5; tick++) {
            if (tick == 1) assertEquals(2, viewer.packetsFor(id).size(), "spawn + meta before the first physics tick");
            bobber.tick(tick * 50L);
        }
        // 1.8 EntityTrackerEntry: the counter increments AFTER the check, so counter 0 sends on the tick after spawn
        List<ServerPacket> forBobber = viewer.packetsFor(id);
        assertEquals(4, forBobber.size(), "one correction teleport, on the tick after spawn: " + forBobber);
        SpawnEntityPacket spawn = assertInstanceOf(SpawnEntityPacket.class, forBobber.get(0));
        assertEquals(shooter.getEntityId(), spawn.data(), "spawn data = the owner id (drives the 1.8 line)");
        assertTrue(spawn.velocity().lengthSquared() > 0, "the tracker spawn carries the launch velocity");
        EntityTeleportPacket correction = assertInstanceOf(EntityTeleportPacket.class, forBobber.get(3));
        assertTrue(correction.delta().distanceSquared(Vec.ZERO) > 0, "the correction teleport carries the live velocity");
        viewer.player.remove();
        bobber.remove();
        shooter.remove();
    }

    /** mmc18: syncInterval(0) + velocitySyncInterval(0) = a fully client-predicted wire - spawn + meta, then silence -
     *  with the spawn state on the 1.8 grid so the client's prediction IS the flight. */
    @Test
    void mmc18SilentWireIsFullyClientPredicted() {
        var viewer = FakePlayer.connect(instance, new Pos(120.5, 150, 8.5), "SilentWire");
        // the silent contract is the 1.8 viewer's; modern viewers ride hookModernSync
        Polyp.getInstance().clientInfo().setProxyDetails(viewer.player, "{\"version\":47}");
        LivingEntity shooter = angler(new Pos(120.5, 150, 10.5, 37.0f, 12.5f));
        viewer.sent.clear();
        ProjectileEntity bobber = launch(Projectiles.config(), shooter);
        Pos spawn = bobber.getSpawnPosition();
        assertNotNull(spawn);
        assertEquals((int) (spawn.x() * 32) / 32.0, spawn.x());
        assertEquals((int) (spawn.y() * 32) / 32.0, spawn.y());
        assertEquals((int) (spawn.z() * 32) / 32.0, spawn.z());
        Vec v = bobber.velocityBt();
        Vec lp = lpRoundTrip(v);
        assertEquals(new Vec(legacyShortAxis(lp.x()), legacyShortAxis(lp.y()), legacyShortAxis(lp.z())), v);

        double px = spawn.x(), py = spawn.y(), pz = spawn.z();
        double vx = v.x(), vy = v.y(), vz = v.z();
        for (int tick = 1; tick <= 15; tick++) {
            px += vx; py += vy; pz += vz;
            vy -= GRAVITY_004F;
            vx *= DRAG_092F; vy *= DRAG_092F; vz *= DRAG_092F;
            bobber.tick(tick * 50L);
            assertEquals(px, bobber.getPosition().x(), "x @ tick " + tick);
            assertEquals(py, bobber.getPosition().y(), "y @ tick " + tick);
            assertEquals(pz, bobber.getPosition().z(), "z @ tick " + tick);
        }
        assertEquals(2, viewer.packetsFor(bobber.getEntityId()).size(), "spawn + meta, then a silent wire");
        viewer.player.remove();
        bobber.remove();
        shooter.remove();
    }

    @Test
    void pseudoHookFlashesReflashesAndFallsAway() {
        var config = Projectiles.config();
        var system = new ProjectileSystem(Polyp.getInstance(), config);
        config.shootables().forEach(s -> s.install(system.node(), system)); // incl. the PseudoHook re-flash listener
        MinecraftServer.getGlobalEventHandler().addChild(system.node());
        try {
            FakePlayer victimFp = FakePlayer.connect(instance, new Pos(32.5, 64, 44.5), "RodVictim");
            var victim = victimFp.player;
            LivingEntity shooter = angler(new Pos(32.5, 64, 40.5, 0.0f, 0.0f)); // faces +z toward the victim
            FishingBobberEntity bobber = (FishingBobberEntity) launch(config, shooter);
            int tick = 1;
            for (; tick <= 10 && bobber.getHookedEntity() == null; tick++) bobber.tick(tick * 50L);
            assertEquals(victim, bobber.getHookedEntity(), "the pseudo-hook flashes on the victim");
            var tagged = victim.getTag(PseudoHook.HOOKED_BY);
            assertNotNull(tagged);
            assertFalse(tagged.isEmpty());

            bobber.tick(tick++ * 50L); // the pin tick (hookDisplayTicks 1): held at the victim's 0.8 body height
            Pos pin = victim.getPosition().add(0, victim.getBoundingBox().height() * 0.8, 0);
            assertEquals(pin.x(), bobber.getPosition().x(), 1e-9);
            assertEquals(pin.y(), bobber.getPosition().y(), 1e-9);
            assertEquals(pin.z(), bobber.getPosition().z(), 1e-9);
            bobber.tick(tick++ * 50L);
            assertNull(bobber.getHookedEntity(), "the flash releases after hookDisplayTicks");
            assertFalse(bobber.isRemoved(), "the bobber stays for re-flashes until retract");

            EventDispatcher.call(new PlayerMoveEvent(victim, victim.getPosition().add(0.3, 0, 0), true));
            assertEquals(victim, bobber.getHookedEntity(), "re-hooks on the victim's position move");
            bobber.tick(tick++ * 50L); // pin
            bobber.tick(tick++ * 50L); // release
            assertNull(bobber.getHookedEntity());
            EventDispatcher.call(new PlayerMoveEvent(victim, victim.getPosition().withView(90f, 0f), true));
            assertNull(bobber.getHookedEntity(), "a look-only move must not re-flash");

            Vec fall = null;
            for (int i = 0; i < 12 && fall == null; i++, tick++) {
                bobber.tick(tick * 50L);
                Vec vel = bobber.velocityBt();
                if (Math.hypot(vel.x(), vel.z()) > 0.02) fall = vel;
            }
            assertNotNull(fall, "the fall velocity applies after the flash");
            // 0.8 b/s = the old lib's Minestom units: a 0.04 b/t drop-off, not an impulse
            assertEquals(0.04, Math.hypot(fall.x(), fall.z()), 1e-9, "gentle slide away from the shooter");
            assertTrue(fall.z() > 0, "away = the victim's side of the shooter (+z)");
            assertEquals(-0.02, fall.y(), 1e-9);

            assertEquals(0, bobber.retrieve(), "nothing hooked at retract");
            assertNull(victim.getTag(PseudoHook.HOOKED_BY), "retract clears the pseudo tag");
            victim.remove();
            shooter.remove();
        } finally {
            MinecraftServer.getGlobalEventHandler().removeChild(system.node());
        }
    }

    /** Once settled, the vanilla send threshold (4/32 + 60-tick keepalive) keeps the wire quiet: the predicting
     *  client plays its own landing instead of being yanked by same-spot corrections. */
    @Test
    void restedBobberSendsNoPositionCorrections() {
        var viewer = FakePlayer.connect(instance, new Pos(88.5, 66, 40.5), "RestWire");
        LivingEntity shooter = angler(new Pos(90.5, 66, 40.5, 0.0f, 90.0f));
        ProjectileEntity bobber = launch(Vanilla18.projectiles(), shooter);
        int id = bobber.getEntityId();
        int tick = 1;
        for (; tick <= 60; tick++) bobber.tick(tick * 50L); // land + settle
        long teleportsAfterSettle = viewer.packetsFor(id).stream().filter(p -> p instanceof EntityTeleportPacket).count();
        for (; tick <= 100; tick++) bobber.tick(tick * 50L);
        long teleportsAtRest = viewer.packetsFor(id).stream().filter(p -> p instanceof EntityTeleportPacket).count()
                - teleportsAfterSettle;
        assertTrue(teleportsAtRest <= 1, "at most the 60-tick keepalive while rested, got " + teleportsAtRest);
        viewer.player.remove();
        bobber.remove();
        shooter.remove();
    }
    private static long countSyncs(FakePlayer viewer, int id) {
        return viewer.sent.stream()
                .map(pk -> net.minestom.server.network.packet.server.SendablePacket
                        .extractServerPacket(net.minestom.server.network.ConnectionState.PLAY, pk))
                .filter(pk -> pk instanceof net.minestom.server.network.packet.server.play.EntityPositionSyncPacket sp
                        && sp.entityId() == id)
                .count();
    }

    /** 1.8 hooks keep their momentum in water (paper EntityFishingHook: buoyancy + gradual drag, NO entry
     *  damp): a fast cast punches THROUGH a thin wall, yet floats in a deep pool. mmc18 inherits this. */
    @Test
    void legacyHookPunchesThinWaterButFloatsInPools() {
        for (int x = 55; x <= 58; x++)
            for (int z = 8; z <= 12; z++) instance.loadChunk(x, z).join();
        // a thin standing wall of water across the cast line (2 thick), nothing behind it
        for (int x = 895; x <= 905; x++)
            for (int y = 130; y <= 140; y++)
                for (int z = 146; z <= 147; z++)
                    instance.setBlock(x, y, z, Block.WATER);
        LivingEntity thinAngler = angler(new Pos(900.5, 135, 140.5, 0, 0));
        ProjectileEntity thin = launch(Projectiles.config(), thinAngler);
        double maxZ = 0;
        for (int t = 1; t <= 30 && !thin.isRemoved(); t++) {
            thin.tick(t * 50L);
            maxZ = Math.max(maxZ, thin.getPosition().z());
        }

        // the deep pool: the same physics runs out of momentum and floats up
        for (int x = 890; x <= 930; x++)
            for (int y = 58; y <= 64; y++)
                for (int z = 140; z <= 180; z++)
                    instance.setBlock(x, y, z, Block.WATER);
        LivingEntity poolAngler = angler(new Pos(910.5, 66, 150.5, 0, 10));
        ProjectileEntity pool = launch(Projectiles.config(), poolAngler);
        double poolEndY = 0;
        for (int t = 1; t <= 60 && !pool.isRemoved(); t++) {
            pool.tick(t * 50L);
            poolEndY = pool.getPosition().y();
        }
        System.out.println("[hook-water] thin maxZ=" + String.format("%.2f", maxZ)
                + " pool endY=" + String.format("%.2f", poolEndY));
        assertTrue(maxZ > 149.0, "momentum carries the hook through a 2-thick wall (maxZ " + maxZ + ")");
        assertTrue(poolEndY > 64.2, "the same hook floats in a deep pool (endY " + poolEndY + ")");
        if (!thin.isRemoved()) thin.remove();
        if (!pool.isRemoved()) pool.remove();
        thinAngler.remove();
        poolAngler.remove();
    }

    /** The silent mmc18 wire trusts client prediction; the hook runs 1.8 water physics, which a 1.8
     *  viewer predicts EXACTLY (silence holds even in water) - only MODERN viewers mismatch (their client
     *  stops dead at entry and floats, 26.1 FishingHook) and ride the water-window escort. */
    @Test
    void waterWindowEscortsOnlyMismatchedGenerations() {
        // own pool: test-method ordering must not decide whether the water exists
        for (int x = 900; x <= 910; x++)
            for (int y = 60; y <= 64; y++)
                for (int z = 148; z <= 162; z++)
                    instance.setBlock(x, y, z, Block.WATER);
        var modern = FakePlayer.connect(instance, new Pos(905.5, 70, 143.5), "ModernRodViewer");
        var legacy = FakePlayer.connect(instance, new Pos(905.5, 70, 141.5), "LegacyRodViewer");
        Polyp.getInstance().clientInfo().setProxyDetails(legacy.player, "{\"version\":47}");

        // cast DOWN into the hook-water pool (y 58..64): air first, water contact within a few ticks
        LivingEntity shooter = angler(new Pos(905.5, 70, 145.5, 0, 35));
        ProjectileEntity bobber = launch(Projectiles.config(), shooter);
        int id = bobber.getEntityId();
        modern.sent.clear();
        legacy.sent.clear();
        bobber.tick(50L); // first flight tick: still in the air
        long airSyncs = countSyncs(modern, id);
        for (int t = 2; t <= 14 && !bobber.isRemoved(); t++) bobber.tick(t * 50L);
        long modernSyncs = countSyncs(modern, id);
        long legacySyncs = countSyncs(legacy, id);
        System.out.println("[hook-wire] airSyncs=" + airSyncs + " modernSyncs=" + modernSyncs
                + " legacySyncs=" + legacySyncs);
        assertTrue(airSyncs == 0, "the air flight stays on the silent contract for everyone (" + airSyncs + ")");
        assertTrue(modernSyncs >= 3, "modern viewers ride the escort from the water window (" + modernSyncs + ")");
        assertTrue(legacySyncs == 0, "1.8 viewers predict this physics exactly - never escorted (" + legacySyncs + ")");
        if (!bobber.isRemoved()) bobber.remove();
        shooter.remove();
        modern.player.remove();
        legacy.player.remove();
    }
}
