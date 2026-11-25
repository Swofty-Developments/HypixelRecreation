package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.explosion.ExplosionEvent;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A MineMen fireball blows at the PRE-MOVE position, so the blast always lands on one of the flight's tick-start
 * positions. Pinned by mmcfbupimpact: ceiling shots detonate at three heights spaced by the launch and cruise
 * steps exactly. A constant wall gap from a fixed stance is NOT evidence of a collision box - hypixel's is 0.567,
 * which matches no box. KB falloff and the block-break rays both measure from this centre.
 */
class Mmc18FireballImpactTest extends HeadlessServerTest {

    private static final double WALL_Z = 12;
    private static final Pos STANCE = new Pos(3.5, 70, 2.5, 0.0f, 0.0f); // yaw 0 = +z, dead flat

    @BeforeAll
    static void installExplosions() {
        ExplosionSystem.install(polyp, io.github.term4.polyp.presets.mmc18.Explosion.config()
                .toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build());
    }

    private static ProjectileEntity launch(InstanceContainer inst, ProjectileConfig config) {
        LivingEntity shooter = new LivingEntity(EntityType.ZOMBIE);
        shooter.setInstance(inst, STANCE).join();
        try {
            var snap = ProjectileSnapshot.of(shooter, Fireball.INSTANCE).withConfig(config);
            ProjectileEntity fb = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
            assertNotNull(fb);
            awaitSpawn(fb);
            return fb;
        } finally {
            shooter.remove();
        }
    }

    @Test
    void blastLandsOnATickStartNotOnTheWallFace() {
        ProjectileConfig config = Projectiles.config();

        // the same flight with nothing to hit: its tick-start positions are the ladder the blast must land on
        ProjectileEntity free = launch(flatInstance(null), config);
        List<Double> ladder = new ArrayList<>();
        for (int tick = 1; tick <= 12 && !free.isRemoved(); tick++) {
            ladder.add(free.getPosition().z());
            free.tick(tick * 50L);
        }
        free.remove();

        InstanceContainer walled = flatInstance(null);
        for (int x = 0; x < 8; x++)
            for (int y = 69; y <= 73; y++) walled.setBlock(x, y, (int) WALL_Z, Block.STONE);

        List<Point> centers = new ArrayList<>();
        EventNode<Event> node = EventNode.all("test:fbimpact");
        node.addListener(ExplosionEvent.class, e -> centers.add(e.center()));
        MinecraftServer.getGlobalEventHandler().addChild(node);
        try {
            ProjectileEntity fb = launch(walled, config);
            for (int tick = 1; tick <= 60 && !fb.isRemoved(); tick++) fb.tick(tick * 50L);
            assertNotNull(fb.impactPosition(), "the fireball never reached the wall");
        } finally {
            MinecraftServer.getGlobalEventHandler().removeChild(node);
        }

        assertEquals(1, centers.size(), "one detonation");
        double z = centers.getFirst().z();
        assertTrue(ladder.stream().anyMatch(t -> Math.abs(t - z) < 1.0e-9),
                "blast z " + z + " is not a tick-start position of the free flight " + ladder);
        // a swept box would rest one half-extent off the face; the pre-move centre trails it by much more
        assertTrue(WALL_Z - z > 0.5, "pre-move centre stands well off the face, got " + (WALL_Z - z));
    }
}
