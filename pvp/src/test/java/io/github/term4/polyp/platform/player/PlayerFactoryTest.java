package io.github.term4.polyp.platform.player;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** {@code Polyp.playerFactory}: the provider reads the field per-connect, so an app's subclass rides the real
 *  join path - even when set after init. */
class PlayerFactoryTest extends HeadlessServerTest {

    static final class CustomPlayer extends OptimizedPlayer {
        CustomPlayer(PlayerConnection connection, GameProfile profile) { super(connection, profile); }
    }

    @Test
    void theFactorySuppliesTheSubclassThroughTheRealJoinPath() {
        var previous = polyp.playerFactory;
        polyp.playerFactory = CustomPlayer::new;
        try {
            FakePlayer fake = FakePlayer.connect(instance, new Pos(0.5, 65, 0.5), "FactoryTest");
            try {
                assertInstanceOf(CustomPlayer.class, fake.player,
                        "the provider must build the app's subclass, set after init");
            } finally {
                fake.player.remove();
            }
        } finally {
            polyp.playerFactory = previous;
        }
    }
}
