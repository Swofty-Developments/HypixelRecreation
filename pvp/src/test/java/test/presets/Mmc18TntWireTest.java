package test.presets;

import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityPositionSyncPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import org.junit.jupiter.api.Test;
import io.github.term4.polyp.presets.mmc18.Explosion;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.presets.mmc18.Tnt;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mmc18 TNT wire shape (capture 2026-07-07 vs minemen pakkit logs): spawn carries data=1 + the kick
 * (data=0 makes ViaRewind synthesize a delayed zero SET_ENTITY_MOTION that kills the 1.8 hop prediction),
 * syncs ride 10t boundaries while moving, and a resting TNT goes wire-silent.
 */
class Mmc18TntWireTest extends HeadlessServerTest {

    @Test
    void spawnCarriesKickAndRestingTntIsSilent() {
        ExplosionSystem explosions = new ExplosionSystem(polyp, Explosion.config().toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build()); // shared instance: measure wire, not terrain
        FakePlayer fp = FakePlayer.connect(instance, new Pos(8, 65, 8), "TntWireProbe");
        int before = fp.sent.size();

        PrimedTnt tnt = Tnt.spawn(explosions, instance, new BlockVec(2, 64, 8));
        List<Integer> velocityTicks = new ArrayList<>();
        List<Integer> syncTicks = new ArrayList<>();
        for (int tick = 1; tick <= 45; tick++) {
            int from = fp.sent.size();
            tnt.tick(0);
            int posSyncAt = -1, velocityAt = -1, i = 0;
            for (SendablePacket sp : fp.sent.subList(Math.max(from, before), fp.sent.size())) {
                ServerPacket p = SendablePacket.extractServerPacket(ConnectionState.PLAY, sp);
                if (p instanceof EntityVelocityPacket v && v.entityId() == tnt.getEntityId()) {
                    velocityTicks.add(tick);
                    velocityAt = i;
                    assertTrue(v.velocity().length() > 1e-9, "no zero-velocity packets on the wire");
                } else if (p instanceof EntityPositionSyncPacket ps && ps.entityId() == tnt.getEntityId()) {
                    syncTicks.add(tick);
                    posSyncAt = i;
                }
                i++;
            }
            if (posSyncAt >= 0 || velocityAt >= 0) {
                assertTrue(posSyncAt >= 0 && velocityAt > posSyncAt, "sync = etp then vel (1.8 tracker order), tick " + tick);
            }
        }
        SpawnEntityPacket spawn = fp.sent.stream()
                .map(sp -> SendablePacket.extractServerPacket(ConnectionState.PLAY, sp))
                .filter(p -> p instanceof SpawnEntityPacket s && s.entityId() == tnt.getEntityId())
                .map(p -> (SpawnEntityPacket) p).findFirst().orElseThrow();
        assertEquals(1, spawn.data(), "data=1 so the 1.8 spawn carries the velocity (ViaRewind passthrough branch)");
        assertEquals(0.2, spawn.velocity().y(), 1e-9, "spawn packet embeds the vanilla kick");

        assertTrue(velocityTicks.stream().anyMatch(t -> t >= 10 && t <= 12), "moving sync near tick 11: " + velocityTicks);
        assertTrue(velocityTicks.stream().noneMatch(t -> t >= 35), "resting TNT goes silent: " + velocityTicks);
        assertEquals(velocityTicks, syncTicks, "every sync pairs etp with vel");
        tnt.remove();
    }

    /**
     * A fuse longer than the 1.8 client's own ~80t self-count (a stretched slo-mo fuse, or a long native one)
     * outlives that count, so the TNT would vanish early for legacy viewers. It is re-spawned to them under that
     * window - never to modern viewers, who honour the fuse meta and wait for the real REMOVE.
     */
    @Test
    void longFuseReArmsOnlyLegacyViewers() {
        FakePlayer legacy = FakePlayer.connect(instance, new Pos(8, 65, 8), "LegacyTntViewer");
        FakePlayer modern = FakePlayer.connect(instance, new Pos(8, 65, 8), "ModernTntViewer");
        ((OptimizedPlayer) legacy.player).compat().setLegacyClient(true);

        ExplosionSystem explosions = new ExplosionSystem(polyp, Explosion.config().toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build());
        var cfg = new PrimedTnt.Config(120, 4.0f, true, PrimedTnt.Wire.HYPIXEL, false, null); // 120 > 80: outlives the client count
        PrimedTnt tnt = PrimedTnt.spawn(explosions, instance, new BlockVec(8, 70, 8), cfg);
        awaitSpawn(tnt);
        assertTrue(tnt.getViewers().contains(legacy.player) && tnt.getViewers().contains(modern.player), "both view the TNT");
        for (int t = 0; t < 62; t++) tnt.tick(0); // one re-arm at tick 60 (< the 80t client count)

        assertTrue(destroyed(legacy, tnt), "legacy viewer re-armed: destroy under the 80t window");
        assertTrue(spawnCount(legacy, tnt) >= 2, "the re-arm re-sends the spawn");
        assertFalse(destroyed(modern, tnt), "modern viewer honours the meta - never re-armed");
        tnt.remove();
    }

    @Test
    void shortFuseNeverReArms() {
        FakePlayer legacy = FakePlayer.connect(instance, new Pos(8, 65, 8), "ShortFuseViewer");
        ((OptimizedPlayer) legacy.player).compat().setLegacyClient(true);

        ExplosionSystem explosions = new ExplosionSystem(polyp, Explosion.config().toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build());
        var cfg = new PrimedTnt.Config(50, 4.0f, true, PrimedTnt.Wire.HYPIXEL, false, null); // 50 < 80: the client's own count covers it
        PrimedTnt tnt = PrimedTnt.spawn(explosions, instance, new BlockVec(8, 70, 8), cfg);
        awaitSpawn(tnt);
        for (int t = 0; t < 49 && !tnt.isRemoved(); t++) tnt.tick(0); // stop before the 50t detonation's own REMOVE

        assertFalse(destroyed(legacy, tnt), "a 50t fuse dies before the client's 80t count - no re-arm");
        if (!tnt.isRemoved()) tnt.remove();
    }

    private static boolean destroyed(FakePlayer fp, PrimedTnt tnt) {
        return fp.sent(DestroyEntitiesPacket.class).stream().anyMatch(d -> d.entityIds().contains(tnt.getEntityId()));
    }

    private static long spawnCount(FakePlayer fp, PrimedTnt tnt) {
        return fp.sent(SpawnEntityPacket.class).stream().filter(s -> s.entityId() == tnt.getEntityId()).count();
    }
}
