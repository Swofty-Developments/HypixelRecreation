package test.presets;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.presets.hypixel.Hypixel;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** BedWars pearls are heard game-wide at full volume; every other Hypixel mode keeps the positional sound. */
class HypixelPearlSoundTest extends HeadlessServerTest {

    private static SoundEffectPacket soundFor(FakePlayer p) {
        return p.sent.stream()
                .map(sp -> SendablePacket.extractServerPacket(ConnectionState.PLAY, sp))
                .filter(SoundEffectPacket.class::isInstance)
                .map(SoundEffectPacket.class::cast)
                .findFirst().orElse(null);
    }

    /** Fires PEARL_TELEPORT under {@code profile} at {@code landing} and returns the far player's sound, if any. */
    private static SoundEffectPacket farSound(io.github.term4.polyp.MechanicsProfile profile, Pos landing,
                                              FakePlayer thrower, FakePlayer far) {
        polyp.profiles().setGlobal(profile);
        thrower.sent.clear();
        far.sent.clear();
        Fx.play(polyp.services(), Fx.PEARL_TELEPORT,
                FxContext.at(MechanicsWorld.of(thrower.player), landing, thrower.player));
        return soundFor(far);
    }

    @Test
    void bedwarsPearlsAreHeardAcrossTheMapButOtherModesFade() {
        Pos landing = new Pos(700.5, 65, 700.5);
        FakePlayer thrower = FakePlayer.connect(instance, landing, "PearlThrower");
        FakePlayer far = FakePlayer.connect(instance, new Pos(1400.5, 65, 1400.5), "PearlFarAway");
        var previous = polyp.profiles().global();
        try {
            SoundEffectPacket bedwars = farSound(Hypixel.bedwars(), landing, thrower, far);
            assertNotNull(bedwars, "bedwars: a player on the far side still gets the packet");
            // anchored on the LISTENER, not the landing, so the client has no distance to attenuate over
            // (the packet puts position on its own wire grid, hence the block of slack)
            assertTrue(Math.abs(bedwars.x() - far.player.getPosition().x()) <= 1.0,
                    "bedwars: sound follows the listener, got x=" + bedwars.x());
            assertEquals(1.0f, bedwars.volume(), 1.0e-6, "and at full volume");

            SoundEffectPacket normal = farSound(Hypixel.profile(), landing, thrower, far);
            assertNotNull(normal, "other modes still send it - the client does the fading");
            assertTrue(Math.abs(normal.x() - landing.x()) <= 1.0,
                    "other modes: sound stays at the landing, got x=" + normal.x());
        } finally {
            if (previous != null) polyp.profiles().setGlobal(previous);
            far.player.remove();
            thrower.player.remove();
        }
    }
}
