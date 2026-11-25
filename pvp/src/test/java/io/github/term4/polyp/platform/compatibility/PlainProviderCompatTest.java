package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerStartSprintingEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The sprint strip is plain server API, so it must reach players a FOREIGN provider built - only the
 * packet-level compat (meta fix, pose interception) is {@code OptimizedPlayer}-bound.
 */
class PlainProviderCompatTest extends HeadlessServerTest {

    @Test
    void foreignProviderPlayersStillGetTheSprintStrip() {
        var manager = MinecraftServer.getConnectionManager();
        var previousProfile = polyp.profiles().global();
        manager.setPlayerProvider(Player::new); // a server that replaced our provider entirely
        FakePlayer fake = null;
        try {
            fake = FakePlayer.connect(instance, new Pos(30.5, 65, 30.5), "PlainSprint");
            assertFalse(fake.player instanceof OptimizedPlayer, "the point: a plain player");

            polyp.profiles().setGlobal(previousProfile.toBuilder()
                    .set(MechanicsKeys.COMPAT, CompatConfig.builder().restrictSprintSneak(true).build())
                    .build());
            EventDispatcher.call(new PlayerStartSprintingEvent(fake.player)); // tracked client sprint
            fake.player.setSprinting(true);

            fake.player.setSneaking(true);
            EventDispatcher.call(new PlayerMoveEvent(fake.player, new Pos(30.6, 65, 30.5), true));
            assertFalse(fake.player.isSprinting(), "sneak while sprinting strips the server sprint state");

            fake.player.setSneaking(false);
            EventDispatcher.call(new PlayerMoveEvent(fake.player, new Pos(30.7, 65, 30.5), true));
            assertTrue(fake.player.isSprinting(), "and un-sneaking restores the sprint we stripped");
        } finally {
            if (fake != null) fake.player.remove();
            polyp.profiles().setGlobal(previousProfile);
            manager.setPlayerProvider((conn, profile) -> polyp.playerFactory.apply(conn, profile));
        }
    }
}
