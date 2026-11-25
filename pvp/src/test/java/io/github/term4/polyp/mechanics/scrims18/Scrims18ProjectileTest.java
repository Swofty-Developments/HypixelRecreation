package io.github.term4.polyp.presets.scrims18;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.mechanics.projectile.types.Egg;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileType;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.mechanics.projectile.types.SplashPotion;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * scrims18 projectiles are fully client-predicted: spawn + launch velocity, then never a correction. Pinned
 * against scrims18snowballbow + scrims18moreprojectiles, where 141 spawns produced zero {@code entity_teleport}
 * and zero {@code entity_relative_move} across snowball, arrow, pearl and bobber.
 */
class Scrims18ProjectileTest extends HeadlessServerTest {

    /**
     * scrims18 with 1.8's launch speeds put back. The reference has to keep everything else - the silent wire
     * snaps the launch velocity onto the client's decode grid, which shaves it, so a vanilla18 config would be
     * measuring the quantizer as much as the speed.
     */
    private static final ProjectileConfig SCRIMS_AT_VANILLA_SPEED = ProjectileConfig.builder(Scrims18.projectiles())
            .defaults(ProjectileTypeConfig.builder(Scrims18.projectiles().defaults()).speed(1.5).build())
            .typeConfigs(ProjectileTypeConfig.builder(
                    Scrims18.projectiles().typeConfig(FishingBobber.KEY)).speed(1.5).build())
            .build();

    private record Wire(long teleports, long velocities) {}

    private static Wire flyFor30Ticks(FakePlayer shooter, ProjectileConfig config, ProjectileType type) {
        var snap = ProjectileSnapshot.of(shooter.player, type).withConfig(config);
        ProjectileEntity p = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(p);
        awaitSpawn(p);
        shooter.sent.clear(); // discard the spawn + its launch velocity; count only post-spawn corrections
        for (int tick = 1; tick <= 30 && !p.isRemoved(); tick++) p.tick(tick * 50L);
        var mine = shooter.sent.stream()
                .map(sp -> SendablePacket.extractServerPacket(ConnectionState.PLAY, sp))
                .toList();
        long tp = mine.stream()
                .filter(pk -> pk instanceof EntityTeleportPacket t && t.entityId() == p.getEntityId()).count();
        long vel = mine.stream()
                .filter(pk -> pk instanceof EntityVelocityPacket v && v.entityId() == p.getEntityId()).count();
        if (!p.isRemoved()) p.remove();
        return new Wire(tp, vel);
    }

    @Test
    void everyProjectileTypeFliesSilentAfterSpawn() {
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(200.5, 90, 200.5, 0.0f, -10.0f), "ScrimShooter");
        try {
            ProjectileConfig scrims = Scrims18.projectiles();
            for (ProjectileType type : new ProjectileType[]{
                    Snowball.INSTANCE, Egg.INSTANCE, Pearl.INSTANCE, SplashPotion.INSTANCE, Arrow.INSTANCE}) {
                Wire w = flyFor30Ticks(shooter, scrims, type);
                assertEquals(0, w.teleports(), type.key() + ": no position correction after spawn");
                assertEquals(0, w.velocities(), type.key() + ": no velocity after the launch one");
            }
        } finally {
            shooter.player.remove();
        }
    }

    /** One launch from {@code stance}, or null if the type refused to spawn. */
    private static Vec launchOnce(FakePlayer shooter, Pos stance, ProjectileConfig config, ProjectileType type) {
        shooter.player.teleport(stance).join();
        var snap = ProjectileSnapshot.of(shooter.player, type).withConfig(config);
        ProjectileEntity p = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(p);
        awaitSpawn(p);
        Vec v = p.getVelocity();
        p.remove();
        return v;
    }

    /**
     * Held-aim captures (scrims18staticsnowballtest 128 throws, scrims18staticrod 40 casts) each collapse onto
     * one velocity vector per aim with a hard-zero off-axis component - vanilla's gaussian would scatter every
     * launch - and both land under the 1.5F Paper 1.8 uses. Speed is checked as a RATIO against a vanilla launch
     * down the same path, so any constant of our own cancels out.
     */
    @Test
    void oneAimLaunchesOneVectorShortOfVanilla() {
        ProjectileConfig scrims = Scrims18.projectiles();
        Pos stance = new Pos(220.5, 90, 220.5, 90.0f, 0.35f); // the capture's aim: yaw 90, a shade below level
        FakePlayer shooter = FakePlayer.connect(instance, stance, "ScrimStatic");
        try {
            shooter.player.setItemInMainHand(ItemStack.of(Material.FISHING_ROD)); // the bobber needs one in hand
            for (var probe : List.of(
                    Map.entry(Snowball.INSTANCE, 1.4955 / 1.5),
                    Map.entry(FishingBobber.INSTANCE, 1.4655 / 1.5))) {
                ProjectileType type = probe.getKey();
                Set<Vec> launched = new HashSet<>();
                for (int i = 0; i < 20; i++) launched.add(launchOnce(shooter, stance, scrims, type));
                assertEquals(1, launched.size(), type.key() + ": one aim must launch one vector, got " + launched);

                double scrimsSpeed = launched.iterator().next().length();
                // same config at 1.8's speed, so the ratio isolates the constant from the wire snap
                double vanillaSpeed = launchOnce(shooter, stance, SCRIMS_AT_VANILLA_SPEED, type).length();
                assertEquals(probe.getValue(), scrimsSpeed / vanillaSpeed, 1.0e-3,
                        type.key() + ": launches under vanilla, " + scrimsSpeed + " vs " + vanillaSpeed);
            }
        } finally {
            shooter.player.remove();
        }
    }

    /**
     * scrims18staticsnowballtest + scrims18staticrod, 95 spawns from a held aim: the sideways step 1.8 builds in
     * ({@code locX -= cos(yaw) * 0.16F}) is a hard zero, traded for a push straight down the aim. Both captures
     * stood on a .5 coordinate, and the silent wire snaps the spawn to the client's 1/32 grid, so the readings
     * reproduce exactly - 0.4375 facing -x (snowballs), 0.40625 facing +x (rod), off the same 0.425.
     */
    @Test
    void projectilesSpawnDownTheViewNotOffTheShoulder() {
        ProjectileConfig scrims = Scrims18.projectiles();
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(240.5, 90, 240.5), "ScrimSpawn");
        try {
            for (var facing : List.of(Map.entry(90.0f, 0.4375), Map.entry(270.0f, 0.40625))) {
                Pos stance = new Pos(240.5, 90, 240.5, facing.getKey(), 0.0f);
                shooter.player.teleport(stance).join();
                var snap = ProjectileSnapshot.of(shooter.player, Snowball.INSTANCE).withConfig(scrims);
                ProjectileEntity p = new ProjectileSystem(Polyp.getInstance(), scrims).launch(snap);
                assertNotNull(p);
                awaitSpawn(p);
                Pos at = p.getPosition();
                p.remove();
                assertEquals(stance.z(), at.z(), 1.0e-9, "yaw " + facing.getKey() + ": no sideways step");
                double forward = Math.abs(at.x() - stance.x());
                assertEquals(facing.getValue(), forward, 1.0e-9, "yaw " + facing.getKey() + ": pushed down the aim");
            }
        } finally {
            shooter.player.remove();
        }
    }

    @Test
    void vanillaKeepsCorrectingTheSameThrow() {
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(210.5, 90, 210.5, 0.0f, -10.0f), "VanillaShooter");
        try {
            // the contrast: vanilla18's tracker re-syncs on the 10-tick throwable cadence
            assertTrue(flyFor30Ticks(shooter, Vanilla18.projectiles(), Snowball.INSTANCE).teleports() > 0,
                    "vanilla18: the tracker keeps re-syncing position");
        } finally {
            shooter.player.remove();
        }
    }
}
