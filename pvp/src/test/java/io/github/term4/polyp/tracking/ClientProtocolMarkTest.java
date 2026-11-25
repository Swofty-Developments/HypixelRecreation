package io.github.term4.polyp.tracking;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** An app-set protocol mark outranks the Via details message (a re-terminating proxy lies there) and syncs
 *  the legacy-compat state both ways; UNKNOWN clears back to detection. */
class ClientProtocolMarkTest extends HeadlessServerTest {

    @Test
    void markOutranksViaAndSyncsCompat() {
        FakePlayer fp = FakePlayer.connect(instance, new Pos(0.5, 65, 800.5), "ProtocolMark");
        var info = polyp.clientInfo();
        try {
            // ViaProxy-style lie: the wire says 1.8 while the real client is modern
            info.setProxyDetails(fp.player, "{\"version\": 47}");
            assertTrue(info.isLegacy(fp.player), "via details resolve legacy");
            assertTrue(((OptimizedPlayer) fp.player).compat().legacyClient());

            info.setProtocol(fp.player, 776); // the app learned the truth over its own channel
            assertEquals(776, info.getProtocol(fp.player), "mark outranks the via details");
            assertFalse(((OptimizedPlayer) fp.player).compat().legacyClient(), "compat follows the mark down");

            polyp.client(fp.player).protocol(47);
            assertTrue(info.isLegacy(fp.player));
            assertTrue(((OptimizedPlayer) fp.player).compat().legacyClient(), "and back up");

            info.setProtocol(fp.player, ClientVersion.UNKNOWN_PROTOCOL);
            assertEquals(47, info.getProtocol(fp.player), "cleared: the via details resolve again");
        } finally {
            fp.player.remove();
        }
    }
}
