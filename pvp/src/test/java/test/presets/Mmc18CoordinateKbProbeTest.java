package test.presets;

import io.github.term4.polyp.mechanics.explosion.BlockBreaking;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.presets.mmc18.Explosion;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The same relative blast/player geometry must deliver bit-identical knockback at any world offset. */
class Mmc18CoordinateKbProbeTest extends HeadlessServerTest {

    private static final double[] OFFSETS = {0, 10_000, 100_000, 937_168};

    @Test
    void knockbackIsOffsetInvariant() {
        InstanceContainer inst = MinecraftServer.getInstanceManager().createInstanceContainer();
        inst.setGenerator(unit -> unit.modifier().fillHeight(0, 64, Block.STONE));
        ExplosionSystem explosions = new ExplosionSystem(polyp,
                Explosion.config().toBuilder().blockBreaking((BlockBreaking) null).build());

        Vec wireAtOrigin = null;
        for (double off : OFFSETS) {
            int ccx = (int) Math.floor((off + 8) / 16);
            for (int cx = ccx - 2; cx <= ccx + 2; cx++)
                for (int cz = -2; cz <= 2; cz++) inst.loadChunk(cx, cz).join();

            Pos feet = new Pos(off + 8 + 2.6875, 64.0, 8 + 0.9375);
            Vec center = new Vec(off + 8 + 0.4375, 64.09, 8 + 0.5625);
            FakePlayer fp = FakePlayer.connect(inst, feet, "off" + (int) (off / 1000));
            awaitSpawn(fp.player);
            fp.player.teleport(feet).join();

            Entity source = new Entity(EntityType.ARMOR_STAND);
            source.setInstance(inst, center.asPos()).join();
            fp.sent.clear();
            explosions.explode(inst, center, 2.0f, source, null);

            List<EntityVelocityPacket> vels = fp.sent(EntityVelocityPacket.class).stream()
                    .filter(p -> p.entityId() == fp.player.getEntityId()).toList();
            Vec wire = vels.getLast().velocity();
            if (wireAtOrigin == null) wireAtOrigin = wire;
            else assertEquals(wireAtOrigin, wire, "wire KB must be bit-identical at offset " + off);
            fp.player.remove();
            source.remove();
        }
    }
}
