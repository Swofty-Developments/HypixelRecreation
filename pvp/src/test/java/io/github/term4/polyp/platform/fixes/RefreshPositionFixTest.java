package io.github.term4.polyp.platform.fixes;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Minestom swallows a refresh whose target equals the stale lastSyncedPosition; the OptimizedPlayer retry must not. */
class RefreshPositionFixTest extends HeadlessServerTest {

    @Test
    void refreshBackToSpawnApplies() {
        Pos start = new Pos(8.5, 64, 8.5);
        Player p = FakePlayer.connect(instance, start, "RefreshFix").player;
        assertInstanceOf(OptimizedPlayer.class, p);

        p.refreshPosition(new Pos(8.5, 65, 8.5), false, false);
        assertEquals(65, p.getPosition().y());

        // back to the position lastSyncedPosition still holds
        p.refreshPosition(start, false, false);
        assertEquals(start.y(), p.getPosition().y(), 1e-9);
        assertEquals(start.x(), p.getPosition().x());
        assertEquals(start.z(), p.getPosition().z());
        p.remove();
    }
}
