package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileType;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.mechanics.projectile.types.SplashPotion;
import io.github.term4.polyp.presets.hypixel.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hypixel runs the stock 1.8 tracker on throwables, so the preset must too. From hypixelALLPROJECTILES (441
 * projectiles): snowball / egg / pearl / splash correct every 10 ticks and the fishing bobber every 5, matching
 * 1.8's {@code addEntity(entity, 64, 10, true)} and {@code (64, 5, true)} rows. Launch physics from the same
 * capture: snowball/egg/pearl 1.5 b/t with gravity 0.03 and drag 0.99, splash 0.5 with gravity 0.05, bobber 1.5
 * with gravity 0.04 and drag 0.92 - all already the vanilla18 baseline this preset inherits.
 */
class HypixelProjectileWireTest extends HeadlessServerTest {

    /** Ticks the projectile in open air and returns the tick number of each position correction. */
    private static List<Integer> syncTicks(FakePlayer viewer, ProjectileType type, int ticks) {
        ProjectileConfig config = Projectiles.config();
        var snap = ProjectileSnapshot.of(viewer.player, type).withConfig(config);
        ProjectileEntity p = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(p);
        awaitSpawn(p);
        List<Integer> at = new ArrayList<>();
        for (int tick = 1; tick <= ticks && !p.isRemoved(); tick++) {
            viewer.sent.clear();
            p.tick(tick * 50L);
            boolean synced = viewer.sent.stream()
                    .map(sp -> SendablePacket.extractServerPacket(ConnectionState.PLAY, sp))
                    .anyMatch(pk -> pk instanceof EntityTeleportPacket t && t.entityId() == p.getEntityId());
            if (synced) at.add(tick);
        }
        if (!p.isRemoved()) p.remove();
        return at;
    }

    private static void assertCadence(List<Integer> at, int expected, String what) {
        assertTrue(at.size() >= 3, what + ": too few corrections to read a cadence, got " + at);
        assertEquals(1, at.getFirst(), what + ": 1.8's tracker fires at counter 0, so the first lands on tick 1");
        for (int i = 1; i < at.size(); i++) {
            assertEquals(expected, at.get(i) - at.get(i - 1),
                    what + ": expected a correction every " + expected + " ticks, got " + at);
        }
    }

    @Test
    void throwablesCorrectEveryTenTicksAndTheBobberEveryFive() {
        // high up and aimed flat: nothing to hit inside the window, so the cadence is not cut short
        FakePlayer viewer = FakePlayer.connect(instance, new Pos(400.5, 200, 400.5, 0.0f, -20.0f), "HypixelWire");
        try {
            for (ProjectileType type : new ProjectileType[]{
                    Snowball.INSTANCE, Pearl.INSTANCE, SplashPotion.INSTANCE}) {
                assertCadence(syncTicks(viewer, type, 40), 10, type.key().toString());
            }
            // the bobber discards itself the moment the angler stops holding a rod
            viewer.player.setItemInMainHand(ItemStack.of(Material.FISHING_ROD));
            assertCadence(syncTicks(viewer, FishingBobber.INSTANCE, 40), 5, "bobber");
        } finally {
            viewer.player.remove();
        }
    }

    /**
     * hypixelALLPROJECTILES, 103 pearls: |v| spreads 1.472-1.526 (sd 0.0117 at speed 1.5), the stock 1.8 0.0075
     * inaccuracy. A zeroed spread would launch every throw on the identical vector.
     */
    @Test
    void pearlsLaunchWithTheVanillaSpread() {
        ProjectileConfig config = Projectiles.config();
        Pos stance = new Pos(410.5, 200, 410.5, 0.0f, 0.0f);
        FakePlayer shooter = FakePlayer.connect(instance, stance, "PearlSpread");
        try {
            Set<Vec> launched = new HashSet<>();
            for (int i = 0; i < 20; i++) {
                shooter.player.teleport(stance).join();
                var snap = ProjectileSnapshot.of(shooter.player, Pearl.INSTANCE).withConfig(config);
                ProjectileEntity p = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
                assertNotNull(p);
                awaitSpawn(p);
                launched.add(p.getVelocity());
                p.remove();
            }
            assertTrue(launched.size() > 10,
                    "20 pearls from one stance produced " + launched.size() + " distinct launch vectors; "
                            + "the 1.8 gaussian spread should make nearly all of them differ");
        } finally {
            shooter.player.remove();
        }
    }

    /** A type entry REPLACES the base map entry, so the preset's pearl must rebase on {@code Vanilla18.pearl()}
     *  or it silently loses 1.8's self-pass-through. */
    @Test
    void pearlKeepsTheSelfPassThrough() {
        var pearl = Projectiles.config().typeConfig(Pearl.KEY);
        assertNotNull(pearl);
        assertNotNull(pearl.selfHit, "hypixel pearls keep 1.8's self-pass-through");
    }
}
