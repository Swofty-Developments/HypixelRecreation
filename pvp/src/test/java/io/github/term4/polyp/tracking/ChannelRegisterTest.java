package io.github.term4.polyp.tracking;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.compatibility.CompatAnimatium;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fabric gates {@code ClientPlayNetworking#canSend} on the server's {@code minecraft:register} payload. */
class ChannelRegisterTest extends HeadlessServerTest {

    @Test
    void spawnAnnouncesReceivableChannels() {
        Polyp polyp = Polyp.getInstance();
        CompatAnimatium.install(polyp); // idempotent; ensures the channel is registered
        FakePlayer viewer = FakePlayer.connect(instance, new Pos(0.5, 42, 0.5), "RegisterViewer");
        try {
            boolean announced = viewer.sent.stream().anyMatch(p ->
                    p instanceof PluginMessagePacket msg && "minecraft:register".equals(msg.channel())
                            && new String(msg.data(), StandardCharsets.UTF_8).contains(CompatAnimatium.INFO_CHANNEL));
            assertTrue(announced, "first spawn announces animatium:info via minecraft:register");
        } finally {
            viewer.player.remove();
        }
    }
}
