package test.presets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mmcgeometryclip: 96 point-blank throws at a wall/ceiling corner, diffed against minemen's recorded
 * response per throw - teleport (position) or refusal (unmoved). The ground-truth pearl harness.
 */
class MmcPearlCorpusParityTest extends PearlReplayTest {

    @Test
    void corpusReplayMatchesMinemen() throws Exception {
        JsonObject corpus = corpus("/mmc-pearl-corpus.json");
        for (var el : corpus.getAsJsonArray("blocks")) {
            JsonArray b = el.getAsJsonArray();
            instance.setBlock(b.get(0).getAsInt(), b.get(1).getAsInt(), b.get(2).getAsInt(), Block.STONE);
        }
        // floor (top y=70) pre-dates the capture's block model; the throws never reach it, the shooter needs footing
        for (int x = 946686; x <= 946696; x++)
            for (int z = 946668; z <= 946686; z++) instance.setBlock(x, 69, z, Block.STONE);

        ProjectileConfig config = Projectiles.config();
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(946693, 71, 946677), "CorpusPearl");
        StringBuilder report = new StringBuilder();
        int ok = 0, falseRefusal = 0, falseTeleport = 0, posOff = 0, n = 0;
        try {
            for (var el : corpus.getAsJsonArray("throws")) {
                JsonObject t = el.getAsJsonObject();
                JsonArray f = t.getAsJsonArray("feet");
                Pos from = new Pos(f.get(0).getAsDouble(), f.get(1).getAsDouble(), f.get(2).getAsDouble(),
                        (float) t.get("yaw").getAsDouble(), (float) t.get("pitch").getAsDouble());
                boolean expectEcho = t.get("echo").getAsBoolean();
                JsonArray tp = t.getAsJsonArray("tp");
                Pos expected = new Pos(tp.get(0).getAsDouble(), tp.get(1).getAsDouble(), tp.get(2).getAsDouble());
                Pos got = thrownPearl(shooter, from, config);
                boolean moved = got.distanceSquared(from) > 1e-8;
                n++;

                if (expectEcho && !moved) { ok++; continue; }
                if (expectEcho) {
                    falseTeleport++;
                    report.append(String.format("#%d EXPECT refusal, GOT tp (%.3f,%.3f,%.3f) from (%.3f,%.3f,%.3f) p%.1f%n",
                            n, got.x(), got.y(), got.z(), from.x(), from.y(), from.z(), from.pitch()));
                } else if (!moved) {
                    falseRefusal++;
                    report.append(String.format("#%d EXPECT tp (%.3f,%.3f,%.3f), GOT refusal from (%.3f,%.3f,%.3f) p%.1f%n",
                            n, expected.x(), expected.y(), expected.z(), from.x(), from.y(), from.z(), from.pitch()));
                } else if (got.distance(expected) < 0.5) {
                    ok++;
                } else {
                    posOff++;
                    report.append(String.format("#%d tp OFF by %.3f: expect (%.3f,%.3f,%.3f) got (%.3f,%.3f,%.3f)%n",
                            n, got.distance(expected), expected.x(), expected.y(), expected.z(), got.x(), got.y(), got.z()));
                }
            }
        } finally {
            shooter.player.remove();
        }
        String summary = String.format("corpus %d: ok %d, falseRefusal %d, falseTeleport %d, posOff %d%n%s",
                n, ok, falseRefusal, falseTeleport, posOff, report);
        // >=95%: a handful of steep point-blank hug rows sit inside tick-phase fuzz (the corpus's own 4cm boundary)
        assertTrue(ok >= n * 0.95, summary);
    }
}
