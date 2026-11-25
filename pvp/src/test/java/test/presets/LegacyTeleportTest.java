package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.Teleports;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.RelativeFlags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Delta movement on a teleport is a client-era question, not a preset one: 1.8 zeroes its own motion on an
 * absolute position packet, while 26.x asks for {@code Relative.DELTA}.
 */
class LegacyTeleportTest extends HeadlessServerTest {

    private static Vec teleportFrom(String name, boolean legacy, double x) {
        var fake = FakePlayer.connect(instance, new Pos(x, 66, 0.5), name);
        try {
            if (legacy) Polyp.getInstance().clientInfo().setProxyDetails(fake.player, "{\"version\":47}");
            fake.player.setVelocity(new Vec(4, 0, 0));
            Teleports.place(fake.player, new Pos(x, 68, 0.5), RelativeFlags.VIEW);
            return fake.player.getVelocity();
        } finally {
            fake.player.remove();
        }
    }

    @Test
    void legacyClientTeleportDropsVelocity() {
        assertEquals(0.0, teleportFrom("LegacyTp", true, 10.5).length(), 1.0e-9,
                "1.8 has no delta field; its absolute position packet zeroes client motion");
    }

    @Test
    void modernClientTeleportKeepsVelocity() {
        assertTrue(teleportFrom("ModernTp", false, 14.5).length() > 1.0e-9,
                "26.x asks for Relative.DELTA, so momentum survives the teleport");
    }
}
