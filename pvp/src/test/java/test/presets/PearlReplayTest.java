package test.presets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Replays a captured minemen pearl session ({@code {throws:[{feet,yaw,pitch,tp}]}}) through the mmc18 pearl. */
abstract class PearlReplayTest extends HeadlessServerTest {

    record Replay(int n, int ok, int falseRefusal, int posOff, String summary) {}

    /**
     * Loads a capture corpus, or SKIPS the test when it isn't on the classpath. Some corpora are gitignored, so a
     * clone (CI included) has the tests but not their fixtures - skipping says that, where reading through a null
     * stream just NPEs.
     */
    static JsonObject corpus(String resource) throws Exception {
        var stream = PearlReplayTest.class.getResourceAsStream(resource);
        assumeTrue(stream != null, () -> "capture corpus " + resource + " is not on the classpath (gitignored)");
        try (var in = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(in, JsonObject.class);
        }
    }

    /** Throws a pearl from {@code from} and returns where the shooter ends up. */
    static Pos thrownPearl(FakePlayer shooter, Pos from, ProjectileConfig config) {
        shooter.player.teleport(from).join();
        var snap = ProjectileSnapshot.of(shooter.player, Pearl.INSTANCE).withConfig(config);
        ProjectileEntity pearl = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        if (pearl == null) return from;
        awaitSpawn(pearl);
        for (int tick = 1; tick <= 100 && !pearl.isRemoved(); tick++) pearl.tick(tick * 50L);
        if (!pearl.isRemoved()) pearl.remove();
        return shooter.player.getPosition();
    }

    /** Runs every throw; ok = teleport within 0.5 of minemen's (1.8 table-trig launch scatter). */
    static Replay replay(String resource, String label, Pos shooterHome) throws Exception {
        JsonObject corpus = corpus(resource);
        ProjectileConfig config = Projectiles.config();
        FakePlayer shooter = FakePlayer.connect(instance, shooterHome, label);
        StringBuilder report = new StringBuilder();
        int ok = 0, falseRefusal = 0, posOff = 0, n = 0;
        try {
            for (var el : corpus.getAsJsonArray("throws")) {
                JsonObject t = el.getAsJsonObject();
                JsonArray f = t.getAsJsonArray("feet");
                Pos from = new Pos(f.get(0).getAsDouble(), f.get(1).getAsDouble(), f.get(2).getAsDouble(),
                        (float) t.get("yaw").getAsDouble(), (float) t.get("pitch").getAsDouble());
                JsonArray tp = t.getAsJsonArray("tp");
                Pos expected = new Pos(tp.get(0).getAsDouble(), tp.get(1).getAsDouble(), tp.get(2).getAsDouble());
                Pos got = thrownPearl(shooter, from, config);
                n++;
                if (got.distanceSquared(from) <= 1e-8) {
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
        String summary = String.format("%s %d: ok %d, falseRefusal %d, posOff %d%n%s",
                label, n, ok, falseRefusal, posOff, report);
        return new Replay(n, ok, falseRefusal, posOff, summary);
    }
}
